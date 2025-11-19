/*
 * Copyright (C) 2011-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.hc.core5.http.HttpHeaders.HOST;

import com.github.tomakehurst.wiremock.admin.*;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.QueryParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.recording.RecordSpec;
import com.github.tomakehurst.wiremock.recording.RecordSpecBuilder;
import com.github.tomakehurst.wiremock.recording.RecordingStatusResult;
import com.github.tomakehurst.wiremock.recording.SnapshotRecordResult;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.security.NotAuthorisedException;
import com.github.tomakehurst.wiremock.stubbing.StubImport;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HttpAdminClient implements Admin {

  private static final String ADMIN_URL_PREFIX = "%s://%s%s%s/__admin";
  private static final int NO_PORT_DEFINED = -1;

  private final String scheme;
  private final String host;
  private final int port;
  private final String urlPathPrefix;
  private final String hostHeader;
  private final ClientAuthenticator authenticator;

  private final AdminRoutes adminRoutes;

  private final HttpClient httpClient;

  public HttpAdminClient(
      String scheme,
      String host,
      int port,
      String urlPathPrefix,
      String hostHeader,
      ClientAuthenticator authenticator,
      HttpClient httpClient) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.urlPathPrefix = urlPathPrefix;
    this.hostHeader = hostHeader;
    this.authenticator = authenticator;
    this.httpClient = httpClient;

    adminRoutes = AdminRoutes.forClient();
  }

  @Override
  public void addStubMapping(StubMapping stubMapping) {
    if (stubMapping.getRequest().hasInlineCustomMatcher()) {
      throw new AdminException(
          "Custom matchers can't be used when administering a remote WireMock server. "
              + "Use WireMockRule.stubFor() or WireMockServer.stubFor() to administer the local instance.");
    }

    executeRequest(
        adminRoutes.requestSpecForTask(CreateStubMappingTask.class),
        PathParams.empty(),
        stubMapping,
        Void.class);
  }

  @Override
  public void editStubMapping(StubMapping stubMapping) {
    putJsonAssertOkAndReturnBody(
        urlFor(EditStubMappingTask.class, PathParams.single("id", stubMapping.getId().toString())),
        Json.write(stubMapping));
  }

  @Override
  public void removeStubMapping(StubMapping stubbMapping) {
    postJsonAssertOkAndReturnBody(
        urlFor(RemoveMatchingStubMappingTask.class), Json.write(stubbMapping));
  }

  @Override
  public void removeStubMapping(UUID id) {
    executeRequest(
        adminRoutes.requestSpecForTask(RemoveStubMappingByIdTask.class),
        PathParams.single("id", id),
        Void.class);
  }

  @Override
  public ListStubMappingsResult listAllStubMappings() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetAllStubMappingsTask.class), ListStubMappingsResult.class);
  }

  @Override
  public SingleStubMappingResult getStubMapping(UUID id) {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetStubMappingTask.class),
        PathParams.single("id", id),
        SingleStubMappingResult.class);
  }

  @Override
  public void saveMappings() {
    postJsonAssertOkAndReturnBody(urlFor(SaveMappingsTask.class), null);
  }

  @Override
  public void resetAll() {
    postJsonAssertOkAndReturnBody(urlFor(ResetTask.class), null);
  }

  @Override
  public void resetRequests() {
    executeRequest(adminRoutes.requestSpecForTask(ResetRequestsTask.class));
  }

  @Override
  public void resetScenarios() {
    executeRequest(adminRoutes.requestSpecForTask(ResetScenariosTask.class));
  }

  @Override
  public void resetMappings() {
    executeRequest(adminRoutes.requestSpecForTask(ResetStubMappingsTask.class));
  }

  @Override
  public void resetToDefaultMappings() {
    postJsonAssertOkAndReturnBody(urlFor(ResetToDefaultMappingsTask.class), null);
  }

  @Override
  public GetServeEventsResult getServeEvents() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetAllRequestsTask.class), GetServeEventsResult.class);
  }

  @Override
  public GetServeEventsResult getServeEvents(ServeEventQuery query) {
    final QueryParams queryParams = new QueryParams();
    queryParams.add("unmatched", String.valueOf(query.isOnlyUnmatched()));

    if (query.getStubMappingId() != null) {
      queryParams.add("matchingStub", query.getStubMappingId().toString());
    }

    return executeRequest(
        adminRoutes.requestSpecForTask(GetAllRequestsTask.class),
        PathParams.empty(),
        queryParams,
        null,
        GetServeEventsResult.class);
  }

  @Override
  public SingleServedStubResult getServedStub(UUID id) {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetServedStubTask.class),
        PathParams.single("id", id),
        SingleServedStubResult.class);
  }

  @Override
  public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
    String body =
        postJsonAssertOkAndReturnBody(
            urlFor(GetRequestCountTask.class), Json.write(requestPattern));
    return VerificationResult.from(body);
  }

  @Override
  public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
    String body =
        postJsonAssertOkAndReturnBody(urlFor(FindRequestsTask.class), Json.write(requestPattern));
    return Json.read(body, FindRequestsResult.class);
  }

  @Override
  public FindRequestsResult findUnmatchedRequests() {
    String body = getJsonAssertOkAndReturnBody(urlFor(FindUnmatchedRequestsTask.class));
    return Json.read(body, FindRequestsResult.class);
  }

  @Override
  public void removeServeEvent(UUID eventId) {
    executeRequest(
        adminRoutes.requestSpecForTask(RemoveServeEventTask.class),
        PathParams.single("id", eventId),
        Void.class);
  }

  @Override
  public FindServeEventsResult removeServeEventsMatching(RequestPattern requestPattern) {
    String body =
        postJsonAssertOkAndReturnBody(
            urlFor(RemoveServeEventsByRequestPatternTask.class), Json.write(requestPattern));
    return Json.read(body, FindServeEventsResult.class);
  }

  @Override
  public FindServeEventsResult removeServeEventsForStubsMatchingMetadata(
      StringValuePattern metadataPattern) {
    String body =
        postJsonAssertOkAndReturnBody(
            urlFor(RemoveServeEventsByStubMetadataTask.class), Json.write(metadataPattern));
    return Json.read(body, FindServeEventsResult.class);
  }

  @Override
  public FindNearMissesResult findNearMissesForUnmatchedRequests() {
    String body = getJsonAssertOkAndReturnBody(urlFor(FindNearMissesForUnmatchedTask.class));
    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public GetScenariosResult getAllScenarios() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetAllScenariosTask.class), GetScenariosResult.class);
  }

  @Override
  public void resetScenario(String name) {
    executeRequest(
        adminRoutes.requestSpecForTask(SetScenarioStateTask.class),
        PathParams.single("name", name),
        Void.class);
  }

  @Override
  public void setScenarioState(String name, String state) {
    executeRequest(
        adminRoutes.requestSpecForTask(SetScenarioStateTask.class),
        PathParams.single("name", name),
        new ScenarioState(state),
        Void.class);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
    String body =
        postJsonAssertOkAndReturnBody(
            urlFor(FindNearMissesForRequestTask.class), Json.write(loggedRequest));

    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
    String body =
        postJsonAssertOkAndReturnBody(
            urlFor(FindNearMissesForRequestPatternTask.class), Json.write(requestPattern));

    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public void updateGlobalSettings(GlobalSettings settings) {
    postJsonAssertOkAndReturnBody(urlFor(GlobalSettingsUpdateTask.class), Json.write(settings));
  }

  @Override
  public SnapshotRecordResult snapshotRecord() {
    String body = postJsonAssertOkAndReturnBody(urlFor(SnapshotTask.class), "");

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public SnapshotRecordResult snapshotRecord(RecordSpecBuilder spec) {
    return snapshotRecord(spec.build());
  }

  @Override
  public SnapshotRecordResult snapshotRecord(RecordSpec spec) {
    String body = postJsonAssertOkAndReturnBody(urlFor(SnapshotTask.class), Json.write(spec));

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public void startRecording(String targetBaseUrl) {
    startRecording(RecordSpec.forBaseUrl(targetBaseUrl));
  }

  @Override
  public void startRecording(RecordSpec recordSpec) {
    postJsonAssertOkAndReturnBody(urlFor(StartRecordingTask.class), Json.write(recordSpec));
  }

  @Override
  public void startRecording(RecordSpecBuilder recordSpec) {
    startRecording(recordSpec.build());
  }

  @Override
  public SnapshotRecordResult stopRecording() {
    String body = postJsonAssertOkAndReturnBody(urlFor(StopRecordingTask.class), "");

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public RecordingStatusResult getRecordingStatus() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetRecordingStatusTask.class), RecordingStatusResult.class);
  }

  @Override
  public Options getOptions() {
    return new WireMockConfiguration().port(port).bindAddress(host);
  }

  @Override
  public void shutdownServer() {
    postJsonAssertOkAndReturnBody(urlFor(ShutdownServerTask.class), null);
  }

  @Override
  public ListStubMappingsResult findUnmatchedStubs() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetUnmatchedStubMappingsTask.class),
        ListStubMappingsResult.class);
  }

  @Override
  public ListStubMappingsResult findAllStubsByMetadata(StringValuePattern pattern) {
    return executeRequest(
        adminRoutes.requestSpecForTask(FindStubMappingsByMetadataTask.class),
        pattern,
        ListStubMappingsResult.class);
  }

  @Override
  public void removeStubsByMetadata(StringValuePattern pattern) {
    executeRequest(
        adminRoutes.requestSpecForTask(RemoveStubMappingsByMetadataTask.class),
        pattern,
        Void.class);
  }

  @Override
  public void importStubs(StubImport stubImport) {
    executeRequest(
        adminRoutes.requestSpecForTask(ImportStubMappingsTask.class), stubImport, Void.class);
  }

  @Override
  public void removeStubMappings(List<StubMapping> stubMappings) {
    executeRequest(
        adminRoutes.requestSpecForTask(RemoveMatchingStubMappingTask.class),
        Map.of("mappings", stubMappings),
        Void.class);
  }

  @Override
  public GetGlobalSettingsResult getGlobalSettings() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetGlobalSettingsTask.class), GetGlobalSettingsResult.class);
  }

  public int port() {
    return port;
  }

  private String postJsonAssertOkAndReturnBody(String url, String json) {
    ImmutableRequest.Builder post = ImmutableRequest.create().withMethod(POST).withAbsoluteUrl(url);
    post.withHeader(CONTENT_TYPE, "application/json");
    post.withBody(Optional.ofNullable(json).orElse("").getBytes(UTF_8));
    return safelyExecuteRequest(url, post);
  }

  @SuppressWarnings("UnusedReturnValue")
  private String putJsonAssertOkAndReturnBody(String url, String json) {
    ImmutableRequest.Builder put = ImmutableRequest.create().withMethod(PUT).withAbsoluteUrl(url);
    put.withHeader(CONTENT_TYPE, "application/json");
    put.withBody(Optional.ofNullable(json).orElse("").getBytes(UTF_8));
    return safelyExecuteRequest(url, put);
  }

  protected String getJsonAssertOkAndReturnBody(String url) {
    ImmutableRequest.Builder get = ImmutableRequest.create().withMethod(GET).withAbsoluteUrl(url);
    return safelyExecuteRequest(url, get);
  }

  private void executeRequest(RequestSpec requestSpec) {
    executeRequest(requestSpec, PathParams.empty(), null, Void.class);
  }

  private <B, R> R executeRequest(RequestSpec requestSpec, B requestBody, Class<R> responseType) {
    return executeRequest(requestSpec, PathParams.empty(), requestBody, responseType);
  }

  private <R> R executeRequest(RequestSpec requestSpec, Class<R> responseType) {
    return executeRequest(requestSpec, PathParams.empty(), null, responseType);
  }

  private <R> R executeRequest(
      RequestSpec requestSpec, PathParams pathParams, Class<R> responseType) {
    return executeRequest(requestSpec, pathParams, null, responseType);
  }

  private <B, R> R executeRequest(
      RequestSpec requestSpec, PathParams pathParams, B requestBody, Class<R> responseType) {
    return executeRequest(requestSpec, pathParams, QueryParams.EMPTY, requestBody, responseType);
  }

  private <B, R> R executeRequest(
      RequestSpec requestSpec,
      PathParams pathParams,
      QueryParams queryParams,
      B requestBody,
      Class<R> responseType) {
    String url = getAdminUrl(requestSpec.path(pathParams) + queryParams);
    ImmutableRequest.Builder requestBuilder =
        ImmutableRequest.create().withMethod(requestSpec.method()).withAbsoluteUrl(url);

    if (requestSpec.method().hasEntity()) {
      requestBuilder.withBody(
          Optional.ofNullable(requestBody).map(Json::write).orElse("").getBytes(UTF_8));
      requestBuilder.withHeader(CONTENT_TYPE, "application/json");
    }

    String responseBodyString = safelyExecuteRequest(url, requestBuilder);

    return responseType == Void.class ? null : Json.read(responseBodyString, responseType);
  }

  private void injectHeaders(ImmutableRequest.Builder request) {
    if (hostHeader != null) {
      request.withHeader(HOST, hostHeader);
    }

    List<HttpHeader> httpHeaders = authenticator.generateAuthHeaders();
    httpHeaders.forEach(header -> request.withHeader(header.key(), header.values()));
  }

  private void verifyResponseStatus(String url, int responseStatusCode) {
    if (HttpStatus.isServerError(responseStatusCode)) {
      throw new VerificationException(responseStatusErrorMessage(url, responseStatusCode));
    }

    if (responseStatusCode == 401) {
      throw new NotAuthorisedException(responseStatusErrorMessage(url, responseStatusCode));
    }
  }

  private String responseStatusErrorMessage(String url, int responseStatusCode) {
    return "Expected status 2xx for " + url + " but was " + responseStatusCode;
  }

  private String safelyExecuteRequest(String url, ImmutableRequest.Builder request) {
    injectHeaders(request);

    try {
      Response response = httpClient.execute(request.build());
      int statusCode = response.getStatus();

      verifyResponseStatus(url, statusCode);

      String body = response.getBodyAsString();
      if (HttpStatus.isClientError(statusCode)) {
        throw parseClientError(url, body, statusCode);
      }

      return body;
    } catch (Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  static ClientError parseClientError(String url, String responseBody, int responseStatusCode) {
    Errors errors;
    try {
      errors = Json.read(responseBody, Errors.class);
    } catch (JsonException e) {
      Errors.Error jsonError = e.getErrors().first();
      String jsonErrorDetail = jsonError.getDetail();
      String extendedDetail =
          "Error parsing response body '"
              + responseBody
              + "' with status code "
              + responseStatusCode
              + " for "
              + url
              + ". Error: "
              + jsonErrorDetail;
      errors =
          Errors.single(
              jsonError.getCode(),
              jsonError.getSource().getPointer(),
              jsonError.getTitle(),
              extendedDetail);
    }

    return ClientError.fromErrors(errors);
  }

  private String urlFor(Class<? extends AdminTask> taskClass) {
    return urlFor(taskClass, PathParams.empty());
  }

  private String urlFor(Class<? extends AdminTask> taskClass, PathParams pathParams) {
    RequestSpec requestSpec = adminRoutes.requestSpecForTask(taskClass);
    requireNonNull(requestSpec, "No admin task URL is registered for " + taskClass.getSimpleName());
    return getAdminUrl(requestSpec.path(pathParams));
  }

  private String getAdminUrl(String pathSuffix) {
    String portPart = port == NO_PORT_DEFINED ? "" : ":" + port;
    return String.format(ADMIN_URL_PREFIX + pathSuffix, scheme, host, portPart, urlPathPrefix);
  }
}
