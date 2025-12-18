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
import com.github.tomakehurst.wiremock.verification.MessageJournal;
import com.github.tomakehurst.wiremock.verification.MessageServeEvent;
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.MessageChannels;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageStubMappings {

  private final MessageStubMappingStore store;
  private final Map<String, RequestMatcherExtension> customMatchers;
  private final MessageJournal messageJournal;

  public MessageStubMappings(
      MessageStubMappingStore store,
      Map<String, RequestMatcherExtension> customMatchers,
      MessageJournal messageJournal) {
    this.store = store;
    this.customMatchers = customMatchers;
    this.messageJournal = messageJournal;
  }

  public void add(MessageStubMapping mapping) {
    store.add(mapping);
  }

  public void remove(UUID id) {
    store.remove(id);
  }

  public Optional<MessageStubMapping> get(UUID id) {
    return store.get(id);
  }

  public List<MessageStubMapping> getAll() {
    return store.getAll().collect(Collectors.toList());
  }

  public List<MessageStubMapping> getAllSortedByPriority() {
    return store
        .getAll()
        .sorted(
            Comparator.comparingInt(
                m ->
                    m.getPriority() != null
                        ? m.getPriority()
                        : MessageStubMapping.DEFAULT_PRIORITY))
        .collect(Collectors.toList());
  }

  public void clear() {
    store.clear();
  }

  public int size() {
    return (int) store.getAll().count();
  }

  public boolean processMessage(
      MessageChannel channel, String message, MessageChannels messageChannels) {
    Optional<MessageStubMapping> matchingStub = findMatchingStub(channel, message);
    if (matchingStub.isPresent()) {
      MessageStubMapping stub = matchingStub.get();
      stub.executeActions(channel, messageChannels, message);

      MessageServeEvent event = MessageServeEvent.receivedMatched(channel, message, stub);
      messageJournal.messageReceived(event);
      return true;
    } else {
      MessageServeEvent event = MessageServeEvent.receivedUnmatched(channel, message);
      messageJournal.messageReceived(event);
    }
    return false;
  }

  public Optional<MessageStubMapping> findMatchingStub(MessageChannel channel, String message) {
    return getAllSortedByPriority().stream()
        .filter(stub -> stub.matches(channel, message, customMatchers))
        .findFirst();
  }
}
