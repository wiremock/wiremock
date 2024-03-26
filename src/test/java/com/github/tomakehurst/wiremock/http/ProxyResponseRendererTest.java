/*
 * Copyright (C) 2020-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.crypto.X509CertificateVersion.V3;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.stubbing.ServeEventFactory.newPostMatchServeEvent;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.spy;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.crypto.CertificateSpecification;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import com.github.tomakehurst.wiremock.http.client.ApacheBackedHttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.store.InMemorySettingsStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

@DisabledForJreRange(
    min = JRE.JAVA_17,
    disabledReason = "does not support generating certificates at runtime")
public class ProxyResponseRendererTest {

  private static final int PROXY_TIMEOUT = 200_000;

  CloseableHttpClient reverseProxyApacheClient;
  CloseableHttpClient forwardProxyApacheClient;

  @RegisterExtension
  public WireMockExtension origin =
      WireMockExtension.newInstance()
          .options(
              options()
                  .httpDisabled(true)
                  .dynamicHttpsPort()
                  .keystorePath(generateKeystore().getAbsolutePath()))
          .build();

  private final ProxyResponseRenderer proxyResponseRenderer = buildProxyResponseRenderer(false);

  @Test
  public void acceptsAnyCertificateForStandardProxying() {

    origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

    ServeEvent serveEvent = reverseProxyServeEvent("/proxied");

    Response response = proxyResponseRenderer.render(serveEvent);

    assertEquals(response.getBodyAsString(), "Result");
  }

  @Test
  public void rejectsSelfSignedCertificateForForwardProxyingByDefault() {

    origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

    final ServeEvent serveEvent = forwardProxyServeEvent("/proxied");

    Response response = proxyResponseRenderer.render(serveEvent);

    assertEquals(HTTP_INTERNAL_ERROR, response.getStatus());
    assertThat(
        response.getBodyAsString(),
        startsWith(
            "SSL failure trying to make a proxied request from WireMock to "
                + origin.url("/proxied")));
    assertThat(
        response.getBodyAsString(),
        containsString("unable to find valid certification path to requested target"));
  }

  @Test
  public void acceptsSelfSignedCertificateForForwardProxyingIfTrustAllProxyTargets() {
    ProxyResponseRenderer trustAllProxyResponseRenderer = buildProxyResponseRenderer(true);

    origin.stubFor(get("/proxied").willReturn(aResponse().withBody("Result")));

    ServeEvent serveEvent = forwardProxyServeEvent("/proxied");
    Response response = trustAllProxyResponseRenderer.render(serveEvent);

    assertEquals(response.getBodyAsString(), "Result");
  }

  @Test
  void passesThroughCorsResponseHeadersWhenStubCorsDisabled() {
    ProxyResponseRenderer responseRenderer = buildProxyResponseRenderer(true, false, null);

    origin.stubFor(
        get("/proxied")
            .willReturn(ok("Result").withHeader("Access-Control-Allow-Headers", "X-Blah")));

    ServeEvent serveEvent = forwardProxyServeEvent("/proxied");
    Response response = responseRenderer.render(serveEvent);

    HttpHeader corsHeader = response.getHeaders().getHeader("Access-Control-Allow-Headers");
    assertThat(
        "CORS response header sent from the origin is not present in the response",
        corsHeader.isPresent(),
        is(true));
    assertThat(corsHeader.firstValue(), is("X-Blah"));
  }

  @Test
  void doesNotPassThroughCorsResponseHeadersWhenStubCorsEnabled() {
    ProxyResponseRenderer responseRenderer = buildProxyResponseRenderer(true, true, null);

    origin.stubFor(
        get("/proxied")
            .willReturn(ok("Result").withHeader("Access-Control-Allow-Headers", "X-Blah")));

    ServeEvent serveEvent = forwardProxyServeEvent("/proxied");
    Response response = responseRenderer.render(serveEvent);

    HttpHeader corsHeader = response.getHeaders().getHeader("Access-Control-Allow-Headers");
    assertThat(
        "CORS response header sent from the origin is present in the response",
        corsHeader.isPresent(),
        is(false));
  }

  @Test
  void doesNotAddEntityIfEmptyBodyReverseProxy() throws IOException {
    ServeEvent serveEvent = reverseProxyServeEvent("/proxied");

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() == null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void doesNotAddEntityIfEmptyBodyForwardProxy() throws IOException {
    ServeEvent serveEvent = forwardProxyServeEvent("/proxied");

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(forwardProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() == null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void addsEntityIfNotEmptyBodyReverseProxy() throws IOException {
    ServeEvent serveEvent =
        serveEvent("/proxied", false, "Text body".getBytes(StandardCharsets.UTF_8));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() != null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void addsEntityIfNotEmptyBodyForwardProxy() throws IOException {
    ServeEvent serveEvent =
        serveEvent("/proxied", true, "Text body".getBytes(StandardCharsets.UTF_8));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(forwardProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() != null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void addsEmptyEntityIfEmptyBodyForwardProxyPOST() throws IOException {
    ProxyResponseRenderer trustAllProxyResponseRenderer = buildProxyResponseRenderer(true);
    origin.stubFor(post("/proxied/empty-post").willReturn(aResponse().withBody("Result")));

    ServeEvent serveEvent =
        serveEvent(
            "/proxied/empty-post",
            true,
            new byte[0],
            RequestMethod.POST,
            new HttpHeaders(new HttpHeader("Content-Length", "0")));

    trustAllProxyResponseRenderer.render(serveEvent);
    Mockito.verify(forwardProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() != null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
    List<LoggedRequest> requests =
        origin.findAll(postRequestedFor(urlPathMatching("/proxied/empty-post")));
    Assertions.assertThat(requests)
        .hasSizeGreaterThan(0)
        .allMatch(r -> "0".equals(r.getHeader("Content-Length")))
        .noneMatch(r -> r.containsHeader("Content-Type"));
  }

  @Test
  void addsEmptyEntityIfEmptyBodyForwardProxyGET() throws IOException {
    ProxyResponseRenderer trustAllProxyResponseRenderer = buildProxyResponseRenderer(true);
    origin.stubFor(get("/proxied/empty-get").willReturn(aResponse().withBody("Result")));

    ServeEvent serveEvent =
        serveEvent(
            "/proxied/empty-get",
            true,
            new byte[0],
            GET,
            new HttpHeaders(new HttpHeader("Content-Length", "0")));

    trustAllProxyResponseRenderer.render(serveEvent);
    Mockito.verify(forwardProxyApacheClient)
        .execute(
            argThat(request -> request.getEntity() != null),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
    List<LoggedRequest> requests =
        origin.findAll(getRequestedFor(urlPathMatching("/proxied/empty-get")));
    Assertions.assertThat(requests)
        .hasSizeGreaterThan(0)
        .allMatch(r -> "0".equals(r.getHeader("Content-Length")))
        .noneMatch(r -> r.containsHeader("Content-Type"));
  }

  @Test
  void usesCorrectProxyRequestTimeout() {
    RequestConfig forwardProxyClientRequestConfig =
        reflectiveSpyField(RequestConfig.class, "defaultConfig", forwardProxyApacheClient);
    RequestConfig reverseProxyClientRequestConfig =
        reflectiveSpyField(RequestConfig.class, "defaultConfig", reverseProxyApacheClient);

    assertThat(
        forwardProxyClientRequestConfig.getResponseTimeout().toMilliseconds(),
        is((long) PROXY_TIMEOUT));
    assertThat(
        reverseProxyClientRequestConfig.getResponseTimeout().toMilliseconds(),
        is((long) PROXY_TIMEOUT));
  }

  @Test
  void additionalProxyRequestHeaders() throws IOException {
    ServeEvent serveEvent =
        serveEvent(
            "/proxied",
            false,
            null,
            RequestMethod.GET,
            new HttpHeaders(),
            aResponse()
                .proxiedFrom(origin.baseUrl())
                .withAdditionalRequestHeader("header", "value")
                .build());

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            argThat(request -> request.getFirstHeader("header").getValue().equals("value")),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void removeProxyRequestHeaders() throws IOException {
    ServeEvent serveEvent =
        serveEvent(
            "/proxied",
            false,
            null,
            RequestMethod.GET,
            new HttpHeaders(new HttpHeader("header", "value")),
            aResponse().proxiedFrom(origin.baseUrl()).withRemoveRequestHeader("Header").build());

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            argThat(request -> request.getHeaders().length == 0),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void maintainsAcceptEncodingIfNoSupportedEncodingsSpecified() throws IOException {

    Set<String> supportedProxyEncodings = null;
    ProxyResponseRenderer proxyResponseRenderer =
        buildProxyResponseRenderer(false, false, supportedProxyEncodings);
    ServeEvent serveEvent =
        serveEvent(
            "/proxied",
            false,
            new byte[0],
            GET,
            new HttpHeaders(HttpHeader.httpHeader("Accept-Encoding", "gzip,br")));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            assertArg(
                request ->
                    assertThat(
                        Arrays.stream(request.getHeaders("Accept-Encoding"))
                            .map(NameValuePair::getValue)
                            .collect(Collectors.toList()),
                        is(List.of("gzip,br")))),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void limitsAcceptEncodingToSupportedEncodings() throws IOException {

    Set<String> supportedProxyEncodings = Set.of("gzip", "br");
    ProxyResponseRenderer proxyResponseRenderer =
        buildProxyResponseRenderer(false, false, supportedProxyEncodings);
    ServeEvent serveEvent =
        serveEvent(
            "/proxied",
            false,
            new byte[0],
            GET,
            new HttpHeaders(HttpHeader.httpHeader("Accept-Encoding", "gzip,deflate,br")));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            assertArg(
                request ->
                    assertThat(
                        Arrays.stream(request.getHeaders("Accept-Encoding"))
                            .map(NameValuePair::getValue)
                            .collect(Collectors.toList()),
                        is(List.of("gzip,br")))),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  @Test
  void removesAcceptEncodingIfNoneSupported() throws IOException {

    Set<String> supportedProxyEncodings = Set.of("gzip");
    ProxyResponseRenderer proxyResponseRenderer =
        buildProxyResponseRenderer(false, false, supportedProxyEncodings);
    ServeEvent serveEvent =
        serveEvent(
            "/proxied",
            false,
            new byte[0],
            GET,
            new HttpHeaders(HttpHeader.httpHeader("Accept-Encoding", "deflate,br")));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            assertArg(request -> assertFalse(request.containsHeader("Accept-Encoding"))),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  private static <T> T reflectiveInnerSpyField(
      Class<T> fieldType, String outerFieldName, String innerFieldName, Object object) {
    try {
      Field outerField = object.getClass().getDeclaredField(outerFieldName);
      outerField.setAccessible(true);
      Object outerFieldObject = outerField.get(object);
      Field innerField = outerFieldObject.getClass().getDeclaredField(innerFieldName);
      innerField.setAccessible(true);
      T spy = spy(fieldType.cast(innerField.get(outerFieldObject)));
      innerField.set(outerFieldObject, spy);
      return spy;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void proxyUrlPrefixToRemove() throws IOException {
    ServeEvent serveEvent =
        serveEvent(
            "/prefix/proxied",
            false,
            null,
            RequestMethod.GET,
            new HttpHeaders(new HttpHeader("header", "value")),
            aResponse()
                .proxiedFrom(origin.baseUrl())
                .withProxyUrlPrefixToRemove("/prefix")
                .build());

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(reverseProxyApacheClient)
        .execute(
            argThat(request -> request.getRequestUri().equals("/proxied")),
            ArgumentMatchers.any(HttpClientResponseHandler.class));
  }

  private static <T> T reflectiveSpyField(Class<T> fieldType, String fieldName, Object object) {
    try {
      Field field = object.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      T spy = spy(fieldType.cast(field.get(object)));
      field.set(object, spy);
      return spy;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private ServeEvent reverseProxyServeEvent(String path) {
    return serveEvent(path, false, new byte[0]);
  }

  private ServeEvent forwardProxyServeEvent(String path) {
    return serveEvent(path, true, new byte[0]);
  }

  private ServeEvent serveEvent(String path, boolean isBrowserProxyRequest, byte[] body) {
    return serveEvent(path, isBrowserProxyRequest, body, GET, new HttpHeaders());
  }

  private ServeEvent serveEvent(
      String path,
      boolean isBrowserProxyRequest,
      byte[] body,
      RequestMethod method,
      HttpHeaders headers) {
    return serveEvent(
        path,
        isBrowserProxyRequest,
        body,
        method,
        headers,
        aResponse().proxiedFrom(origin.baseUrl()).build());
  }

  private ServeEvent serveEvent(
      String path,
      boolean isBrowserProxyRequest,
      byte[] body,
      RequestMethod method,
      HttpHeaders headers,
      ResponseDefinition responseDefinition) {

    LoggedRequest loggedRequest =
        LoggedRequest.createFrom(
            mockRequest()
                .url(path)
                .absoluteUrl(origin.url(path))
                .method(method)
                .headers(headers)
                .isBrowserProxyRequest(isBrowserProxyRequest)
                .body(body)
                .protocol("HTTP/1.1"));
    responseDefinition.setOriginalRequest(loggedRequest);

    return newPostMatchServeEvent(loggedRequest, responseDefinition);
  }

  private File generateKeystore() throws Exception {

    InMemoryKeyStore ks =
        new InMemoryKeyStore(InMemoryKeyStore.KeyStoreType.JKS, new Secret("password"));

    CertificateSpecification certificateSpecification =
        new X509CertificateSpecification(
            /* version= */ V3,
            /* subject= */ "CN=localhost",
            /* issuer= */ "CN=wiremock.org",
            /* notBefore= */ new Date(),
            /* notAfter= */ new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)));
    KeyPair keyPair = generateKeyPair();
    ks.addPrivateKey("wiremock", keyPair, certificateSpecification.certificateFor(keyPair));

    File keystoreFile = File.createTempFile("wiremock-test", "keystore");

    ks.saveAs(keystoreFile);

    return keystoreFile;
  }

  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    return keyGen.generateKeyPair();
  }

  private ProxyResponseRenderer buildProxyResponseRenderer(boolean trustAllProxyTargets) {
    return buildProxyResponseRenderer(trustAllProxyTargets, false, null);
  }

  private ProxyResponseRenderer buildProxyResponseRenderer(
      boolean trustAllProxyTargets, boolean stubCorsEnabled, Set<String> supportedProxyEncodings) {

    reverseProxyApacheClient =
        spy(
            HttpClientFactory.createClient(
                1000,
                PROXY_TIMEOUT,
                ProxySettings.NO_PROXY,
                KeyStoreSettings.NO_STORE,
                true,
                Collections.emptyList(),
                true,
                NetworkAddressRules.ALLOW_ALL,
                true));
    HttpClient reverseProxyClient = new ApacheBackedHttpClient(reverseProxyApacheClient);

    forwardProxyApacheClient =
        spy(
            HttpClientFactory.createClient(
                1000,
                PROXY_TIMEOUT,
                ProxySettings.NO_PROXY,
                KeyStoreSettings.NO_STORE,
                trustAllProxyTargets,
                Collections.emptyList(),
                false,
                NetworkAddressRules.ALLOW_ALL,
                true));
    HttpClient forwardProxyClient = new ApacheBackedHttpClient(forwardProxyApacheClient);

    return new ProxyResponseRenderer(
        /* preserveHostHeader= */ false,
        /* hostHeaderValue= */ null,
        new InMemorySettingsStore(),
        stubCorsEnabled,
        supportedProxyEncodings,
        reverseProxyClient,
        forwardProxyClient);
  }

  // Just exists to make the compiler happy by having the throws clause
  public ProxyResponseRendererTest() throws Exception {}
}
