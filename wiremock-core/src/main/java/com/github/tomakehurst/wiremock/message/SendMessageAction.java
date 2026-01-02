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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SendMessageAction implements MessageAction {

  private final MessageDefinition message;
  private final RequestPattern targetChannelPattern;
  private final boolean sendToOriginatingChannel;
  private final List<String> transformers;
  private final Parameters transformerParameters;

  @JsonCreator
  public SendMessageAction(
      @JsonProperty("message") MessageDefinition message,
      @JsonProperty("targetChannelPattern") RequestPattern targetChannelPattern,
      @JsonProperty("sendToOriginatingChannel") Boolean sendToOriginatingChannel,
      @JsonProperty("transformers") List<String> transformers,
      @JsonProperty("transformerParameters") Parameters transformerParameters) {
    this.message = message;
    this.targetChannelPattern = targetChannelPattern;
    this.sendToOriginatingChannel =
        sendToOriginatingChannel != null ? sendToOriginatingChannel : false;
    this.transformers = transformers != null ? new ArrayList<>(transformers) : new ArrayList<>();
    this.transformerParameters =
        transformerParameters != null ? transformerParameters : Parameters.empty();
  }

  public static SendMessageAction toOriginatingChannel(EntityDefinition body) {
    return new SendMessageAction(new MessageDefinition(body), null, true, null, null);
  }

  public static SendMessageAction toOriginatingChannel(String messageBody) {
    return toOriginatingChannel(new StringEntityDefinition(messageBody));
  }

  public static SendMessageAction toMatchingChannels(
      EntityDefinition body, RequestPattern targetChannelPattern) {
    return new SendMessageAction(
        new MessageDefinition(body), targetChannelPattern, false, null, null);
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

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<String> getTransformers() {
    return Collections.unmodifiableList(transformers);
  }

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Parameters getTransformerParameters() {
    return transformerParameters;
  }

  @Override
  public boolean hasTransformer(Extension transformer) {
    return transformers.contains(transformer.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SendMessageAction that = (SendMessageAction) o;
    return sendToOriginatingChannel == that.sendToOriginatingChannel
        && Objects.equals(message, that.message)
        && Objects.equals(targetChannelPattern, that.targetChannelPattern)
        && Objects.equals(transformers, that.transformers)
        && Objects.equals(transformerParameters, that.transformerParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        message,
        targetChannelPattern,
        sendToOriginatingChannel,
        transformers,
        transformerParameters);
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
        + ", transformers="
        + transformers
        + '}';
  }
}
