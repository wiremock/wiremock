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
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.AFTER_COMPLETE;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.AFTER_MATCH;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.BEFORE_MATCH;
import static com.github.tomakehurst.wiremock.extension.MessageServeEventListener.MessagePhase.MESSAGE_SENT;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.message.ChannelType.WEBSOCKET;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.MessageServeEventListener;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MessageServeEventListenerAcceptanceTest {

  WireMockServer wm;
  WireMockTestClient httpClient;
  WebsocketTestClient wsClient = new WebsocketTestClient();

  @AfterEach
  void cleanup() {
    if (wm != null) {
      wm.stop();
    }
  }

  void initWithListeners(MessageServeEventListener... listeners) {
    wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(listeners)).startServer();
    httpClient = new WireMockTestClient(wm.port());
    wm.stubFor(get(anyUrl()).atPriority(10).willReturn(aResponse().openWebsocketChannel()));
  }

  String websocketUrl(String path) {
    return "ws://localhost:" + wm.port() + path;
  }

  @Test
  void globalListenerIsTriggeredInAllPhasesForAMatchedIncomingMessage() {
    RecordingListener listener = new RecordingListener("recorder", true);
    initWithListeners(listener);

    wm.messageStubFor(
        message()
            .withBody(equalTo("hello"))
            .willTriggerActions(sendMessage("world").onOriginatingChannel()));

    wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "hello");

    await().atMost(5, SECONDS).until(listener::phases, hasSize(4));
    assertThat(
        listener.phases(), contains(BEFORE_MATCH, AFTER_MATCH, MESSAGE_SENT, AFTER_COMPLETE));

    MessageServeEvent beforeMatch = listener.eventIn(BEFORE_MATCH);
    assertThat(beforeMatch.isReceived(), is(true));
    assertThat(beforeMatch.getMessage().getBodyAsString(), is("hello"));
    assertThat(beforeMatch.getStubMapping(), nullValue());
    assertThat(beforeMatch.getWasMatched(), is(false));

    MessageServeEvent afterMatch = listener.eventIn(AFTER_MATCH);
    assertThat(afterMatch.getId(), is(beforeMatch.getId()));
    assertThat(afterMatch.getStubMapping(), notNullValue());
    assertThat(afterMatch.getWasMatched(), is(true));

    MessageServeEvent sent = listener.eventIn(MESSAGE_SENT);
    assertThat(sent.isSent(), is(true));
    assertThat(sent.getMessage().getBodyAsString(), is("world"));

    MessageServeEvent afterComplete = listener.eventIn(AFTER_COMPLETE);
    assertThat(afterComplete.getId(), is(beforeMatch.getId()));
    assertThat(afterComplete.getWasMatched(), is(true));
  }

  @Test
  void globalListenerIsTriggeredForAnUnmatchedIncomingMessage() {
    RecordingListener listener = new RecordingListener("recorder", true);
    initWithListeners(listener);

    wsClient.sendMessage(websocketUrl("/ws"), "unexpected");

    await().atMost(5, SECONDS).until(listener::phases, hasSize(3));
    assertThat(listener.phases(), contains(BEFORE_MATCH, AFTER_MATCH, AFTER_COMPLETE));
    assertThat(listener.eventIn(AFTER_COMPLETE).getWasMatched(), is(false));
    assertThat(listener.eventIn(AFTER_COMPLETE).getStubMapping(), nullValue());
  }

  @Test
  void globalListenerIsTriggeredWhenAnHttpStubTriggersAMessage() {
    RecordingListener listener = new RecordingListener("recorder", true);
    initWithListeners(listener);

    wm.stubFor(get(urlPathEqualTo("/api/trigger")).willReturn(ok()));
    wm.messageStubFor(
        message()
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/api/trigger")))
            .willTriggerActions(
                sendMessage("pushed")
                    .onChannelsMatching(newRequestPattern().withUrl(urlPathEqualTo("/ws-push")))));

    wsClient.connect(websocketUrl("/ws-push"));
    await().atMost(5, SECONDS).until(wsClient::isConnected);

    httpClient.get("/api/trigger");

    await().atMost(5, SECONDS).until(listener::phases, contains(MESSAGE_SENT));
    MessageServeEvent sent = listener.eventIn(MESSAGE_SENT);
    assertThat(sent.isSent(), is(true));
    assertThat(sent.getMessage().getBodyAsString(), is("pushed"));
    assertThat(sent.getChannelRequest().getUrl(), is("/ws-push"));
  }

  @Test
  void globalListenerIsTriggeredWhenAMessageIsSentViaTheAdminApi() {
    RecordingListener listener = new RecordingListener("recorder", true);
    initWithListeners(listener);

    wsClient.connect(websocketUrl("/ws-admin"));
    await().atMost(5, SECONDS).until(wsClient::isConnected);

    wm.sendChannelMessage(
        WEBSOCKET, newRequestPattern().withUrl(urlPathEqualTo("/ws-admin")).build(), "from-admin");

    await().atMost(5, SECONDS).until(listener::phases, contains(MESSAGE_SENT));
    assertThat(listener.eventIn(MESSAGE_SENT).getMessage().getBodyAsString(), is("from-admin"));
  }

  @Test
  void perStubListenerIsTriggeredOnlyForStubsThatReferenceItWithTheirParameters() {
    RecordingListener listener = new RecordingListener("reporter", false);
    initWithListeners(listener);

    wm.messageStubFor(
        message()
            .withBody(equalTo("a"))
            .withServeEventListener("reporter", Parameters.one("tag", "stub-a"))
            .willTriggerActions(sendMessage("A").onOriginatingChannel()));
    wm.messageStubFor(
        message()
            .withBody(equalTo("b"))
            .willTriggerActions(sendMessage("B").onOriginatingChannel()));

    wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "a");
    wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "b");

    await().atMost(5, SECONDS).until(listener::phases, hasSize(3));
    assertThat(listener.phases(), contains(AFTER_MATCH, MESSAGE_SENT, AFTER_COMPLETE));
    assertThat(listener.parameterValues("tag"), everyItem(is("stub-a")));
  }

  @Test
  void perStubListenerCanBeRestrictedToSpecificPhases() {
    RecordingListener listener = new RecordingListener("reporter", false);
    initWithListeners(listener);

    wm.messageStubFor(
        message()
            .withBody(equalTo("ping"))
            .withServeEventListener(AFTER_COMPLETE, "reporter", Parameters.one("tag", "done"))
            .withServeEventListener(MESSAGE_SENT, "reporter", Parameters.one("tag", "sent"))
            .willTriggerActions(sendMessage("pong").onOriginatingChannel()));

    wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "ping");

    await().atMost(5, SECONDS).until(listener::phases, hasSize(2));
    assertThat(listener.phases(), contains(MESSAGE_SENT, AFTER_COMPLETE));
    assertThat(listener.parameterValues("tag"), contains("sent", "done"));
  }

  @Test
  void perStubListenerIsNotTriggeredWhenNoStubReferencesIt() {
    RecordingListener listener = new RecordingListener("reporter", false);
    initWithListeners(listener);

    wm.messageStubFor(
        message()
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage("pong").onOriginatingChannel()));

    String response = wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "ping");

    assertThat(response, is("pong"));
    assertThat(listener.phases(), empty());
  }

  @Test
  void continuesWithNoEffectWhenANonExistentListenerIsReferenced() {
    initWithListeners();

    wm.messageStubFor(
        message()
            .withBody(equalTo("ping"))
            .withServeEventListener("does-not-exist", Parameters.one("tag", "ignored"))
            .willTriggerActions(sendMessage("pong").onOriginatingChannel()));

    String response = wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "ping");

    assertThat(response, is("pong"));
  }

  @Test
  void serveEventListenersAreSerialisedWithTheMessageStub() {
    initWithListeners();

    MessageStubMapping stub =
        wm.messageStubFor(
            message()
                .withBody(equalTo("ping"))
                .withServeEventListener(
                    AFTER_COMPLETE, "reporter", Parameters.one("counterName", "things"))
                .willTriggerActions(sendMessage("pong").onOriginatingChannel()));

    String json = httpClient.get("/__admin/message-mappings/" + stub.getId()).content();

    assertThat(
        json,
        jsonPartEquals(
            "serveEventListeners",
            // language=json
            """
            [
              {
                "name": "reporter",
                "phases": ["AFTER_COMPLETE"],
                "parameters": {
                  "counterName": "things"
                }
              }
            ]
            """));
  }

  @Test
  void serveEventListenersCanBeSpecifiedInJsonWhenCreatingAMessageStub() {
    RecordingListener listener = new RecordingListener("reporter", false);
    initWithListeners(listener);

    WireMockResponse response =
        httpClient.postJson(
            "/__admin/message-mappings",
            // language=json
            """
            {
              "trigger": {
                "message": {
                  "body": { "equalTo": "ping" }
                }
              },
              "actions": [
                {
                  "type": "send",
                  "message": { "body": "pong" },
                  "channelTarget": { "type": "originating" }
                }
              ],
              "serveEventListeners": [
                {
                  "name": "reporter",
                  "phases": ["AFTER_MATCH", "AFTER_COMPLETE"],
                  "parameters": { "tag": "from-json" }
                }
              ]
            }
            """);
    assertThat(response.content(), response.statusCode(), is(201));

    MessageStubMapping created = Json.read(response.content(), MessageStubMapping.class);
    assertThat(created.getServeEventListeners(), hasSize(1));
    assertThat(created.getServeEventListeners().get(0).getName(), is("reporter"));

    wsClient.sendMessageAndWaitForResponse(websocketUrl("/ws"), "ping");

    await().atMost(5, SECONDS).until(listener::phases, hasSize(2));
    assertThat(listener.phases(), containsInAnyOrder(AFTER_MATCH, AFTER_COMPLETE));
    assertThat(listener.phases(), not(contains(MESSAGE_SENT)));
    assertThat(listener.parameterValues("tag"), everyItem(is("from-json")));
  }

  record Invocation(
      MessageServeEventListener.MessagePhase phase,
      MessageServeEvent event,
      Parameters parameters) {}

  static class RecordingListener implements MessageServeEventListener {

    private final String name;
    private final boolean global;
    private final List<Invocation> invocations = new CopyOnWriteArrayList<>();

    RecordingListener(String name, boolean global) {
      this.name = name;
      this.global = global;
    }

    @Override
    public void onEvent(
        MessagePhase messagePhase, MessageServeEvent messageServeEvent, Parameters parameters) {
      invocations.add(new Invocation(messagePhase, messageServeEvent, parameters));
    }

    @Override
    public boolean applyGlobally() {
      return global;
    }

    @Override
    public String getName() {
      return name;
    }

    List<MessagePhase> phases() {
      return invocations.stream().map(Invocation::phase).toList();
    }

    MessageServeEvent eventIn(MessagePhase phase) {
      return invocations.stream()
          .filter(invocation -> invocation.phase() == phase)
          .findFirst()
          .orElseThrow()
          .event();
    }

    List<String> parameterValues(String key) {
      return invocations.stream()
          .map(invocation -> invocation.parameters().getString(key))
          .toList();
    }
  }
}
