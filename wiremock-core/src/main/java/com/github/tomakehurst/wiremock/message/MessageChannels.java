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
package com.github.tomakehurst.wiremock.message;

import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.store.MessageChannelStore;
import com.github.tomakehurst.wiremock.store.Stores;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageChannels {

  private final MessageChannelStore store;
  private final Stores stores;

  public MessageChannels(Stores stores) {
    this.store = stores.getMessageChannelStore();
    this.stores = stores;
  }

  public void add(MessageChannel channel) {
    store.add(channel);
  }

  public void remove(UUID id) {
    store.remove(id).ifPresent(MessageChannel::close);
  }

  public Optional<MessageChannel> get(UUID id) {
    return store.get(id);
  }

  public List<MessageChannel> getAll() {
    return store.getAll().collect(Collectors.toList());
  }

  public List<MessageChannel> getAllByType(ChannelType type) {
    return store.getAll().filter(channel -> channel.getType() == type).collect(Collectors.toList());
  }

  public List<MessageChannel> getAllOpen() {
    return store.getAll().filter(MessageChannel::isOpen).collect(Collectors.toList());
  }

  public List<MessageChannel> getAllOpenByType(ChannelType type) {
    return store
        .getAll()
        .filter(MessageChannel::isOpen)
        .filter(channel -> channel.getType() == type)
        .collect(Collectors.toList());
  }

  public List<RequestInitiatedMessageChannel> findByRequestPattern(
      RequestPattern requestPattern, Map<String, RequestMatcherExtension> customMatchers) {
    return store
        .getAll()
        .filter(MessageChannel::isOpen)
        .filter(RequestInitiatedMessageChannel.class::isInstance)
        .map(RequestInitiatedMessageChannel.class::cast)
        .filter(
            channel ->
                requestPattern.match(channel.getInitiatingRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  public List<RequestInitiatedMessageChannel> findByTypeAndRequestPattern(
      ChannelType type,
      RequestPattern requestPattern,
      Map<String, RequestMatcherExtension> customMatchers) {
    return store
        .getAll()
        .filter(MessageChannel::isOpen)
        .filter(channel -> channel.getType() == type)
        .filter(RequestInitiatedMessageChannel.class::isInstance)
        .map(RequestInitiatedMessageChannel.class::cast)
        .filter(
            channel ->
                requestPattern.match(channel.getInitiatingRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  public int sendMessageToMatching(
      RequestPattern requestPattern,
      MessageDefinition messageDefinition,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<RequestInitiatedMessageChannel> matchingChannels =
        findByRequestPattern(requestPattern, customMatchers);
    Message message = new Message(messageDefinition.getBody().resolve(stores));
    for (RequestInitiatedMessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels.size();
  }

  public List<RequestInitiatedMessageChannel> sendMessageToMatchingByType(
      ChannelType type,
      RequestPattern requestPattern,
      MessageDefinition messageDefinition,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<RequestInitiatedMessageChannel> matchingChannels =
        findByTypeAndRequestPattern(type, requestPattern, customMatchers);
    Message message = new Message(messageDefinition.getBody().resolve(stores));
    for (RequestInitiatedMessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels;
  }

  public int size() {
    return (int) store.getAll().count();
  }

  public int sizeByType(ChannelType type) {
    return (int) store.getAll().filter(channel -> channel.getType() == type).count();
  }

  public int openCount() {
    return (int) store.getAll().filter(MessageChannel::isOpen).count();
  }

  public int openCountByType(ChannelType type) {
    return (int)
        store
            .getAll()
            .filter(MessageChannel::isOpen)
            .filter(channel -> channel.getType() == type)
            .count();
  }
}
