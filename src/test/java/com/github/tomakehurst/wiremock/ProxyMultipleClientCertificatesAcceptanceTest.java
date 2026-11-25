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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.filePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Two mTLS upstreams, each trusting a different client certificate, proxied by one WireMock whose
 * trust store holds both client keys.
 *
 * <p>Both client certificates (multi-client-certs/client-store.p12, aliases client-a and client-b)
 * are self-signed with the same subject, so the CA names an upstream sends in its certificate
 * request cannot tell them apart - the same situation as an upstream that sends no CA names. The
 * JDK then presents the same key to both, so one upstream rejects it. With --https-client-key-alias
 * each upstream gets its own key. trust-a.p12 / trust-b.p12 trust only client-a / client-b.
 */
public class ProxyMultipleClientCertificatesAcceptanceTest {

  private static final String PASSWORD = "password";
  private static final String CLIENT_STORE_PATH = filePath("multi-client-certs/client-store.p12");

  private final List<WireMockServer> servers = new ArrayList<>();
  private WireMockServer upstreamA;
  private WireMockServer upstreamB;

  @BeforeEach
  void startUpstreams() {
    upstreamA = start(mtlsUpstream(filePath("multi-client-certs/trust-a.p12")));
    upstreamB = start(mtlsUpstream(filePath("multi-client-certs/trust-b.p12")));
    upstreamA.stubFor(get("/a").willReturn(aResponse().withBody("from A")));
    upstreamB.stubFor(get("/b").willReturn(aResponse().withBody("from B")));
  }

  @AfterEach
  void stopServers() {
    servers.forEach(WireMockServer::stop);
  }

  @Test
  void presentsTheMappedClientCertificateToEachHost() {
    WireMockServer proxy =
        start(
            proxyOptions()
                .clientKeyAlias("localhost", "client-a")
                .clientKeyAlias("127.0.0.1", "client-b"));
    stubProxies(proxy);

    WireMockTestClient client = new WireMockTestClient(proxy.port());
    WireMockResponse a = client.get("/a");
    WireMockResponse b = client.get("/b");

    assertThat(a.content(), a.statusCode(), is(200));
    assertThat(a.content(), is("from A"));
    assertThat(b.content(), b.statusCode(), is(200));
    assertThat(b.content(), is("from B"));
  }

  @Test
  void withoutMappingTheSameCertificateIsPresentedToBothHostsSoOneFails() {
    WireMockServer proxy = start(proxyOptions());
    stubProxies(proxy);

    WireMockTestClient client = new WireMockTestClient(proxy.port());
    int a = client.get("/a").statusCode();
    int b = client.get("/b").statusCode();

    assertThat("exactly one upstream accepts the default client certificate", a == 200 ^ b == 200);
  }

  @Test
  void failsAtStartupWhenMappedAliasIsNotAKeyInTheTrustStore() {
    Exception e =
        assertThrows(
            Exception.class,
            () -> start(proxyOptions().clientKeyAlias("localhost", "no-such-alias")));

    assertThat(rootMessage(e), containsString("no-such-alias"));
    assertThat(rootMessage(e), containsString("client-a"));
    assertThat(rootMessage(e), not(containsString(PASSWORD)));
  }

  private WireMockConfiguration proxyOptions() {
    return options()
        .dynamicPort()
        .trustStorePath(CLIENT_STORE_PATH)
        .trustStorePassword(PASSWORD)
        .trustStoreType("PKCS12");
  }

  private void stubProxies(WireMockServer proxy) {
    proxy.stubFor(
        get("/a")
            .willReturn(aResponse().proxiedFrom("https://localhost:" + upstreamA.httpsPort())));
    proxy.stubFor(
        get("/b")
            .willReturn(aResponse().proxiedFrom("https://127.0.0.1:" + upstreamB.httpsPort())));
  }

  private static WireMockConfiguration mtlsUpstream(String trustStorePath) {
    return options()
        .httpDisabled(true)
        .dynamicHttpsPort()
        .needClientAuth(true)
        .trustStorePath(trustStorePath)
        .trustStorePassword(PASSWORD)
        .trustStoreType("PKCS12");
  }

  private WireMockServer start(WireMockConfiguration options) {
    WireMockServer server = new WireMockServer(options);
    servers.add(server);
    server.start();
    return server;
  }

  private static String rootMessage(Throwable t) {
    StringBuilder messages = new StringBuilder();
    for (Throwable c = t; c != null; c = c.getCause()) {
      messages.append(c.getMessage()).append('\n');
    }
    return messages.toString();
  }
}
