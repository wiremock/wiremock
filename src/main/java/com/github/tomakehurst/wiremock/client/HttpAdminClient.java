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

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.AdminTasks;
import com.github.tomakehurst.wiremock.admin.EditStubMappingTask;
import com.github.tomakehurst.wiremock.admin.FindNearMissesForRequestPatternTask;
import com.github.tomakehurst.wiremock.admin.FindNearMissesForRequestTask;
import com.github.tomakehurst.wiremock.admin.FindNearMissesForUnmatchedTask;
import com.github.tomakehurst.wiremock.admin.FindRequestsTask;
import com.github.tomakehurst.wiremock.admin.FindUnmatchedRequestsTask;
import com.github.tomakehurst.wiremock.admin.GetRequestCountTask;
import com.github.tomakehurst.wiremock.admin.GlobalSettingsUpdateTask;
import com.github.tomakehurst.wiremock.admin.RemoveStubMappingTask;
import com.github.tomakehurst.wiremock.admin.RequestSpec;
import com.github.tomakehurst.wiremock.admin.ResetRequestsTask;
import com.github.tomakehurst.wiremock.admin.ResetScenariosTask;
import com.github.tomakehurst.wiremock.admin.ResetTask;
import com.github.tomakehurst.wiremock.admin.ResetToDefaultMappingsTask;
import com.github.tomakehurst.wiremock.admin.RootTask;
import com.github.tomakehurst.wiremock.admin.SaveMappingsTask;
import com.github.tomakehurst.wiremock.admin.ShutdownServerTask;
import com.github.tomakehurst.wiremock.admin.StubMappingTask;
import com.github.tomakehurst.wiremock.common.AdminException;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.FindNearMissesResult;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.UnsupportedEncodingException;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsStringAndCloseStream;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.apache.http.HttpHeaders.HOST;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class HttpAdminClient implements Admin {

    private static final String ADMIN_URL_PREFIX = "%s://%s:%d%s/__admin";

    private final String scheme;
    private final String host;
    private final int port;
    private final String urlPathPrefix;
    private final String hostHeader;

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

        postJsonAssertOkAndReturnBody(
                urlFor(StubMappingTask.class),
                Json.write(stubMapping),
                HTTP_CREATED);
    }

    @Override
    public void editStubMapping(StubMapping stubMapping) {
        postJsonAssertOkAndReturnBody(
                urlFor(EditStubMappingTask.class),
                Json.write(stubMapping),
                HTTP_NO_CONTENT);
    }

    @Override
    public void removeStubMapping(StubMapping stubbMapping) {

        postJsonAssertOkAndReturnBody(
                urlFor(RemoveStubMappingTask.class),
                Json.write(stubbMapping),
                HTTP_OK);

    }

    @Override
    public ListStubMappingsResult listAllStubMappings() {
        String body = getJsonAssertOkAndReturnBody(
                urlFor(RootTask.class),
                HTTP_OK);
        return Json.read(body, ListStubMappingsResult.class);
    }

    @Override
    public void saveMappings() {
        postJsonAssertOkAndReturnBody(urlFor(SaveMappingsTask.class), null, HTTP_OK);
    }

    @Override
    public void resetMappings() {
        postJsonAssertOkAndReturnBody(urlFor(ResetTask.class), null, HTTP_OK);
    }

    @Override
    public void resetRequests() {
        postJsonAssertOkAndReturnBody(urlFor(ResetRequestsTask.class), null, HTTP_OK);
    }

    @Override
    public void resetScenarios() {
        postJsonAssertOkAndReturnBody(urlFor(ResetScenariosTask.class), null, HTTP_OK);
    }

    @Override
    public void resetToDefaultMappings() {
        postJsonAssertOkAndReturnBody(urlFor(ResetToDefaultMappingsTask.class), null, HTTP_OK);
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

    private String safelyExecuteRequest(String url, int expectedStatus, HttpRequestBase request) {
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
        RequestSpec requestSpec = AdminTasks.requestSpecForTask(taskClass);
        checkNotNull(requestSpec, "No admin task URL is registered for " + taskClass.getSimpleName());
        return String.format(ADMIN_URL_PREFIX + requestSpec.path(), scheme, host, port, urlPathPrefix);
    }
}
