/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;

import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class WebsocketHttpTriggerAcceptanceTest extends WebsocketAcceptanceTestBase {

  @Test
  void messageStubTriggeredByHttpStubSendsMessageToWebsocketChannel() {
    stubFor(
        get(urlPathEqualTo("/api/trigger-event"))
            .withId(UUID.fromString("11111111-2222-3333-4444-555555555555"))
            .willReturn(aResponse().withStatus(200).withBody("OK")));

    wireMockServer.messageStubFor(
        message()
            .withName("HTTP stub triggered message")
            .triggeredByHttpStub("11111111-2222-3333-4444-555555555555")
            .willTriggerActions(
                sendMessage("event triggered")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/ws-events")))));

    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = websocketUrl("/ws-events");
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    testClient.get("/api/trigger-event");

    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("event triggered"));
  }

  @Test
  void messageStubTriggeredByHttpRequestPatternSendsMessageToWebsocketChannel() {
    messageStubFor(
        message()
            .withName("HTTP request pattern triggered message")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathMatching("/api/notify/.*")))
            .willTriggerActions(
                sendMessage()
                    .withBody("notification received")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/ws-notifications")))));

    stubFor(
        get(urlPathMatching("/api/notify/.*"))
            .willReturn(aResponse().withStatus(200).withBody("Notified")));

    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = websocketUrl("/ws-notifications");
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    testClient.get("/api/notify/user123");

    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("notification received"));
  }

  @Test
  void messageStubTriggeredByHttpRequestPatternWorksWithoutMatchingHttpStub() {
    messageStubFor(
        message()
            .withName("HTTP request pattern triggered without stub")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/api/no-stub")))
            .willTriggerActions(
                sendMessage("request received")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/ws-no-stub")))));

    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = websocketUrl("/ws-no-stub");
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    testClient.get("/api/no-stub");

    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("request received"));
  }

  @Test
  void multipleWebsocketClientsReceiveMessageWhenHttpStubIsTriggered() {
    stubFor(
        post(urlPathEqualTo("/api/broadcast"))
            .withId(UUID.fromString("22222222-3333-4444-5555-666666666666"))
            .willReturn(aResponse().withStatus(200).withBody("Broadcast sent")));

    messageStubFor(
        message()
            .withName("Broadcast on HTTP stub")
            .triggeredByHttpStub("22222222-3333-4444-5555-666666666666")
            .willTriggerActions(
                sendMessage("broadcast message")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathMatching("/ws-broadcast.*")))));

    WebsocketTestClient wsClient1 = new WebsocketTestClient();
    WebsocketTestClient wsClient2 = new WebsocketTestClient();
    String wsUrl1 = websocketUrl("/ws-broadcast-1");
    String wsUrl2 = websocketUrl("/ws-broadcast-2");
    wsClient1.connect(wsUrl1);
    wsClient2.connect(wsUrl2);
    waitAtMost(5, SECONDS).until(wsClient1::isConnected);
    waitAtMost(5, SECONDS).until(wsClient2::isConnected);

    testClient.postWithBody("/api/broadcast", "trigger", "text/plain");

    waitAtMost(5, SECONDS).until(() -> wsClient1.getMessages().contains("broadcast message"));
    waitAtMost(5, SECONDS).until(() -> wsClient2.getMessages().contains("broadcast message"));
  }
}
