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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.tomakehurst.wiremock.message.ChannelType;
import com.github.tomakehurst.wiremock.message.FixedChannel;
import com.github.tomakehurst.wiremock.message.MessageChannel;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = LoggedRequestInitiatedChannel.class, name = "websocket"),
  @JsonSubTypes.Type(value = LoggedFixedChannel.class, name = "fixed")
})
@NullMarked
public sealed interface LoggedMessageChannel
    permits LoggedRequestInitiatedChannel, LoggedFixedChannel {

  UUID getId();

  ChannelType getType();

  boolean isOpen();

  static LoggedMessageChannel createFrom(MessageChannel channel) {
    if (channel instanceof FixedChannel fixedChannel) {
      return new LoggedFixedChannel(
          channel.getId(),
          channel.isOpen(),
          fixedChannel.getProviderName(),
          fixedChannel.getChannelName());
    }
    LoggedRequest loggedRequest = null;
    if (channel instanceof RequestInitiatedMessageChannel requestChannel) {
      loggedRequest = LoggedRequest.createFrom(requestChannel.getInitiatingRequest());
    }
    return new LoggedRequestInitiatedChannel(
        channel.getId(), channel.getType(), loggedRequest, channel.isOpen());
  }
}
