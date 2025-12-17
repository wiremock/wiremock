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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.MessageChannels;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Defines a stub mapping for incoming websocket messages. When a message arrives on a websocket, it
 * is tested against each MessageStubMapping in turn until one matches. When a match is found, all
 * configured actions are executed.
 */
public class MessageStubMapping {

  public static final int DEFAULT_PRIORITY = 5;

  private final UUID id;
  private final String name;
  private final Integer priority;
  private final RequestPattern channelPattern;
  private final StringValuePattern messagePattern;
  private final List<MessageAction> actions;

  @JsonCreator
  public MessageStubMapping(
      @JsonProperty("id") UUID id,
      @JsonProperty("name") String name,
      @JsonProperty("priority") Integer priority,
      @JsonProperty("channelPattern") RequestPattern channelPattern,
      @JsonProperty("messagePattern") StringValuePattern messagePattern,
      @JsonProperty("actions") List<MessageAction> actions) {
    this.id = id != null ? id : UUID.randomUUID();
    this.name = name;
    this.priority = priority;
    this.channelPattern = channelPattern;
    this.messagePattern = messagePattern;
    this.actions = actions != null ? actions : Collections.emptyList();
  }

  public static Builder builder() {
    return new Builder();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Integer getPriority() {
    return priority;
  }

  public RequestPattern getChannelPattern() {
    return channelPattern;
  }

  public StringValuePattern getMessagePattern() {
    return messagePattern;
  }

  public List<MessageAction> getActions() {
    return actions;
  }

  /**
   * Tests whether this stub mapping matches the given message on the given channel.
   *
   * @param channel the channel on which the message was received
   * @param message the message content
   * @param customMatchers custom request matchers for channel pattern matching
   * @return true if this stub matches the message
   */
  public boolean matches(
      MessageChannel channel,
      String message,
      Map<String, com.github.tomakehurst.wiremock.matching.RequestMatcherExtension>
          customMatchers) {
    // Check channel pattern if specified
    if (channelPattern != null) {
      MatchResult channelMatch = channelPattern.match(channel.getRequest(), customMatchers);
      if (!channelMatch.isExactMatch()) {
        return false;
      }
    }

    // Check message pattern if specified
    if (messagePattern != null) {
      MatchResult messageMatch = messagePattern.match(message);
      if (!messageMatch.isExactMatch()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Executes all actions configured for this stub mapping.
   *
   * @param originatingChannel the channel on which the message was received
   * @param messageChannels the collection of all message channels
   * @param incomingMessage the message that triggered this stub
   */
  public void executeActions(
      MessageChannel originatingChannel, MessageChannels messageChannels, String incomingMessage) {
    for (MessageAction action : actions) {
      action.execute(originatingChannel, messageChannels, incomingMessage);
    }
  }

  public int comparePriorityWith(MessageStubMapping other) {
    int thisPriority = priority != null ? priority : DEFAULT_PRIORITY;
    int otherPriority = other.priority != null ? other.priority : DEFAULT_PRIORITY;
    return thisPriority - otherPriority;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    MessageStubMapping that = (MessageStubMapping) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "MessageStubMapping{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", priority="
        + priority
        + ", channelPattern="
        + channelPattern
        + ", messagePattern="
        + messagePattern
        + ", actions="
        + actions
        + '}';
  }

  public static class Builder {
    private UUID id;
    private String name;
    private Integer priority;
    private RequestPattern channelPattern;
    private StringValuePattern messagePattern;
    private java.util.ArrayList<MessageAction> actions = new java.util.ArrayList<>();

    public Builder() {}

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withPriority(Integer priority) {
      this.priority = priority;
      return this;
    }

    public Builder withChannelPattern(RequestPattern channelPattern) {
      this.channelPattern = channelPattern;
      return this;
    }

    public Builder withMessagePattern(StringValuePattern messagePattern) {
      this.messagePattern = messagePattern;
      return this;
    }

    public Builder withActions(List<MessageAction> actions) {
      this.actions = new java.util.ArrayList<>(actions);
      return this;
    }

    public Builder withAction(MessageAction action) {
      this.actions.add(action);
      return this;
    }

    public MessageStubMapping build() {
      return new MessageStubMapping(id, name, priority, channelPattern, messagePattern, actions);
    }
  }
}
