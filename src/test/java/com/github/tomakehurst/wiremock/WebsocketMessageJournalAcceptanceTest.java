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
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.findAllMessageEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllMessageServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThan;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.messageStubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEvent;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsForStubsMatchingMetadata;
import static com.github.tomakehurst.wiremock.client.WireMock.removeMessageServeEventsMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.resetMessageJournal;
import static com.github.tomakehurst.wiremock.client.WireMock.verifyMessageEvent;
import static com.github.tomakehurst.wiremock.message.MessagePattern.messagePattern;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import com.github.tomakehurst.wiremock.testsupport.WebsocketTestClient;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
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
    assertThat(event.getMessage(), is("journal-test"));
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
    assertThat(event.getMessage(), is("unmatched-message"));
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
    assertThat(events.get(0).getMessage(), is("find-alpha"));
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
    String url = websocketUrl("/metadata-test");

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
}
