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
package com.github.tomakehurst.wiremock.verification;

import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.message.MessagePattern;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.store.MessageJournalStore;
import com.github.tomakehurst.wiremock.store.StoreEvent;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class StoreBackedMessageJournal implements MessageJournal {

  protected final MessageJournalStore store;
  private final Integer maxEntries;

  public StoreBackedMessageJournal(Integer maxEntries, MessageJournalStore store) {
    if (maxEntries != null && maxEntries < 0) {
      throw new IllegalArgumentException(
          "Maximum number of entries of journal must be greater than zero");
    }
    this.maxEntries = maxEntries;
    this.store = store;
  }

  @Override
  public int countEventsMatching(MessagePattern pattern) {
    return (int) store.getAll().filter(pattern::matches).count();
  }

  @Override
  public List<MessageServeEvent> getEventsMatching(MessagePattern pattern) {
    List<MessageServeEvent> events = store.getAll().filter(pattern::matches).collect(toList());
    Collections.reverse(events);
    return events;
  }

  @Override
  public List<MessageServeEvent> getAllMessageServeEvents() {
    return store.getAll().collect(toList());
  }

  @Override
  public Optional<MessageServeEvent> getMessageServeEvent(UUID id) {
    return store.get(id);
  }

  @Override
  public void reset() {
    store.clear();
  }

  @Override
  public void messageReceived(MessageServeEvent event) {
    store.add(event);
    removeOldEntries();
  }

  @Override
  public void removeEvent(UUID eventId) {
    store.remove(eventId);
  }

  @Override
  public List<MessageServeEvent> removeEventsMatching(MessagePattern pattern) {
    List<MessageServeEvent> toDelete = store.getAll().filter(pattern::matches).collect(toList());
    for (MessageServeEvent event : toDelete) {
      store.remove(event.getId());
    }
    return toDelete;
  }

  @Override
  public List<MessageServeEvent> removeEventsForStubsMatchingMetadata(
      StringValuePattern metadataPattern) {
    return removeEventsMatching(withStubMetadataMatching(metadataPattern));
  }

  @Override
  public Optional<MessageServeEvent> waitForEvent(MessagePattern pattern, Duration maxWait) {
    Optional<MessageServeEvent> existing = store.getAll().filter(pattern::matches).findFirst();
    if (existing.isPresent()) {
      return existing;
    }

    CountDownLatch latch = new CountDownLatch(1);
    final MessageServeEvent[] result = new MessageServeEvent[1];

    Consumer<StoreEvent<UUID, MessageServeEvent>> listener =
        storeEvent -> {
          if (storeEvent.getNewValue() != null && pattern.matches(storeEvent.getNewValue())) {
            result[0] = storeEvent.getNewValue();
            latch.countDown();
          }
        };

    store.registerEventListener(listener);

    try {
      boolean found = latch.await(maxWait.toMillis(), TimeUnit.MILLISECONDS);
      if (found) {
        return Optional.ofNullable(result[0]);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      store.unregisterEventListener(listener);
    }

    return store.getAll().filter(pattern::matches).findFirst();
  }

  @Override
  public List<MessageServeEvent> waitForEvents(
      MessagePattern pattern, int count, Duration maxWait) {
    long deadline = System.currentTimeMillis() + maxWait.toMillis();
    while (System.currentTimeMillis() < deadline) {
      List<MessageServeEvent> current = store.getAll().filter(pattern::matches).collect(toList());
      if (current.size() >= count) {
        return current.subList(0, count);
      }

      long remaining = deadline - System.currentTimeMillis();
      if (remaining > 0) {
        CountDownLatch latch = new CountDownLatch(1);
        Consumer<StoreEvent<UUID, MessageServeEvent>> listener =
            storeEvent -> {
              if (storeEvent.getNewValue() != null && pattern.matches(storeEvent.getNewValue())) {
                latch.countDown();
              }
            };

        store.registerEventListener(listener);

        try {
          latch.await(Math.min(remaining, 100), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        } finally {
          store.unregisterEventListener(listener);
        }
      }
    }

    return store.getAll().filter(pattern::matches).limit(count).collect(toList());
  }

  private void removeOldEntries() {
    if (maxEntries != null) {
      while (store.getAllKeys().count() > maxEntries) {
        store.removeLast();
      }
    }
  }

  private static MessagePattern withStubMetadataMatching(final StringValuePattern metadataPattern) {
    return new MessagePattern(null, null) {
      @Override
      public boolean matches(MessageServeEvent event) {
        MessageStubMapping stub = event.getStubMapping();
        if (stub != null) {
          String metadataJson = Json.write(stub.getMetadata());
          return metadataPattern.match(metadataJson).isExactMatch();
        }
        return false;
      }
    };
  }
}
