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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.MessageChannel;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import java.util.UUID;

public class LoggedMessageChannel {

  private final UUID id;
  private final ChannelType type;
  private final LoggedRequest initiatingRequest;
  private final boolean open;

  @JsonCreator
  public LoggedMessageChannel(
      @JsonProperty("id") UUID id,
      @JsonProperty("type") ChannelType type,
      @JsonProperty("initiatingRequest") LoggedRequest initiatingRequest,
      @JsonProperty("open") boolean open) {
    this.id = id;
    this.type = type;
    this.initiatingRequest = initiatingRequest;
    this.open = open;
  }

  public static LoggedMessageChannel createFrom(MessageChannel channel) {
    LoggedRequest loggedRequest = null;
    if (channel instanceof RequestInitiatedMessageChannel) {
      loggedRequest =
          LoggedRequest.createFrom(
              ((RequestInitiatedMessageChannel) channel).getInitiatingRequest());
    }
    return new LoggedMessageChannel(
        channel.getId(), channel.getType(), loggedRequest, channel.isOpen());
  }

  public UUID getId() {
    return id;
  }

  public ChannelType getType() {
    return type;
  }

  public LoggedRequest getInitiatingRequest() {
    return initiatingRequest;
  }

  @JsonProperty("open")
  public boolean isOpen() {
    return open;
  }
}
