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

import static com.github.tomakehurst.wiremock.client.WireMock.text;

import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.util.ArrayList;
import java.util.List;

public class SendMessageActionBuilder {

  private TextEntityDefinition.Builder textEntityBuilder = text();
  private final List<String> transformers = new ArrayList<>();
  private Parameters transformerParameters = Parameters.empty();

  public SendMessageActionBuilder() {}

  public SendMessageActionBuilder withBody(String message) {
    this.textEntityBuilder.withBody(message);
    return this;
  }

  public SendMessageActionBuilder withBodyFromStore(String storeName, String key) {
    textEntityBuilder.withDataStore(storeName);
    textEntityBuilder.withDataRef(key);
    return this;
  }

  public SendMessageActionBuilder withBodyFromFile(String filePath) {
    textEntityBuilder.withFilePath(filePath);
    return this;
  }

  public SendMessageActionBuilder withTransformer(String transformerName) {
    this.transformers.add(transformerName);
    return this;
  }

  public SendMessageActionBuilder withTransformers(String... transformerNames) {
    for (String name : transformerNames) {
      this.transformers.add(name);
    }
    return this;
  }

  public SendMessageActionBuilder withTransformerParameters(Parameters parameters) {
    this.transformerParameters = parameters;
    return this;
  }

  public SendMessageActionBuilder withTransformerParameter(String key, Object value) {
    this.transformerParameters = this.transformerParameters.merge(Parameters.one(key, value));
    return this;
  }

  private EntityDefinition resolveBody() {
    return textEntityBuilder.build();
  }

  public SendMessageAction onOriginatingChannel() {
    return new SendMessageAction(
        new MessageDefinition(resolveBody()),
        OriginatingChannelTarget.INSTANCE,
        transformers,
        transformerParameters);
  }

  public SendMessageAction onChannelsMatching(RequestPattern targetChannelPattern) {
    return new SendMessageAction(
        new MessageDefinition(resolveBody()),
        RequestInitiatedChannelTarget.forPattern(targetChannelPattern),
        transformers,
        transformerParameters);
  }

  public SendMessageAction onChannelsMatching(RequestPatternBuilder targetChannelPatternBuilder) {
    return onChannelsMatching(targetChannelPatternBuilder.build());
  }

  public TargetedSendMessageActionBuilder toOriginatingChannel() {
    return new TargetedSendMessageActionBuilder(
        OriginatingChannelTarget.INSTANCE, transformers, transformerParameters);
  }

  public TargetedSendMessageActionBuilder toMatchingChannels(RequestPattern targetChannelPattern) {
    return new TargetedSendMessageActionBuilder(
        RequestInitiatedChannelTarget.forPattern(targetChannelPattern),
        transformers,
        transformerParameters);
  }

  public TargetedSendMessageActionBuilder toMatchingChannels(
      RequestPatternBuilder targetChannelPatternBuilder) {
    return new TargetedSendMessageActionBuilder(
        RequestInitiatedChannelTarget.forPattern(targetChannelPatternBuilder.build()),
        transformers,
        transformerParameters);
  }

  public static class TargetedSendMessageActionBuilder {
    private final ChannelTarget channelTarget;
    private final List<String> transformers;
    private final Parameters transformerParameters;

    TargetedSendMessageActionBuilder(
        ChannelTarget channelTarget, List<String> transformers, Parameters transformerParameters) {
      this.channelTarget = channelTarget;
      this.transformers = transformers;
      this.transformerParameters = transformerParameters;
    }

    public SendMessageAction withMessage(EntityDefinition body) {
      return new SendMessageAction(
          new MessageDefinition(body), channelTarget, transformers, transformerParameters);
    }

    public SendMessageAction withMessage(EntityDefinition.Builder<?> bodyBuilder) {
      return withMessage(bodyBuilder.build());
    }
  }
}
