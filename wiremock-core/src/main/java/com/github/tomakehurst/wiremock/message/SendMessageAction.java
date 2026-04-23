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
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

@JsonInclude(NON_EMPTY)
public class SendMessageAction implements MessageAction {

  @NonNull private final MessageDefinition message;
  @NonNull private final ChannelTarget channelTarget;
  @NonNull private final List<String> transformers;
  @NonNull private final Parameters transformerParameters;

  @JsonCreator
  public SendMessageAction(
      @JsonProperty("message") MessageDefinition message,
      @JsonProperty("channelTarget") ChannelTarget channelTarget,
      @JsonProperty("transformers") List<String> transformers,
      @JsonProperty("transformerParameters") Parameters transformerParameters) {
    this.message = message;
    this.channelTarget = channelTarget != null ? channelTarget : OriginatingChannelTarget.INSTANCE;
    this.transformers = transformers != null ? new ArrayList<>(transformers) : new ArrayList<>();
    this.transformerParameters =
        transformerParameters != null ? transformerParameters : Parameters.empty();
  }

  public static SendMessageAction toOriginatingChannel(EntityDefinition body) {
    return new SendMessageAction(
        new MessageDefinition(body), OriginatingChannelTarget.INSTANCE, null, null);
  }

  public static SendMessageAction toOriginatingChannel(String messageBody) {
    return toOriginatingChannel(new StringEntityDefinition(messageBody));
  }

  public static SendMessageAction toMatchingChannels(
      EntityDefinition body, RequestPattern targetChannelPattern) {
    return new SendMessageAction(
        new MessageDefinition(body),
        RequestInitiatedChannelTarget.forPattern(targetChannelPattern),
        null,
        null);
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

  public ChannelTarget getChannelTarget() {
    return channelTarget;
  }

  @JsonInclude(NON_EMPTY)
  public List<String> getTransformers() {
    return Collections.unmodifiableList(transformers);
  }

  @JsonInclude(NON_EMPTY)
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
    return Objects.equals(message, that.message)
        && Objects.equals(channelTarget, that.channelTarget)
        && Objects.equals(transformers, that.transformers)
        && Objects.equals(transformerParameters, that.transformerParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, channelTarget, transformers, transformerParameters);
  }

  @Override
  public String toString() {
    return "SendMessageAction{"
        + "message='"
        + message
        + '\''
        + ", channelTarget="
        + channelTarget
        + ", transformers="
        + transformers
        + '}';
  }
}
