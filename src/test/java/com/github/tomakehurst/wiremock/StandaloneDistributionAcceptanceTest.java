/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.HttpsAcceptanceTest.readKeyStore;
import static com.github.tomakehurst.wiremock.HttpsAcceptanceTest.secureContentFor;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.KEY_STORE_PATH;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PASSWORD;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PATH;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.wiremock.webhooks.Webhooks.webhook;

import com.github.tomakehurst.wiremock.http.client.apache5.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Exercises a packaged WireMock distribution (standalone JVM jar or native binary) as a separate
 * process, covering behaviour that the in-process acceptance tests cannot reach. Only runs when
 * {@link ExternalWireMock#COMMAND_PROPERTY} is set.
 */
public class StandaloneDistributionAcceptanceTest {

  @TempDir Path root;

  private ExternalWireMock external;
  private WireMockServer target;

  @BeforeAll
  static void requireExternalDistribution() {
    assumeTrue(ExternalWireMock.enabled(), "Runs against an external WireMock distribution only");
  }

  @AfterEach
  void stop() {
    if (target != null) {
      target.stop();
    }
    if (external != null) {
      external.stop();
    }
  }

  @Test
  void printsHelp() {
    ExternalWireMock.Result result = ExternalWireMock.runToCompletion("--help");

    assertThat(result.output(), result.exitCode(), is(0));
    assertThat(result.output(), containsString("--port <String>"));
    assertThat(result.output(), containsString("--https-port <String>"));
    assertThat(result.output(), not(containsString("Exception")));
  }

  @Test
  void printsVersion() {
    ExternalWireMock.Result result = ExternalWireMock.runToCompletion("--version");

    assertThat(result.output(), result.exitCode(), is(0));
    assertThat(result.output().trim(), matchesPattern("\\d+\\.\\d+\\.\\d+.*"));
  }

  @Test
  void rejectsUnknownOption() {
    ExternalWireMock.Result result = ExternalWireMock.runToCompletion("--no-such-option");

    assertThat(result.output(), result.exitCode(), not(is(0)));
    assertThat(result.output(), containsString("no-such-option is not a recognized option"));
  }

  @Test
  void failsClearlyWhenAnExtensionClassIsNotAvailable() {
    ExternalWireMock.Result result =
        ExternalWireMock.runToCompletion(
            "--port", "0", "--root-dir", root.toString(), "--extensions", "com.example.Missing");

    assertThat(result.output(), result.exitCode(), not(is(0)));
    assertThat(result.output(), containsString("com.example.Missing"));
  }

  @Test
  void servesHttpsUsingTheBundledKeystore() throws Exception {
    external = ExternalWireMock.start(root.toString(), "--https-port", "0");
    external.stubFor(get("/secure").willReturn(ok("Secure content")));

    assertThat(httpsGet(external.httpsPort(), "/secure"), is("Secure content"));
  }

  @Test
  void servesHttpsUsingACustomKeystore() throws Exception {
    external =
        ExternalWireMock.start(
            root.toString(),
            "--https-port",
            "0",
            "--https-keystore",
            filePath("test-keystore-key-man-pwd"),
            "--keystore-type",
            "JKS",
            "--keystore-password",
            "password",
            "--key-manager-password",
            "anotherpassword");
    external.stubFor(get("/secure").willReturn(ok("Custom keystore")));

    assertThat(httpsGet(external.httpsPort(), "/secure"), is("Custom keystore"));
  }

  @Test
  void failsToStartWithTheWrongKeyManagerPassword() throws Exception {
    ExternalWireMock.Result result =
        ExternalWireMock.runToCompletion(
            "--port",
            "0",
            "--https-port",
            "0",
            "--root-dir",
            root.toString(),
            "--https-keystore",
            filePath("test-keystore-key-man-pwd"),
            "--key-manager-password",
            "wrongpassword");

    assertThat(result.output(), result.exitCode(), not(is(0)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"pkcs12", "JCEKS"})
  void requiresAClientCertificateWhenConfigured(String trustStoreType) throws Exception {
    external =
        ExternalWireMock.start(
            root.toString(),
            "--https-port",
            "0",
            "--https-keystore",
            KEY_STORE_PATH,
            "--https-truststore",
            trustStoreOfType(trustStoreType),
            "--truststore-type",
            trustStoreType,
            "--truststore-password",
            TRUST_STORE_PASSWORD,
            "--https-require-client-cert");
    external.stubFor(get("/client-cert").willReturn(ok("Client certificate accepted")));

    assertThat(
        secureContentFor(
            "https://localhost:" + external.httpsPort() + "/client-cert",
            TRUST_STORE_PATH,
            TRUST_STORE_PASSWORD),
        is("Client certificate accepted"));
    assertThrows(IOException.class, () -> httpsGet(external.httpsPort(), "/client-cert"));
  }

  @Test
  void proxiesToATargetThatRequiresAClientCertificate() {
    target =
        new WireMockServer(
            wireMockConfig()
                .dynamicPort()
                .dynamicHttpsPort()
                .keystorePath(KEY_STORE_PATH)
                .trustStorePath(TRUST_STORE_PATH)
                .trustStorePassword(TRUST_STORE_PASSWORD)
                .needClientAuth(true));
    target.start();
    target.stubFor(get("/client-cert-proxy").willReturn(ok("From target")));
    external =
        ExternalWireMock.start(
            root.toString(),
            "--https-truststore",
            TRUST_STORE_PATH,
            "--truststore-password",
            TRUST_STORE_PASSWORD);
    external.stubFor(
        get("/client-cert-proxy")
            .willReturn(aResponse().proxiedFrom("https://localhost:" + target.httpsPort())));

    WireMockResponse response = new WireMockTestClient(external.port()).get("/client-cert-proxy");

    assertThat(response.content(), response.statusCode(), is(200));
    assertThat(response.content(), is("From target"));
  }

  @Test
  void negotiatesHttp2OverTls() throws Exception {
    external = ExternalWireMock.start(root.toString(), "--https-port", "0");
    external.stubFor(get("/h2").willReturn(ok("HTTP/2 response")));

    ClientConnector connector = new ClientConnector();
    connector.setSslContextFactory(new SslContextFactory.Client(true));
    HttpClient client =
        new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client(connector)));
    client.start();
    try {
      ContentResponse response = client.GET("https://localhost:" + external.httpsPort() + "/h2");

      assertThat(response.getVersion(), is(HttpVersion.HTTP_2));
      assertThat(response.getContentAsString(), is("HTTP/2 response"));
    } finally {
      client.stop();
    }
  }

  @Test
  void startsWithTheDefaultBanner() {
    external = ExternalWireMock.launch(List.of("--root-dir", root.toString()), false);

    assertThat(external.output(), containsString("port:"));
    assertThat(external.output(), not(containsString("Exception")));
  }

  @Test
  void browserProxiesHttpsWithAGeneratedCertificateAuthority() {
    target = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());
    target.start();
    target.stubFor(get("/through-proxy").willReturn(ok("From target")));
    external =
        ExternalWireMock.start(
            root.toString(),
            "--enable-browser-proxying",
            "--trust-all-proxy-targets",
            "--ca-keystore",
            root.resolve("ca-keystore.jks").toString());

    WireMockResponse response =
        new WireMockTestClient(target.httpsPort())
            .getViaProxy(
                "https://localhost:" + target.httpsPort() + "/through-proxy", external.port());

    assertThat(response.content(), is("From target"));
    assertThat(root.resolve("ca-keystore.jks").toFile().exists(), is(true));
  }

  @Test
  void rendersResponseTemplateHelpers() {
    external = ExternalWireMock.start(root.toString());
    external.stubFor(
        post("/templated")
            .willReturn(
                aResponse()
                    .withTransformers("response-template")
                    .withBody(
                        String.join(
                            "|",
                            "{{request.path.[0]}}",
                            "{{request.headers.X-Test}}",
                            "{{jsonPath request.body '$.name'}}",
                            "{{#each (jsonPath request.body '$.items') as"
                                + " |item|}}{{item}};{{/each}}",
                            "{{upper (jsonPath request.body '$.name')}}",
                            "{{regexExtract request.headers.X-Test '[0-9]+'}}",
                            "{{base64 'hello'}}",
                            "{{math 6 '*' 7}}",
                            "{{date (parseDate '2024-01-15T10:00:00Z') format='EEEE d MMMM yyyy'}}",
                            "{{numberFormat 1234567.891 'integer' 'de_DE'}}",
                            "{{size (randomValue length=12 type='ALPHANUMERIC')}}",
                            "{{now timezone='Australia/Sydney' format='yyyy zzzz'}}",
                            "{{formatJson '{\"a\":1}'}}",
                            "{{xPath '<a><b>xml value</b></a>' '/a/b/text()'}}"))));

    WireMockResponse response =
        new WireMockTestClient(external.port())
            .postJson(
                "/templated",
                "{\"name\": \"wiremock\", \"items\": [\"one\", \"two\"]}",
                withHeader("X-Test", "abc123"));

    List<String> parts = List.of(response.content().split("\\|"));
    assertThat(response.content(), parts.get(0), is("templated"));
    assertThat(response.content(), parts.get(1), is("abc123"));
    assertThat(response.content(), parts.get(2), is("wiremock"));
    assertThat(response.content(), parts.get(3), is("one;two;"));
    assertThat(response.content(), parts.get(4), is("WIREMOCK"));
    assertThat(response.content(), parts.get(5), is("123"));
    assertThat(response.content(), parts.get(6), is("aGVsbG8="));
    assertThat(response.content(), parts.get(7), is("42"));
    assertThat(response.content(), parts.get(8), is("Monday 15 January 2024"));
    assertThat(response.content(), parts.get(9), is("1.234.568"));
    assertThat(response.content(), parts.get(10), is("12"));
    assertThat(
        response.content(),
        parts.get(11),
        matchesPattern("\\d{4} Australian Eastern (Daylight|Standard) Time"));
    assertThat(response.content(), parts.get(12), containsString("\"a\" : 1"));
    assertThat(response.content(), parts.get(13), is("xml value"));
  }

  @Test
  void firesWebhooks() throws IOException {
    target = new WireMockServer(wireMockConfig().dynamicPort());
    target.start();
    target.stubFor(post("/callback").willReturn(ok()));
    external = ExternalWireMock.start(root.toString());
    external.stubFor(
        post("/trigger")
            .willReturn(ok())
            .withServeEventListener(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(target.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"id\": \"{{jsonPath originalRequest.body '$.id'}}\" }")));

    new WireMockTestClient(external.port()).postJson("/trigger", "{\"id\": \"abc\"}");

    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(
            () ->
                target.verify(
                    postRequestedFor(urlEqualTo("/callback"))
                        .withRequestBody(equalToJson("{\"id\": \"abc\"}"))));
  }

  private static String httpsGet(int httpsPort, String path) throws IOException {
    try (CloseableHttpClient client = ApacheHttpClientFactory.createClient()) {
      return client.execute(
          new HttpGet("https://localhost:" + httpsPort + path),
          response -> EntityUtils.toString(response.getEntity(), UTF_8));
    }
  }

  private String trustStoreOfType(String type) throws Exception {
    KeyStore clientStore = readKeyStore(TRUST_STORE_PATH, TRUST_STORE_PASSWORD);
    KeyStore trustStore = KeyStore.getInstance(type);
    trustStore.load(null, TRUST_STORE_PASSWORD.toCharArray());
    for (String alias : Collections.list(clientStore.aliases())) {
      trustStore.setCertificateEntry(alias, clientStore.getCertificate(alias));
    }
    Path path = root.resolve("truststore." + type.toLowerCase());
    try (OutputStream output = Files.newOutputStream(path)) {
      trustStore.store(output, TRUST_STORE_PASSWORD.toCharArray());
    }
    return path.toString();
  }
}
