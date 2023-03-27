/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.matching.RequestPattern.thatMatch;
import static com.github.tomakehurst.wiremock.matching.RequestPattern.withRequstMatching;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.store.RequestJournalStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.ImmutableList;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class AbstractRequestJournal implements RequestJournal {

  protected final RequestJournalStore store;

  private final Optional<Integer> maxEntries;
  private final Map<String, RequestMatcherExtension> customMatchers;

  public AbstractRequestJournal(
      Optional<Integer> maxEntries,
      Map<String, RequestMatcherExtension> customMatchers,
      RequestJournalStore store) {

    if (maxEntries.isPresent() && maxEntries.get() < 0) {
      throw new IllegalArgumentException(
          "Maximum number of entries of journal must be greater than zero");
    }
    this.maxEntries = maxEntries;
    this.customMatchers = customMatchers;
    this.store = store;
  }

  @Override
  public int countRequestsMatching(RequestPattern requestPattern) {
    return (int) getRequests().filter(thatMatch(requestPattern, customMatchers)).count();
  }

  @Override
  public List<LoggedRequest> getRequestsMatching(RequestPattern requestPattern) {
    return getRequests().filter(thatMatch(requestPattern, customMatchers)).collect(toList());
  }

  @Override
  public void requestReceived(ServeEvent serveEvent) {
    store.add(serveEvent);
    removeOldEntries();
  }

  @Override
  public void removeEvent(final UUID eventId) {
    store.remove(eventId);
  }

  @Override
  public List<ServeEvent> removeEventsMatching(RequestPattern requestPattern) {
    return removeServeEvents(withRequstMatching(requestPattern)::apply);
  }

  @Override
  public List<ServeEvent> removeServeEventsForStubsMatchingMetadata(
      StringValuePattern metadataPattern) {
    return removeServeEvents(withStubMetadataMatching(metadataPattern));
  }

  private List<ServeEvent> removeServeEvents(Predicate<ServeEvent> predicate) {
    List<ServeEvent> toDelete = store.getAll().filter(predicate).collect(toList());

    for (ServeEvent event : toDelete) {
      store.remove(event.getId());
    }

    return toDelete;
  }

  @Override
  public List<ServeEvent> getAllServeEvents() {
    return ImmutableList.copyOf(store.getAll().collect(toList())).reverse();
  }

  @Override
  public Optional<ServeEvent> getServeEvent(final UUID id) {
    return store.get(id);
  }

  @Override
  public void reset() {
    store.clear();
  }

  private Stream<LoggedRequest> getRequests() {
    return store.getAll().map(ServeEvent::getRequest);
  }

  private void removeOldEntries() {
    if (maxEntries.isPresent()) {
      while (store.getAllKeys().count() > maxEntries.get()) {
        store.removeLast();
      }
    }
  }

  private static Predicate<ServeEvent> withStubMetadataMatching(
      final StringValuePattern metadataPattern) {

    return (ServeEvent serveEvent) -> {
      StubMapping stub = serveEvent.getStubMapping();
      if (stub != null) {
        String metadataJson = Json.write(stub.getMetadata());
        return metadataPattern.match(metadataJson).isExactMatch();
      }

      return false;
    };
  }
}
