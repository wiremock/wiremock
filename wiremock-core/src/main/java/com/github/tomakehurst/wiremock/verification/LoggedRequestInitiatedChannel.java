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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.message.ChannelType;
import java.util.UUID;
import java.util.function.Consumer;

@JsonInclude(NON_NULL)
@JsonPropertyOrder({"type", "id", "open", "initiatingRequest"})
public final class LoggedRequestInitiatedChannel implements LoggedMessageChannel {

  private final UUID id;
  private final ChannelType type;
  private final LoggedRequest initiatingRequest;
  private final boolean open;

  @JsonCreator
  public LoggedRequestInitiatedChannel(
      @JsonProperty("id") UUID id,
      @JsonProperty("open") boolean open,
      @JsonProperty("initiatingRequest") LoggedRequest initiatingRequest) {
    this.id = id;
    this.type = ChannelType.WEBSOCKET;
    this.open = open;
    this.initiatingRequest = initiatingRequest;
  }

  LoggedRequestInitiatedChannel(
      UUID id, ChannelType type, LoggedRequest initiatingRequest, boolean open) {
    this.id = id;
    this.type = type;
    this.open = open;
    this.initiatingRequest = initiatingRequest;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  @JsonIgnore
  public ChannelType getType() {
    return type;
  }

  @Override
  @JsonProperty("open")
  public boolean isOpen() {
    return open;
  }

  @JsonSerialize(using = NonEmptyFieldsSerializer.class)
  public LoggedRequest getInitiatingRequest() {
    return initiatingRequest;
  }

  public LoggedRequestInitiatedChannel transform(Consumer<Builder> consumer) {
    Builder builder = new Builder(this);
    consumer.accept(builder);
    return builder.build();
  }

  public static class Builder {
    private UUID id;
    private ChannelType type;
    private LoggedRequest initiatingRequest;
    private boolean open;

    public Builder() {}

    public Builder(LoggedRequestInitiatedChannel existing) {
      this.id = existing.id;
      this.type = existing.type;
      this.initiatingRequest = existing.initiatingRequest;
      this.open = existing.open;
    }

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder type(ChannelType type) {
      this.type = type;
      return this;
    }

    public Builder initiatingRequest(LoggedRequest initiatingRequest) {
      this.initiatingRequest = initiatingRequest;
      return this;
    }

    public Builder open(boolean open) {
      this.open = open;
      return this;
    }

    public LoggedRequestInitiatedChannel build() {
      return new LoggedRequestInitiatedChannel(id, type, initiatingRequest, open);
    }
  }
}
