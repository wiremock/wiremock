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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.message.ChannelType.FIXED;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.common.ClientError;
import com.github.tomakehurst.wiremock.common.ConflictException;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FixedMessageChannelAcceptanceTest extends WebsocketAcceptanceTestBase {

  static UUID channelId;

  @BeforeAll
  static void setupChannels() {
    registerChannelProvider(channelProvider().named("events").withDriver("in-memory"));
    channelId = createFixedChannel(fixedChannel().onProvider("events").named("orders"));
  }

  @AfterEach
  void resetMessageState() {
    resetMessageStubs();
    resetMessageJournal();
  }

  @Test
  void registeringProviderWithUnknownDriverTypeReturnsAnError() {
    assertThrows(
        InvalidInputException.class,
        () -> registerChannelProvider(channelProvider().named("bad-provider").withDriver("kafka")));
  }

  @Test
  void creatingChannelWithUnregisteredProviderReturnsAnError() {
    assertThrows(
        InvalidInputException.class,
        () ->
            createFixedChannel(fixedChannel().onProvider("nonexistent-provider").named("orders")));
  }

  @Test
  void reRegisteringProviderWithSameNameIsIdempotent() {
    registerChannelProvider(channelProvider().named("events").withDriver("in-memory"));
  }

  @Test
  void httpRequestTriggerSendsMessageToInMemoryFixedChannel() {
    messageStubFor(
        message()
            .withName("Send order event on HTTP trigger")
            .triggeredByHttpRequest(newRequestPattern().withUrl(urlPathEqualTo("/api/orders")))
            .willTriggerActions(
                sendMessage().withBody("order-created").onChannel("events", "orders")));

    testClient.get("/api/orders");

    Optional<MessageServeEvent> maybeEvent =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("order-created")).build(), Duration.ofSeconds(5));

    assertThat(maybeEvent.isPresent(), is(true));

    MessageServeEvent event = maybeEvent.get();
    assertThat(event.getMessage().getBodyAsString(), is("order-created"));
    assertThat(event.getChannelType(), is(FIXED));
    assertThat(event.getChannelId(), is(channelId));
  }

  @Test
  void creatingTheSameFixedChannelTwiceReturnsAnError() {
    assertThrows(
        ConflictException.class,
        () -> createFixedChannel(fixedChannel().onProvider("events").named("orders")));
  }

  @Test
  void stubActionTargetingNonExistentFixedChannelReturnsAnError() {
    // No channel named "notifications" has been created
    messageStubFor(
        message()
            .withName("Send to non-existent channel")
            .triggeredByMessageOnChannel("events", "orders")
            .withBody(equalTo("trigger"))
            .willTriggerActions(
                sendMessage().withBody("response").onChannel("events", "notifications")));

    assertThrows(ClientError.class, () -> sendMessageToFixedChannel("events", "orders", "trigger"));
  }

  @Test
  void unmatchedInboundFixedChannelMessageIsRecordedAsUnmatched() {
    sendMessageToFixedChannel("events", "orders", "no-match");

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("no-match")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getWasMatched(), is(false));
  }

  @Test
  void matchedInboundFixedChannelMessageHasStubMappingAttached() {
    MessageStubMapping stub =
        messageStubFor(
            message()
                .withName("Attach stub mapping test")
                .triggeredByMessageOnChannel("events", "orders")
                .withBody(equalTo("attach-test"))
                .willTriggerActions(sendMessage().withBody("reply").onChannel("events", "orders")));

    sendMessageToFixedChannel("events", "orders", "attach-test");

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("attach-test")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getWasMatched(), is(true));
    assertThat(event.get().getStubMapping(), notNullValue());
    assertThat(event.get().getStubMapping().getId(), is(stub.getId()));
  }

  @Test
  void higherPriorityFixedChannelStubIsMatchedAndLowerPriorityIsSkipped() {
    messageStubFor(
        message()
            .atPriority(2)
            .triggeredByMessageOnChannel("events", "orders")
            .withBody(equalTo("priority-test"))
            .willTriggerActions(
                sendMessage().withBody("low-priority-response").onChannel("events", "orders")));

    messageStubFor(
        message()
            .atPriority(1)
            .triggeredByMessageOnChannel("events", "orders")
            .withBody(equalTo("priority-test"))
            .willTriggerActions(
                sendMessage().withBody("high-priority-response").onChannel("events", "orders")));

    sendMessageToFixedChannel("events", "orders", "priority-test");

    Optional<MessageServeEvent> highPriorityEvent =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("high-priority-response")).build(),
            Duration.ofSeconds(5));

    assertThat(highPriorityEvent.isPresent(), is(true));

    List<MessageServeEvent> lowPriorityEvents =
        findAllMessageEvents(messagePattern().withBody(equalTo("low-priority-response")).build());
    assertThat(lowPriorityEvents.isEmpty(), is(true));
  }

  @Test
  void removedChannelProviderCanBeReRegistered() {
    registerChannelProvider(channelProvider().named("temp-provider").withDriver("in-memory"));
    removeChannelProvider("temp-provider");

    registerChannelProvider(channelProvider().named("temp-provider").withDriver("in-memory"));
    removeChannelProvider("temp-provider");
  }

  @Test
  void deletedChannelIsNoLongerRetrievable() {
    UUID deletedId = createFixedChannel(fixedChannel().onProvider("events").named("ephemeral"));

    removeMessageChannel(deletedId);

    assertThat(getMessageChannel(deletedId).isPresent(), is(false));
  }

  @Test
  void deletedChannelCanBeRecreatedWithTheSameName() {
    UUID firstId = createFixedChannel(fixedChannel().onProvider("events").named("recreatable"));
    removeMessageChannel(firstId);

    UUID secondId = createFixedChannel(fixedChannel().onProvider("events").named("recreatable"));
    removeMessageChannel(secondId);
  }

  @Test
  void incomingMessageOnFixedChannelTriggersSendToFixedChannel() {
    messageStubFor(
        message()
            .withName("Echo on fixed channel")
            .triggeredByMessageOnChannel("events", "orders")
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage().withBody("pong").onChannel("events", "orders")));

    sendMessageToFixedChannel("events", "orders", "ping");

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("pong")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getMessage().getBodyAsString(), is("pong"));
  }

  @Test
  void inboundFixedChannelMessageIsForwardedToMatchingRequestInitiatedChannels() {
    messageStubFor(
        message()
            .withName("Forward fixed channel message to WebSocket subscribers")
            .triggeredByMessageOnChannel("events", "orders")
            .withBody(equalTo("forward-me"))
            .triggersAction(
                sendMessage("forwarded")
                    .onChannelsMatching(newRequestPattern().withUrl("/ws-subscribers")))
            .build());

    WebsocketTestClient wsClient = new WebsocketTestClient();
    wsClient.connect(websocketUrl("/ws-subscribers"));
    Awaitility.waitAtMost(5, TimeUnit.SECONDS).until(wsClient::isConnected);

    sendMessageToFixedChannel("events", "orders", "forward-me");

    Awaitility.waitAtMost(5, TimeUnit.SECONDS)
        .until(() -> wsClient.getMessages().contains("forwarded"));
  }

  @Test
  void channelNameStringValuePatternMatchesIncomingFixedChannelMessage() {
    messageStubFor(
        message()
            .withName("Echo with channel name pattern")
            .triggeredByMessageOnChannel("events", matching("order.*"))
            .withBody(equalTo("ping"))
            .willTriggerActions(sendMessage().withBody("pong").onChannel("events", "orders")));

    sendMessageToFixedChannel("events", "orders", "ping");

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("pong")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getMessage().getBodyAsString(), is("pong"));
  }
}
