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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MessageTemplatingAcceptanceTest {

  WireMockServer wm;

  @AfterEach
  void cleanup() {
    if (wm != null) {
      wm.stop();
    }
  }

  @Test
  void templatesMessageBodyWithIncomingMessageContent() {
    wm = new WireMockServer(wireMockConfig().dynamicPort()).startServer();

    wm.messageStubFor(
        message()
            .withName("Echo template stub")
            .withBody(matching(".*"))
            .willTriggerActions(sendMessage("You said: {{message.body}}").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/template-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "hello world");
    assertThat(response, is("You said: hello world"));
  }

  @Test
  void templatesMessageBodyWithRandomValue() {
    wm = new WireMockServer(wireMockConfig().dynamicPort()).startServer();

    wm.messageStubFor(
        message()
            .withName("Random template stub")
            .withBody(equalTo("random"))
            .willTriggerActions(
                sendMessage("Random: {{randomValue length=5 type='ALPHANUMERIC'}}")
                    .onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/random-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "random");
    assertThat(response.startsWith("Random: "), is(true));
    assertThat(response.length(), is("Random: ".length() + 5));
  }

  @Test
  void templatesMessageBodyWithJsonPath() {
    wm = new WireMockServer(wireMockConfig().dynamicPort()).startServer();

    wm.messageStubFor(
        message()
            .withName("JsonPath template stub")
            .withBody(matchingJsonPath("$.name"))
            .willTriggerActions(
                sendMessage("Hello {{jsonPath message.body '$.name'}}!").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/jsonpath-test");

    String response =
        testClient.sendMessageAndWaitForResponse(url, "{\"name\": \"Alice\", \"age\": 30}");
    assertThat(response, is("Hello Alice!"));
  }

  @Test
  void templatesMessageBodyWithInitiatingRequestData() {
    wm = new WireMockServer(wireMockConfig().dynamicPort()).startServer();

    wm.messageStubFor(
        message()
            .withName("Request data template stub")
            .onWebsocketChannelFromRequestMatching(
                newRequestPattern().withUrl(urlPathMatching("/my.*")))
            .withBody(equalTo("info"))
            .willTriggerActions(sendMessage("Path: {{request.path}}").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/my-channel");

    String response = testClient.sendMessageAndWaitForResponse(url, "info");
    assertThat(response, is("Path: /my-channel"));
  }

  @Test
  void templatesMessageBodyWithMultipleHelpers() {
    wm = new WireMockServer(wireMockConfig().dynamicPort()).startServer();

    wm.messageStubFor(
        message()
            .withName("Multi-helper template stub")
            .willTriggerActions(
                sendMessage("Upper: {{upper message.body}}, Length: {{size message.body}}")
                    .onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/multi-helper-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response, is("Upper: TEST, Length: 4"));
  }

  private String websocketUrl(String path) {
    return "ws://localhost:" + wm.port() + path;
  }
}
