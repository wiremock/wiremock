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
package com.github.tomakehurst.wiremock.websocket;

import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages message channels. Similar to StubMappings, this class stores and manages MessageChannel
 * instances of various types (WebSocket, SSE, etc.).
 */
public class MessageChannels {

  private final Map<UUID, MessageChannel> channels = new ConcurrentHashMap<>();

  /** Adds a new message channel. */
  public void add(MessageChannel channel) {
    channels.put(channel.getId(), channel);
  }

  /** Removes a message channel by its ID. */
  public void remove(UUID id) {
    MessageChannel channel = channels.remove(id);
    if (channel != null) {
      channel.close();
    }
  }

  /** Gets a message channel by its ID. */
  public Optional<MessageChannel> get(UUID id) {
    return Optional.ofNullable(channels.get(id));
  }

  /** Returns all message channels. */
  public List<MessageChannel> getAll() {
    return new ArrayList<>(channels.values());
  }

  /** Returns all message channels of the specified type. */
  public List<MessageChannel> getAllByType(ChannelType type) {
    return channels.values().stream()
        .filter(channel -> channel.getType() == type)
        .collect(Collectors.toList());
  }

  /** Returns all open message channels. */
  public List<MessageChannel> getAllOpen() {
    return channels.values().stream().filter(MessageChannel::isOpen).collect(Collectors.toList());
  }

  /** Returns all open message channels of the specified type. */
  public List<MessageChannel> getAllOpenByType(ChannelType type) {
    return channels.values().stream()
        .filter(MessageChannel::isOpen)
        .filter(channel -> channel.getType() == type)
        .collect(Collectors.toList());
  }

  /**
   * Finds all message channels whose originating request matches the given request pattern.
   *
   * @param requestPattern the pattern to match against
   * @param customMatchers custom request matchers
   * @return list of matching message channels
   */
  public List<MessageChannel> findByRequestPattern(
      RequestPattern requestPattern, Map<String, RequestMatcherExtension> customMatchers) {
    return channels.values().stream()
        .filter(MessageChannel::isOpen)
        .filter(
            channel -> requestPattern.match(channel.getRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  /**
   * Finds all message channels of the specified type whose originating request matches the given
   * request pattern.
   *
   * @param type the channel type to filter by
   * @param requestPattern the pattern to match against
   * @param customMatchers custom request matchers
   * @return list of matching message channels
   */
  public List<MessageChannel> findByTypeAndRequestPattern(
      ChannelType type,
      RequestPattern requestPattern,
      Map<String, RequestMatcherExtension> customMatchers) {
    return channels.values().stream()
        .filter(MessageChannel::isOpen)
        .filter(channel -> channel.getType() == type)
        .filter(
            channel -> requestPattern.match(channel.getRequest(), customMatchers).isExactMatch())
        .collect(Collectors.toList());
  }

  /**
   * Sends a message to all channels matching the given request pattern.
   *
   * @param requestPattern the pattern to match against
   * @param message the message to send
   * @param customMatchers custom request matchers
   * @return the number of channels the message was sent to
   */
  public int sendMessageToMatching(
      RequestPattern requestPattern,
      String message,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<MessageChannel> matchingChannels = findByRequestPattern(requestPattern, customMatchers);
    for (MessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels.size();
  }

  /**
   * Sends a message to all channels of the specified type matching the given request pattern.
   *
   * @param type the channel type to filter by
   * @param requestPattern the pattern to match against
   * @param message the message to send
   * @param customMatchers custom request matchers
   * @return the number of channels the message was sent to
   */
  public int sendMessageToMatchingByType(
      ChannelType type,
      RequestPattern requestPattern,
      String message,
      Map<String, RequestMatcherExtension> customMatchers) {
    List<MessageChannel> matchingChannels =
        findByTypeAndRequestPattern(type, requestPattern, customMatchers);
    for (MessageChannel channel : matchingChannels) {
      channel.sendMessage(message);
    }
    return matchingChannels.size();
  }

  /** Clears all message channels, closing them first. */
  public void clear() {
    for (MessageChannel channel : channels.values()) {
      channel.close();
    }
    channels.clear();
  }

  /** Returns the number of channels. */
  public int size() {
    return channels.size();
  }

  /** Returns the number of channels of the specified type. */
  public int sizeByType(ChannelType type) {
    return (int) channels.values().stream().filter(channel -> channel.getType() == type).count();
  }

  /** Returns the number of open channels. */
  public int openCount() {
    return (int) channels.values().stream().filter(MessageChannel::isOpen).count();
  }

  /** Returns the number of open channels of the specified type. */
  public int openCountByType(ChannelType type) {
    return (int)
        channels.values().stream()
            .filter(MessageChannel::isOpen)
            .filter(channel -> channel.getType() == type)
            .count();
  }
}
