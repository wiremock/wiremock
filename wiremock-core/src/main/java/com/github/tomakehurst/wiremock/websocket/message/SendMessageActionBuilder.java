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

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

/**
 * Builder for creating SendMessageAction instances using a fluent DSL.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * sendMessage("hello").onOriginatingChannel()
 * sendMessage("broadcast").onChannelsMatching(newRequestPattern().withUrl("/broadcast").build())
 * }</pre>
 */
public class SendMessageActionBuilder {

  private final String message;

  /**
   * Creates a new builder for a message action.
   *
   * @param message the message to send
   */
  public SendMessageActionBuilder(String message) {
    this.message = message;
  }

  /**
   * Configures the action to send the message to the originating channel (the channel on which the
   * triggering message was received).
   *
   * @return the built SendMessageAction
   */
  public SendMessageAction onOriginatingChannel() {
    return SendMessageAction.toOriginatingChannel(message);
  }

  /**
   * Configures the action to send the message to all channels matching the specified pattern.
   *
   * @param targetChannelPattern the pattern to match target channels
   * @return the built SendMessageAction
   */
  public SendMessageAction onChannelsMatching(RequestPattern targetChannelPattern) {
    return SendMessageAction.toMatchingChannels(message, targetChannelPattern);
  }

  /**
   * Configures the action to send the message to all channels matching the specified pattern
   * builder.
   *
   * @param targetChannelPatternBuilder the pattern builder to match target channels
   * @return the built SendMessageAction
   */
  public SendMessageAction onChannelsMatching(RequestPatternBuilder targetChannelPatternBuilder) {
    return SendMessageAction.toMatchingChannels(message, targetChannelPatternBuilder.build());
  }
}
