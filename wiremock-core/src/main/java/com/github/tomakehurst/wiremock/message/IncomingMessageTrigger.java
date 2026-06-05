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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Objects;

@JsonInclude(NON_EMPTY)
public class IncomingMessageTrigger implements MessageTrigger {

  public static final IncomingMessageTrigger ANYTHING = new IncomingMessageTrigger(null, null);

  private final ChannelPattern channelPattern;
  private final MessagePattern messagePattern;

  @JsonCreator
  public IncomingMessageTrigger(
      @JsonProperty("channel") ChannelPattern channelPattern,
      @JsonProperty("message") MessagePattern messagePattern) {
    this.channelPattern = channelPattern;
    this.messagePattern = messagePattern;
  }

  public ChannelPattern getChannel() {
    return channelPattern;
  }

  public MessagePattern getMessage() {
    return messagePattern;
  }

  @JsonIgnore
  public ContentPattern<?> getBodyPattern() {
    return messagePattern != null ? messagePattern.getBodyPattern() : null;
  }

  @JsonIgnore
  public RequestPattern getInitiatingRequestPattern() {
    if (channelPattern instanceof RequestInitiatedChannelPattern) {
      return ((RequestInitiatedChannelPattern) channelPattern).getInitiatingRequestPattern();
    }
    return null;
  }

  public boolean matches(MessageChannel channel, Message message) {
    if (channelPattern != null && !channelPattern.matches(channel)) {
      return false;
    }

    if (messagePattern != null) {
      return messagePattern.matches(channel, message);
    }

    return true;
  }

  public boolean matches(Request channelRequest, Message message) {
    if (channelPattern instanceof RequestInitiatedChannelPattern requestInitiated) {
      if (!requestInitiated.matches(channelRequest)) {
        return false;
      }
    } else if (channelPattern != null) {
      return false;
    }

    if (messagePattern != null) {
      return messagePattern.matches(channelRequest, message);
    }

    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IncomingMessageTrigger that = (IncomingMessageTrigger) o;
    return Objects.equals(channelPattern, that.channelPattern)
        && Objects.equals(messagePattern, that.messagePattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(channelPattern, messagePattern);
  }
}
