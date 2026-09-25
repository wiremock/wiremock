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
package com.github.tomakehurst.wiremock.store;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.verification.LoggedRequestInitiatedChannel;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class InMemoryMessageJournalStoreTest {

  @Test
  void addWithNullMaxEntriesRetainsAllEntries() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    MessageServeEvent two = messageServeEvent();
    MessageServeEvent three = messageServeEvent();

    store.add(one, null);
    store.add(two, null);
    store.add(three, null);

    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(
        store.getAllKeys().collect(toList()), contains(three.getId(), two.getId(), one.getId()));
  }

  @Test
  void addDoesNotEvictWhenEntryCountEqualsMaxEntries() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    MessageServeEvent two = messageServeEvent();
    MessageServeEvent three = messageServeEvent();

    store.add(one, 3);
    store.add(two, 3);
    store.add(three, 3);

    assertThat(store.getAllKeys().count(), is(3L));
    assertThat(
        store.getAllKeys().collect(toList()), contains(three.getId(), two.getId(), one.getId()));
  }

  @Test
  void addEvictsOldestEntryWhenExceedingMaxEntriesByOne() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    MessageServeEvent two = messageServeEvent();
    MessageServeEvent three = messageServeEvent();

    store.add(one, 2);
    store.add(two, 2);
    store.add(three, 2);

    assertThat(store.getAllKeys().count(), is(2L));
    assertThat(store.get(one.getId()).isPresent(), is(false));
    assertThat(store.get(two.getId()).isPresent(), is(true));
    assertThat(store.get(three.getId()).isPresent(), is(true));
  }

  @Test
  void addWithMaxEntriesOfZeroLeavesStoreEmpty() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    store.add(one, 0);

    assertThat(store.getAllKeys().count(), is(0L));
    assertThat(store.get(one.getId()).isPresent(), is(false));
  }

  @Test
  void addEvictsMultipleEntriesInASingleCallWhenFarExceedingMaxEntries() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    MessageServeEvent two = messageServeEvent();
    MessageServeEvent three = messageServeEvent();
    MessageServeEvent four = messageServeEvent();

    store.add(one, null);
    store.add(two, null);
    store.add(three, null);

    List<StoreEvent<UUID, MessageServeEvent>> events = new ArrayList<>();
    store.registerEventListener(events::add);

    store.add(four, 1);

    assertThat(store.getAllKeys().count(), is(1L));
    assertThat(store.get(four.getId()).isPresent(), is(true));
    assertThat(store.get(one.getId()).isPresent(), is(false));
    assertThat(store.get(two.getId()).isPresent(), is(false));
    assertThat(store.get(three.getId()).isPresent(), is(false));
    assertThat(
        events,
        contains(
            new StoreEvent<>(four.getId(), null, four),
            new StoreEvent<>(one.getId(), one, null),
            new StoreEvent<>(two.getId(), two, null),
            new StoreEvent<>(three.getId(), three, null)));
  }

  @Test
  void addNotifiesListenersForBothTheAddedEntryAndAnyEvictedEntries() {
    InMemoryMessageJournalStore store = new InMemoryMessageJournalStore();

    MessageServeEvent one = messageServeEvent();
    MessageServeEvent two = messageServeEvent();
    store.add(one, 1);

    List<StoreEvent<UUID, MessageServeEvent>> events = new ArrayList<>();
    store.registerEventListener(events::add);

    store.add(two, 1);

    assertThat(
        events,
        contains(
            new StoreEvent<>(two.getId(), null, two), new StoreEvent<>(one.getId(), one, null)));
  }

  private static MessageServeEvent messageServeEvent() {
    return MessageServeEvent.builder()
        .withEventType(MessageServeEvent.EventType.RECEIVED)
        .withChannel(new LoggedRequestInitiatedChannel(UUID.randomUUID(), true, null))
        .withMessage(Message.builder().withTextBody("test").build())
        .build();
  }
}
