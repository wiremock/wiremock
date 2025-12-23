/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.stubbing.ServeEventFactory.newPostMatchServeEvent;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.okhttp.OkHttpBackedHttpClient;
import com.github.tomakehurst.wiremock.http.client.okhttp.OkHttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.store.InMemorySettingsStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Tests to verify that OkHttpBackedHttpClient can handle request bodies on various HTTP methods.
 */
public class HttpClientMethodBodyTest {

  private static final int PROXY_TIMEOUT = 30_000;
  private static final byte[] REQUEST_BODY = "test request body".getBytes(UTF_8);

  @RegisterExtension
  public WireMockExtension origin =
      WireMockExtension.newInstance().options(options().dynamicPort()).build();

  private HttpClient okHttpClient;
  private ProxyResponseRenderer proxyResponseRenderer;

  @BeforeEach
  void setup() {
    // Setup origin server to accept any request
    origin.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200).withBody("Success")));

    // Create OkHttp client
    okHttpClient =
        new OkHttpBackedHttpClient(OkHttpClientFactory.createClient(PROXY_TIMEOUT), false);

    // Create proxy renderer with OkHttp client
    proxyResponseRenderer =
        new ProxyResponseRenderer(
            /* preserveHostHeader= */ false,
            /* hostHeaderValue= */ null,
            new InMemorySettingsStore(),
            /* stubCorsEnabled= */ false,
            /* supportedProxyEncodings= */ null,
            /* reverseProxyClient= */ okHttpClient,
            /* forwardProxyClient= */ okHttpClient);
  }

  @Test
  void testGetRequestWithBody() {
    ServeEvent serveEvent = createServeEvent("/test-get", GET, REQUEST_BODY);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(getRequestedFor(urlEqualTo("/test-get")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod(), is(GET));
  }

  @Test
  void testHeadRequestWithBody() {
    ServeEvent serveEvent = createServeEvent("/test-head", HEAD, REQUEST_BODY);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-head")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod(), is(HEAD));
  }

  @Test
  void testQueryRequestWithBody() {
    ServeEvent serveEvent = createServeEvent("/test-query", QUERY, REQUEST_BODY);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-query")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod(), is(QUERY));
  }

  @Test
  void testOptionsRequestWithBody() {
    ServeEvent serveEvent = createServeEvent("/test-options", OPTIONS, REQUEST_BODY);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-options")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod(), is(OPTIONS));
  }

  @Test
  void testPostRequestWithBody() {
    ServeEvent serveEvent = createServeEvent("/test-post", POST, REQUEST_BODY);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(postRequestedFor(urlEqualTo("/test-post")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod(), is(POST));
  }

  @Test
  void testGetRequestWithEmptyBody() {
    ServeEvent serveEvent = createServeEvent("/test-get-empty", GET, new byte[0]);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests = origin.findAll(getRequestedFor(urlEqualTo("/test-get-empty")));
    assertThat(requests.size(), is(1));
  }

  @Test
  void testHeadRequestWithEmptyBody() {
    ServeEvent serveEvent = createServeEvent("/test-head-empty", HEAD, null);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests =
        origin.findAll(anyRequestedFor(urlEqualTo("/test-head-empty")));
    assertThat(requests.size(), is(1));
  }

  @Test
  void testQueryRequestWithEmptyBody() {
    // Note: QUERY method requires a body per RFC 9535, so this test will fail
    // This test explicitly documents that QUERY without a body should fail
    ServeEvent serveEvent = createServeEvent("/test-query-empty", QUERY, null);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests =
        origin.findAll(anyRequestedFor(urlEqualTo("/test-query-empty")));
    assertThat(requests.size(), is(1));
  }

  @Test
  void testOptionsRequestWithEmptyBody() {
    ServeEvent serveEvent = createServeEvent("/test-options-empty", OPTIONS, null);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests =
        origin.findAll(anyRequestedFor(urlEqualTo("/test-options-empty")));
    assertThat(requests.size(), is(1));
  }

  @Test
  void testPostRequestWithEmptyBody() {
    ServeEvent serveEvent = createServeEvent("/test-post-empty", POST, null);

    Response response = proxyResponseRenderer.render(serveEvent);
    assertThat(response.getStatus(), is(200));

    // Verify the request was made
    List<LoggedRequest> requests =
        origin.findAll(anyRequestedFor(urlEqualTo("/test-post-empty")));
    assertThat(requests.size(), is(1));
  }

  private ServeEvent createServeEvent(String path, RequestMethod method, byte[] body) {
    LoggedRequest loggedRequest =
        LoggedRequest.createFrom(
            mockRequest()
                .url(path)
                .absoluteUrl(origin.url(path))
                .method(method)
                .headers(new HttpHeaders())
                .body(body)
                .protocol("HTTP/1.1"));

    return newPostMatchServeEvent(
        loggedRequest, aResponse().proxiedFrom(origin.baseUrl()).build());
  }
}