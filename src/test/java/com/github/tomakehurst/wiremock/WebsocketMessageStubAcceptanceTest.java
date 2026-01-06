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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.findMessageStubsByMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllMessageStubMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageStubsByMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.messageStubMappingWithName;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import java.util.List;
import org.junit.jupiter.api.Test;

public class WebsocketMessageStubAcceptanceTest extends WebsocketAcceptanceTestBase {

  @Test
  void messageStubMappingRespondsToOriginatingChannel() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Echo stub")
            .withBody(equalTo("ping"))
            .triggersAction(SendMessageAction.toOriginatingChannel("pong"))
            .build();
    wireMockServer.addMessageStubMapping(stub);

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/echo");

    String response = testClient.sendMessageAndWaitForResponse(url, "ping");
    assertThat(response, is("pong"));
  }

  @Test
  void messageStubMappingMatchesWithRegexPattern() {
    messageStubFor(
        message()
            .withName("Greeting stub")
            .withBody(matching("hello.*"))
            .willTriggerActions(sendMessage("hi there!").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/greet");

    String response = testClient.sendMessageAndWaitForResponse(url, "hello world");
    assertThat(response, is("hi there!"));
  }

  @Test
  void messageStubMappingWithChannelPatternMatchesSpecificChannels() {
    messageStubFor(
        message()
            .withName("VIP stub")
            .onChannelFromRequestMatching(newRequestPattern().withUrl("/vip-channel"))
            .withBody(equalTo("request"))
            .willTriggerActions(sendMessage("VIP response").onOriginatingChannel()));

    WebsocketTestClient vipClient = new WebsocketTestClient();
    WebsocketTestClient regularClient = new WebsocketTestClient();
    String vipUrl = websocketUrl("/vip-channel");
    String regularUrl = websocketUrl("/regular-channel");

    String vipResponse = vipClient.sendMessageAndWaitForResponse(vipUrl, "request");
    assertThat(vipResponse, is("VIP response"));

    regularClient.sendMessage(regularUrl, "request");
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThat(regularClient.getMessages().isEmpty(), is(true));
  }

  @Test
  void messageStubMappingPriorityDeterminesMatchOrder() {
    messageStubFor(
        message()
            .withName("Low priority stub")
            .withPriority(10)
            .withBody(matching(".*"))
            .willTriggerActions(sendMessage("low priority").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("High priority stub")
            .withPriority(1)
            .withBody(equalTo("test"))
            .willTriggerActions(sendMessage("high priority").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/priority-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response, is("high priority"));
  }

  @Test
  void messageStubMappingCanSendToMatchingChannels() {
    messageStubFor(
        message()
            .withName("Broadcast stub")
            .withBody(equalTo("broadcast"))
            .willTriggerActions(
                sendMessage("broadcast message")
                    .onChannelsMatching(
                        newRequestPattern().withUrl(urlPathMatching("/broadcast/.*")))));

    WebsocketTestClient senderClient = new WebsocketTestClient();
    WebsocketTestClient receiverClient1 = new WebsocketTestClient();
    WebsocketTestClient receiverClient2 = new WebsocketTestClient();
    String senderUrl = websocketUrl("/sender");
    String receiverUrl1 = websocketUrl("/broadcast/user1");
    String receiverUrl2 = websocketUrl("/broadcast/user2");

    receiverClient1.withWebsocketSession(
        receiverUrl1,
        session1 ->
            receiverClient2.withWebsocketSession(
                receiverUrl2,
                session2 -> {
                  senderClient.sendMessage(senderUrl, "broadcast");
                  return null;
                }));

    waitAtMost(5, SECONDS).until(() -> receiverClient1.getMessages().contains("broadcast message"));
    waitAtMost(5, SECONDS).until(() -> receiverClient2.getMessages().contains("broadcast message"));
  }

  @Test
  void messageStubMappingCanBeRemoved() {
    MessageStubMapping stub =
        messageStubFor(
            message()
                .withName("Removable stub")
                .withBody(equalTo("test"))
                .willTriggerActions(sendMessage("response").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/remove-test");

    String response1 = testClient.sendMessageAndWaitForResponse(url, "test");
    assertThat(response1, is("response"));

    wireMockServer.removeMessageStubMapping(stub.getId());
    testClient.clearMessages();

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
    messageStubFor(
        message()
            .withName("Multi-action stub")
            .withBody(equalTo("multi"))
            .willTriggerActions(
                sendMessage("response1").onOriginatingChannel(),
                sendMessage("response2").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/multi-action");

    testClient.sendMessage(url, "multi");

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("response1"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("response2"));
    assertThat(testClient.getMessages().size(), is(2));
  }

  @Test
  void messageStubMappingCanBeCreatedUsingDsl() {
    messageStubFor(
        message()
            .withName("DSL stub")
            .withBody(equalTo("hello"))
            .willTriggerActions(sendMessage("world").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/dsl-test");

    String response = testClient.sendMessageAndWaitForResponse(url, "hello");
    assertThat(response, is("world"));
  }

  @Test
  void messageStubMappingDslSupportsMultipleActions() {
    messageStubFor(
        message()
            .onChannelFromRequestMatching(newRequestPattern().withUrl("/dsl-multi"))
            .withName("DSL multi-action stub")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage("first").onOriginatingChannel(),
                sendMessage("second").onOriginatingChannel()));

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/dsl-multi");

    testClient.sendMessage(url, "trigger");

    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("first"));
    waitAtMost(5, SECONDS).until(() -> testClient.getMessages().contains("second"));
    assertThat(testClient.getMessages().size(), is(2));
  }

  @Test
  void messageStubMappingDslSupportsBroadcastToMatchingChannels() {
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
    String url = websocketUrl("/dsl-broadcast");

    client1.connect(url);
    client2.connect(url);

    waitAtMost(5, SECONDS).until(client1::isConnected);
    waitAtMost(5, SECONDS).until(client2::isConnected);

    client1.sendMessage("broadcast");

    waitAtMost(5, SECONDS).until(() -> client1.getMessages().contains("broadcasted"));
    waitAtMost(5, SECONDS).until(() -> client2.getMessages().contains("broadcasted"));
  }

  @Test
  void canFindMessageStubsByMetadata() {
    wireMockServer.resetMessageStubMappings();

    messageStubFor(
        message()
            .withName("Stub with metadata 1")
            .withBody(equalTo("test1"))
            .withMetadata(metadata().attr("category", "important").attr("version", "1"))
            .willTriggerActions(sendMessage("response1").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("Stub with metadata 2")
            .withBody(equalTo("test2"))
            .withMetadata(metadata().attr("category", "important").attr("version", "2"))
            .willTriggerActions(sendMessage("response2").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("Stub without matching metadata")
            .withBody(equalTo("test3"))
            .withMetadata(metadata().attr("category", "unimportant"))
            .willTriggerActions(sendMessage("response3").onOriginatingChannel()));

    List<MessageStubMapping> found =
        findMessageStubsByMetadata(matchingJsonPath("$.category", equalTo("important")));

    assertThat(found, hasSize(2));
    assertThat(found, hasItem(messageStubMappingWithName("Stub with metadata 1")));
    assertThat(found, hasItem(messageStubMappingWithName("Stub with metadata 2")));
  }

  @Test
  void canRemoveMessageStubsByMetadata() {
    wireMockServer.resetMessageStubMappings();

    messageStubFor(
        message()
            .withName("Stub to remove 1")
            .withBody(equalTo("remove1"))
            .withMetadata(metadata().attr("toRemove", true))
            .willTriggerActions(sendMessage("response1").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("Stub to remove 2")
            .withBody(equalTo("remove2"))
            .withMetadata(metadata().attr("toRemove", true))
            .willTriggerActions(sendMessage("response2").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("Stub to keep")
            .withBody(equalTo("keep"))
            .withMetadata(metadata().attr("toRemove", false))
            .willTriggerActions(sendMessage("response3").onOriginatingChannel()));

    assertThat(listAllMessageStubMappings().getMessageMappings(), hasSize(3));

    removeMessageStubsByMetadata(equalToJson("{ \"toRemove\": true }"));

    assertThat(listAllMessageStubMappings().getMessageMappings(), hasSize(1));
    assertThat(
        listAllMessageStubMappings().getMessageMappings().get(0).getName(), is("Stub to keep"));
  }
}
