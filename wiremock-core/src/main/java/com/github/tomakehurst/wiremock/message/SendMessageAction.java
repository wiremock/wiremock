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
package com.github.tomakehurst.wiremock.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Objects;

public class SendMessageAction implements MessageAction {

  private final MessageDefinition message;
  private final RequestPattern targetChannelPattern;
  private final boolean sendToOriginatingChannel;

  @JsonCreator
  public SendMessageAction(
      @JsonProperty("message") MessageDefinition message,
      @JsonProperty("targetChannelPattern") RequestPattern targetChannelPattern,
      @JsonProperty("sendToOriginatingChannel") Boolean sendToOriginatingChannel) {
    this.message = message;
    this.targetChannelPattern = targetChannelPattern;
    this.sendToOriginatingChannel =
        sendToOriginatingChannel != null ? sendToOriginatingChannel : false;
  }

  public static SendMessageAction toOriginatingChannel(EntityDefinition body) {
    return new SendMessageAction(new MessageDefinition(body), null, true);
  }

  public static SendMessageAction toOriginatingChannel(String messageBody) {
    return toOriginatingChannel(new StringEntityDefinition(messageBody));
  }

  public static SendMessageAction toMatchingChannels(
      EntityDefinition body, RequestPattern targetChannelPattern) {
    return new SendMessageAction(new MessageDefinition(body), targetChannelPattern, false);
  }

  public static SendMessageAction toMatchingChannels(
      String messageBody, RequestPattern targetChannelPattern) {
    return toMatchingChannels(new StringEntityDefinition(messageBody), targetChannelPattern);
  }

  public MessageDefinition getMessage() {
    return message;
  }

  @JsonIgnore
  public EntityDefinition getBody() {
    return message != null ? message.getBody() : null;
  }

  public RequestPattern getTargetChannelPattern() {
    return targetChannelPattern;
  }

  public boolean isSendToOriginatingChannel() {
    return sendToOriginatingChannel;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SendMessageAction that = (SendMessageAction) o;
    return sendToOriginatingChannel == that.sendToOriginatingChannel
        && Objects.equals(message, that.message)
        && Objects.equals(targetChannelPattern, that.targetChannelPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, targetChannelPattern, sendToOriginatingChannel);
  }

  @Override
  public String toString() {
    return "SendMessageAction{"
        + "message='"
        + message
        + '\''
        + ", targetChannelPattern="
        + targetChannelPattern
        + ", sendToOriginatingChannel="
        + sendToOriginatingChannel
        + '}';
  }
}
