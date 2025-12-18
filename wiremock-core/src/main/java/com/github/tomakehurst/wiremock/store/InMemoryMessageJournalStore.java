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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

/**
 * In-memory implementation of MessageJournalStore. Stores message serve events in memory with
 * support for event listeners.
 */
@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryMessageJournalStore implements MessageJournalStore {

  private final Deque<UUID> deque = new ConcurrentLinkedDeque<>();
  private final Map<UUID, MessageServeEvent> events = new ConcurrentHashMap<>();
  private final List<Consumer<? super StoreEvent<UUID, MessageServeEvent>>> eventListeners =
      new CopyOnWriteArrayList<>();

  @Override
  public void add(MessageServeEvent event) {
    MessageServeEvent previous = events.put(event.getId(), event);
    deque.addFirst(event.getId());
    notifyListeners(new StoreEvent<>(event.getId(), previous, event));
  }

  @Override
  public Stream<MessageServeEvent> getAll() {
    return deque.stream().map(events::get).filter(Objects::nonNull);
  }

  @Override
  public void removeLast() {
    final UUID id = deque.pollLast();
    if (id != null) {
      MessageServeEvent removed = events.remove(id);
      if (removed != null) {
        notifyListeners(new StoreEvent<>(id, removed, null));
      }
    }
  }

  @Override
  public Stream<UUID> getAllKeys() {
    return getAll().map(MessageServeEvent::getId);
  }

  @Override
  public Optional<MessageServeEvent> get(UUID id) {
    return Optional.ofNullable(events.get(id));
  }

  @Override
  public void put(UUID id, MessageServeEvent event) {
    if (deque.contains(id)) {
      MessageServeEvent previous = events.put(id, event);
      notifyListeners(new StoreEvent<>(id, previous, event));
    }
  }

  @Override
  public void remove(UUID id) {
    deque.stream().filter(eventId -> eventId.equals(id)).forEach(deque::remove);
    MessageServeEvent removed = events.remove(id);
    if (removed != null) {
      notifyListeners(new StoreEvent<>(id, removed, null));
    }
  }

  @Override
  public void clear() {
    deque.clear();
    events.clear();
  }

  @Override
  public void registerEventListener(
      Consumer<? super StoreEvent<UUID, MessageServeEvent>> listener) {
    eventListeners.add(listener);
  }

  private void notifyListeners(StoreEvent<UUID, MessageServeEvent> event) {
    for (Consumer<? super StoreEvent<UUID, MessageServeEvent>> listener : eventListeners) {
      try {
        listener.accept(event);
      } catch (Exception e) {
        // Ignore listener exceptions to prevent them from affecting the store operation
      }
    }
  }
}
