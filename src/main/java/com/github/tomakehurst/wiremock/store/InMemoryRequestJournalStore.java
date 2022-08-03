/*
 * Copyright (C) 2022 Thomas Akehurst
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
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class InMemoryRequestJournalStore implements RequestJournalStore {

  private final Queue<ServeEvent> serveEvents = new ConcurrentLinkedQueue<>();

  @Override
  public void add(ServeEvent event) {
    serveEvents.add(event);
  }

  @Override
  public Stream<ServeEvent> getAll() {
    return serveEvents.stream();
  }

  @Override
  public void removeLast() {
    serveEvents.poll();
  }

  @Override
  public Stream<UUID> getAllKeys() {
    return getAll().map(ServeEvent::getId);
  }

  @Override
  public Optional<ServeEvent> get(UUID id) {
    return serveEvents.stream().filter(event -> event.getId().equals(id)).findFirst();
  }

  @Override
  public void put(UUID key, ServeEvent event) {
    add(event);
  }

  @Override
  public void remove(UUID key) {
    get(key).ifPresent(serveEvents::remove);
  }

  @Override
  public void clear() {
    serveEvents.clear();
  }

  @Override
  public void start() {}

  @Override
  public void stop() {}
}
