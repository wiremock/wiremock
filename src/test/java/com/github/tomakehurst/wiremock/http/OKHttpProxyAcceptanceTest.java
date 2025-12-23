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
import static com.github.tomakehurst.wiremock.common.ContentTypes.CONTENT_ENCODING;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getLast;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.ServeEventFactory.newPostMatchServeEvent;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.client.apache5.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.okhttp.OkHttpClientFactory;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multimap;
import com.sun.net.httpserver.HttpServer;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class OKHttpProxyAcceptanceTest {

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
    target = WireMock.create().host("localhost").port(targetService.port()).build();

    targetServiceBaseUrl = "http://localhost:" + targetService.port();

    proxyingServiceOptions.dynamicPort().bindAddress("127.0.0.1").httpClientFactory(
        new OkHttpClientFactory());
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
  public void successfullyGetsResponseFromOtherServiceViaProxy() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response = testClient.get("/proxied/resource?param=value");

    assertThat(response.content(), is("Proxied content"));
    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
  }

  @Test
  public void
  successfullyGetsResponseFromOtherServiceViaProxyWhenInjectingAdditionalRequestHeaders() {
    initWithDefaultConfig();

    proxy.register(
        any(urlEqualTo("/additional/headers"))
            .atPriority(10)
            .willReturn(
                aResponse()
                    .proxiedFrom(targetServiceBaseUrl)
                    .withAdditionalRequestHeader("a", "b")
                    .withAdditionalRequestHeader("c", "d")));

    testClient.get("/additional/headers");

    target.verifyThat(
        getRequestedFor(urlEqualTo("/additional/headers"))
            .withHeader("a", equalTo("b"))
            .withHeader("c", equalTo("d")));
  }

  @Test
  public void
  successfullyGetsResponseFromOtherServiceViaProxyInjectingHeadersOverridingSentHeaders() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .withHeader("a", equalTo("b"))
            .willReturn(aResponse().withStatus(200).withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(
                aResponse()
                    .proxiedFrom(targetServiceBaseUrl)
                    .withAdditionalRequestHeader("a", "b")));

    WireMockResponse response =
        testClient.get("/proxied/resource?param=value", withHeader("a", "doh"));

    assertThat(response.content(), is("Proxied content"));
  }

  @Test
  public void successfullyPostsResponseToOtherServiceViaProxy() {
    initWithDefaultConfig();

    target.register(post(urlEqualTo("/proxied/resource")).willReturn(aResponse().withStatus(204)));

    proxy.register(
        any(urlEqualTo("/proxied/resource"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response =
        testClient.postWithBody("/proxied/resource", "Post content", "text/plain", "utf-8");

    assertThat(response.statusCode(), is(204));
    target.verifyThat(
        postRequestedFor(urlEqualTo("/proxied/resource"))
            .withRequestBody(matching("Post content")));
  }

  @Test
  public void successfullyGetsResponseFromOtherServiceViaProxyWithEscapeCharsInUrl() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26"))
            .willReturn(aResponse().withStatus(200)));

    proxy.register(
        any(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response = testClient.get("/%26%26The%20Lord%20of%20the%20Rings%26%26");

    assertThat(response.statusCode(), is(200));
  }

  @Test
  public void successfullyGetsResponseBinaryResponses() throws IOException {
    initWithDefaultConfig();

    final byte[] bytes =
        new byte[] {
            0x10, 0x49, 0x6e, (byte) 0xb7, 0x46, (byte) 0xe6, 0x52, (byte) 0x95, (byte) 0x95, 0x42
        };
    com.sun.net.httpserver.HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
    server.createContext(
        "/binary",
        exchange -> {
          InputStream request = exchange.getRequestBody();

          byte[] buffy = new byte[10];
          request.read(buffy);

          if (Arrays.equals(buffy, bytes)) {
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream out = exchange.getResponseBody();
            out.write(bytes);
            out.close();
          } else {
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
          }
        });
    server.start();

    proxy.register(
        post(urlEqualTo("/binary"))
            .willReturn(
                aResponse()
                    .proxiedFrom("http://localhost:" + server.getAddress().getPort())
                    .withBody(bytes)));

    WireMockResponse post =
        testClient.post("/binary", new ByteArrayEntity(bytes, ContentType.DEFAULT_BINARY));
    assertThat(post.statusCode(), is(200));
    assertThat(post.binaryContent(), Matchers.equalTo(bytes));
  }

  @Test
  public void sendsContentLengthHeaderInRequestWhenPostingIfPresentInOriginalRequest() {
    initWithDefaultConfig();

    target.register(post(urlEqualTo("/with/length")).willReturn(aResponse().withStatus(201)));
    proxy.register(
        post(urlEqualTo("/with/length")).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.postWithBody("/with/length", "TEST", "application/x-www-form-urlencoded", "utf-8");

    target.verifyThat(
        postRequestedFor(urlEqualTo("/with/length")).withHeader("Content-Length", equalTo("4")));
  }

  @Test
  public void returnsContentLengthHeaderFromTargetResponseIfPresentAndChunkedEncodingEnabled()
      throws Exception {
    init(wireMockConfig().useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.ALWAYS));

    String path = "/response/length";
    target.register(head(urlPathEqualTo(path)).willReturn(ok().withHeader("Content-Length", "4")));
    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    CloseableHttpClient httpClient = ApacheHttpClientFactory.createClient();
    HttpHead request = new HttpHead(proxyingService.baseUrl() + path);
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      assertThat(response.getCode(), is(200));
      assertThat(response.getFirstHeader("Content-Length").getValue(), is("4"));
    }
  }

  @Test
  public void returnsContentLengthHeaderFromTargetResponseIfPresentAndChunkedEncodingDisabled()
      throws Exception {
    init(wireMockConfig().useChunkedTransferEncoding(Options.ChunkedEncodingPolicy.NEVER));

    String path = "/response/length";
    target.register(head(urlPathEqualTo(path)).willReturn(ok().withHeader("Content-Length", "4")));
    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    CloseableHttpClient httpClient = ApacheHttpClientFactory.createClient();
    HttpHead request = new HttpHead(proxyingService.baseUrl() + path);
    try (CloseableHttpResponse response = httpClient.execute(request)) {
      assertThat(response.getCode(), is(200));
      assertThat(response.getFirstHeader("Content-Length").getValue(), is("4"));
    }
  }

  @Test
  public void sendsTransferEncodingChunkedWhenPostingIfPresentInOriginalRequest() {
    initWithDefaultConfig();

    target.register(post(urlEqualTo("/chunked")).willReturn(aResponse().withStatus(201)));
    proxy.register(
        post(urlEqualTo("/chunked")).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.postWithChunkedBody("/chunked", "TEST".getBytes());

    List<LoggedRequest> loggedRequests = target.find(postRequestedFor(urlEqualTo("/chunked")));
    assertThat(loggedRequests.size(), is(1));
    assertThat(loggedRequests.get(0).header("Transfer-Encoding").firstValue(), is("chunked"));
  }

  @Test
  public void preservesHostHeaderWhenSpecified() {
    init(wireMockConfig().preserveHostHeader(true));

    target.register(
        get(urlEqualTo("/preserve-host-header")).willReturn(aResponse().withStatus(200)));
    proxy.register(
        get(urlEqualTo("/preserve-host-header"))
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.get("/preserve-host-header", withHeader("Host", "my.host"));

    proxy.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-host-header"))
            .withHeader("Host", equalTo("my.host")));
    target.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-host-header"))
            .withHeader("Host", equalTo("my.host")));
  }

  @Test
  public void usesProxyUrlBasedHostHeaderWhenPreserveHostHeaderNotSpecified() {
    init(wireMockConfig().preserveHostHeader(false));

    target.register(get(urlEqualTo("/host-header")).willReturn(aResponse().withStatus(200)));
    proxy.register(
        get(urlEqualTo("/host-header")).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.get("/host-header", withHeader("Host", "my.host"));

    proxy.verifyThat(
        getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("my.host")));
    target.verifyThat(
        getRequestedFor(urlEqualTo("/host-header"))
            .withHeader("Host", equalTo("localhost:" + targetService.port())));
  }

  @Test
  public void preservesUserAgentProxyHeaderWhenSpecified() {
    init(wireMockConfig().preserveUserAgentProxyHeader(true));

    target.register(
        get(urlEqualTo("/preserve-user-agent-header")).willReturn(aResponse().withStatus(200)));
    proxy.register(
        get(urlEqualTo("/preserve-user-agent-header"))
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.get("/preserve-user-agent-header", withHeader("User-Agent", "my-user-agent"));

    proxy.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-user-agent-header"))
            .withHeader("User-Agent", equalTo("my-user-agent")));
    target.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-user-agent-header"))
            .withHeader("User-Agent", equalTo("my-user-agent")));
  }

  @Test
  public void usesWireMockUserAgentProxyHeaderWhenPreserveUserAgentProxyHeaderNotSpecified() {
    init(wireMockConfig());

    target.register(
        get(urlEqualTo("/preserve-user-agent-header")).willReturn(aResponse().withStatus(200)));
    proxy.register(
        get(urlEqualTo("/preserve-user-agent-header"))
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.get("/preserve-user-agent-header", withHeader("User-Agent", "my-user-agent"));

    proxy.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-user-agent-header"))
            .withHeader("User-Agent", equalTo("my-user-agent")));
    target.verifyThat(
        getRequestedFor(urlEqualTo("/preserve-user-agent-header"))
            .withHeader("User-Agent", matching("WireMock .*")));
  }

  @Test
  public void proxiesPatchRequestsWithBody() {
    initWithDefaultConfig();

    target.register(patch(urlEqualTo("/patch")).willReturn(aResponse().withStatus(200)));
    proxy.register(
        patch(urlEqualTo("/patch")).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.patchWithBody("/patch", "Patch body", "text/plain", "utf-8");

    target.verifyThat(
        patchRequestedFor(urlEqualTo("/patch")).withRequestBody(equalTo("Patch body")));
  }

  @Test
  public void addsSpecifiedHeadersToResponse() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/extra/headers"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/extra/headers"))
            .willReturn(
                aResponse()
                    .withHeader("X-Additional-Header", "Yep")
                    .proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response = testClient.get("/extra/headers");

    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
    assertThat(response.firstHeader("X-Additional-Header"), is("Yep"));
  }

  @Test
  public void doesNotDuplicateCookieHeaders() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/duplicate/cookies"))
            .willReturn(aResponse().withStatus(200).withHeader("Set-Cookie", "session=1234")));
    proxy.register(
        get(urlEqualTo("/duplicate/cookies"))
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    testClient.get("/duplicate/cookies");
    testClient.get("/duplicate/cookies", withHeader("Cookie", "session=1234"));

    LoggedRequest lastRequest =
        getLast(target.find(getRequestedFor(urlEqualTo("/duplicate/cookies"))));
    assertThat(lastRequest.getHeaders().getHeader("Cookie").values().size(), is(1));
  }

  // TODO: This is passing even when it probably shouldn't - investigate
  @Test
  public void doesNotDuplicateConnectionHeader() {
    initWithDefaultConfig();
    register200StubOnProxyAndTarget("/duplicate/connection-header");

    testClient.get("/duplicate/connection-header");
    LoggedRequest lastRequest =
        getLast(target.find(getRequestedFor(urlEqualTo("/duplicate/connection-header"))));
    assertThat(lastRequest.getHeaders().getHeader("Connection").values(), hasItem("keep-alive"));
  }

  @Test
  public void acceptsSelfSignedSslCertFromProxyTarget() {
    initWithDefaultConfig();
    register200StubOnProxyAndTarget("/ssl-cert");

    assertThat(testClient.get("/ssl-cert").statusCode(), is(200));
  }

  @Test
  public void canProxyViaAForwardProxy() {
    WireMockServer forwardProxy =
        new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
    forwardProxy.start();
    init(wireMockConfig().proxyVia(new ProxySettings("localhost", forwardProxy.port())));

    register200StubOnProxyAndTarget("/proxy-via");

    assertThat(testClient.get("/proxy-via").statusCode(), is(200));
  }

  @Test
  public void doesNotAddAcceptEncodingHeaderToProxyRequest() {
    initWithDefaultConfig();
    register200StubOnProxyAndTarget("/no-accept-encoding-header");

    testClient.get("/no-accept-encoding-header");
    LoggedRequest lastRequest =
        getLast(target.find(getRequestedFor(urlEqualTo("/no-accept-encoding-header"))));
    assertFalse(
        lastRequest.getHeaders().getHeader("Accept-Encoding").isPresent(),
        "Accept-Encoding header should not be present");
  }

  @Test
  public void passesMultipleValuesOfTheSameHeaderToTheTarget() {
    initWithDefaultConfig();
    register200StubOnProxyAndTarget("/multi-value-header");

    testClient.get(
        "/multi-value-header", withHeader("Accept", "accept1"), withHeader("Accept", "accept2"));

    LoggedRequest lastRequest =
        getLast(target.find(getRequestedFor(urlEqualTo("/multi-value-header"))));

    assertThat(lastRequest.header("Accept").values(), hasItems("accept1", "accept2"));
  }

  @Test
  public void maintainsGZippedRequest() {
    initWithDefaultConfig();

    target.register(post("/gzipped").willReturn(aResponse().withStatus(201)));
    proxy.register(post("/gzipped").willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    HttpEntity gzippedBody =
        new GzipCompressingEntity(new StringEntity("gzipped body", TEXT_PLAIN));
    testClient.post("/gzipped", gzippedBody);

    target.verifyThat(
        postRequestedFor(urlEqualTo("/gzipped"))
            .withHeader(CONTENT_ENCODING, containing("gzip"))
            .withRequestBody(equalTo("gzipped body")));
  }

  @Test
  public void contextPathsWithoutTrailingSlashesArePreserved() {
    initWithDefaultConfig();

    target.register(get("/example").willReturn(ok()));
    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response =
        testClient.getViaProxy("http://localhost:" + proxyingService.port() + "/example");
    assertThat(response.statusCode(), is(200));

    target.verifyThat(1, getRequestedFor(urlEqualTo("/example")));
    target.verifyThat(0, getRequestedFor(urlEqualTo("/example/")));
  }

  @Test
  public void contextPathsWithTrailingSlashesArePreserved() {
    initWithDefaultConfig();

    target.register(get("/example/").willReturn(ok()));
    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response =
        testClient.getViaProxy("http://localhost:" + proxyingService.port() + "/example/");
    assertThat(response.statusCode(), is(200));

    target.verifyThat(1, getRequestedFor(urlEqualTo("/example/")));
    target.verifyThat(0, getRequestedFor(urlEqualTo("/example")));
  }

  /**
   * NOTE: {@link org.apache.hc.core5.http.client.HttpClient} always has a / when the context path
   * is empty. This is also the behaviour of curl (see e.g. <a
   * href="https://curl.haxx.se/mail/archive-2016-08/0027.html">here</a>)
   */
  @Test
  public void clientLibrariesTendToAddTheTrailingSlashWhenTheContextPathIsEmpty() {
    initWithDefaultConfig();

    target.register(get("/").willReturn(ok()));
    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse responseToRequestWithoutSlash =
        testClient.getViaProxy("http://localhost:" + proxyingService.port());
    assertThat(responseToRequestWithoutSlash.statusCode(), is(200));

    WireMockResponse responseToRequestWithSlash =
        testClient.getViaProxy("http://localhost:" + proxyingService.port() + "/");
    assertThat(responseToRequestWithSlash.statusCode(), is(200));

    target.verifyThat(2, getRequestedFor(urlEqualTo("/")));
    target.verifyThat(0, getRequestedFor(urlEqualTo("")));
  }

  @Test
  public void fixedDelaysAreAddedToProxiedResponses() {
    initWithDefaultConfig();

    target.register(get("/delayed").willReturn(ok()));
    proxy.register(
        any(anyUrl())
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl).withFixedDelay(300)));

    Stopwatch stopwatch = Stopwatch.createStarted();
    WireMockResponse response =
        testClient.getViaProxy("http://localhost:" + proxyingService.port() + "/delayed");
    stopwatch.stop();

    assertThat(response.statusCode(), is(200));
    assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(300L));
  }

  @Test
  public void chunkedDribbleDelayIsAddedToProxiedResponse() {
    initWithDefaultConfig();

    target.register(get("/chunk-delayed").willReturn(ok()));
    proxy.register(
        any(anyUrl())
            .willReturn(
                aResponse().proxiedFrom(targetServiceBaseUrl).withChunkedDribbleDelay(10, 300)));

    Stopwatch stopwatch = Stopwatch.createStarted();
    WireMockResponse response =
        testClient.getViaProxy("http://localhost:" + proxyingService.port() + "/chunk-delayed");
    stopwatch.stop();

    assertThat(response.statusCode(), is(200));
    assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(300L));
  }

  @Test
  public void removesPrefixFromProxyRequestWhenMatching() {
    initWithDefaultConfig();

    proxy.register(
        get("/other/service/doc/123")
            .willReturn(
                aResponse()
                    .proxiedFrom(targetServiceBaseUrl + "/approot")
                    .withProxyUrlPrefixToRemove("/other/service")));

    target.register(get("/approot/doc/123").willReturn(ok()));

    WireMockResponse response = testClient.get("/other/service/doc/123");

    assertThat(response.statusCode(), is(200));
  }

  @Test
  public void removesPrefixFromProxyRequestWhenResponseTransformersAreUsed() {
    init(wireMockConfig().templatingEnabled(true).globalTemplating(true));

    proxy.register(
        get("/other/service/doc/123")
            .willReturn(
                aResponse()
                    .proxiedFrom(targetServiceBaseUrl + "/approot")
                    .withProxyUrlPrefixToRemove("/other/service")));

    target.register(get("/approot/doc/123").willReturn(ok()));

    WireMockResponse response = testClient.get("/other/service/doc/123");

    assertThat(response.statusCode(), is(200));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE", "QUERY", "BLAH"})
  void proxiesRequestBodyForAnyMethod(String method) {
    initWithDefaultConfig();

    target.register(any(anyUrl()).willReturn(ok()));

    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

//    testClient.request(method, "/somewhere", "Proxied content",
//        new TestHttpHeader("Content-Type", "text/plain"));
    testClient.request(method, "/somewhere", "Proxied content");

    List<LoggedRequest> requests = target.find(anyRequestedFor(urlEqualTo("/somewhere")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod().getName(), is(method));
    assertThat(requests.get(0).getBodyAsString(), is("Proxied content"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "TRACE", "QUERY", "BLAH"})
  void proxiesRequestWithNoBodyForAnyMethod(String method) {
    initWithDefaultConfig();

    target.register(any(anyUrl()).willReturn(ok()));

    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

//    testClient.request(method, "/somewhere", new TestHttpHeader("Content-Type", "text/plain"));
    testClient.request(method, "/somewhere");

    List<LoggedRequest> requests = target.find(anyRequestedFor(urlEqualTo("/somewhere")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod().getName(), is(method));
    assertThat(requests.get(0).getBodyAsString(), is(""));
  }

  @ParameterizedTest
  @ValueSource(strings = {"POST"})
  void proxiesRequestWithNoBodyForSomeMethod(String method) {
    initWithDefaultConfig();

    target.register(any(anyUrl()).willReturn(ok()));

    proxy.register(any(anyUrl()).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

//    testClient.request(method, "/somewhere", new TestHttpHeader("Content-Type", "text/plain"));
    testClient.request(method, "/somewhere");

    List<LoggedRequest> requests = target.find(anyRequestedFor(urlEqualTo("/somewhere")));
    assertThat(requests.size(), is(1));
    assertThat(requests.get(0).getMethod().getName(), is(method));
    assertThat(requests.get(0).getBodyAsString(), is(""));
  }
  
  @Test
  void preventsProxyingToExcludedIpAddress() {
    init(
        wireMockConfig()
            .limitProxyTargets(
                NetworkAddressRules.builder()
                    .deny("10.1.2.3")
                    .deny("192.168.10.1-192.168.11.254")
                    .build()));

    proxy.register(proxyAllTo("https://10.1.2.3"));
    WireMockResponse response = testClient.get("/");
    assertThat(response.statusCode(), is(500));
    assertThat(
        response.content(), is("The target proxy address is denied in WireMock's configuration."));

    proxy.register(proxyAllTo("https://192.168.10.255"));
    assertThat(testClient.get("/").statusCode(), is(500));
  }

  @Test
  void preventsProxyingToExcludedHostnames() {
    init(
        wireMockConfig()
            .limitProxyTargets(NetworkAddressRules.builder().deny("*.wiremock.org").build()));

    proxy.register(proxyAllTo("http://noway.wiremock.org"));
    assertThat(
        testClient.get("/").content(),
        is("The target proxy address is denied in WireMock's configuration."));
  }

  @Test
  void preventsProxyingToNonIncludedHostnames() {
    init(
        wireMockConfig()
            .limitProxyTargets(NetworkAddressRules.builder().allow("wiremock.org").build()));

    proxy.register(proxyAllTo("http://wiremock.io"));
    assertThat(
        testClient.get("/").content(),
        is("The target proxy address is denied in WireMock's configuration."));
  }

  @Test
  void preventsProxyingToIpResolvedFromHostname() {
    init(
        wireMockConfig()
            .limitProxyTargets(NetworkAddressRules.builder().deny("127.0.0.1").build()));

    proxy.register(proxyAllTo("http://localhost"));
    assertThat(
        testClient.get("/").content(),
        is("The target proxy address is denied in WireMock's configuration."));
  }

  @Test
  void proxyRequestWillNotTimeoutIfProxyResponseIsFastEnough() {
    init(wireMockConfig().proxyTimeout(1000));

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withFixedDelay(500)
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response = testClient.get("/proxied/resource?param=value");

    assertThat(response.content(), is("Proxied content"));
    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
  }

  @Test
  void proxyRequestWillTimeoutIfProxyResponseIsTooSlow() {
    init(wireMockConfig().proxyTimeout(1000));

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withFixedDelay(1500)
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));

    WireMockResponse response = testClient.get("/proxied/resource?param=value");

    assertThat(
        response.content(),
        startsWith("Network failure trying to make a proxied request from WireMock"));
    assertThat(response.statusCode(), is(500));
  }

  @Test
  void multiValueResponseHeadersWithDifferentCasesAreHandledCorrectly() {
    initWithDefaultConfig();

    target.register(
        get(urlPathEqualTo("/multi-value-headers"))
            .willReturn(
                ok().withHeader("Set-Cookie", "session=1234")
                    .withHeader("set-cookie", "ads_id=5678")
                    .withHeader("SET-COOKIE", "trk=t-9987")));

    proxy.register(proxyAllTo(targetServiceBaseUrl));

    WireMockResponse response = testClient.get("/multi-value-headers");
    assertThat(response.statusCode(), is(200));

    Multimap<String, String> headers = response.headers();
    assertThat(headers.get("Set-Cookie").size(), is(3));
    assertThat(headers.get("Set-Cookie"), hasItems("session=1234", "ads_id=5678", "trk=t-9987"));
  }

  @Test
  public void proxiedFromIllegalSource() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(aResponse().proxiedFrom("/not/an/absolute/url")));

    WireMockResponse response = testClient.get("/proxied/resource?param=value");

    assertThat(response.statusCode(), is(500));
    assertThat(
        response.content(),
        is(
            "The target proxy address `/not/an/absolute/url/proxied/resource?param=value` is not an absolute URL."));
    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
  }

  @Test
  public void templatedProxiedFrom() {

    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(
                aResponse()
                    .withTransformers(ResponseTemplateTransformer.NAME)
                    .proxiedFrom("{{request.headers.X-WM-Uri}}")));

    WireMockResponse response =
        testClient.get(
            "/proxied/resource?param=value",
            TestHttpHeader.withHeader("X-WM-Uri", targetServiceBaseUrl));

    assertThat(response.content(), is("Proxied content"));
    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
  }

  @Test
  public void templatedProxiedFromWithIllegalTemplatedSource() {
    initWithDefaultConfig();

    target.register(
        get(urlEqualTo("/proxied/resource?param=value"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Proxied content")));

    proxy.register(
        any(urlEqualTo("/proxied/resource?param=value"))
            .atPriority(10)
            .willReturn(
                aResponse()
                    .withTransformers(ResponseTemplateTransformer.NAME)
                    .proxiedFrom("{{request.headers.X-WM-Uri}}")));

    WireMockResponse response =
        testClient.get(
            "/proxied/resource?param=value",
            TestHttpHeader.withHeader("X-WM-Uri", "/not/an/absolute/url"));

    assertThat(response.statusCode(), is(500));
    assertThat(
        response.content(),
        is(
            "The target proxy address `/not/an/absolute/url/proxied/resource?param=value` is not an absolute URL."));
    assertThat(response.firstHeader("Content-Type"), is("text/plain"));
  }

  private void register200StubOnProxyAndTarget(String url) {
    target.register(get(urlEqualTo(url)).willReturn(aResponse().withStatus(200)));
    proxy.register(get(urlEqualTo(url)).willReturn(aResponse().proxiedFrom(targetServiceBaseUrl)));
  }
//  private static final int PROXY_TIMEOUT = 30_000;
//  private static final byte[] REQUEST_BODY = "test request body".getBytes(UTF_8);
//
//  @RegisterExtension
//  public WireMockExtension origin =
//      WireMockExtension.newInstance().options(options().dynamicPort()).build();
//
//  private HttpClient okHttpClient;
//  private ProxyResponseRenderer proxyResponseRenderer;
//
//  @BeforeEach
//  void setup() {
//    // Setup origin server to accept any request
//    origin.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(200).withBody("Success")));
//
//    // Create OkHttp client
//    okHttpClient =
//        new OkHttpBackedHttpClient(OkHttpClientFactory.createClient(PROXY_TIMEOUT), false);
//
//    // Create proxy renderer with OkHttp client
//    proxyResponseRenderer =
//        new ProxyResponseRenderer(
//            /* preserveHostHeader= */ false,
//            /* hostHeaderValue= */ null,
//            new InMemorySettingsStore(),
//            /* stubCorsEnabled= */ false,
//            /* supportedProxyEncodings= */ null,
//            /* reverseProxyClient= */ okHttpClient,
//            /* forwardProxyClient= */ okHttpClient);
//  }
//
//  @Test
//  void testGetRequestWithBody() {
//    ServeEvent serveEvent = createServeEvent("/test-get", GET, REQUEST_BODY);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(getRequestedFor(urlEqualTo("/test-get")));
//    assertThat(requests.size(), is(1));
//    assertThat(requests.get(0).getMethod(), is(GET));
//  }
//
//  @Test
//  void testHeadRequestWithBody() {
//    ServeEvent serveEvent = createServeEvent("/test-head", HEAD, REQUEST_BODY);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-head")));
//    assertThat(requests.size(), is(1));
//    assertThat(requests.get(0).getMethod(), is(HEAD));
//  }
//
//  @Test
//  void testQueryRequestWithBody() {
//    ServeEvent serveEvent = createServeEvent("/test-query", QUERY, REQUEST_BODY);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-query")));
//    assertThat(requests.size(), is(1));
//    assertThat(requests.get(0).getMethod(), is(QUERY));
//  }
//
//  @Test
//  void testOptionsRequestWithBody() {
//    ServeEvent serveEvent = createServeEvent("/test-options", OPTIONS, REQUEST_BODY);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(anyRequestedFor(urlEqualTo("/test-options")));
//    assertThat(requests.size(), is(1));
//    assertThat(requests.get(0).getMethod(), is(OPTIONS));
//  }
//
//  @Test
//  void testPostRequestWithBody() {
//    ServeEvent serveEvent = createServeEvent("/test-post", POST, REQUEST_BODY);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(postRequestedFor(urlEqualTo("/test-post")));
//    assertThat(requests.size(), is(1));
//    assertThat(requests.get(0).getMethod(), is(POST));
//  }
//
//  @Test
//  void testGetRequestWithEmptyBody() {
//    ServeEvent serveEvent = createServeEvent("/test-get-empty", GET, new byte[0]);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests = origin.findAll(getRequestedFor(urlEqualTo("/test-get-empty")));
//    assertThat(requests.size(), is(1));
//  }
//
//  @Test
//  void testHeadRequestWithEmptyBody() {
//    ServeEvent serveEvent = createServeEvent("/test-head-empty", HEAD, new byte[0]);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests =
//        origin.findAll(anyRequestedFor(urlEqualTo("/test-head-empty")));
//    assertThat(requests.size(), is(1));
//  }
//
//  @Test
//  void testQueryRequestWithEmptyBody() {
//    // Note: QUERY method requires a body per RFC 9535, so this test will fail
//    // This test explicitly documents that QUERY without a body should fail
//    ServeEvent serveEvent = createServeEvent("/test-query-empty", QUERY, new byte[0]);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests =
//        origin.findAll(anyRequestedFor(urlEqualTo("/test-query-empty")));
//    assertThat(requests.size(), is(1));
//  }
//
//  @Test
//  void testOptionsRequestWithEmptyBody() {
//    ServeEvent serveEvent = createServeEvent("/test-options-empty", OPTIONS, new byte[0]);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests =
//        origin.findAll(anyRequestedFor(urlEqualTo("/test-options-empty")));
//    assertThat(requests.size(), is(1));
//  }
//
//  @Test
//  void testPostRequestWithEmptyBody() {
//    ServeEvent serveEvent = createServeEvent("/test-post-empty", POST, new byte[0]);
//
//    Response response = proxyResponseRenderer.render(serveEvent);
//    assertThat(response.getStatus(), is(200));
//
//    // Verify the request was made
//    List<LoggedRequest> requests =
//        origin.findAll(anyRequestedFor(urlEqualTo("/test-post-empty")));
//    assertThat(requests.size(), is(1));
//  }
//
//  private ServeEvent createServeEvent(String path, RequestMethod method, byte[] body) {
//    LoggedRequest loggedRequest =
//        LoggedRequest.createFrom(
//            mockRequest()
//                .url(path)
//                .absoluteUrl(origin.url(path))
//                .method(method)
//                .headers(new HttpHeaders())
//                .body(body)
//                .protocol("HTTP/1.1"));
//
//    return newPostMatchServeEvent(
//        loggedRequest, aResponse().proxiedFrom(origin.baseUrl()).build());
//  }
}