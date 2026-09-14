/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WebsocketStubMatchingAcceptanceTest extends WebsocketAcceptanceTestBase {

  private final WireMockTestClient testClient = new WireMockTestClient(wireMockServer.port());

  @BeforeEach
  void resetCatchAllStub() {
    WireMock.reset();
  }

  @Test
  void stubWithoutAcceptWebSocketWinsOverDefaultWebSocketHandling() {
    stubFor(get(urlEqualTo("/ws")).willReturn(ok("stub body")));

    WireMockResponse response =
        testClient.get(
            "/ws",
            new TestHttpHeader("Upgrade", "websocket"),
            new TestHttpHeader("Connection", "Upgrade"),
            new TestHttpHeader("Sec-WebSocket-Version", "13"),
            new TestHttpHeader("Sec-WebSocket-Key", "dGhlIHNhbXBsZSBub25jZQ=="));

    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), containsString("stub body"));
  }

  @Test
  void noStubDoesNotUpgradeWebSocket() {
    var wsClient = new WebsocketTestClient();
    String url = websocketUrl("/ws");

    assertThrows(RuntimeException.class, () -> wsClient.withWebsocketSession(url, session -> null));
  }

  @Test
  void stubWithAcceptWebSocketPerformsUpgrade() {
    stubFor(get(urlEqualTo("/accept-ws")).willReturn(aResponse().withAcceptWebSocket()));

    WebsocketTestClient wsClient = new WebsocketTestClient();
    String url = websocketUrl("/accept-ws");

    wsClient.withWebsocketSession(
        url,
        session -> {
          assertThat(session.isOpen(), is(true));
          return null;
        });
  }

  @Test
  void acceptWebSocketRejectsBody() {
    assertThrows(
        IllegalStateException.class,
        () -> aResponse().withAcceptWebSocket().withBody("body").build());
  }

  @Test
  void acceptWebSocketRejectsProxy() {
    assertThrows(
        IllegalStateException.class,
        () -> aResponse().withAcceptWebSocket().proxiedFrom("http://example.com").build());
  }

  @Test
  void acceptWebSocketRejectsFault() {
    assertThrows(
        IllegalStateException.class,
        () ->
            aResponse()
                .withAcceptWebSocket()
                .withFault(com.github.tomakehurst.wiremock.http.Fault.EMPTY_RESPONSE)
                .build());
  }
}
