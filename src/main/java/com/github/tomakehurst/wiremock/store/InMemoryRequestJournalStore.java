/*
 * Copyright (C) 2022-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryRequestJournalStore implements RequestJournalStore {

  private final Deque<UUID> deque = new ConcurrentLinkedDeque<>();
  private final Map<UUID, ServeEvent> serveEvents = new ConcurrentHashMap<>();

  @Override
  public void add(ServeEvent event) {
    serveEvents.put(event.getId(), event);
    deque.addFirst(event.getId());
  }

  @Override
  public Stream<ServeEvent> getAll() {
    return deque.stream().map(serveEvents::get).filter(Objects::nonNull);
  }

  @Override
  public void removeLast() {
    final UUID id = deque.pollLast();
    if (id != null) {
      serveEvents.remove(id);
    }
  }

  @Override
  public Stream<UUID> getAllKeys() {
    return getAll().map(ServeEvent::getId);
  }

  @Override
  public Optional<ServeEvent> get(UUID id) {
    return Optional.ofNullable(serveEvents.get(id));
  }

  @Override
  public void put(UUID id, ServeEvent event) {
    if (deque.contains(id)) {
      serveEvents.put(id, event);
    }
  }

  @Override
  public void remove(UUID id) {
    getAndRemove(id);
  }

  @Override
  public Optional<ServeEvent> getAndRemove(UUID id) {
    deque.stream().filter(eventId -> eventId.equals(id)).forEach(deque::remove);
    return Optional.ofNullable(serveEvents.remove(id));
  }

  @Override
  public void clear() {
    deque.clear();
    serveEvents.clear();
  }
}
