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
package com.github.tomakehurst.wiremock.proxy;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_LENGTH;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProxiedHostnameRewriteResponseTransformerTest {

  private static final String LOCATION = "Location";

  private String targetServiceBaseUrl;

  WireMockServer targetService;
  WireMock target;

  WireMockServer proxyingService;
  WireMock proxy;

  WireMockTestClient testClient;

  void init(WireMockConfiguration proxyingServiceOptions) {
    targetService =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .bindAddress("127.0.0.1")
                .stubCorsEnabled(true));
    targetService.start();
    target = WireMock.create().port(targetService.port()).build();

    targetServiceBaseUrl = "http://localhost:" + targetService.port();

    proxyingServiceOptions.dynamicPort().bindAddress("127.0.0.1");
    proxyingService = new WireMockServer(proxyingServiceOptions);
    proxyingService.start();
    proxy = WireMock.create().port(proxyingService.port()).build();

    testClient = new WireMockTestClient(proxyingService.port());

    WireMock.configureFor(targetService.port());
  }

  void initWithDefaultConfig() {
    init(wireMockConfig());
  }

  @AfterEach
  public void stop() {
    targetService.stop();
    proxyingService.stop();
  }

  @Test
  public void rewritesTheHostnameInHeader() {
    initWithDefaultConfig();

    // Set up the target service to return a redirect
    target.register(
        get(urlPathEqualTo("/start"))
            .willReturn(
                aResponse()
                    .withStatus(307) // Temporary redirect
                    .withHeader(LOCATION, "http://localhost:" + targetService.port() + "/end")));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/start");

    // Verify the location header has been rewritten
    assertThat(response.firstHeader(LOCATION), is("http://localhost:" + proxyingService.port() + "/end"));
  }

  @Test
  public void rewritesThePortInHeader() {
    initWithDefaultConfig();

    // Set up the target service to return a redirect
    target.register(
        get(urlPathEqualTo("/start"))
            .willReturn(
                aResponse()
                    .withStatus(307) // Temporary redirect
                    .withHeader(LOCATION, "http://localhost:" + targetService.port() + "/end")));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/start");

    // Verify the location header has been rewritten
    assertThat(
        response.firstHeader(LOCATION), is("http://localhost:" + proxyingService.port() + "/end"));
  }

  @Test
  public void rewritesTheHostnameInUngzippedBody() {
    initWithDefaultConfig();

    // JSON response with a link containing the localhost hostname
    String responseString = "{ \"link\": \"http://localhost:" + targetService.port() + "/other\" }";

    // Set up the target service to return the JSON
    target.register(
        get(urlPathEqualTo("/json"))
            .willReturn(
                okJson(responseString)
                    .withHeader(CONTENT_LENGTH, String.valueOf(responseString.getBytes().length))));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/json");

    String responseContent = response.content();

    // Verify the link in the response has been rewritten
    assertThat("{ \"link\": \"http://localhost:" + proxyingService.port() + "/other\" }", is(responseContent));

    // Verify the content length header is correct
    assertThat(
        responseContent.length(), is(Integer.parseInt(response.firstHeader(CONTENT_LENGTH))));
  }

  @Test
  public void rewritesThePortInUngzippedBody() {
    initWithDefaultConfig();

    // JSON response with a link containing the localhost hostname
    String responseString = "{ \"link\": \"http://localhost:" + targetService.port() + "/other\" }";

    // Set up the target service to return the JSON
    target.register(
        get(urlPathEqualTo("/json"))
            .willReturn(
                okJson(responseString)
                    .withHeader(CONTENT_LENGTH, String.valueOf(responseString.getBytes().length))));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/json");

    String responseContent = response.content();

    // Verify the link in the response has been rewritten
    assertThat(
        "{ \"link\": \"http://localhost:" + proxyingService.port() + "/other\" }",
        is(responseContent));

    // Verify the content length header is correct
    assertThat(
        responseContent.length(), is(Integer.parseInt(response.firstHeader(CONTENT_LENGTH))));
  }

  @Test
  public void rewritesTheHostnameInGzippedBody() {
    initWithDefaultConfig();

    // JSON response with a link containing the localhost hostname
    String responseString = "{ \"link\": \"http://localhost:" + targetService.port() + "/other\" }";

    // Gzipping the original body
    byte[] gzippedBody = Gzip.gzip(responseString.getBytes());

    // Set up the target service to return the JSON
    target.register(
        get(urlPathEqualTo("/json"))
            .willReturn(
                aResponse()
                    .withBody(gzippedBody)
                    .withHeader("Content-Encoding", "gzip")
                    .withHeader(CONTENT_LENGTH, String.valueOf(gzippedBody.length))
                    .withHeader("Content-Type", "application/json")));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/json");

    // Verify the response is still Gzipped
    assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

    String responseContent = Gzip.unGzipToString(response.binaryContent());

    // Expected body with the hostname rewritten
    String expectedBody = "{ \"link\": \"http://localhost:" + proxyingService.port() + "/other\" }";

    // Verify the content of the response after unzipping and rewriting
    assertThat(responseContent, is(expectedBody));

    // Verify the Content-Length header matches the new Gzipped body length
    byte[] updatedGzippedBody = Gzip.gzip(expectedBody.getBytes());
    assertThat(response.firstHeader(CONTENT_LENGTH), is(String.valueOf(updatedGzippedBody.length)));
  }

  @Test
  public void rewritesThePortInGzippedBody() {
    initWithDefaultConfig();

    // JSON response with a link containing the localhost hostname
    String responseString = "{ \"link\": \"http://localhost:" + targetService.port() + "/other\" }";

    // Gzipping the original body
    byte[] gzippedBody = Gzip.gzip(responseString.getBytes());

    // Set up the target service to return the JSON
    target.register(
        get(urlPathEqualTo("/json"))
            .willReturn(
                aResponse()
                    .withBody(gzippedBody)
                    .withHeader("Content-Encoding", "gzip")
                    .withHeader(CONTENT_LENGTH, String.valueOf(gzippedBody.length))
                    .withHeader("Content-Type", "application/json")));

    // Set up the proxy with the hostname rewrite transformer
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse()
                    .withTransformers("proxied-hostname-rewrite")
                    .proxiedFrom(targetServiceBaseUrl)));

    // Make the request with a specific host header
    WireMockResponse response = testClient.get("/json");

    // Verify the response is still Gzipped
    assertThat(response.firstHeader("Content-Encoding"), is("gzip"));

    String responseContent = Gzip.unGzipToString(response.binaryContent());

    // Expected body with the hostname rewritten
    String expectedBody = "{ \"link\": \"http://localhost:" + proxyingService.port() + "/other\" }";

    // Verify the content of the response after unzipping and rewriting
    assertThat(responseContent, is(expectedBody));

    // Verify the Content-Length header matches the new Gzipped body length
    byte[] updatedGzippedBody = Gzip.gzip(expectedBody.getBytes());
    assertThat(response.firstHeader(CONTENT_LENGTH), is(String.valueOf(updatedGzippedBody.length)));
  }
}
