/*
 * Copyright (C) 2013-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.google.common.io.Resources;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.MalformedChunkCodingException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class HttpsAcceptanceTest {

  private WireMockServer wireMockServer;
  private WireMockServer proxy;
  private CloseableHttpClient httpClient;

  @AfterEach
  public void serverShutdown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }

    if (proxy != null) {
      proxy.shutdown();
    }
  }

  @Test
  public void shouldReturnStubOnSpecifiedPort() throws Exception {
    startServerWithDefaultKeystore();
    stubFor(
        get(urlEqualTo("/https-test"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    assertThat(contentFor(url("/https-test")), is("HTTPS content"));
  }

  @Test
  public void shouldReturnOnlyOnHttpsWhenHttpDisabled() throws Exception {
    Throwable exception =
        assertThrows(
            IllegalStateException.class,
            () -> {
              // HTTPS
              WireMockConfiguration config = wireMockConfig().httpDisabled(true).dynamicHttpsPort();
              wireMockServer = new WireMockServer(config);
              wireMockServer.start();
              WireMock.configureFor("https", "localhost", wireMockServer.httpsPort());
              httpClient = HttpClientFactory.createClient();

              stubFor(
                  get(urlEqualTo("/https-test"))
                      .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

              wireMockServer.port();
              assertThat(contentFor(url("/https-test")), is("HTTPS content"));
            });
    assertTrue(
        exception
            .getMessage()
            .contains(
                "Not listening on HTTP port. Either HTTP is not enabled or the WireMock server is stopped."));
  }

  @Test
  @DisabledOnOs(
      value = OS.WINDOWS,
      disabledReason =
          "This feature does not work on Windows " + "because of differing native socket behaviour")
  public void connectionResetByPeerFault() throws IOException {
    startServerWithDefaultKeystore();
    stubFor(
        get(urlEqualTo("/connection/reset"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

    try {
      httpClient.execute(new HttpGet(url("/connection/reset"))).getEntity();
      fail("Expected a SocketException or SSLException to be thrown");
    } catch (Exception e) {
      assertThat(
          e.getClass().getName(),
          Matchers.anyOf(is(SocketException.class.getName()), is(SSLException.class.getName())));
    }
  }

  @Test
  public void emptyResponseFault() {
    startServerWithDefaultKeystore();
    stubFor(
        get(urlEqualTo("/empty/response")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

    getAndAssertUnderlyingExceptionInstanceClass(
        url("/empty/response"), NoHttpResponseException.class);
  }

  @Test
  public void malformedResponseChunkFault() {
    startServerWithDefaultKeystore();
    stubFor(
        get(urlEqualTo("/malformed/response"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

    getAndAssertUnderlyingExceptionInstanceClass(
        url("/malformed/response"), MalformedChunkCodingException.class);
  }

  @Test
  public void randomDataOnSocketFault() {
    startServerWithDefaultKeystore();
    stubFor(
        get(urlEqualTo("/random/data"))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

    getAndAssertUnderlyingExceptionInstanceClass(
        url("/random/data"), NoHttpResponseException.class);
  }

  @Test
  public void throwsExceptionWhenBadAlternativeKeystore() {
    assertThrows(
        Exception.class,
        () -> {
          String testKeystorePath = Resources.getResource("bad-keystore").toString();
          startServerWithKeystore(testKeystorePath);
        });
  }

  @Test
  public void acceptsAlternativeKeystore() throws Exception {
    String testKeystorePath = Resources.getResource("test-keystore").toString();
    startServerWithKeystore(testKeystorePath);
    stubFor(
        get(urlEqualTo("/https-test"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    assertThat(contentFor(url("/https-test")), is("HTTPS content"));
  }

  @Test
  public void acceptsAlternativeKeystoreWithNonDefaultPassword() throws Exception {
    String testKeystorePath = Resources.getResource("test-keystore-pwd").toString();
    startServerWithKeystore(testKeystorePath, "nondefaultpass", "password");
    stubFor(
        get(urlEqualTo("/https-test"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    assertThat(contentFor(url("/https-test")), is("HTTPS content"));
  }

  @Test
  public void acceptsAlternativeKeystoreWithNonDefaultKeyManagerPassword() throws Exception {
    String keystorePath = Resources.getResource("test-keystore-key-man-pwd").toString();
    startServerWithKeystore(keystorePath, "password", "anotherpassword");
    stubFor(
        get(urlEqualTo("/alt-password-https"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    assertThat(contentFor(url("/alt-password-https")), is("HTTPS content"));
  }

  @Test
  public void failsToStartWithAlternativeKeystoreWithWrongKeyManagerPassword() {
    try {
      String keystorePath = Resources.getResource("test-keystore-key-man-pwd").toString();
      startServerWithKeystore(keystorePath, "password", "wrongpassword");
      fail("Expected a SocketException or SSLHandshakeException to be thrown");
    } catch (Exception e) {
      assertThat(e.getClass().getName(), is(FatalStartupException.class.getName()));
    }
  }

  @Test
  public void rejectsWithoutClientCertificate() {
    startServerEnforcingClientCert(KEY_STORE_PATH, TRUST_STORE_PATH, TRUST_STORE_PASSWORD);
    wireMockServer.stubFor(
        get(urlEqualTo("/https-test"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    try {
      contentFor(url("/https-test")); // this lacks the required client certificate
      fail("Expected a SocketException, SSLHandshakeException or SSLException to be thrown");
    } catch (Exception e) {
      assertThat(
          e.getClass().getName(),
          Matchers.anyOf(
              is(HttpHostConnectException.class.getName()),
              is(SSLHandshakeException.class.getName()),
              is(SSLException.class.getName()),
              is(SocketException.class.getName())));
    }
  }

  @Test
  public void acceptWithClientCertificate() throws Exception {
    String testTrustStorePath = TRUST_STORE_PATH;
    String testClientCertPath = TRUST_STORE_PATH;

    startServerEnforcingClientCert(KEY_STORE_PATH, testTrustStorePath, TRUST_STORE_PASSWORD);
    wireMockServer.stubFor(
        get(urlEqualTo("/https-test"))
            .willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

    assertThat(
        secureContentFor(url("/https-test"), testClientCertPath, TRUST_STORE_PASSWORD),
        is("HTTPS content"));
  }

  @Test
  public void supportsProxyingWhenTargetRequiresClientCert() throws Exception {
    startServerEnforcingClientCert(KEY_STORE_PATH, TRUST_STORE_PATH, TRUST_STORE_PASSWORD);
    wireMockServer.stubFor(
        get(urlEqualTo("/client-cert-proxy")).willReturn(aResponse().withStatus(200)));

    proxy =
        new WireMockServer(
            wireMockConfig()
                .port(Options.DYNAMIC_PORT)
                .trustStorePath(TRUST_STORE_PATH)
                .trustStorePassword(TRUST_STORE_PASSWORD));
    proxy.start();
    proxy.stubFor(
        get(urlEqualTo("/client-cert-proxy"))
            .willReturn(
                aResponse().proxiedFrom("https://localhost:" + wireMockServer.httpsPort())));

    HttpGet get = new HttpGet("http://localhost:" + proxy.port() + "/client-cert-proxy");
    HttpResponse response = httpClient.execute(get);
    assertThat(response.getCode(), is(200));
  }

  @Test
  public void proxyingFailsWhenTargetServiceRequiresClientCertificatesAndProxyDoesNotSend()
      throws Exception {
    startServerEnforcingClientCert(KEY_STORE_PATH, TRUST_STORE_PATH, TRUST_STORE_PASSWORD);
    wireMockServer.stubFor(
        get(urlEqualTo("/client-cert-proxy-fail")).willReturn(aResponse().withStatus(200)));

    proxy = new WireMockServer(wireMockConfig().port(Options.DYNAMIC_PORT));
    proxy.start();
    proxy.stubFor(
        get(urlEqualTo("/client-cert-proxy-fail"))
            .willReturn(
                aResponse().proxiedFrom("https://localhost:" + wireMockServer.httpsPort())));

    HttpGet get = new HttpGet("http://localhost:" + proxy.port() + "/client-cert-proxy-fail");
    HttpResponse response = httpClient.execute(get);
    assertThat(response.getCode(), is(500));
  }

  private String url(String path) {
    return String.format("https://localhost:%d%s", wireMockServer.httpsPort(), path);
  }

  private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
    boolean thrown = false;
    try {
      contentFor(url);
    } catch (Exception e) {
      Throwable cause = e.getCause();
      e.printStackTrace();
      if (cause != null) {
        assertThat(e.getCause(), instanceOf(expectedClass));
      } else {
        assertThat(e, instanceOf(expectedClass));
      }

      thrown = true;
    }

    assertTrue(thrown, "No exception was thrown");
  }

  private String contentFor(String url) throws Exception {
    HttpGet get = new HttpGet(url);
    ClassicHttpResponse response = httpClient.execute(get);
    String content = EntityUtils.toString(response.getEntity());
    return content;
  }

  private void startServerEnforcingClientCert(
      String keystorePath, String truststorePath, String trustStorePassword) {
    WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicHttpsPort();
    if (keystorePath != null) {
      config.keystorePath(keystorePath);
    }
    if (truststorePath != null) {
      config.trustStorePath(truststorePath);
      config.trustStorePassword(trustStorePassword);
      config.needClientAuth(true);
    }
    config.bindAddress("localhost");

    wireMockServer = new WireMockServer(config);
    wireMockServer.start();
    WireMock.configureFor("https", "localhost", wireMockServer.httpsPort());

    httpClient = HttpClientFactory.createClient();
  }

  private void startServerWithKeystore(
      String keystorePath, String keystorePassword, String keyManagerPassword) {
    WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicHttpsPort();
    if (keystorePath != null) {
      config
          .keystorePath(keystorePath)
          .keystorePassword(keystorePassword)
          .keyManagerPassword(keyManagerPassword);
    }

    wireMockServer = new WireMockServer(config);
    wireMockServer.start();
    WireMock.configureFor(wireMockServer.port());

    httpClient = HttpClientFactory.createClient();
  }

  private void startServerWithKeystore(String keystorePath) {
    startServerWithKeystore(keystorePath, "password", "password");
  }

  private void startServerWithDefaultKeystore() {
    startServerWithKeystore(null);
  }

  static String secureContentFor(String url, String clientTrustStore, String trustStorePassword)
      throws Exception {
    KeyStore trustStore = readKeyStore(clientTrustStore, trustStorePassword);

    SSLContext sslcontext =
        SSLContexts.custom()
            .loadTrustMaterial(null, new TrustSelfSignedStrategy())
            .loadKeyMaterial(trustStore, trustStorePassword.toCharArray())
            .setKeyStoreType("pkcs12")
            .setProtocol("TLS")
            .build();

    SSLConnectionSocketFactory sslSocketFactory =
        new SSLConnectionSocketFactory(
            sslcontext,
            null, // supported protocols
            null, // supported cipher suites
            NoopHostnameVerifier.INSTANCE);

    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslSocketFactory)
            .build();

    CloseableHttpClient httpClient =
        HttpClients.custom().setConnectionManager(connectionManager).build();

    HttpGet get = new HttpGet(url);
    ClassicHttpResponse response = httpClient.execute(get);
    String content = EntityUtils.toString(response.getEntity());
    return content;
  }

  static KeyStore readKeyStore(String path, String password)
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    try (FileInputStream instream = new FileInputStream(path)) {
      trustStore.load(instream, password.toCharArray());
    }
    return trustStore;
  }
}
