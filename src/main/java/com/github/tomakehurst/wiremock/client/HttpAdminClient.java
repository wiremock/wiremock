/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.github.tomakehurst.wiremock.common.Strings.isNotBlank;
import static com.github.tomakehurst.wiremock.security.NoClientAuthenticator.noClientAuthenticator;
import static java.util.Objects.requireNonNull;
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
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpStatus;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

public class HttpAdminClient implements Admin {

  private static final String ADMIN_URL_PREFIX = "%s://%s:%d%s/__admin";

  private final String scheme;
  private final String host;
  private final int port;
  private final String urlPathPrefix;
  private final String hostHeader;
  private final ClientAuthenticator authenticator;

  private final AdminRoutes adminRoutes;

  private final CloseableHttpClient httpClient;

  public HttpAdminClient(String scheme, String host, int port) {
    this(scheme, host, port, "");
  }

  public HttpAdminClient(String host, int port, String urlPathPrefix) {
    this("http", host, port, urlPathPrefix);
  }

  public HttpAdminClient(String scheme, String host, int port, String urlPathPrefix) {
    this(scheme, host, port, urlPathPrefix, null, null, 0, noClientAuthenticator());
  }

  public HttpAdminClient(
      String scheme, String host, int port, String urlPathPrefix, String hostHeader) {
    this(scheme, host, port, urlPathPrefix, hostHeader, null, 0, noClientAuthenticator());
  }

  public HttpAdminClient(
      String scheme,
      String host,
      int port,
      String urlPathPrefix,
      String hostHeader,
      String proxyHost,
      int proxyPort) {
    this(
        scheme,
        host,
        port,
        urlPathPrefix,
        hostHeader,
        proxyHost,
        proxyPort,
        noClientAuthenticator());
  }

  public HttpAdminClient(
      String scheme,
      String host,
      int port,
      String urlPathPrefix,
      String hostHeader,
      String proxyHost,
      int proxyPort,
      ClientAuthenticator authenticator) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.urlPathPrefix = urlPathPrefix;
    this.hostHeader = hostHeader;
    this.authenticator = authenticator;

    adminRoutes = AdminRoutes.forClient();

    httpClient = HttpClientFactory.createClient(createProxySettings(proxyHost, proxyPort));
  }

  public HttpAdminClient(String host, int port) {
    this(host, port, "");
  }

  private static StringEntity jsonStringEntity(String json) {
    return new StringEntity(json, StandardCharsets.UTF_8);
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
  public GetGlobalSettingsResult getGlobalSettings() {
    return executeRequest(
        adminRoutes.requestSpecForTask(GetGlobalSettingsTask.class), GetGlobalSettingsResult.class);
  }

  public int port() {
    return port;
  }

  private ProxySettings createProxySettings(String proxyHost, int proxyPort) {
    if (isNotBlank(proxyHost)) {
      return new ProxySettings(proxyHost, proxyPort);
    }
    return ProxySettings.NO_PROXY;
  }

  private String postJsonAssertOkAndReturnBody(String url, String json) {
    HttpPost post = new HttpPost(url);
    post.setEntity(jsonStringEntity(Optional.ofNullable(json).orElse("")));
    return safelyExecuteRequest(url, post);
  }

  private String putJsonAssertOkAndReturnBody(String url, String json) {
    HttpPut put = new HttpPut(url);
    put.setEntity(jsonStringEntity(Optional.ofNullable(json).orElse("")));
    return safelyExecuteRequest(url, put);
  }

  protected String getJsonAssertOkAndReturnBody(String url) {
    HttpGet get = new HttpGet(url);
    return safelyExecuteRequest(url, get);
  }

  private void executeRequest(RequestSpec requestSpec) {
    executeRequest(requestSpec, PathParams.empty(), null, Void.class);
  }

  private <B, R> R executeRequest(RequestSpec requestSpec, B requestBody, Class<R> responseType) {
    return executeRequest(requestSpec, PathParams.empty(), requestBody, responseType);
  }

  private <B, R> R executeRequest(RequestSpec requestSpec, Class<R> responseType) {
    return executeRequest(requestSpec, PathParams.empty(), null, responseType);
  }

  private <B, R> R executeRequest(
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
    String url =
        String.format(
            ADMIN_URL_PREFIX + requestSpec.path(pathParams) + queryParams,
            scheme,
            host,
            port,
            urlPathPrefix);
    ClassicRequestBuilder requestBuilder =
        ClassicRequestBuilder.create(requestSpec.method().getName()).setUri(url);

    if (requestSpec.method().hasEntity()) {
      requestBuilder.setEntity(
          jsonStringEntity(Optional.ofNullable(requestBody).map(Json::write).orElse("")));
    }

    String responseBodyString = safelyExecuteRequest(url, requestBuilder.build());

    return responseType == Void.class ? null : Json.read(responseBodyString, responseType);
  }

  private void injectHeaders(ClassicHttpRequest request) {
    if (hostHeader != null) {
      request.addHeader(HOST, hostHeader);
    }

    List<HttpHeader> httpHeaders = authenticator.generateAuthHeaders();
    for (HttpHeader header : httpHeaders) {
      for (String value : header.values()) {
        request.addHeader(header.key(), value);
      }
    }
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

  private String safelyExecuteRequest(String url, ClassicHttpRequest request) {
    injectHeaders(request);

    try (CloseableHttpResponse response = httpClient.execute(request)) {
      int statusCode = response.getCode();

      verifyResponseStatus(url, statusCode);

      String body = getEntityAsStringAndCloseStream(response);
      if (HttpStatus.isClientError(statusCode)) {
        throwParsedClientError(url, body, statusCode);
      }

      return body;
    } catch (Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  private void throwParsedClientError(String url, String responseBody, int responseStatusCode) {
    Errors errors;
    try {
      errors = Json.read(responseBody, Errors.class);
    } catch (JsonException e) {
      Errors.Error jsonError = e.getErrors().first();
      String jsonErrorDetail = jsonError.getDetail();
      String extendedDetail =
          new StringBuilder()
              .append("Error parsing response body '")
              .append(responseBody)
              .append("' with status code ")
              .append(responseStatusCode)
              .append(" for ")
              .append(url)
              .append(". Error: ")
              .append(jsonErrorDetail)
              .toString();
      errors =
          Errors.single(
              jsonError.getCode(),
              jsonError.getSource().getPointer(),
              jsonError.getTitle(),
              extendedDetail);
    }

    throw ClientError.fromErrors(errors);
  }

  private String urlFor(Class<? extends AdminTask> taskClass) {
    return urlFor(taskClass, PathParams.empty());
  }

  private String urlFor(Class<? extends AdminTask> taskClass, PathParams pathParams) {
    RequestSpec requestSpec = adminRoutes.requestSpecForTask(taskClass);
    requireNonNull(requestSpec, "No admin task URL is registered for " + taskClass.getSimpleName());
    return String.format(
        ADMIN_URL_PREFIX + requestSpec.path(pathParams), scheme, host, port, urlPathPrefix);
  }
}
