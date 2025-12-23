/*
 * Copyright (C) 2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.findAllMessageEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllMessageServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsForStubsMatchingMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageJournal;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageStubs;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verifyMessageEvent;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.model.SendChannelMessageResult;
import com.github.tomakehurst.wiremock.common.entity.BinaryEntityDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WebsocketAcceptanceTest extends AcceptanceTestBase {

  @AfterEach
  void resetMessageStubsAfterTest() {
    resetMessageStubs();
  }

  @Test
  void websocketConnectionCanBeEstablished() {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/notifications";
    testClient.withWebsocketSession(
        url,
        session -> {
          assertThat(session.isOpen(), is(true));
          return null;
        });
  }

  @Test
  void canSendMessageToWebsocketViaAdminApi() {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/notifications";
    testClient.withWebsocketSession(
        url,
        session -> {
          RequestPattern pattern = newRequestPattern().withUrl("/notifications").build();
          SendChannelMessageResult result1 =
              wireMockServer.sendWebSocketMessage(pattern, "Hello WebSocket!");
          SendChannelMessageResult result2 =
              wireMockServer.sendWebSocketMessage(pattern, "Second message");

          assertThat(result1.getChannelsMessaged(), is(1));
          assertThat(result2.getChannelsMessaged(), is(1));

          return null;
        });

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().size() == 2);
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("Hello WebSocket!"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("Second message"));
  }

  @Test
  void canSendMessageToMultipleWebsocketConnections() {
    WebsocketTestClient testClient1 = new WebsocketTestClient();
    WebsocketTestClient testClient2 = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/broadcast";

    testClient1.withWebsocketSession(
        url,
        session1 ->
            testClient2.withWebsocketSession(
                url,
                session2 -> {
                  RequestPattern pattern = newRequestPattern().withUrl("/broadcast").build();
                  SendChannelMessageResult result =
                      wireMockServer.sendWebSocketMessage(pattern, "Broadcast message");

                  assertThat(result.getChannelsMessaged(), is(2));

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClient1.getMessages().contains("Broadcast message"));
    waitAtMost(5, SECONDS).until(() -> testClient2.getMessages().contains("Broadcast message"));
  }

  @Test
  void requestPatternMatchingFiltersWebsocketChannels() {
    WebsocketTestClient testClientA = new WebsocketTestClient();
    WebsocketTestClient testClientB = new WebsocketTestClient();
    String urlA = "ws://localhost:" + wireMockServer.port() + "/channel-a";
    String urlB = "ws://localhost:" + wireMockServer.port() + "/channel-b";

    testClientA.withWebsocketSession(
        urlA,
        sessionA ->
            testClientB.withWebsocketSession(
                urlB,
                sessionB -> {
                  RequestPattern patternA = newRequestPattern().withUrl("/channel-a").build();
                  SendChannelMessageResult result =
                      wireMockServer.sendWebSocketMessage(patternA, "Message for A");

                  assertThat(result.getChannelsMessaged(), is(1));

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClientA.getMessages().contains("Message for A"));
    // Verify client B did not receive the message
    assertThat(testClientB.getMessages().isEmpty(), is(true));
  }

  @Test
  void urlPatternMatchingWorksForWebsocketChannels() {
    WebsocketTestClient testClient1 = new WebsocketTestClient();
    WebsocketTestClient testClient2 = new WebsocketTestClient();
    String url1 = "ws://localhost:" + wireMockServer.port() + "/events/user1";
    String url2 = "ws://localhost:" + wireMockServer.port() + "/events/user2";

    testClient1.withWebsocketSession(
        url1,
        session1 ->
            testClient2.withWebsocketSession(
                url2,
                session2 -> {
                  RequestPattern pattern =
                      newRequestPattern().withUrl(urlPathMatching("/events/.*")).build();
                  SendChannelMessageResult result =
                      wireMockServer.sendWebSocketMessage(pattern, "Event notification");

                  assertThat(result.getChannelsMessaged(), is(2));

                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> testClient1.getMessages().contains("Event notification"));
    waitAtMost(5, SECONDS).until(() -> testClient2.getMessages().contains("Event notification"));
  }

  @Test
  void channelIsRemovedWhenWebsocketCloses() throws Exception {
    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/temp-channel";

    testClient.withWebsocketSession(
        url,
        session -> {
          RequestPattern pattern = newRequestPattern().withUrl("/temp-channel").build();
          SendChannelMessageResult result1 =
              wireMockServer.sendWebSocketMessage(pattern, "Before close");
          assertThat(result1.getChannelsMessaged(), is(1));
          return null;
        });

    // Give the server a moment to process the close
    Thread.sleep(100);

    // Verify channel is removed
    RequestPattern pattern = newRequestPattern().withUrl("/temp-channel").build();
    SendChannelMessageResult result2 = wireMockServer.sendWebSocketMessage(pattern, "After close");
    assertThat(result2.getChannelsMessaged(), is(0));
  }

  // Message stub mapping tests

  @Test
  void messageStubMappingRespondsToOriginatingChannel() {
    // Register a message stub that responds to the originating channel
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Echo stub")
            .withBody(equalTo("ping"))
            .triggersAction(SendMessageAction.toOriginatingChannel("pong"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/echo";

    String response = testClient.sendMessageAndWaitForResponse(url, "ping");
    assertThat(response, is("pong"));
  }

  @Test
  void messageStubMappingMatchesWithRegexPattern() {
    // Register a message stub with regex pattern matching
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Greeting stub")
            .withBody(matching("hello.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("hi there!"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/greet";

    String response = testClient.sendMessageAndWaitForResponse(url, "hello world");
    assertThat(response, is("hi there!"));
  }

  @Test
  void messageStubMappingWithChannelPatternMatchesSpecificChannels() {
    RequestPattern channelPattern = newRequestPattern().withUrl("/vip-channel").build();
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("VIP stub")
            .onChannelFromRequestMatching(channelPattern)
            .withBody(equalTo("request"))
            .triggersAction(SendMessageAction.toOriginatingChannel("VIP response"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient vipClient = new WebsocketTestClient();
    WebsocketTestClient regularClient = new WebsocketTestClient();
    String vipUrl = "ws://localhost:" + wireMockServer.port() + "/vip-channel";
    String regularUrl = "ws://localhost:" + wireMockServer.port() + "/regular-channel";

    // VIP channel should get response
    String vipResponse = vipClient.sendMessageAndWaitForResponse(vipUrl, "request");
    assertThat(vipResponse, is("VIP response"));

    // Regular channel should not get response (no matching stub)
    regularClient.sendMessage(regularUrl, "request");
    // Wait a bit to ensure no response comes
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThat(regularClient.getMessages().isEmpty(), is(true));
  }

  @Test
  void messageStubMappingPriorityDeterminesMatchOrder() {
    // Register two stubs with different priorities
    MessageStubMapping lowPriorityStub =
        MessageStubMapping.builder()
            .withName("Low priority stub")
            .withPriority(10)
            .withBody(matching(".*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("low priority"))
            .build();

    MessageStubMapping highPriorityStub =
        MessageStubMapping.builder()
            .withName("High priority stub")
            .withPriority(1)
            .withBody(equalTo("test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("high priority"))
            .build();

    wireMockServer.addMessageStubMapping(lowPriorityStub);
    wireMockServer.addMessageStubMapping(highPriorityStub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/priority-test";

    // "test" should match high priority stub
    String response = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response, is("high priority"));
  }

  @Test
  void messageStubMappingCanSendToMatchingChannels() {
    // Register a stub that broadcasts to all channels matching a pattern
    RequestPattern targetPattern =
        newRequestPattern().withUrl(urlPathMatching("/broadcast/.*")).build();
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Broadcast stub")
            .withBody(equalTo("broadcast"))
            .triggersAction(
                SendMessageAction.toMatchingChannels("broadcast message", targetPattern))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient senderClient = new WebsocketTestClient();
    WebsocketTestClient receiverClient1 = new WebsocketTestClient();
    WebsocketTestClient receiverClient2 = new WebsocketTestClient();
    String senderUrl = "ws://localhost:" + wireMockServer.port() + "/sender";
    String receiverUrl1 = "ws://localhost:" + wireMockServer.port() + "/broadcast/user1";
    String receiverUrl2 = "ws://localhost:" + wireMockServer.port() + "/broadcast/user2";

    // Connect receivers first
    receiverClient1.withWebsocketSession(
        receiverUrl1,
        session1 ->
            receiverClient2.withWebsocketSession(
                receiverUrl2,
                session2 -> {
                  // Send broadcast message from sender
                  senderClient.sendMessage(senderUrl, "broadcast");
                  return null;
                }));

    // Wait for messages to be received
    waitAtMost(5, SECONDS).until(() -> receiverClient1.getMessages().contains("broadcast message"));
    waitAtMost(5, SECONDS).until(() -> receiverClient2.getMessages().contains("broadcast message"));
  }

  @Test
  void messageStubMappingCanBeRemoved() {
    // Register a stub
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Removable stub")
            .withBody(equalTo("test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/remove-test";

    // Verify stub works
    String response1 = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response1, is("response"));

    // Remove the stub
    wireMockServer.removeMessageStubMapping(stub.getId());
    testClient.clearMessages();

    // Verify stub no longer works
    testClient.sendMessage(url, "test");
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThat(testClient.getMessages().isEmpty(), is(true));
  }

  @Test
  void messageStubMappingWithMultipleActions() {
    // Register a stub with multiple actions
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Multi-action stub")
            .withBody(equalTo("multi"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response1"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response2"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/multi-action";

    testClient.sendMessage(url, "multi");

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("response1"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("response2"));
    assertThat(testClient.getMessages().size(), is(2));
  }

  @Test
  void messageStubMappingCanBeCreatedUsingDsl() {
    // Register a message stub using the DSL
    messageStubFor(
        message()
            .withName("DSL stub")
            .withBody(equalTo("hello"))
            .willTriggerActions(sendMessage("world").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/dsl-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "hello");
    assertThat(response, is("world"));
  }

  @Test
  void messageStubMappingDslSupportsMultipleActions() {
    // Register a message stub with multiple actions using the DSL
    messageStubFor(
        message()
            .onChannelFromRequestMatching(newRequestPattern().withUrl("/dsl-multi"))
            .withName("DSL multi-action stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage("first").onOriginatingChannel(),
                sendMessage("second").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/dsl-multi";

    testClient.sendMessage(url, "trigger");

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("first"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("second"));
    assertThat(testClient.getMessages().size(), is(2));
  }

  @Test
  void messageStubMappingDslSupportsBroadcastToMatchingChannels() {
    // Register a message stub that broadcasts to matching channels using the DSL
    messageStubFor(
        message()
            .onChannelFromRequestMatching("/dsl-broadcast")
            .withName("DSL broadcast stub")
            .withBody(equalTo("broadcast"))
            .willTriggerActions(
                sendMessage("broadcasted")
                    .onChannelsMatching(newRequestPattern().withUrl("/dsl-broadcast"))));

    WebsocketTestClient client1 = new WebsocketTestClient();
    WebsocketTestClient client2 = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/dsl-broadcast";

    // Connect both clients
    client1.connect(url);
    client2.connect(url);

    waitAtMost(5, SECONDS).until(client1::isConnected);
    waitAtMost(5, SECONDS).until(client2::isConnected);

    // Send message from client1
    client1.sendMessage("broadcast");

    // Both clients should receive the broadcast
    waitAtMost(5, SECONDS).until(() -> client1.getMessages().contains("broadcasted"));
    waitAtMost(5, SECONDS).until(() -> client2.getMessages().contains("broadcasted"));
  }

  // Message Journal tests

  @Test
  void messageJournalRecordsReceivedMessages() {
    // Register a message stub
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Journal test stub")
            .withBody(equalTo("journal-test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    messageStubFor(stub);

    // Reset the message journal
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/journal-test";

    testClient.sendMessageAndWaitForResponse(url, "journal-test");

    // Verify the message was recorded in the journal
    var events = getAllMessageServeEvents();
    assertThat(events.size(), is(1));

    var event = events.get(0);
    assertThat(event.getMessage(), is("journal-test"));
    assertThat(event.getWasMatched(), is(true));
    assertThat(event.getStubMapping().getName(), is("Journal test stub"));
  }

  @Test
  void messageJournalRecordsUnmatchedMessages() {
    // Reset the message journal
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/unmatched-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);
    testClient.sendMessage("unmatched-message");

    // Wait for the message to be processed
    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    // Verify the unmatched message was recorded
    var events = getAllMessageServeEvents();
    assertThat(events.size(), is(1));

    var event = events.get(0);
    assertThat(event.getMessage(), is("unmatched-message"));
    assertThat(event.getWasMatched(), is(false));
  }

  @Test
  void canCountMessageEventsMatchingPredicate() {
    // Register a message stub
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Count test stub")
            .withBody(matching("count-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("counted"))
            .build();
    messageStubFor(stub);

    // Reset the message journal
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/count-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("count-1");
    testClient.sendMessage("count-2");
    testClient.sendMessage("count-3");

    // Wait for all messages to be processed
    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 3);

    // Verify count
    int count =
        findAllMessageEvents(messagePattern().withBody(matching("count-.*")).build()).size();
    assertThat(count, is(3));
  }

  @Test
  void canFindMessageEventsMatchingPredicate() {
    // Register a message stub
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Find test stub")
            .withBody(matching("find-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("found"))
            .build();
    messageStubFor(stub);

    // Reset the message journal
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/find-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("find-alpha");
    testClient.sendMessage("find-beta");

    // Wait for all messages to be processed
    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 2);

    // Find specific message
    var events = findAllMessageEvents(messagePattern().withBody(equalTo("find-alpha")).build());
    assertThat(events.size(), is(1));
    assertThat(events.get(0).getMessage(), is("find-alpha"));
  }

  @Test
  void canResetMessageJournal() {
    // Reset the message journal
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/reset-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);
    testClient.sendMessage("before-reset");

    // Wait for the message to be processed
    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    // Reset the journal
    resetMessageJournal();

    // Verify journal is empty
    var events = getAllMessageServeEvents();
    assertThat(events.size(), is(0));
  }

  @Test
  void canVerifyAtLeastOneMessageEventMatchesPredicate() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Verify test stub")
            .withBody(matching("verify-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("verified"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/verify-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("verify-one");
    testClient.sendMessage("verify-two");

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 2);

    verifyMessageEvent(messagePattern().withBody(equalTo("verify-one")).build());
    verifyMessageEvent(messagePattern().withBody(matching("verify-.*")).build());
  }

  @Test
  void canVerifyExactCountOfMessageEvents() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Verify count stub")
            .withBody(matching("count-verify-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("counted"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/verify-count-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("count-verify-1");
    testClient.sendMessage("count-verify-2");
    testClient.sendMessage("count-verify-3");

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 3);

    verifyMessageEvent(3, messagePattern().withBody(matching("count-verify-.*")).build());
    verifyMessageEvent(1, messagePattern().withBody(equalTo("count-verify-2")).build());
  }

  @Test
  void canVerifyMessageEventsWithCountMatchingStrategy() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Verify strategy stub")
            .withBody(matching("strategy-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("strategized"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/verify-strategy-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("strategy-a");
    testClient.sendMessage("strategy-b");

    MessagePattern strategyPattern = messagePattern().withBody(matching("strategy-.*")).build();

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 2);

    verifyMessageEvent(moreThanOrExactly(1), strategyPattern);
    verifyMessageEvent(lessThan(5), strategyPattern);
    verifyMessageEvent(exactly(2), strategyPattern);
  }

  @Test
  void verifyMessageEventThrowsWhenNoMatchingEvents() {
    resetMessageJournal();

    org.junit.jupiter.api.Assertions.assertThrows(
        com.github.tomakehurst.wiremock.client.VerificationException.class,
        () -> verifyMessageEvent(messagePattern().withBody(equalTo("non-existent")).build()));
  }

  @Test
  void verifyMessageEventThrowsWhenCountDoesNotMatch() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Verify fail stub")
            .withBody(matching("fail-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("failed"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/verify-fail-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("fail-1");
    testClient.sendMessage("fail-2");

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 2);

    org.junit.jupiter.api.Assertions.assertThrows(
        com.github.tomakehurst.wiremock.client.VerificationException.class,
        () -> verifyMessageEvent(5, messagePattern().withBody(matching("fail-.*")).build()));
  }

  @Test
  void canGetSingleMessageServeEventById() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Get single event stub")
            .withBody(matching("get-single-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("got it"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/get-single-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("get-single-event");

    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    MessageServeEvent event = getAllMessageServeEvents().get(0);
    MessageServeEvent retrievedEvent = getMessageServeEvent(event.getId());

    assertThat(retrievedEvent.getId(), is(event.getId()));
    assertThat(retrievedEvent.getMessage(), is(event.getMessage()));
  }

  @Test
  void canRemoveSingleMessageServeEventById() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Remove single event stub")
            .withBody(matching("remove-single-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("removed"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/remove-single-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("remove-single-1");
    testClient.sendMessage("remove-single-2");

    waitAtMost(5, SECONDS).until(() -> getAllMessageServeEvents().size() >= 2);

    int initialCount = getAllMessageServeEvents().size();
    MessageServeEvent eventToRemove = getAllMessageServeEvents().get(0);

    removeMessageServeEvent(eventToRemove.getId());

    assertThat(getAllMessageServeEvents().size(), is(initialCount - 1));
  }

  @Test
  void canRemoveMessageServeEventsMatchingPattern() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Remove by pattern stub")
            .withBody(matching("remove-pattern-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("removed"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/remove-pattern-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("remove-pattern-1");
    testClient.sendMessage("remove-pattern-2");
    testClient.sendMessage("other-message");

    waitAtMost(5, SECONDS).until(() -> getAllMessageServeEvents().size() >= 3);

    assertThat(getAllMessageServeEvents().size(), is(3));

    var result =
        removeMessageServeEventsMatching(
            messagePattern().withBody(matching("remove-pattern-.*")).build());

    assertThat(result.getMessageServeEvents().size(), is(2));
    assertThat(getAllMessageServeEvents().size(), is(1));
    assertThat(getAllMessageServeEvents().get(0).getMessage(), is("other-message"));
  }

  @Test
  void canRemoveMessageServeEventsForStubsMatchingMetadata() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Metadata stub")
            .withBody(matching("metadata-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/metadata-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("metadata-a");

    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    assertThat(getAllMessageServeEvents().size(), is(1));

    var result =
        removeMessageServeEventsForStubsMatchingMetadata(
            com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath(
                "$.nonexistent", equalTo("value")));

    assertThat(result.getMessageServeEvents().size(), is(0));
    assertThat(getAllMessageServeEvents().size(), is(1));
  }

  // TextEntityDefinition resolution tests

  @Test
  void textEntityDefinitionWithStringDataResolvesToString() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("String data stub")
            .withBody(equalTo("trigger"))
            .triggersAction(sendMessage().withBody("hello world").onOriginatingChannel())
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/string-data-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, is("hello world"));
  }

  @Test
  void textEntityDefinitionWithObjectDataSerializesToJson() {
    Map<String, Object> objectData = Map.of("name", "John", "age", 30);
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Object data stub")
            .withBody(equalTo("trigger"))
            .triggersAction(sendMessage().withBody(objectData).onOriginatingChannel())
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/object-data-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, jsonEquals("{\"name\":\"John\",\"age\":30}"));
  }

  @Test
  void textEntityDefinitionWithDataStoreResolvesFromStore() {
    wireMockServer
        .getOptions()
        .getStores()
        .getObjectStore("testStore")
        .put("testKey", "stored value");

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Store data stub")
            .withBody(equalTo("trigger"))
            .triggersAction(
                sendMessage().withBodyFromStore("testStore", "testKey").onOriginatingChannel())
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/store-data-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, is("stored value"));
  }

  @Test
  void textEntityDefinitionWithDataStoreResolvesObjectFromStoreAsJson() {
    Map<String, Object> storedObject = Map.of("key", "value", "number", 42);
    wireMockServer
        .getOptions()
        .getStores()
        .getObjectStore("objectStore")
        .put("objectKey", storedObject);

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Store object data stub")
            .withBody(equalTo("trigger"))
            .triggersAction(
                sendMessage().withBodyFromStore("objectStore", "objectKey").onOriginatingChannel())
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/store-object-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "trigger");
    assertThat(response, jsonEquals("{\"number\":42,\"key\":\"value\"}"));
  }

  // Binary message matching tests

  @Test
  void binaryEqualToCanBeUsedToMatchMessageBody() {
    byte[] expectedBytes = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05};

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Binary matching stub")
            .withBody(binaryEqualTo(expectedBytes))
            .triggersAction(SendMessageAction.toOriginatingChannel("binary matched!"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/binary-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendBinaryMessage(expectedBytes);

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("binary matched!"));
    assertThat(testClient.getMessages().contains("binary matched!"), is(true));
  }

  @Test
  void binaryMessageCanBeSentAsResponse() {
    byte[] responseBytes = new byte[] {0x0A, 0x0B, 0x0C, 0x0D, 0x0E};

    BinaryEntityDefinition binaryBody =
        BinaryEntityDefinition.aBinaryMessage().withBody(responseBytes).build();

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Binary response stub")
            .withBody(equalTo("send-binary"))
            .triggersAction(SendMessageAction.toOriginatingChannel(binaryBody))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/binary-response-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("send-binary");

    waitAtMost(5, SECONDS).until(() -> !testClient.getBinaryMessages().isEmpty());
    assertThat(testClient.getBinaryMessages().get(0), is(responseBytes));
  }

  @Test
  void binaryMessageFromDataStoreCanBeSentAsResponse() {
    byte[] storedBytes = new byte[] {0x10, 0x20, 0x30, 0x40, 0x50};
    wireMockServer.getOptions().getStores().getObjectStore("binaryStore").put("binaryKey", storedBytes);

    BinaryEntityDefinition binaryBody =
        BinaryEntityDefinition.aBinaryMessage()
            .withDataStore("binaryStore")
            .withDataRef("binaryKey")
            .build();

    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Binary from store stub")
            .withBody(equalTo("send-stored-binary"))
            .triggersAction(SendMessageAction.toOriginatingChannel(binaryBody))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/binary-store-test";

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("send-stored-binary");

    waitAtMost(5, SECONDS).until(() -> !testClient.getBinaryMessages().isEmpty());
    assertThat(testClient.getBinaryMessages().get(0), is(storedBytes));
  }

  // HTTP stub trigger tests

  @Test
  void messageStubTriggeredByHttpStubSendsMessageToWebsocketChannel() {
    // Create an HTTP stub
    stubFor(
        get(urlPathEqualTo("/api/trigger-event"))
            .withId(UUID.fromString("11111111-2222-3333-4444-555555555555"))
            .willReturn(aResponse().withStatus(200).withBody("OK")));

    // Create a message stub that triggers when the HTTP stub is served
    MessageStubMapping messageStub =
        MessageStubMapping.builder()
            .withName("HTTP stub triggered message")
            .triggeredByHttpStub("11111111-2222-3333-4444-555555555555")
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "event triggered",
                    newRequestPattern().withUrl(urlPathEqualTo("/ws-events")).build()))
            .build();
    wireMockServer.addMessageStubMapping(messageStub);

    // Connect a WebSocket client
    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = "ws://localhost:" + wireMockServer.port() + "/ws-events";
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    // Make an HTTP request that matches the stub
    testClient.get("/api/trigger-event");

    // Verify the WebSocket client received the message
    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("event triggered"));
  }

  @Test
  void messageStubTriggeredByHttpRequestPatternSendsMessageToWebsocketChannel() {
    // Create a message stub that triggers on any HTTP request matching a pattern
    MessageStubMapping messageStub =
        MessageStubMapping.builder()
            .withName("HTTP request pattern triggered message")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathMatching("/api/notify/.*")))
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "notification received",
                    newRequestPattern().withUrl(urlPathEqualTo("/ws-notifications")).build()))
            .build();
    wireMockServer.addMessageStubMapping(messageStub);

    // Create an HTTP stub for the endpoint (so the request doesn't fail)
    stubFor(
        get(urlPathMatching("/api/notify/.*"))
            .willReturn(aResponse().withStatus(200).withBody("Notified")));

    // Connect a WebSocket client
    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = "ws://localhost:" + wireMockServer.port() + "/ws-notifications";
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    // Make an HTTP request that matches the pattern
    testClient.get("/api/notify/user123");

    // Verify the WebSocket client received the message
    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("notification received"));
  }

  @Test
  void messageStubTriggeredByHttpRequestPatternWorksWithoutMatchingHttpStub() {
    // Create a message stub that triggers on any HTTP request matching a pattern
    // Note: No HTTP stub is created for this endpoint
    MessageStubMapping messageStub =
        MessageStubMapping.builder()
            .withName("HTTP request pattern triggered without stub")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/api/no-stub")))
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "request received",
                    newRequestPattern().withUrl(urlPathEqualTo("/ws-no-stub")).build()))
            .build();
    wireMockServer.addMessageStubMapping(messageStub);

    // Connect a WebSocket client
    WebsocketTestClient wsClient = new WebsocketTestClient();
    String wsUrl = "ws://localhost:" + wireMockServer.port() + "/ws-no-stub";
    wsClient.connect(wsUrl);
    waitAtMost(5, SECONDS).until(wsClient::isConnected);

    // Make an HTTP request (will return 404 since no stub, but message should still be sent)
    testClient.get("/api/no-stub");

    // Verify the WebSocket client received the message
    waitAtMost(5, SECONDS).until(() -> wsClient.getMessages().contains("request received"));
  }

  @Test
  void multipleWebsocketClientsReceiveMessageWhenHttpStubIsTriggered() {
    // Create an HTTP stub
    stubFor(
        post(urlPathEqualTo("/api/broadcast"))
            .withId(UUID.fromString("22222222-3333-4444-5555-666666666666"))
            .willReturn(aResponse().withStatus(200).withBody("Broadcast sent")));

    // Create a message stub that broadcasts to all matching channels
    MessageStubMapping messageStub =
        MessageStubMapping.builder()
            .withName("Broadcast on HTTP stub")
            .triggeredByHttpStub("22222222-3333-4444-5555-666666666666")
            .triggersAction(
                SendMessageAction.toMatchingChannels(
                    "broadcast message",
                    newRequestPattern().withUrl(urlPathMatching("/ws-broadcast.*")).build()))
            .build();
    wireMockServer.addMessageStubMapping(messageStub);

    // Connect multiple WebSocket clients
    WebsocketTestClient wsClient1 = new WebsocketTestClient();
    WebsocketTestClient wsClient2 = new WebsocketTestClient();
    String wsUrl1 = "ws://localhost:" + wireMockServer.port() + "/ws-broadcast-1";
    String wsUrl2 = "ws://localhost:" + wireMockServer.port() + "/ws-broadcast-2";
    wsClient1.connect(wsUrl1);
    wsClient2.connect(wsUrl2);
    waitAtMost(5, SECONDS).until(wsClient1::isConnected);
    waitAtMost(5, SECONDS).until(wsClient2::isConnected);

    // Make an HTTP request that triggers the broadcast
    testClient.postWithBody("/api/broadcast", "trigger", "text/plain");

    // Verify both WebSocket clients received the message
    waitAtMost(5, SECONDS).until(() -> wsClient1.getMessages().contains("broadcast message"));
    waitAtMost(5, SECONDS).until(() -> wsClient2.getMessages().contains("broadcast message"));
  }
}
