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
package com.github.tomakehurst.wiremock.websocket.message;

import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.store.MessageStubMappingStore;
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.MessageChannels;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages message stub mappings. When a message arrives on a channel, it is tested against each
 * stub mapping in priority order until one matches.
 */
public class MessageStubMappings {

  private final MessageStubMappingStore store;
  private final Map<String, RequestMatcherExtension> customMatchers;

  public MessageStubMappings(
      MessageStubMappingStore store, Map<String, RequestMatcherExtension> customMatchers) {
    this.store = store;
    this.customMatchers = customMatchers;
  }

  /** Adds a new message stub mapping. */
  public void add(MessageStubMapping mapping) {
    store.add(mapping);
  }

  /** Removes a message stub mapping by its ID. */
  public void remove(UUID id) {
    store.remove(id);
  }

  /** Gets a message stub mapping by its ID. */
  public Optional<MessageStubMapping> get(UUID id) {
    return store.get(id);
  }

  /** Returns all message stub mappings. */
  public List<MessageStubMapping> getAll() {
    return store.getAll().collect(Collectors.toList());
  }

  /** Returns all message stub mappings sorted by priority. */
  public List<MessageStubMapping> getAllSortedByPriority() {
    return store
        .getAll()
        .sorted(
            Comparator.comparingInt(
                m ->
                    m.getPriority() != null ? m.getPriority() : MessageStubMapping.DEFAULT_PRIORITY))
        .collect(Collectors.toList());
  }

  /** Clears all message stub mappings. */
  public void clear() {
    store.clear();
  }

  /** Returns the number of message stub mappings. */
  public int size() {
    return (int) store.getAll().count();
  }

  /**
   * Processes an incoming message on a channel. Finds the first matching stub mapping and executes
   * its actions.
   *
   * @param channel the channel on which the message was received
   * @param message the message content
   * @param messageChannels the collection of all message channels (for action execution)
   * @return true if a matching stub was found and executed, false otherwise
   */
  public boolean processMessage(
      MessageChannel channel, String message, MessageChannels messageChannels) {
    Optional<MessageStubMapping> matchingStub = findMatchingStub(channel, message);
    if (matchingStub.isPresent()) {
      matchingStub.get().executeActions(channel, messageChannels, message);
      return true;
    }
    return false;
  }

  /**
   * Finds the first stub mapping that matches the given message on the given channel.
   *
   * @param channel the channel on which the message was received
   * @param message the message content
   * @return the matching stub mapping, or empty if none matches
   */
  public Optional<MessageStubMapping> findMatchingStub(MessageChannel channel, String message) {
    return getAllSortedByPriority().stream()
        .filter(stub -> stub.matches(channel, message, customMatchers))
        .findFirst();
  }
}
