/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class InMemoryRequestJournalStore implements RequestJournalStore {

  private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();
  private final Map<UUID, ServeEvent> serveEvents = new ConcurrentHashMap<>();

  @Override
  public void add(ServeEvent event) {
    serveEvents.put(event.getId(), event);
    queue.add(event.getId());
  }

  @Override
  public Stream<ServeEvent> getAll() {
    return queue.stream().map(serveEvents::get);
  }

  @Override
  public void removeLast() {
    final UUID id = queue.poll();
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
    if (queue.contains(id)) {
      serveEvents.put(id, event);
    }
  }

  @Override
  public void remove(UUID id) {
    queue.stream().filter(eventId -> eventId.equals(id)).forEach(queue::remove);
    serveEvents.remove(id);
  }

  @Override
  public void clear() {
    queue.clear();
    serveEvents.clear();
  }
}
