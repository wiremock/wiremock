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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PASSWORD;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.http.ssl.HostVerifyingSSLSocketFactory;
import com.github.tomakehurst.wiremock.http.ssl.SSLContextBuilder;
import com.github.tomakehurst.wiremock.http.ssl.X509KeyStore;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.TestFiles;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.extension.RegisterExtension;

public class HttpsBrowserProxyAcceptanceTest {

  private static final String TARGET_KEYSTORE_WITH_CUSTOM_CERT = TestFiles.KEY_STORE_PATH;
  private static final String PROXY_KEYSTORE_WITH_CUSTOM_CA_CERT = TestFiles.KEY_STORE_WITH_CA_PATH;
  private static final String NO_PREEXISTING_KEYSTORE_PATH =
      tempNonExistingPath("wiremock-keystores", "ca-keystore.jks");

  @RegisterExtension
  public static WireMockExtension target =
      WireMockExtension.newInstance()
          .options(
              options()
                  .httpDisabled(true)
                  .keystorePath(TARGET_KEYSTORE_WITH_CUSTOM_CERT)
                  .dynamicHttpsPort())
          .build();

  @RegisterExtension
  public static WireMockExtension proxy =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .dynamicHttpsPort()
                  .fileSource(new SingleRootFileSource(setupTempFileRoot()))
                  .caKeystorePath(NO_PREEXISTING_KEYSTORE_PATH)
                  .enableBrowserProxying(true)
                  .trustAllProxyTargets(true))
          .build();

  private WireMockTestClient testClient;

  @BeforeEach
  public void addAResourceToProxy() {
    testClient = new WireMockTestClient(target.getHttpsPort());
  }

  @Test
  public void canProxyHttpsInBrowserProxyMode() throws Exception {
    target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

    assertThat(
        testClient.getViaProxy(target.url("/whatever"), proxy.getPort()).content(), is("Got it"));
  }

  @Test
  public void canProxyHttpsInBrowserHttpsProxyMode() throws Exception {
    target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

    WireMockResponse response =
        testClient.getViaProxy(target.url("/whatever"), proxy.getHttpsPort(), "https");
    assertThat(response.content(), is("Got it"));
  }

  @Test
  public void canStubHttpsInBrowserProxyMode() throws Exception {
    target.stubFor(
        get(urlEqualTo("/stubbed")).willReturn(aResponse().withBody("Should Not Be Returned")));
    proxy.stubFor(get(urlEqualTo("/stubbed")).willReturn(aResponse().withBody("Stubbed Value")));
    target.stubFor(
        get(urlEqualTo("/not_stubbed"))
            .willReturn(aResponse().withBody("Should be served from target")));

    assertThat(
        testClient.getViaProxy(target.url("/stubbed"), proxy.getPort()).content(),
        is("Stubbed Value"));
    assertThat(
        testClient.getViaProxy(target.url("/not_stubbed"), proxy.getPort()).content(),
        is("Should be served from target"));
  }

  @Test
  public void canRecordHttpsInBrowserProxyMode() throws Exception {

    // given
    proxy.startRecording(target.baseUrl());
    String recordedEndpoint = target.url("/record_me");

    // and
    target.stubFor(
        get(urlEqualTo("/record_me")).willReturn(aResponse().withBody("Target response")));

    // then
    assertThat(
        testClient.getViaProxy(recordedEndpoint, proxy.getPort()).content(), is("Target response"));

    // when
    proxy.stopRecording();

    // then
    assertThat(
        testClient.getViaProxy(recordedEndpoint, proxy.getPort()).content(), is("Target response"));
  }

  @Test
  public void rejectsUntrustedTarget() {

    WireMockServer scepticalProxy =
        new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));

    try {
      scepticalProxy.start();

      target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

      WireMockResponse response =
          testClient.getViaProxy(target.url("/whatever"), scepticalProxy.port());

      assertThat(response.statusCode(), is(500));
    } finally {
      scepticalProxy.stop();
    }
  }

  @Test
  public void trustsTargetIfTrustStoreContainsItsCertificate() {

    WireMockServer scepticalProxy =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .enableBrowserProxying(true)
                .trustStorePath(TRUST_STORE_PATH)
                .trustStorePassword(TRUST_STORE_PASSWORD));

    try {
      scepticalProxy.start();

      target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

      WireMockResponse response =
          testClient.getViaProxy(target.url("/whatever"), scepticalProxy.port());

      assertThat(response.statusCode(), is(200));
      assertThat(response.content(), is("Got it"));
    } finally {
      scepticalProxy.stop();
    }
  }

  @Test
  public void canTrustSpecificTargetHosts() {

    WireMockServer scepticalProxy =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .enableBrowserProxying(true)
                .trustedProxyTargets("localhost"));

    try {
      scepticalProxy.start();

      target.stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

      WireMockResponse response =
          testClient.getViaProxy(target.url("/whatever"), scepticalProxy.port());

      assertThat(response.statusCode(), is(200));
      assertThat(response.content(), is("Got it"));
    } finally {
      scepticalProxy.stop();
    }
  }

  @Test
  @DisabledForJreRange(
      min = JRE.JAVA_17,
      disabledReason = "does not support generating certificates at runtime")
  public void certificatesSignedWithUsersRootCertificate() throws Exception {
    WireMockServer proxyWithCustomCaKeyStore =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .enableBrowserProxying(true)
                .trustAllProxyTargets(true)
                .caKeystorePath(PROXY_KEYSTORE_WITH_CUSTOM_CA_CERT));

    try {
      proxyWithCustomCaKeyStore.start();
      KeyStore trustStore =
          HttpsAcceptanceTest.readKeyStore(PROXY_KEYSTORE_WITH_CUSTOM_CA_CERT, "password");

      // given
      PoolingHttpClientConnectionManager connectionManager =
          PoolingHttpClientConnectionManagerBuilder.create()
              .setDnsResolver(new CustomLocalTldDnsResolver("internal"))
              .setSSLSocketFactory(sslSocketFactoryThatTrusts(trustStore))
              .build();
      CloseableHttpClient httpClient =
          HttpClients.custom()
              .setConnectionManager(connectionManager)
              .setProxy(new HttpHost("localhost", proxyWithCustomCaKeyStore.port()))
              .build();

      // when
      httpClient.execute(
          new HttpGet(
              "https://fake1.nowildcards1.internal:" + target.getHttpsPort() + "/whatever"));

      // then no exception is thrown

      // when
      httpClient.execute(
          new HttpGet(
              "https://fake2.nowildcards2.internal:" + target.getHttpsPort() + "/whatever"));

      // then no exception is thrown
    } finally {
      proxyWithCustomCaKeyStore.stop();
    }
  }

  @Test
  @DisabledForJreRange(
      min = JRE.JAVA_17,
      disabledReason = "does not support generating certificates at runtime")
  public void certificatesSignedWithGeneratedRootCertificate() throws Exception {
    KeyStore trustStore =
        HttpsAcceptanceTest.readKeyStore(NO_PREEXISTING_KEYSTORE_PATH, "password");

    // given
    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setDnsResolver(new CustomLocalTldDnsResolver("internal"))
            .setSSLSocketFactory(sslSocketFactoryThatTrusts(trustStore))
            .build();
    CloseableHttpClient httpClient =
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setProxy(new HttpHost("localhost", proxy.getPort()))
            .build();

    // when
    httpClient.execute(
        new HttpGet("https://fake1.nowildcards1.internal:" + target.getHttpsPort() + "/whatever"));

    // then no exception is thrown

    // when
    httpClient.execute(
        new HttpGet("https://fake2.nowildcards2.internal:" + target.getHttpsPort() + "/whatever"));

    // then no exception is thrown
  }

  @Test
  public void failsIfCaKeystorePathIsNotAKeystore() throws IOException {
    assertThrows(
        IOException.class,
        () ->
            new WireMockServer(
                options()
                    .enableBrowserProxying(true)
                    .caKeystorePath(Files.createTempFile("notakeystore", "jks").toString())));
  }

  @Test
  public void failsIfCaKeystoreDoesNotContainACaCertificate() throws Exception {
    assertThrows(
        FatalStartupException.class,
        new WireMockServer(
                options().enableBrowserProxying(true).caKeystorePath(emptyKeyStore().toString()))
            ::start);
  }

  @Test
  @DisabledForJreRange(
      min = JRE.JAVA_17,
      disabledReason = "does not support generating certificates at runtime")
  public void certificateAuthorityCertCanBeDownloaded() throws Exception {
    WireMockTestClient proxyTestClient = new WireMockTestClient(proxy.getPort());

    WireMockResponse certResponse = proxyTestClient.get("/__admin/certs/wiremock-ca.crt");
    assertEquals(200, certResponse.statusCode());
    assertEquals("application/x-pem-file", certResponse.firstHeader("Content-Type"));

    Certificate cert = decode(certResponse.content());
    X509KeyStore keyStore =
        new X509KeyStore(
            HttpsAcceptanceTest.readKeyStore(NO_PREEXISTING_KEYSTORE_PATH, "password"),
            "password".toCharArray());

    assertEquals(keyStore.getCertificateAuthority().certificateChain()[0], cert);
  }

  private Certificate decode(String body) throws Exception {
    String base64 =
        body.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
    byte[] certBytes = Base64.getMimeDecoder().decode(base64);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return cf.generateCertificate(new ByteArrayInputStream(certBytes));
  }

  private SSLConnectionSocketFactory sslSocketFactoryThatTrusts(KeyStore trustStore)
      throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
    SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(trustStore).build();
    return new SSLConnectionSocketFactory(
        new HostVerifyingSSLSocketFactory(sslContext.getSocketFactory()),
        new NoopHostnameVerifier() // using Java's hostname verification
        );
  }

  private static File setupTempFileRoot() {
    try {
      File root = Files.createTempDirectory("wiremock").toFile();
      new File(root, MAPPINGS_ROOT).mkdirs();
      new File(root, FILES_ROOT).mkdirs();
      return root;
    } catch (IOException e) {
      return throwUnchecked(e, File.class);
    }
  }

  private static class CustomLocalTldDnsResolver implements DnsResolver {

    private final String tldToSendToLocalhost;

    public CustomLocalTldDnsResolver(String tldToSendToLocalhost) {
      this.tldToSendToLocalhost = tldToSendToLocalhost;
    }

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
      if (host.endsWith("." + tldToSendToLocalhost)) {
        return new InetAddress[] {InetAddress.getLocalHost()};
      } else {
        return new SystemDefaultDnsResolver().resolve(host);
      }
    }

    @Override
    public String resolveCanonicalHostname(String host) throws UnknownHostException {
      final InetAddress[] resolvedAddresses = resolve(host);
      if (resolvedAddresses.length > 0) {
        return resolvedAddresses[0].getCanonicalHostName();
      }
      return host;
    }
  }

  private static String tempNonExistingPath(String prefix, String filename) {
    try {
      Path tempDirectory = Files.createTempDirectory(prefix);
      return tempDirectory.resolve(filename).toFile().getAbsolutePath();
    } catch (IOException e) {
      return throwUnchecked(e, null);
    }
  }

  private static Path emptyKeyStore()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    char[] password = "password".toCharArray();
    keyStore.load(null, password);
    Path keystoreNoCa = Files.createTempFile("keystore-with-no-ca", "jks");
    try (FileOutputStream fos = new FileOutputStream(keystoreNoCa.toFile())) {
      keyStore.store(fos, password);
    }
    return keystoreNoCa;
  }
}
