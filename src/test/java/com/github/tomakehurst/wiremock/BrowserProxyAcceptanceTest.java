/*
 * Copyright (C) 2013-2023 Thomas Akehurst
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class BrowserProxyAcceptanceTest {

  @RegisterExtension
  public static WireMockExtension target = WireMockExtension.newInstance().build();

  private WireMockServer proxy;
  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    testClient = new WireMockTestClient(target.getPort());

    proxy = new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
    proxy.start();
  }

  @AfterEach
  public void stopServer() {
    if (proxy.isRunning()) {
      proxy.stop();
    }
  }

  @Test
  void canProxyHttp() {
    target.stubFor(get("/whatever").willReturn(aResponse().withBody("Got it")));

    assertThat(
        testClient.getViaProxy(target.url("/whatever"), proxy.port()).content(), is("Got it"));
  }

  @Test
  void passesQueryParameters() {
    target.stubFor(
        get(urlEqualTo("/search?q=things&limit=10")).willReturn(aResponse().withStatus(200)));

    assertThat(
        testClient.getViaProxy(target.url("/search?q=things&limit=10"), proxy.port()).statusCode(),
        is(200));
  }

  @Test
  void returnNotConfiguredResponseOnPassThroughDisabled() {
    target.stubFor(get("/whatever").willReturn(ok("Got it")));

    GlobalSettings newSettings =
        target.getGlobalSettings().getSettings().copy().proxyPassThrough(false).build();
    target.updateGlobalSettings(newSettings);

    assertThat(
        testClient.getViaProxy(target.url("/something"), proxy.port()).statusCode(), is(404));
  }

  @Test
  void returnStubbedResponseOnPassThroughDisabled() {
    proxy.updateGlobalSettings(
        proxy.getGlobalSettings().getSettings().copy().proxyPassThrough(false).build());

    proxy.stubFor(get("/whatever").willReturn(ok("Default response")));

    WireMockResponse wireMockResponse =
        testClient.getViaProxy(target.url("/whatever"), proxy.port());

    assertThat(wireMockResponse.statusCode(), is(200));
    assertThat(wireMockResponse.content(), is("Default response"));
  }

  @Test
  void returnStubbedResponseOnPassThroughEnabled() {
    // by default, passProxyThrough is true/enabled
    target.stubFor(get("/whatever").willReturn(ok("Got it")));

    WireMockResponse wireMockResponse =
        testClient.getViaProxy(target.url("/whatever"), proxy.port());
    assertThat(wireMockResponse.statusCode(), is(200));
    assertThat(wireMockResponse.content(), is("Got it"));
  }

  @Test
  void disablingPassThroughDoesNotAffectReverseProxying() {
    proxy.updateGlobalSettings(
        proxy.getGlobalSettings().getSettings().copy().proxyPassThrough(false).build());

    proxy.stubFor(proxyAllTo(target.baseUrl()));

    target.stubFor(get("/whatever").willReturn(ok("Got it")));

    WireMockTestClient testClient = new WireMockTestClient(proxy.port());

    assertThat(testClient.get("/whatever").content(), is("Got it"));
  }

  @Nested
  class Disabled {

    @RegisterExtension
    public WireMockExtension wmWithoutBrowserProxy = WireMockExtension.newInstance().build();

    @Test
    public void browserProxyIsReportedAsFalseInRequestLogWhenDisabled() {
      int httpPort = wmWithoutBrowserProxy.getPort();
      WireMockTestClient testClient = new WireMockTestClient(httpPort);

      testClient.getViaProxy("http://whereever/whatever", httpPort);

      LoggedRequest request =
          wmWithoutBrowserProxy
              .findRequestsMatching(getRequestedFor(urlPathEqualTo("/whatever")).build())
              .getRequests()
              .get(0);
      assertThat(request.isBrowserProxyRequest(), is(false));
    }
  }
}
