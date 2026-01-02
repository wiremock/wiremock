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
import java.util.*;
import java.util.stream.Collectors;

public class MessageChannels {

  private final MessageChannelStore store;

  public MessageChannels(MessageChannelStore store) {
    this.store = store;
  }

  public void add(MessageChannel channel) {
    store.add(channel);
  }

  public void remove(UUID id) {
    Optional<MessageChannel> channel = store.get(id);
    store.remove(id);
    channel.ifPresent(MessageChannel::close);
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

  public List<MessageChannel> findByRequestPattern(
      RequestPattern requestPattern, Map<String, RequestMatcherExtension> customMatchers) {
    return store
        .getAll()
        .filter(MessageChannel::isOpen)
        .filter(
            channel -> requestPattern.match(channel.getRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  public List<MessageChannel> findByTypeAndRequestPattern(
      ChannelType type,
      RequestPattern requestPattern,
      Map<String, RequestMatcherExtension> customMatchers) {
    return store
        .getAll()
        .filter(MessageChannel::isOpen)
        .filter(channel -> channel.getType() == type)
        .filter(
            channel -> requestPattern.match(channel.getRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  public int sendMessageToMatching(
      RequestPattern requestPattern,
      MessageDefinition messageDefinition,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<MessageChannel> matchingChannels = findByRequestPattern(requestPattern, customMatchers);
    Message message = MessageStubRequestHandler.resolveToMessage(messageDefinition, null);
    for (MessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels.size();
  }

  public int sendMessageToMatchingByType(
      ChannelType type,
      RequestPattern requestPattern,
      MessageDefinition messageDefinition,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<MessageChannel> matchingChannels =
        findByTypeAndRequestPattern(type, requestPattern, customMatchers);
    Message message = MessageStubRequestHandler.resolveToMessage(messageDefinition, null);
    for (MessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels.size();
  }

  public void clear() {
    store.getAll().forEach(MessageChannel::close);
    store.clear();
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
