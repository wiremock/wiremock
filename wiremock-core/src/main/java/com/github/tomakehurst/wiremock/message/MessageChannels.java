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

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.common.ConflictException;
import com.github.tomakehurst.wiremock.common.Errors;
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

  public MessageChannels(Stores stores) {
    this.store = stores.getMessageChannelStore();
  }

  public void add(MessageChannel channel) {
    if (channel instanceof FixedChannel fixedChannel
        && findFixed(fixedChannel.getProviderName(), fixedChannel.getChannelName()).isPresent()) {
      throw new ConflictException(
          Errors.single(
              409,
              "Channel %s already exists via provider %s"
                  .formatted(fixedChannel.getChannelName(), fixedChannel.getProviderName())));
    }
    store.add(channel);
  }

  public void remove(UUID id) {
    store.get(id).ifPresent(MessageChannel::close);
    store.remove(id);
  }

  public Optional<MessageChannel> get(UUID id) {
    return store.get(id);
  }

  public List<MessageChannel> getAll() {
    return store.getAll().collect(Collectors.toList());
  }

  public Optional<FixedChannel> findFixed(String providerName, String channelName) {
    return store
        .getAll()
        .filter(FixedChannel.class::isInstance)
        .map(FixedChannel.class::cast)
        .filter(
            c -> c.getProviderName().equals(providerName) && c.getChannelName().equals(channelName))
        .findFirst();
  }

  public FixedChannel requireFixed(String providerName, String channelName) {
    return findFixed(providerName, channelName)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "No fixed channel named '"
                        + channelName
                        + "' exists on provider '"
                        + providerName
                        + "'"));
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

  public List<RequestInitiatedMessageChannel> sendMessageToMatchingByType(
      ChannelType type,
      RequestPattern requestPattern,
      Message message,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<RequestInitiatedMessageChannel> matchingChannels =
        findByTypeAndRequestPattern(type, requestPattern, customMatchers);
    for (RequestInitiatedMessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels;
  }

  public int size() {
    return (int) store.getAll().count();
  }
}
