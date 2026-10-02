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
package com.github.tomakehurst.wiremock.verification;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.github.tomakehurst.wiremock.verification.MessageServeEvent.EventType.RECEIVED;
import static com.github.tomakehurst.wiremock.verification.MessageServeEvent.EventType.SENT;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.common.ParameterUtils;
import com.github.tomakehurst.wiremock.common.entity.Entity;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageChannel;
import com.github.tomakehurst.wiremock.message.MessageStubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.google.common.base.Stopwatch;
import java.time.Instant;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@JsonInclude(NON_EMPTY)
@NullMarked
public class MessageServeEvent {

  public enum EventType {
    RECEIVED,
    SENT;

    @JsonValue
    public String toJson() {
      return name().toLowerCase(java.util.Locale.ROOT);
    }

    @JsonCreator
    public static EventType fromJson(String value) {
      return valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
  }

  private final UUID id;
  private final EventType eventType;
  private final LoggedMessageChannel channel;
  private final Message message;
  @Nullable private final MessageStubMapping stubMapping;
  private final boolean wasMatched;
  private final Instant timestamp;
  private final ConcurrentLinkedQueue<SubEvent> subEvents;
  private final Stopwatch stopwatch;

  @JsonCreator
  public MessageServeEvent(
      @Nullable @JsonProperty("id") UUID id,
      @JsonProperty("eventType") EventType eventType,
      @JsonProperty("channel") LoggedMessageChannel channel,
      @JsonProperty("message") Message message,
      @Nullable @JsonProperty("stubMapping") MessageStubMapping stubMapping,
      @JsonProperty("wasMatched") boolean wasMatched,
      @Nullable @JsonProperty("timestamp") Instant timestamp,
      @Nullable @JsonProperty("subEvents") Queue<SubEvent> subEvents) {
    this.id = id != null ? id : UUID.randomUUID();
    this.eventType = eventType;
    this.channel = channel;
    this.message = message;
    this.stubMapping = stubMapping;
    this.wasMatched = wasMatched;
    this.timestamp = timestamp != null ? timestamp : Instant.now();
    this.subEvents =
        subEvents != null ? new ConcurrentLinkedQueue<>(subEvents) : new ConcurrentLinkedQueue<>();
    this.stopwatch = Stopwatch.createStarted();
  }

  // --- Factory methods taking a live MessageChannel ---

  public static MessageServeEvent receivedMatched(
      MessageChannel channel, Message message, MessageStubMapping stubMapping) {
    return receivedMatched(LoggedMessageChannel.createFrom(channel), message, stubMapping);
  }

  public static MessageServeEvent receivedUnmatched(MessageChannel channel, Message message) {
    return receivedUnmatched(LoggedMessageChannel.createFrom(channel), message);
  }

  public static MessageServeEvent sent(MessageChannel channel, Message message) {
    return sent(LoggedMessageChannel.createFrom(channel), message);
  }

  // --- Factory methods taking an already-logged LoggedMessageChannel ---

  public static MessageServeEvent receivedMatched(
      LoggedMessageChannel channel, Message message, MessageStubMapping stubMapping) {
    return new MessageServeEvent(
        UUID.randomUUID(), RECEIVED, channel, message, stubMapping, true, Instant.now(), null);
  }

  public static MessageServeEvent receivedUnmatched(LoggedMessageChannel channel, Message message) {
    return new MessageServeEvent(
        UUID.randomUUID(), RECEIVED, channel, message, null, false, Instant.now(), null);
  }

  public static MessageServeEvent sent(LoggedMessageChannel channel, Message message) {
    return new MessageServeEvent(
        UUID.randomUUID(), SENT, channel, message, null, true, Instant.now(), null);
  }

  // --- Accessors ---

  public UUID getId() {
    return id;
  }

  public EventType getEventType() {
    return eventType;
  }

  public LoggedMessageChannel getChannel() {
    return channel;
  }

  /** Derived from {@link #getChannel()} — present for convenience. */
  @JsonIgnore
  public ChannelType getChannelType() {
    return channel.getType();
  }

  /** Derived from {@link #getChannel()} — present for convenience. */
  @JsonIgnore
  public UUID getChannelId() {
    return channel.getId();
  }

  /** Derived from {@link #getChannel()} — the initiating HTTP request, if applicable. */
  @JsonIgnore
  @Nullable
  public LoggedRequest getChannelRequest() {
    return channel instanceof LoggedRequestInitiatedChannel ric ? ric.getInitiatingRequest() : null;
  }

  public Message getMessage() {
    return message;
  }

  @Nullable
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
    return eventType == RECEIVED;
  }

  @JsonIgnore
  public boolean isSent() {
    return eventType == SENT;
  }

  public void appendSubEvent(String type, Object data) {
    final long elapsedNanos = stopwatch.elapsed(java.util.concurrent.TimeUnit.NANOSECONDS);
    appendSubEvent(new SubEvent(type, elapsedNanos, data));
  }

  public void appendSubEvent(SubEvent subEvent) {
    subEvents.add(subEvent);
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
    if (getClass() != o.getClass()) return false;
    MessageServeEvent that = (MessageServeEvent) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @NullUnmarked
  public static class Builder {
    private UUID id = UUID.randomUUID();
    private EventType eventType;
    private LoggedMessageChannel channel;
    private Message message = new Message(Entity.EMPTY);
    private MessageStubMapping stubMapping;
    private boolean wasMatched;
    private Instant timestamp;
    private Queue<SubEvent> subEvents;

    public Builder() {}

    public Builder(MessageServeEvent existing) {
      this.id = existing.id;
      this.eventType = existing.eventType;
      this.channel = existing.channel;
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

    public Builder withChannel(LoggedMessageChannel channel) {
      this.channel = channel;
      return this;
    }

    public Builder withMessage(Message message) {
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
      ParameterUtils.checkNotNull(eventType, "event type is required");
      ParameterUtils.checkNotNull(channel, "channel is required");
      ParameterUtils.checkNotNull(message, "message is required");
      return new MessageServeEvent(
          id, eventType, channel, message, stubMapping, wasMatched, timestamp, subEvents);
    }
  }
}
