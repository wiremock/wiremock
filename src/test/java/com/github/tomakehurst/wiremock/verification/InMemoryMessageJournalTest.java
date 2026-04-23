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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.message.MessageStubRequestHandler;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InMemoryMessageJournalTest {

  private MessageServeEvent event1, event2, event3;

  private static Message message(String text) {
    return MessageStubRequestHandler.resolveToMessage(MessageDefinition.fromString(text), null);
  }

  @BeforeEach
  public void createTestEvents() {
    event1 =
        MessageServeEvent.receivedUnmatched(
            ChannelType.WEBSOCKET,
            UUID.randomUUID(),
            mockRequest().url("/channel1"),
            message("message1"));
    event2 =
        MessageServeEvent.receivedUnmatched(
            ChannelType.WEBSOCKET,
            UUID.randomUUID(),
            mockRequest().url("/channel2"),
            message("message2"));
    event3 =
        MessageServeEvent.receivedUnmatched(
            ChannelType.WEBSOCKET,
            UUID.randomUUID(),
            mockRequest().url("/channel3"),
            message("message3"));
  }

  @Test
  public void returnsAllLoggedEventsWhenNoJournalSizeLimit() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event1);
    journal.messageReceived(event2);

    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(3));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message1")).build()),
        is(2));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message2")).build()),
        is(1));
  }

  @Test
  public void resettingTheJournalClearsAllEntries() {
    MessageJournal journal = new InMemoryMessageJournal(1);
    journal.messageReceived(event1);
    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(1));
    journal.reset();
    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(0));
  }

  @Test
  public void discardsOldEventsWhenJournalSizeIsLimited() {
    MessageJournal journal = new InMemoryMessageJournal(2);

    journal.messageReceived(event1);
    journal.messageReceived(event2);

    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(2));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message1")).build()),
        is(1));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message2")).build()),
        is(1));

    journal.messageReceived(event3);
    assertOnlyLastTwoEventsLeft(journal);
  }

  @Test
  public void getEventsMatchingReturnsMatchingEvents() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);
    journal.messageReceived(event3);

    List<MessageServeEvent> matching =
        journal.getEventsMatching(
            MessagePattern.messagePattern().withBody(matching("message.*")).build());
    assertThat(matching, hasSize(3));

    List<MessageServeEvent> matchingOne =
        journal.getEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message2")).build());
    assertThat(matchingOne, hasSize(1));
    assertThat(matchingOne.get(0).getMessage().getBodyAsString(), is("message2"));
  }

  @Test
  public void getAllMessageServeEventsReturnsAllEvents() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);

    List<MessageServeEvent> all = journal.getAllMessageServeEvents();
    assertThat(all, hasSize(2));
  }

  @Test
  public void getMessageServeEventByIdReturnsCorrectEvent() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);

    Optional<MessageServeEvent> found = journal.getMessageServeEvent(event1.getId());
    assertThat(found.isPresent(), is(true));
    assertThat(found.get().getId(), is(event1.getId()));

    Optional<MessageServeEvent> notFound = journal.getMessageServeEvent(UUID.randomUUID());
    assertThat(notFound.isPresent(), is(false));
  }

  @Test
  public void removeEventRemovesSpecificEvent() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);

    journal.removeEvent(event1.getId());

    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(1));
    assertThat(journal.getMessageServeEvent(event1.getId()).isPresent(), is(false));
    assertThat(journal.getMessageServeEvent(event2.getId()).isPresent(), is(true));
  }

  @Test
  public void removeEventsMatchingRemovesMatchingEvents() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);
    journal.messageReceived(event3);

    List<MessageServeEvent> removed =
        journal.removeEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message2")).build());

    assertThat(removed, hasSize(1));
    assertThat(removed.get(0).getMessage().getBodyAsString(), is("message2"));
    assertThat(journal.countEventsMatching(MessagePattern.ANYTHING), is(2));
  }

  @Test
  public void waitForEventReturnsImmediatelyIfEventExists() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);

    Optional<MessageServeEvent> found =
        journal.waitForEvent(
            MessagePattern.messagePattern().withBody(equalTo("message1")).build(),
            Duration.ofSeconds(1));

    assertThat(found.isPresent(), is(true));
    assertThat(found.get().getMessage().getBodyAsString(), is("message1"));
  }

  @Test
  public void waitForEventReturnsEmptyIfNoMatchAndTimeout() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    Optional<MessageServeEvent> found =
        journal.waitForEvent(
            MessagePattern.messagePattern().withBody(equalTo("nonexistent")).build(),
            Duration.ofMillis(100));

    assertThat(found.isPresent(), is(false));
  }

  @Test
  public void waitForEventWaitsForNewEvent() throws Exception {
    MessageJournal journal = new InMemoryMessageJournal(null);
    CountDownLatch latch = new CountDownLatch(1);
    final Optional<MessageServeEvent>[] result = new Optional[] {Optional.empty()};

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(
        () -> {
          result[0] =
              journal.waitForEvent(
                  MessagePattern.messagePattern().withBody(equalTo("message1")).build(),
                  Duration.ofSeconds(5));
          latch.countDown();
        });

    // Give the wait thread time to start
    Thread.sleep(100);

    // Add the event
    journal.messageReceived(event1);

    // Wait for the result
    boolean completed = latch.await(2, TimeUnit.SECONDS);
    executor.shutdown();

    assertThat(completed, is(true));
    assertThat(result[0].isPresent(), is(true));
    assertThat(result[0].get().getMessage().getBodyAsString(), is("message1"));
  }

  @Test
  public void waitForEventsReturnsImmediatelyIfEnoughEventsExist() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);
    journal.messageReceived(event2);

    List<MessageServeEvent> found =
        journal.waitForEvents(MessagePattern.ANYTHING, 2, Duration.ofSeconds(1));

    assertThat(found, hasSize(2));
  }

  @Test
  public void waitForEventsReturnsPartialIfTimeoutBeforeEnoughEvents() {
    MessageJournal journal = new InMemoryMessageJournal(null);

    journal.messageReceived(event1);

    List<MessageServeEvent> found =
        journal.waitForEvents(MessagePattern.ANYTHING, 5, Duration.ofMillis(100));

    assertThat(found, hasSize(1));
  }

  @Test
  public void matchedEventContainsStubMapping() {
    MessageStubMapping stub = MessageStubMapping.builder().withName("Test stub").build();

    MessageServeEvent matchedEvent =
        MessageServeEvent.receivedMatched(
            ChannelType.WEBSOCKET,
            UUID.randomUUID(),
            mockRequest().url("/channel"),
            message("test message"),
            stub);

    assertThat(matchedEvent.getWasMatched(), is(true));
    assertThat(matchedEvent.getStubMapping(), is(notNullValue()));
    assertThat(matchedEvent.getStubMapping().getName(), is("Test stub"));
  }

  @Test
  public void unmatchedEventHasNoStubMapping() {
    assertThat(event1.getWasMatched(), is(false));
    assertThat(event1.getStubMapping(), is(nullValue()));
  }

  @Test
  public void sentEventHasCorrectEventType() {
    MessageServeEvent sentEvent =
        MessageServeEvent.sent(
            ChannelType.WEBSOCKET,
            UUID.randomUUID(),
            mockRequest().url("/channel"),
            message("sent message"));

    assertThat(sentEvent.isSent(), is(true));
    assertThat(sentEvent.isReceived(), is(false));
    assertThat(sentEvent.getEventType(), is(MessageServeEvent.EventType.SENT));
  }

  @Test
  public void receivedEventHasCorrectEventType() {
    assertThat(event1.isReceived(), is(true));
    assertThat(event1.isSent(), is(false));
    assertThat(event1.getEventType(), is(MessageServeEvent.EventType.RECEIVED));
  }

  private void assertOnlyLastTwoEventsLeft(MessageJournal journal) {
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message1")).build()),
        is(0));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message2")).build()),
        is(1));
    assertThat(
        journal.countEventsMatching(
            MessagePattern.messagePattern().withBody(equalTo("message3")).build()),
        is(1));
  }
}
