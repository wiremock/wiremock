/*
 * Copyright (C) 2026 Thomas Akehurst
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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.tomakehurst.wiremock.message.ChannelType;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

@JsonPropertyOrder({"type", "id", "open", "providerName", "channelName"})
@NullMarked
public final class LoggedFixedChannel implements LoggedMessageChannel {

  private final UUID id;
  private final boolean open;
  private final String providerName;
  private final String channelName;

  @JsonCreator
  public LoggedFixedChannel(
      @JsonProperty("id") UUID id,
      @JsonProperty("open") boolean open,
      @JsonProperty("providerName") String providerName,
      @JsonProperty("channelName") String channelName) {
    this.id = id;
    this.open = open;
    this.providerName = providerName;
    this.channelName = channelName;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  @JsonIgnore
  public ChannelType getType() {
    return ChannelType.FIXED;
  }

  @Override
  @JsonProperty("open")
  public boolean isOpen() {
    return open;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getChannelName() {
    return channelName;
  }

  public LoggedFixedChannel transform(Consumer<Builder> consumer) {
    Builder builder = new Builder(this);
    consumer.accept(builder);
    return builder.build();
  }

  @NullUnmarked
  public static class Builder {
    private UUID id;
    private boolean open;
    private String providerName;
    private String channelName;

    public Builder() {}

    public Builder(LoggedFixedChannel existing) {
      this.id = existing.id;
      this.open = existing.open;
      this.providerName = existing.providerName;
      this.channelName = existing.channelName;
    }

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder open(boolean open) {
      this.open = open;
      return this;
    }

    public Builder providerName(String providerName) {
      this.providerName = providerName;
      return this;
    }

    public Builder channelName(String channelName) {
      this.channelName = channelName;
      return this;
    }

    public LoggedFixedChannel build() {
      return new LoggedFixedChannel(id, open, providerName, channelName);
    }
  }
}
