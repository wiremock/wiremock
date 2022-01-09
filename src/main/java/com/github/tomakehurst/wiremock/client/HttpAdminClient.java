/*
 * Copyright (C) 2011-2021 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.security.NoClientAuthenticator.noClientAuthenticator;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.hc.core5.http.HttpHeaders.HOST;

import com.github.tomakehurst.wiremock.admin.*;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.common.*;
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
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
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

  public HttpAdminClient(final String scheme, final String host, final int port) {
    this(scheme, host, port, "");
  }

  public HttpAdminClient(final String host, final int port, final String urlPathPrefix) {
    this("http", host, port, urlPathPrefix);
  }

  public HttpAdminClient(final String scheme, final String host, final int port, final String urlPathPrefix) {
    this(scheme, host, port, urlPathPrefix, null, null, 0, noClientAuthenticator());
  }

  public HttpAdminClient(
      final String scheme, final String host, final int port, final String urlPathPrefix, final String hostHeader) {
        this(scheme, host, port, urlPathPrefix, hostHeader, null, 0, noClientAuthenticator());
    }

  public HttpAdminClient(
      final String scheme,
                           final String host,
                           final int port,
                           final String urlPathPrefix,
                           final String hostHeader,
                           final String proxyHost,
                           final int proxyPort) {
        this(scheme, host, port, urlPathPrefix, hostHeader, proxyHost, proxyPort, noClientAuthenticator());
    }

  public HttpAdminClient(
      final String scheme,
                           final String host,
                           final int port,
                           final String urlPathPrefix,
                           final String hostHeader,
                           final String proxyHost,
                           final int proxyPort,
                           final ClientAuthenticator authenticator) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.urlPathPrefix = urlPathPrefix;
        this.hostHeader = hostHeader;
        this.authenticator = authenticator;

    this.adminRoutes = AdminRoutes.defaults();

    this.httpClient = HttpClientFactory.createClient(this.createProxySettings(proxyHost, proxyPort));
  }

  public HttpAdminClient(final String host, final int port) {
    this(host, port, "");
  }

  private static StringEntity jsonStringEntity(final String json) {
    return new StringEntity(json, StandardCharsets.UTF_8);
  }

  @Override
  public void addStubMapping(final StubMapping stubMapping) {
        if (stubMapping.getRequest().hasInlineCustomMatcher()) {
            throw new AdminException("Custom matchers can't be used when administering a remote WireMock server. "
                                            + "Use WireMockRule.stubFor() or WireMockServer.stubFor() to administer the local instance.");
    }

    this.executeRequest(
        this.adminRoutes.requestSpecForTask(CreateStubMappingTask.class),
        PathParams.empty(),
        stubMapping,
        Void.class);
  }

  @Override
  public void editStubMapping(final StubMapping stubMapping) {
        this.postJsonAssertOkAndReturnBody(this.urlFor(OldEditStubMappingTask.class), Json.write(stubMapping));
  }

  @Override
  public void removeStubMapping(final StubMapping stubbMapping) {
        this.postJsonAssertOkAndReturnBody(this.urlFor(OldRemoveStubMappingTask.class), Json.write(stubbMapping));
  }

  @Override
  public ListStubMappingsResult listAllStubMappings() {
    return this.executeRequest(
        this.adminRoutes.requestSpecForTask(GetAllStubMappingsTask.class), ListStubMappingsResult.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public SingleStubMappingResult getStubMapping(final UUID id) {
        return this.executeRequest(
        this.adminRoutes.requestSpecForTask(GetStubMappingTask.class),
        PathParams.single("id", id),
        SingleStubMappingResult.class);
  }

  @Override
  public void saveMappings() {
    this.postJsonAssertOkAndReturnBody(this.urlFor(SaveMappingsTask.class), null);
  }

  @Override
  public void resetAll() {
    this.postJsonAssertOkAndReturnBody(this.urlFor(ResetTask.class), null);
  }

  @Override
  public void resetRequests() {
    this.executeRequest(this.adminRoutes.requestSpecForTask(ResetRequestsTask.class));
  }

  @Override
  public void resetScenarios() {
    this.executeRequest(this.adminRoutes.requestSpecForTask(ResetScenariosTask.class));
  }

  @Override
  public void resetMappings() {
    this.executeRequest(this.adminRoutes.requestSpecForTask(ResetStubMappingsTask.class));
  }

  @Override
  public void resetToDefaultMappings() {
    this.postJsonAssertOkAndReturnBody(this.urlFor(ResetToDefaultMappingsTask.class), null);
  }

  @Override
  public GetServeEventsResult getServeEvents() {
    return this.executeRequest(
        this.adminRoutes.requestSpecForTask(GetAllRequestsTask.class), GetServeEventsResult.class);
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
  public SingleServedStubResult getServedStub(final UUID id) {
        return this.executeRequest(
        this.adminRoutes.requestSpecForTask(GetServedStubTask.class),
        PathParams.single("id", id),
        SingleServedStubResult.class);
  }

  @Override
  public VerificationResult countRequestsMatching(final RequestPattern requestPattern) {
        final String body = this.postJsonAssertOkAndReturnBody(
            this.urlFor(GetRequestCountTask.class), Json.write(requestPattern));
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
    final String body = this.getJsonAssertOkAndReturnBody(this.urlFor(FindUnmatchedRequestsTask.class));
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
    final String body = this.getJsonAssertOkAndReturnBody(this.urlFor(FindNearMissesForUnmatchedTask.class));
    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public GetScenariosResult getAllScenarios() {
    return this.executeRequest(
                this.adminRoutes.requestSpecForTask(GetAllScenariosTask.class),
                GetScenariosResult.class);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(final LoggedRequest loggedRequest) {
        final String body = this.postJsonAssertOkAndReturnBody(
            this.urlFor(FindNearMissesForRequestTask.class), Json.write(loggedRequest));

    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public FindNearMissesResult findTopNearMissesFor(final RequestPattern requestPattern) {
        final String body = this.postJsonAssertOkAndReturnBody(
            this.urlFor(FindNearMissesForRequestPatternTask.class), Json.write(requestPattern));

    return Json.read(body, FindNearMissesResult.class);
  }

  @Override
  public void updateGlobalSettings(final GlobalSettings settings) {
        this.postJsonAssertOkAndReturnBody(this.urlFor(GlobalSettingsUpdateTask.class), Json.write(settings));
  }

  @Override
  public SnapshotRecordResult snapshotRecord() {
    final String body = this.postJsonAssertOkAndReturnBody(
                this.urlFor(SnapshotTask.class),
                "");

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public SnapshotRecordResult snapshotRecord(final RecordSpecBuilder spec) {
    return this.snapshotRecord(spec.build());
  }

  @Override
  public SnapshotRecordResult snapshotRecord(final RecordSpec spec) {
        final String body = this.postJsonAssertOkAndReturnBody(
                this.urlFor(SnapshotTask.class),
                Json.write(spec));

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public void startRecording(final String targetBaseUrl) {
        this.startRecording(RecordSpec.forBaseUrl(targetBaseUrl));
  }

  @Override
  public void startRecording(final RecordSpec recordSpec) {
        this.postJsonAssertOkAndReturnBody(
                this.urlFor(StartRecordingTask.class),
                Json.write(recordSpec));
    }

  @Override
  public void startRecording(final RecordSpecBuilder recordSpec) {
        this.startRecording(recordSpec.build());
  }

  @Override
  public SnapshotRecordResult stopRecording() {
    final String body = this.postJsonAssertOkAndReturnBody(
                this.urlFor(StopRecordingTask.class),
                "");

    return Json.read(body, SnapshotRecordResult.class);
  }

  @Override
  public RecordingStatusResult getRecordingStatus() {
    return this.executeRequest(
        this.adminRoutes.requestSpecForTask(GetRecordingStatusTask.class), RecordingStatusResult.class);
  }

  @Override
  public Options getOptions() {
    return new WireMockConfiguration().port(this.port).bindAddress(this.host);
  }

  @Override
  public void shutdownServer() {
    this.postJsonAssertOkAndReturnBody(this.urlFor(ShutdownServerTask.class), null);
  }

  @Override
    public ProxyConfig getProxyConfig() {
        return this.executeRequest(this.adminRoutes.requestSpecForTask(GetProxyConfigTask.class), ProxyConfig.class);
    }

    @Override
    public void enableProxy(final UUID id) {
        this.postJsonAssertOkAndReturnBody(this.urlFor(EnableProxyTask.class), null);
    }

    @Override
    public void disableProxy(final UUID id) {
        this.postJsonAssertOkAndReturnBody(this.urlFor(DisableProxyTask.class), null);
    }

    @Override
  public ListStubMappingsResult findAllStubsByMetadata(final StringValuePattern pattern) {
        return this.executeRequest(
                this.adminRoutes.requestSpecForTask(FindStubMappingsByMetadataTask.class),
                pattern,
                ListStubMappingsResult.class);
  }

  @Override
  public void removeStubsByMetadata(final StringValuePattern pattern) {
        this.executeRequest(
                this.adminRoutes.requestSpecForTask(RemoveStubMappingsByMetadataTask.class),
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
    return this.port;
  }

  private ProxySettings createProxySettings(final String proxyHost, final int proxyPort) {
    if (StringUtils.isNotBlank(proxyHost)) {
      return new ProxySettings(proxyHost, proxyPort);
    }
    return ProxySettings.NO_PROXY;
  }

  private String postJsonAssertOkAndReturnBody(final String url, final String json) {
        final HttpPost post = new HttpPost(url);
        if (json != null) {
            post.setEntity(HttpAdminClient.jsonStringEntity(json));
    }

    return this.safelyExecuteRequest(url, post);
  }

  protected String getJsonAssertOkAndReturnBody(final String url) {
        final HttpGet get = new HttpGet(url);
        return this.safelyExecuteRequest(url, get);
  }

  private void executeRequest(final RequestSpec requestSpec) {
        this.executeRequest(requestSpec, PathParams.empty(), null, Void.class);
  }

  private <B, R> R executeRequest(final RequestSpec requestSpec, final B requestBody, final Class<R> responseType) {
    return this.executeRequest(requestSpec, PathParams.empty(), requestBody, responseType);
  }

  private <B, R> R executeRequest(final RequestSpec requestSpec, final Class<R> responseType) {
    return this.executeRequest(requestSpec, PathParams.empty(), null, responseType);
  }

  private <B, R> R executeRequest(
      final RequestSpec requestSpec, final PathParams pathParams, final Class<R> responseType) {
        return this.executeRequest(requestSpec, pathParams, null, responseType);
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

    if (requestBody != null) {
      requestBuilder.setEntity(HttpAdminClient.jsonStringEntity(Json.write(requestBody)));
    }

    final String responseBodyString = this.safelyExecuteRequest(url, requestBuilder.build());

    return responseType == Void.class ? null : Json.read(responseBodyString, responseType);
  }

  private String safelyExecuteRequest(String url, ClassicHttpRequest request) {
    if (hostHeader != null) {
      request.addHeader(HOST, hostHeader);
    }

    final List<HttpHeader> httpHeaders = this.authenticator.generateAuthHeaders();
    for (final HttpHeader header : httpHeaders) {
            for (final String value : header.values()) {
                request.addHeader(header.key(), value);
            }
        }

    try (final CloseableHttpResponse response = this.httpClient.execute(request)) {
      final int statusCode = response.getCode();
      if (HttpStatus.isServerError(statusCode)) {
        throw new VerificationException(
            "Expected status 2xx for " + url + " but was " + statusCode);
      }

      if (statusCode == 401) {
        throw new NotAuthorisedException();
      }

      final String body = getEntityAsStringAndCloseStream(response);
            if (HttpStatus.isClientError(statusCode)) {
                final Errors errors = Json.read(body, Errors.class);
                throw ClientError.fromErrors(errors);
            }

      return body;
    } catch (final Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  private String urlFor(final Class<? extends AdminTask> taskClass) {
        final RequestSpec requestSpec = this.adminRoutes.requestSpecForTask(taskClass);
    checkNotNull(requestSpec, "No admin task URL is registered for " + taskClass.getSimpleName());
    return String.format(HttpAdminClient.ADMIN_URL_PREFIX + requestSpec.path(), this.scheme, this.host, this.port, this.urlPathPrefix);
  }
}
