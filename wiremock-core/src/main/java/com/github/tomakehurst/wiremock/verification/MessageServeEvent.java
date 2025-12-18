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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.websocket.ChannelType;
import com.github.tomakehurst.wiremock.websocket.MessageChannel;
import com.github.tomakehurst.wiremock.websocket.message.MessageStubMapping;
import com.google.common.base.Stopwatch;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class MessageServeEvent {

  public enum EventType {
    RECEIVED,
    SENT
  }

  private final UUID id;
  private final EventType eventType;
  private final ChannelType channelType;
  private final UUID channelId;
  private final LoggedRequest channelRequest;
  private final String message;
  private final MessageStubMapping stubMapping;
  private final boolean wasMatched;
  private final Instant timestamp;
  private final ConcurrentLinkedQueue<SubEvent> subEvents;
  private final Stopwatch stopwatch;

  @JsonCreator
  public MessageServeEvent(
      @JsonProperty("id") UUID id,
      @JsonProperty("eventType") EventType eventType,
      @JsonProperty("channelType") ChannelType channelType,
      @JsonProperty("channelId") UUID channelId,
      @JsonProperty("channelRequest") LoggedRequest channelRequest,
      @JsonProperty("message") String message,
      @JsonProperty("stubMapping") MessageStubMapping stubMapping,
      @JsonProperty("wasMatched") boolean wasMatched,
      @JsonProperty("timestamp") Instant timestamp,
      @JsonProperty("subEvents") Queue<SubEvent> subEvents) {
    this.id = id != null ? id : UUID.randomUUID();
    this.eventType = eventType;
    this.channelType = channelType;
    this.channelId = channelId;
    this.channelRequest = channelRequest;
    this.message = message;
    this.stubMapping = stubMapping;
    this.wasMatched = wasMatched;
    this.timestamp = timestamp != null ? timestamp : Instant.now();
    this.subEvents =
        subEvents != null ? new ConcurrentLinkedQueue<>(subEvents) : new ConcurrentLinkedQueue<>();
    this.stopwatch = Stopwatch.createStarted();
  }

  private MessageServeEvent(
      UUID id,
      EventType eventType,
      ChannelType channelType,
      UUID channelId,
      LoggedRequest channelRequest,
      String message,
      MessageStubMapping stubMapping,
      boolean wasMatched,
      Instant timestamp,
      ConcurrentLinkedQueue<SubEvent> subEvents,
      Stopwatch stopwatch) {
    this.id = id;
    this.eventType = eventType;
    this.channelType = channelType;
    this.channelId = channelId;
    this.channelRequest = channelRequest;
    this.message = message;
    this.stubMapping = stubMapping;
    this.wasMatched = wasMatched;
    this.timestamp = timestamp;
    this.subEvents = subEvents;
    this.stopwatch = stopwatch;
  }

  public static MessageServeEvent receivedMatched(
      ChannelType channelType,
      UUID channelId,
      Request channelRequest,
      String message,
      MessageStubMapping stubMapping) {
    return new MessageServeEvent(
        UUID.randomUUID(),
        EventType.RECEIVED,
        channelType,
        channelId,
        LoggedRequest.createFrom(channelRequest),
        message,
        stubMapping,
        true,
        Instant.now(),
        null);
  }

  public static MessageServeEvent receivedMatched(
      MessageChannel channel, String message, MessageStubMapping stubMapping) {
    return receivedMatched(
        channel.getType(), channel.getId(), channel.getRequest(), message, stubMapping);
  }

  public static MessageServeEvent receivedUnmatched(
      ChannelType channelType, UUID channelId, Request channelRequest, String message) {
    return new MessageServeEvent(
        UUID.randomUUID(),
        EventType.RECEIVED,
        channelType,
        channelId,
        LoggedRequest.createFrom(channelRequest),
        message,
        null,
        false,
        Instant.now(),
        null);
  }

  public static MessageServeEvent receivedUnmatched(MessageChannel channel, String message) {
    return receivedUnmatched(channel.getType(), channel.getId(), channel.getRequest(), message);
  }

  public static MessageServeEvent sent(
      ChannelType channelType, UUID channelId, Request channelRequest, String message) {
    return new MessageServeEvent(
        UUID.randomUUID(),
        EventType.SENT,
        channelType,
        channelId,
        LoggedRequest.createFrom(channelRequest),
        message,
        null,
        true,
        Instant.now(),
        null);
  }

  public static MessageServeEvent sent(MessageChannel channel, String message) {
    return sent(channel.getType(), channel.getId(), channel.getRequest(), message);
  }

  public UUID getId() {
    return id;
  }

  public EventType getEventType() {
    return eventType;
  }

  public ChannelType getChannelType() {
    return channelType;
  }

  public UUID getChannelId() {
    return channelId;
  }

  public LoggedRequest getChannelRequest() {
    return channelRequest;
  }

  public String getMessage() {
    return message;
  }

  public MessageStubMapping getStubMapping() {
    return stubMapping;
  }

  public boolean getWasMatched() {
    return wasMatched;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public Queue<? extends SubEvent> getSubEvents() {
    return subEvents;
  }

  @JsonIgnore
  public boolean isReceived() {
    return eventType == EventType.RECEIVED;
  }

  @JsonIgnore
  public boolean isSent() {
    return eventType == EventType.SENT;
  }

  public void appendSubEvent(String type, Object data) {
    final long elapsedNanos = stopwatch.elapsed(java.util.concurrent.TimeUnit.NANOSECONDS);
    appendSubEvent(new SubEvent(type, elapsedNanos, data));
  }

  public void appendSubEvent(SubEvent subEvent) {
    subEvents.add(subEvent);
  }

  public MessageServeEvent withStubMapping(MessageStubMapping stubMapping) {
    return new MessageServeEvent(
        id,
        eventType,
        channelType,
        channelId,
        channelRequest,
        message,
        stubMapping,
        stubMapping != null,
        timestamp,
        subEvents,
        stopwatch);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static MessageServeEvent create(Consumer<Builder> transformer) {
    final Builder builder = builder();
    transformer.accept(builder);
    return builder.build();
  }

  public MessageServeEvent transform(Consumer<Builder> transformer) {
    final Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MessageServeEvent that = (MessageServeEvent) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public static class Builder {
    private UUID id;
    private EventType eventType;
    private ChannelType channelType;
    private UUID channelId;
    private LoggedRequest channelRequest;
    private String message;
    private MessageStubMapping stubMapping;
    private boolean wasMatched;
    private Instant timestamp;
    private Queue<SubEvent> subEvents;

    public Builder() {}

    public Builder(MessageServeEvent existing) {
      this.id = existing.id;
      this.eventType = existing.eventType;
      this.channelType = existing.channelType;
      this.channelId = existing.channelId;
      this.channelRequest = existing.channelRequest;
      this.message = existing.message;
      this.stubMapping = existing.stubMapping;
      this.wasMatched = existing.wasMatched;
      this.timestamp = existing.timestamp;
      this.subEvents = new ConcurrentLinkedQueue<>(existing.subEvents);
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public Builder withEventType(EventType eventType) {
      this.eventType = eventType;
      return this;
    }

    public Builder withChannelType(ChannelType channelType) {
      this.channelType = channelType;
      return this;
    }

    public Builder withChannelId(UUID channelId) {
      this.channelId = channelId;
      return this;
    }

    public Builder withChannelRequest(LoggedRequest channelRequest) {
      this.channelRequest = channelRequest;
      return this;
    }

    public Builder withMessage(String message) {
      this.message = message;
      return this;
    }

    public Builder withStubMapping(MessageStubMapping stubMapping) {
      this.stubMapping = stubMapping;
      return this;
    }

    public Builder withWasMatched(boolean wasMatched) {
      this.wasMatched = wasMatched;
      return this;
    }

    public Builder withTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder withSubEvents(Queue<SubEvent> subEvents) {
      this.subEvents = subEvents;
      return this;
    }

    public MessageServeEvent build() {
      return new MessageServeEvent(
          id,
          eventType,
          channelType,
          channelId,
          channelRequest,
          message,
          stubMapping,
          wasMatched,
          timestamp,
          subEvents);
    }
  }
}
