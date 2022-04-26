/*
 * Copyright (C) 2020-2022 Thomas Akehurst
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
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.crypto.CertificateSpecification;
import com.github.tomakehurst.wiremock.crypto.InMemoryKeyStore;
import com.github.tomakehurst.wiremock.crypto.Secret;
import com.github.tomakehurst.wiremock.crypto.X509CertificateSpecification;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

@DisabledForJreRange(
    min = JRE.JAVA_17,
    disabledReason = "does not support generating certificates at runtime")
public class ProxyResponseRendererTest {

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
    ProxyResponseRenderer responseRenderer = buildProxyResponseRenderer(true, false);

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
    ProxyResponseRenderer responseRenderer = buildProxyResponseRenderer(true, true);

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
    CloseableHttpClient clientSpy =
        reflectiveSpyField(CloseableHttpClient.class, "reverseProxyClient", proxyResponseRenderer);

    ServeEvent serveEvent = reverseProxyServeEvent("/proxied");

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(clientSpy).execute(argThat(request -> request.getEntity() == null));
  }

  @Test
  void doesNotAddEntityIfEmptyBodyForwardProxy() throws IOException {
    CloseableHttpClient clientSpy =
        reflectiveSpyField(CloseableHttpClient.class, "forwardProxyClient", proxyResponseRenderer);

    ServeEvent serveEvent = forwardProxyServeEvent("/proxied");

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(clientSpy).execute(argThat(request -> request.getEntity() == null));
  }

  @Test
  void addsEntityIfNotEmptyBodyReverseProxy() throws IOException {
    CloseableHttpClient clientSpy =
        reflectiveSpyField(CloseableHttpClient.class, "reverseProxyClient", proxyResponseRenderer);

    ServeEvent serveEvent =
        serveEvent("/proxied", false, "Text body".getBytes(StandardCharsets.UTF_8));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(clientSpy).execute(argThat(request -> request.getEntity() != null));
  }

  @Test
  void addsEntityIfNotEmptyBodyForwardProxy() throws IOException {
    CloseableHttpClient clientSpy =
        reflectiveSpyField(CloseableHttpClient.class, "forwardProxyClient", proxyResponseRenderer);

    ServeEvent serveEvent =
        serveEvent("/proxied", true, "Text body".getBytes(StandardCharsets.UTF_8));

    proxyResponseRenderer.render(serveEvent);
    Mockito.verify(clientSpy).execute(argThat(request -> request.getEntity() != null));
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
    LoggedRequest loggedRequest =
        new LoggedRequest(
            /* url = */ path,
            /* absoluteUrl = */ origin.url(path),
            /* method = */ RequestMethod.GET,
            /* clientIp = */ "127.0.0.1",
            /* headers = */ new HttpHeaders(),
            /* cookies = */ new HashMap<String, Cookie>(),
            /* isBrowserProxyRequest = */ isBrowserProxyRequest,
            /* loggedDate = */ new Date(),
            /* body = */ body,
            /* multiparts = */ null);
    ResponseDefinition responseDefinition = aResponse().proxiedFrom(origin.baseUrl()).build();
    responseDefinition.setOriginalRequest(loggedRequest);

    return ServeEvent.of(loggedRequest, responseDefinition, new StubMapping());
  }

  private File generateKeystore() throws Exception {

    InMemoryKeyStore ks =
        new InMemoryKeyStore(InMemoryKeyStore.KeyStoreType.JKS, new Secret("password"));

    CertificateSpecification certificateSpecification =
        new X509CertificateSpecification(
            /* version = */ V3,
            /* subject = */ "CN=localhost",
            /* issuer = */ "CN=wiremock.org",
            /* notBefore = */ new Date(),
            /* notAfter = */ new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)));
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
    return buildProxyResponseRenderer(trustAllProxyTargets, false);
  }

  private ProxyResponseRenderer buildProxyResponseRenderer(
      boolean trustAllProxyTargets, boolean stubCorsEnabled) {
    return new ProxyResponseRenderer(
        ProxySettings.NO_PROXY,
        KeyStoreSettings.NO_STORE,
        /* preserveHostHeader = */ false,
        /* hostHeaderValue = */ null,
        new GlobalSettingsHolder(),
        trustAllProxyTargets,
        Collections.<String>emptyList(),
        stubCorsEnabled);
  }

  // Just exists to make the compiler happy by having the throws clause
  public ProxyResponseRendererTest() throws Exception {}
}
