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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SendMessageAction implements MessageAction {

  private final EntityDefinition body;
  private final RequestPattern targetChannelPattern;
  private final boolean sendToOriginatingChannel;

  @JsonCreator
  public SendMessageAction(
      @JsonProperty("body") EntityDefinition body,
      @JsonProperty("targetChannelPattern") RequestPattern targetChannelPattern,
      @JsonProperty("sendToOriginatingChannel") Boolean sendToOriginatingChannel) {
    this.body = body;
    this.targetChannelPattern = targetChannelPattern;
    this.sendToOriginatingChannel =
        sendToOriginatingChannel != null ? sendToOriginatingChannel : false;
  }

  public static SendMessageAction toOriginatingChannel(EntityDefinition message) {
    return new SendMessageAction(message, null, true);
  }

  public static SendMessageAction toOriginatingChannel(String message) {
    return toOriginatingChannel(new StringEntityDefinition(message));
  }

  public static SendMessageAction toMatchingChannels(
      EntityDefinition message, RequestPattern targetChannelPattern) {
    return new SendMessageAction(message, targetChannelPattern, false);
  }

  public static SendMessageAction toMatchingChannels(
      String message, RequestPattern targetChannelPattern) {
    return toMatchingChannels(new StringEntityDefinition(message), targetChannelPattern);
  }

  public EntityDefinition getBody() {
    return body;
  }

  public RequestPattern getTargetChannelPattern() {
    return targetChannelPattern;
  }

  public boolean isSendToOriginatingChannel() {
    return sendToOriginatingChannel;
  }

  @Override
  public void execute(
      MessageChannel originatingChannel, MessageChannels messageChannels, String incomingMessage) {
    MessageDefinition messageDefinition = new MessageDefinition(body);
    if (sendToOriginatingChannel) {
      originatingChannel.sendMessage(messageDefinition);
    } else if (targetChannelPattern != null) {
      List<MessageChannel> matchingChannels =
          messageChannels.findByRequestPattern(targetChannelPattern, Collections.emptyMap());
      for (MessageChannel channel : matchingChannels) {
        channel.sendMessage(messageDefinition);
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SendMessageAction that = (SendMessageAction) o;
    return sendToOriginatingChannel == that.sendToOriginatingChannel
        && Objects.equals(body, that.body)
        && Objects.equals(targetChannelPattern, that.targetChannelPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, targetChannelPattern, sendToOriginatingChannel);
  }

  @Override
  public String toString() {
    return "SendMessageAction{"
        + "message='"
        + body
        + '\''
        + ", targetChannelPattern="
        + targetChannelPattern
        + ", sendToOriginatingChannel="
        + sendToOriginatingChannel
        + '}';
  }
}
