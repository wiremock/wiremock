/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.admin.*;
import com.github.tomakehurst.wiremock.admin.model.*;
import com.github.tomakehurst.wiremock.admin.tasks.*;
import com.github.tomakehurst.wiremock.common.AdminException;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.*;
import static org.apache.http.HttpHeaders.HOST;

public class HttpAdminClient implements Admin {

    private static final String ADMIN_URL_PREFIX = "%s://%s:%d%s/__admin";

    private final String scheme;
    private final String host;
    private final int port;
    private final String urlPathPrefix;
    private final String hostHeader;

    private final AdminRoutes adminRoutes;

    private final CloseableHttpClient httpClient;

    public HttpAdminClient(String scheme, String host, int port) {
        this(scheme, host, port, "");
    }

    public HttpAdminClient(String host, int port, String urlPathPrefix) {
        this("http", host, port, urlPathPrefix);
    }

    public HttpAdminClient(String scheme, String host, int port, String urlPathPrefix) {
        this(scheme, host, port, urlPathPrefix, null);
    }

    public HttpAdminClient(String scheme, String host, int port, String urlPathPrefix, String hostHeader) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.urlPathPrefix = urlPathPrefix;
        this.hostHeader = hostHeader;

        adminRoutes = AdminRoutes.defaults();

        httpClient = HttpClientFactory.createClient();
    }

    public HttpAdminClient(String host, int port) {
        this(host, port, "");
    }

    @Override
    public void addStubMapping(StubMapping stubMapping) {
        if (stubMapping.getRequest().hasCustomMatcher()) {
            throw new AdminException("Custom matchers can't be used when administering a remote WireMock server. " +
                "Use WireMockRule.stubFor() or WireMockServer.stubFor() to administer the local instance.");
        }

        executeRequest(
            adminRoutes.requestSpecForTask(CreateStubMappingTask.class),
            PathParams.empty(),
            stubMapping,
            Void.class,
            201
        );
    }

    @Override
    public void editStubMapping(StubMapping stubMapping) {
        postJsonAssertOkAndReturnBody(
            urlFor(OldEditStubMappingTask.class),
            Json.write(stubMapping),
            HTTP_NO_CONTENT);
    }

    @Override
    public void removeStubMapping(StubMapping stubbMapping) {
        postJsonAssertOkAndReturnBody(
            urlFor(OldRemoveStubMappingTask.class),
            Json.write(stubbMapping),
            HTTP_OK);
    }

    @Override
    public ListStubMappingsResult listAllStubMappings() {
        return executeRequest(
            adminRoutes.requestSpecForTask(GetAllStubMappingsTask.class),
            ListStubMappingsResult.class
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public SingleStubMappingResult getStubMapping(UUID id) {
        return executeRequest(
            adminRoutes.requestSpecForTask(GetStubMappingTask.class),
            PathParams.single("id", id),
            SingleStubMappingResult.class
        );
    }

    @Override
    public void saveMappings() {
        postJsonAssertOkAndReturnBody(urlFor(SaveMappingsTask.class), null, HTTP_OK);
    }

    @Override
    public void resetAll() {
        postJsonAssertOkAndReturnBody(urlFor(ResetTask.class), null, HTTP_OK);
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
        postJsonAssertOkAndReturnBody(urlFor(ResetToDefaultMappingsTask.class), null, HTTP_OK);
    }

    @Override
    public GetServeEventsResult getServeEvents() {
        return executeRequest(
            adminRoutes.requestSpecForTask(GetAllRequestsTask.class),
            GetServeEventsResult.class
        );
    }

    @Override
    public SingleServedStubResult getServedStub(UUID id) {
        return executeRequest(
            adminRoutes.requestSpecForTask(GetServedStubTask.class),
            PathParams.single("id", id),
            SingleServedStubResult.class
        );
    }

    @Override
    public VerificationResult countRequestsMatching(RequestPattern requestPattern) {
        String body = postJsonAssertOkAndReturnBody(
            urlFor(GetRequestCountTask.class),
            Json.write(requestPattern),
            HTTP_OK);
        return VerificationResult.from(body);
    }

    @Override
    public FindRequestsResult findRequestsMatching(RequestPattern requestPattern) {
        String body = postJsonAssertOkAndReturnBody(
            urlFor(FindRequestsTask.class),
            Json.write(requestPattern),
            HTTP_OK);
        return Json.read(body, FindRequestsResult.class);
    }

    @Override
    public FindRequestsResult findUnmatchedRequests() {
        String body = getJsonAssertOkAndReturnBody(
            urlFor(FindUnmatchedRequestsTask.class),
            HTTP_OK);
        return Json.read(body, FindRequestsResult.class);
    }

    @Override
    public FindNearMissesResult findNearMissesForUnmatchedRequests() {
        String body = getJsonAssertOkAndReturnBody(urlFor(FindNearMissesForUnmatchedTask.class), HTTP_OK);
        return Json.read(body, FindNearMissesResult.class);
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(LoggedRequest loggedRequest) {
        String body = postJsonAssertOkAndReturnBody(
            urlFor(FindNearMissesForRequestTask.class),
            Json.write(loggedRequest),
            HTTP_OK
        );

        return Json.read(body, FindNearMissesResult.class);
    }

    @Override
    public FindNearMissesResult findTopNearMissesFor(RequestPattern requestPattern) {
        String body = postJsonAssertOkAndReturnBody(
            urlFor(FindNearMissesForRequestPatternTask.class),
            Json.write(requestPattern),
            HTTP_OK
        );

        return Json.read(body, FindNearMissesResult.class);
    }

    @Override
    public void updateGlobalSettings(GlobalSettings settings) {
        postJsonAssertOkAndReturnBody(
            urlFor(GlobalSettingsUpdateTask.class),
            Json.write(settings),
            HTTP_OK);
    }

    @Override
    public void shutdownServer() {
        postJsonAssertOkAndReturnBody(urlFor(ShutdownServerTask.class), null, HTTP_OK);
    }

    public int port() {
        return port;
    }

    private String postJsonAssertOkAndReturnBody(String url, String json, int expectedStatus) {
        HttpPost post = new HttpPost(url);
        if (json != null) {
            post.setEntity(jsonStringEntity(json));
        }

        return safelyExecuteRequest(url, expectedStatus, post);
    }

    private static StringEntity jsonStringEntity(String json) {
        return new StringEntity(json, UTF_8.name());
    }

    protected String getJsonAssertOkAndReturnBody(String url, int expectedStatus) {
        HttpGet get = new HttpGet(url);
        return safelyExecuteRequest(url, expectedStatus, get);
    }

    private void executeRequest(RequestSpec requestSpec) {
        executeRequest(requestSpec, PathParams.empty(), null, Void.class, 200);
    }

    private <B, R> R executeRequest(RequestSpec requestSpec, B requestBody, Class<R> responseType) {
        return executeRequest(requestSpec, PathParams.empty(),requestBody, responseType, 200);
    }

    private <B, R> R executeRequest(RequestSpec requestSpec, Class<R> responseType) {
        return executeRequest(requestSpec, PathParams.empty(), null, responseType, 200);
    }

    private <B, R> R executeRequest(RequestSpec requestSpec, PathParams pathParams, Class<R> responseType) {
        return executeRequest(requestSpec, pathParams, null, responseType, 200);
    }

    private <B, R> R executeRequest(RequestSpec requestSpec, PathParams pathParams, B requestBody, Class<R> responseType, int expectedStatus) {
        String url = String.format(ADMIN_URL_PREFIX + requestSpec.path(pathParams), scheme, host, port, urlPathPrefix);
        RequestBuilder requestBuilder = RequestBuilder
            .create(requestSpec.method().getName())
            .setUri(url);

        if (requestBody != null) {
            requestBuilder.setEntity(jsonStringEntity(Json.write(requestBody)));
        }

        String responseBodyString = safelyExecuteRequest(url, expectedStatus, requestBuilder.build());

        return responseType == Void.class ?
            null :
            Json.read(responseBodyString, responseType);
    }

    private String safelyExecuteRequest(String url, int expectedStatus, HttpUriRequest request) {
        if (hostHeader != null) {
            request.addHeader(HOST, hostHeader);
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != expectedStatus) {
                throw new VerificationException(
                    "Expected status " + expectedStatus + " for " + url + " but was " + statusCode);
            }

            return getEntityAsStringAndCloseStream(response);
        } catch (Exception e) {
            return throwUnchecked(e, String.class);
        }
    }

    private String urlFor(Class<? extends AdminTask> taskClass) {
        RequestSpec requestSpec = adminRoutes.requestSpecForTask(taskClass);
        checkNotNull(requestSpec, "No admin task URL is registered for " + taskClass.getSimpleName());
        return String.format(ADMIN_URL_PREFIX + requestSpec.path(), scheme, host, port, urlPathPrefix);
    }
}
