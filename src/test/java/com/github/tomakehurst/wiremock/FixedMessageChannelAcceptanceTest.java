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
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FixedMessageChannelAcceptanceTest extends AcceptanceTestBase {

  @BeforeAll
  static void setupChannels() {
    registerChannelProvider(channelProvider().named("events").withDriver("in-memory"));
    createFixedChannel(fixedChannel().onProvider("events").named("orders"));
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
        () -> createFixedChannel(fixedChannel().onProvider("nonexistent-provider").named("orders")));
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

    Optional<MessageServeEvent> event =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("order-created")).build(), Duration.ofSeconds(5));

    assertThat(event.isPresent(), is(true));
    assertThat(event.get().getMessage().getBodyAsString(), is("order-created"));
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
}
