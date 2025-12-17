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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubOnChannel;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.admin.model.SendChannelMessageResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.websocket.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.websocket.message.SendMessageAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class WebsocketAcceptanceTest extends AcceptanceTestBase {

  @AfterEach
  void resetMessageStubs() {
    wireMockServer.resetMessageStubMappings();
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
            .withMessagePattern(equalTo("ping"))
            .withAction(SendMessageAction.toOriginatingChannel("pong"))
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
            .withMessagePattern(matching("hello.*"))
            .withAction(SendMessageAction.toOriginatingChannel("hi there!"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/greet";

    String response = testClient.sendMessageAndWaitForResponse(url, "hello world");
    assertThat(response, is("hi there!"));
  }

  @Test
  void messageStubMappingWithChannelPatternMatchesSpecificChannels() {
    // Register a message stub that only matches messages on specific channels
    RequestPattern channelPattern = newRequestPattern().withUrl("/vip-channel").build();
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("VIP stub")
            .withChannelPattern(channelPattern)
            .withMessagePattern(equalTo("request"))
            .withAction(SendMessageAction.toOriginatingChannel("VIP response"))
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
            .withMessagePattern(matching(".*"))
            .withAction(SendMessageAction.toOriginatingChannel("low priority"))
            .build();

    MessageStubMapping highPriorityStub =
        MessageStubMapping.builder()
            .withName("High priority stub")
            .withPriority(1)
            .withMessagePattern(equalTo("test"))
            .withAction(SendMessageAction.toOriginatingChannel("high priority"))
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
            .withMessagePattern(equalTo("broadcast"))
            .withAction(SendMessageAction.toMatchingChannels("broadcast message", targetPattern))
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
            .withMessagePattern(equalTo("test"))
            .withAction(SendMessageAction.toOriginatingChannel("response"))
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
            .withMessagePattern(equalTo("multi"))
            .withAction(SendMessageAction.toOriginatingChannel("response1"))
            .withAction(SendMessageAction.toOriginatingChannel("response2"))
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
    wireMockServer.messageStubFor(
        messageStubOnChannel(newRequestPattern().withUrl("/dsl-test"))
            .withName("DSL stub")
            .withMessageBody(equalTo("hello"))
            .willTriggerActions(sendMessage("world").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/dsl-test";

    String response = testClient.sendMessageAndWaitForResponse(url, "hello");
    assertThat(response, is("world"));
  }

  @Test
  void messageStubMappingDslSupportsMultipleActions() {
    // Register a message stub with multiple actions using the DSL
    wireMockServer.messageStubFor(
        messageStubOnChannel(newRequestPattern().withUrl("/dsl-multi"))
            .withName("DSL multi-action stub")
            .withMessageBody(equalTo("trigger"))
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
    wireMockServer.messageStubFor(
        messageStubOnChannel(newRequestPattern().withUrl("/dsl-broadcast"))
            .withName("DSL broadcast stub")
            .withMessageBody(equalTo("broadcast"))
            .willTriggerActions(
                sendMessage("broadcasted")
                    .onChannelsMatching(newRequestPattern().withUrl("/dsl-broadcast"))));

    WebsocketTestClient client1 = new WebsocketTestClient();
    WebsocketTestClient client2 = new WebsocketTestClient();
    String url = "ws://localhost:" + wireMockServer.port() + "/dsl-broadcast";

    // Connect both clients
    client1.connect(url);
    client2.connect(url);

    waitAtMost(5, SECONDS).until(() -> client1.isConnected());
    waitAtMost(5, SECONDS).until(() -> client2.isConnected());

    // Send message from client1
    client1.sendMessage("broadcast");

    // Both clients should receive the broadcast
    waitAtMost(5, SECONDS).until(() -> client1.getMessages().contains("broadcasted"));
    waitAtMost(5, SECONDS).until(() -> client2.getMessages().contains("broadcasted"));
  }
}
