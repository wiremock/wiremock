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
import static com.github.tomakehurst.wiremock.client.WireMock.getAllMessageServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageJournal;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient.SseStreamClient;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient.SseStreamClient.SseEvent;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SseAcceptanceTest extends AcceptanceTestBase {

  private final WireMockTestClient testClient = new WireMockTestClient(wireMockServer.port());

  @BeforeEach
  void setup() {
    WireMock.reset();
  }

  @AfterEach
  void cleanup() {
    WireMock.resetMessageStubs();
  }

  private String serverUrl(String path) {
    return "http://localhost:" + wireMockServer.port() + path;
  }

  @Test
  void stubWithAcceptEventStreamReturnsEventStreamContentType() throws Exception {
    stubFor(get(urlEqualTo("/events")).willReturn(aResponse().withAcceptEventStream()));

    try (SseStreamClient sse = new SseStreamClient(serverUrl("/events"))) {
      assertEquals(200, sse.connect());
      assertThat(sse.header("content-type"), containsString("text/event-stream"));
    }
  }

  @Test
  void stubWithoutAcceptEventStreamServesRegularResponse() {
    stubFor(get(urlEqualTo("/events")).willReturn(ok("plain body")));

    WireMockResponse response = testClient.get("/events");

    assertThat(response.statusCode(), is(200));
    assertThat(response.content(), is("plain body"));
  }

  @Test
  void noStubGivesNotFound() {
    WireMockResponse response = testClient.get("/events");

    assertThat(response.statusCode(), is(404));
  }

  @Test
  void acceptEventStreamRejectsBody() {
    assertThrows(
        IllegalStateException.class,
        () -> aResponse().withAcceptEventStream().withBody("body").build());
  }

  @Test
  void acceptEventStreamRejectsWebSocket() {
    assertThrows(
        IllegalStateException.class,
        () -> aResponse().withAcceptEventStream().withAcceptWebSocket().build());
  }

  @Test
  void sseStreamReceivesInitialComment() throws Exception {
    stubFor(get(urlEqualTo("/init")).willReturn(aResponse().withAcceptEventStream()));

    try (SseStreamClient sse = new SseStreamClient(serverUrl("/init"))) {
      assertEquals(200, sse.connect());
      SseEvent event = sse.awaitEvent(e -> e.getComment() != null);
      assertNotNull(event);
      assertThat(event.getComment(), is("ok"));
    }
  }

  @Test
  void sseStreamReceivesEventFromHighestPriorityHttpTrigger() throws Exception {
    stubFor(get(urlEqualTo("/multi-stream")).willReturn(aResponse().withAcceptEventStream()));

    stubFor(get(urlPathTemplate("/multi-trigger/{number}")).willReturn(ok("triggered")));

    messageStubFor(
        message()
            .withName("Multi event trigger 1")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/multi-trigger/1")))
            .willTriggerActions(
                sendMessage("event-1")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/multi-stream")))));

    messageStubFor(
        message()
            .withName("Multi event trigger 2")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/multi-trigger/2")))
            .willTriggerActions(
                sendMessage("event-2")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/multi-stream")))));

    messageStubFor(
        message()
            .withName("Multi event trigger 3")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/multi-trigger/3")))
            .willTriggerActions(
                sendMessage("event-3")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/multi-stream")))));

    try (SseStreamClient sse = new SseStreamClient(serverUrl("/multi-stream"))) {
      assertEquals(200, sse.connect());

      testClient.get("/multi-trigger/1");
      SseEvent event1 = sse.awaitEvent(e -> "event-1".equals(e.getData()));
      assertNotNull(event1);

      testClient.get("/multi-trigger/2");
      SseEvent event2 = sse.awaitEvent(e -> "event-2".equals(e.getData()));
      assertNotNull(event2);

      testClient.get("/multi-trigger/3");
      SseEvent event3 = sse.awaitEvent(e -> "event-3".equals(e.getData()));
      assertNotNull(event3);
    }
  }

  @Test
  void channelIsRemovedOnSseDisconnect() throws Exception {
    stubFor(get(urlEqualTo("/cleanup-stream")).willReturn(aResponse().withAcceptEventStream()));
    stubFor(get(urlEqualTo("/cleanup-trigger")).willReturn(ok("triggered")));

    messageStubFor(
        message()
            .withName("Cleanup trigger")
            .triggeredByHttpRequest(
                newRequestPattern().withUrl(urlPathEqualTo("/cleanup-trigger")))
            .willTriggerActions(
                sendMessage("disconnect-payload")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathEqualTo("/cleanup-stream")))));

    try (SseStreamClient sse = new SseStreamClient(serverUrl("/cleanup-stream"))) {
      assertEquals(200, sse.connect());

      resetMessageJournal();
      testClient.get("/cleanup-trigger");
      SseEvent event = sse.awaitEvent(e -> e.hasData());
      assertNotNull(event);

      MessageServeEvent sentEvent =
          getAllMessageServeEvents().stream()
              .filter(MessageServeEvent::isSent)
              .findFirst()
              .orElseThrow();
      UUID channelId = sentEvent.getChannelId();
      assertThat(WireMock.getMessageChannel(channelId).isPresent(), is(true));
    }

    Thread.sleep(200);
    testClient.get("/cleanup-trigger");
    UUID channelId =
        getAllMessageServeEvents().stream()
            .filter(MessageServeEvent::isSent)
            .findFirst()
            .orElseThrow()
            .getChannelId();
    assertThat(WireMock.getMessageChannel(channelId).isPresent(), is(false));
  }
}
