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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.findAllMessageEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllMessageServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllMessageChannels;
import static com.github.tomakehurst.wiremock.client.WireMock.listAllMessageStubMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.message;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsForStubsMatchingMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageJournal;
import static com.github.tomakehurst.wiremock.client.WireMock.sendMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.verifyMessageEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.waitForMessageEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.waitForMessageEvents;
import static com.github.tomakehurst.wiremock.common.Metadata.metadata;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.messageStubMappingWithName;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.admin.model.ListMessageChannelsResult;
import com.github.tomakehurst.wiremock.admin.model.ListMessageStubMappingsResult;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class WebsocketMessageJournalAcceptanceTest extends WebsocketAcceptanceTestBase {

  @Test
  void messageJournalRecordsReceivedMessages() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Journal test stub")
            .withBody(equalTo("journal-test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/journal-test");

    testClient.sendMessageAndWaitForResponse(url, "journal-test");

    var events = getAllMessageServeEvents();
    assertThat(events.size(), is(1));

    var event = events.get(0);
    assertThat(event.getMessage().getBodyAsString(), is("journal-test"));
    assertThat(event.getWasMatched(), is(true));
    assertThat(event.getStubMapping().getName(), is("Journal test stub"));
  }

  @Test
  void messageJournalRecordsUnmatchedMessages() {
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/unmatched-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);
    testClient.sendMessage("unmatched-message");

    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    var events = getAllMessageServeEvents();
    assertThat(events.size(), is(1));

    var event = events.get(0);
    assertThat(event.getMessage().getBodyAsString(), is("unmatched-message"));
    assertThat(event.getWasMatched(), is(false));
  }

  @Test
  void canCountMessageEventsMatchingPredicate() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Count test stub")
            .withBody(matching("count-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("counted"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/count-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("count-1");
    testClient.sendMessage("count-2");
    testClient.sendMessage("count-3");

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 3);

    int count =
        findAllMessageEvents(messagePattern().withBody(matching("count-.*")).build()).size();
    assertThat(count, is(3));
  }

  @Test
  void canFindMessageEventsMatchingPredicate() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Find test stub")
            .withBody(matching("find-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("found"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/find-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("find-alpha");
    testClient.sendMessage("find-beta");

    waitAtMost(5, SECONDS).until(() -> findAllMessageEvents(MessagePattern.ANYTHING).size() >= 2);

    var events = findAllMessageEvents(messagePattern().withBody(equalTo("find-alpha")).build());
    assertThat(events.size(), is(1));
    assertThat(events.get(0).getMessage().getBodyAsString(), is("find-alpha"));
  }

  @Test
  void canResetMessageJournal() {
    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/reset-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);
    testClient.sendMessage("before-reset");

    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    resetMessageJournal();

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
    String url = websocketUrl("/verify-test");

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
    String url = websocketUrl("/verify-count-test");

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
    String url = websocketUrl("/verify-strategy-test");

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
    String url = websocketUrl("/verify-fail-test");

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
    String url = websocketUrl("/get-single-test");

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
    String url = websocketUrl("/remove-single-test");

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
    String url = websocketUrl("/remove-pattern-test");

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
    assertThat(
        getAllMessageServeEvents().get(0).getMessage().getBodyAsString(), is("other-message"));
  }

  @Test
  void canRemoveMessageServeEventsForStubsMatchingMetadata() {
    MessageStubMapping stubWithMetadata =
        MessageStubMapping.builder()
            .withName("Metadata stub")
            .withBody(matching("metadata-.*"))
            .withMetadata(metadata().attr("category", "test").attr("priority", "high"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    messageStubFor(stubWithMetadata);

    MessageStubMapping stubWithoutMetadata =
        MessageStubMapping.builder()
            .withName("No metadata stub")
            .withBody(matching("other-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("other-response"))
            .build();
    messageStubFor(stubWithoutMetadata);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/metadata-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("metadata-a");
    testClient.sendMessage("other-b");

    waitAtMost(5, SECONDS).until(() -> getAllMessageServeEvents().size() >= 2);

    assertThat(getAllMessageServeEvents().size(), is(2));

    var result =
        removeMessageServeEventsForStubsMatchingMetadata(
            matchingJsonPath("$.category", equalTo("test")));

    assertThat(result.getMessageServeEvents().size(), is(1));
    assertThat(getAllMessageServeEvents().size(), is(1));
    assertThat(getAllMessageServeEvents().get(0).getMessage().getBodyAsString(), is("other-b"));
  }

  @Test
  void stubsAreNotRemovedViaMetadataWhenNoneMatch() {
    MessageStubMapping stub =
        MessageStubMapping.builder()
            .withName("Metadata stub")
            .withBody(matching("metadata-.*"))
            .withMetadata(metadata().attr("category", "test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build();
    messageStubFor(stub);

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/metadata-no-match-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("metadata-a");

    waitAtMost(5, SECONDS).until(() -> !getAllMessageServeEvents().isEmpty());

    assertThat(getAllMessageServeEvents().size(), is(1));

    var result =
        removeMessageServeEventsForStubsMatchingMetadata(
            matchingJsonPath("$.nonexistent", equalTo("value")));

    assertThat(result.getMessageServeEvents().size(), is(0));
    assertThat(getAllMessageServeEvents().size(), is(1));
  }

  @Test
  void canListAllMessageStubMappingsViaHttpClient() {
    messageStubFor(
        message()
            .withName("List stub 1")
            .withBody(equalTo("list-test-1"))
            .willTriggerActions(sendMessage("response1").onOriginatingChannel()));

    messageStubFor(
        message()
            .withName("List stub 2")
            .withBody(equalTo("list-test-2"))
            .willTriggerActions(sendMessage("response2").onOriginatingChannel()));

    ListMessageStubMappingsResult result = listAllMessageStubMappings();

    assertThat(result.getMessageMappings().size(), is(greaterThanOrEqualTo(2)));
    assertThat(result.getMessageMappings(), hasItem(messageStubMappingWithName("List stub 1")));
    assertThat(result.getMessageMappings(), hasItem(messageStubMappingWithName("List stub 2")));
  }

  @Test
  void canListAllMessageChannelsViaHttpClient() {
    messageStubFor(
        message()
            .withName("Channel list stub")
            .withBody(equalTo("channel-list-test"))
            .willTriggerActions(sendMessage("response").onOriginatingChannel()));

    WebsocketTestClient testClient1 = new WebsocketTestClient();
    WebsocketTestClient testClient2 = new WebsocketTestClient();
    String url1 = websocketUrl("/channel-list-1");
    String url2 = websocketUrl("/channel-list-2");

    testClient1.connect(url1);
    testClient2.connect(url2);

    waitAtMost(5, SECONDS).until(testClient1::isConnected);
    waitAtMost(5, SECONDS).until(testClient2::isConnected);

    ListMessageChannelsResult result = listAllMessageChannels();

    assertThat(result, is(notNullValue()));
    assertThat(result.getChannels().size(), is(greaterThanOrEqualTo(2)));

    testClient1.disconnect();
    testClient2.disconnect();
  }

  @Test
  void canWaitForSingleMessageEventViaHttpClient() {
    messageStubFor(
        MessageStubMapping.builder()
            .withName("Wait single stub")
            .withBody(equalTo("wait-single-test"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build());

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/wait-single-test");
    testClient.sendMessageAndWaitForResponse(url, "wait-single-test");

    Optional<MessageServeEvent> result =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("wait-single-test")).build(), Duration.ofSeconds(5));

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getMessage().getBodyAsString(), is("wait-single-test"));
  }

  @Test
  void waitForSingleMessageEventReturnsEmptyWhenNoMatch() {
    resetMessageJournal();

    Optional<MessageServeEvent> result =
        waitForMessageEvent(
            messagePattern().withBody(equalTo("non-existent-message")).build(),
            Duration.ofMillis(200));

    assertThat(result.isPresent(), is(false));
  }

  @Test
  void canWaitForMultipleMessageEventsViaHttpClient() {
    messageStubFor(
        MessageStubMapping.builder()
            .withName("Wait multiple stub")
            .withBody(matching("wait-multi-.*"))
            .triggersAction(SendMessageAction.toOriginatingChannel("response"))
            .build());

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/wait-multi-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("wait-multi-1");
    testClient.sendMessage("wait-multi-2");
    testClient.sendMessage("wait-multi-3");

    waitAtMost(5, SECONDS)
        .until(
            () ->
                findAllMessageEvents(messagePattern().withBody(matching("wait-multi-.*")).build())
                        .size()
                    >= 3);

    List<MessageServeEvent> result =
        waitForMessageEvents(
            messagePattern().withBody(matching("wait-multi-.*")).build(), 3, Duration.ofSeconds(5));

    assertThat(result, hasSize(3));
  }

  @Test
  void waitForMultipleMessageEventsReturnsPartialResultsOnTimeout() {
    messageStubFor(
        message()
            .withName("Wait partial stub")
            .withBody(matching("wait-partial-.*"))
            .willTriggerActions(SendMessageAction.toOriginatingChannel("response")));

    resetMessageJournal();

    WebsocketTestClient testClient = new WebsocketTestClient();
    String url = websocketUrl("/wait-partial-test");

    testClient.connect(url);
    waitAtMost(5, SECONDS).until(testClient::isConnected);

    testClient.sendMessage("wait-partial-1");

    waitAtMost(5, SECONDS)
        .until(
            () ->
                !findAllMessageEvents(
                        messagePattern().withBody(matching("wait-partial-.*")).build())
                    .isEmpty());

    List<MessageServeEvent> result =
        waitForMessageEvents(
            messagePattern().withBody(matching("wait-partial-.*")).build(),
            5,
            Duration.ofMillis(200));

    assertThat(result.size(), is(greaterThanOrEqualTo(1)));
  }
}
