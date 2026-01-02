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

import static com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition.aTextMessage;

import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

public class SendMessageActionBuilder {

  private TextEntityDefinition.Builder textEntityBuilder = aTextMessage();

  public SendMessageActionBuilder() {}

  public SendMessageActionBuilder withBody(String message) {
    this.textEntityBuilder.withBody(message);
    return this;
  }

  public SendMessageActionBuilder withBody(Object data) {
    textEntityBuilder.withBody(data);
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

  private EntityDefinition resolveBody() {
    return textEntityBuilder.build();
  }

  public SendMessageAction onOriginatingChannel() {
    return SendMessageAction.toOriginatingChannel(resolveBody());
  }

  public SendMessageAction onChannelsMatching(RequestPattern targetChannelPattern) {
    return SendMessageAction.toMatchingChannels(resolveBody(), targetChannelPattern);
  }

  public SendMessageAction onChannelsMatching(RequestPatternBuilder targetChannelPatternBuilder) {
    return SendMessageAction.toMatchingChannels(resolveBody(), targetChannelPatternBuilder.build());
  }

  public TargetedSendMessageActionBuilder toOriginatingChannel() {
    return new TargetedSendMessageActionBuilder(true, null);
  }

  public TargetedSendMessageActionBuilder toMatchingChannels(RequestPattern targetChannelPattern) {
    return new TargetedSendMessageActionBuilder(false, targetChannelPattern);
  }

  public TargetedSendMessageActionBuilder toMatchingChannels(
      RequestPatternBuilder targetChannelPatternBuilder) {
    return new TargetedSendMessageActionBuilder(false, targetChannelPatternBuilder.build());
  }

  public static class TargetedSendMessageActionBuilder {
    private final boolean sendToOriginatingChannel;
    private final RequestPattern targetChannelPattern;

    TargetedSendMessageActionBuilder(
        boolean sendToOriginatingChannel, RequestPattern targetChannelPattern) {
      this.sendToOriginatingChannel = sendToOriginatingChannel;
      this.targetChannelPattern = targetChannelPattern;
    }

    public SendMessageAction withMessage(EntityDefinition body) {
      if (sendToOriginatingChannel) {
        return SendMessageAction.toOriginatingChannel(body);
      } else {
        return SendMessageAction.toMatchingChannels(body, targetChannelPattern);
      }
    }

    public SendMessageAction withMessage(EntityDefinition.Builder<?> bodyBuilder) {
      return withMessage(bodyBuilder.build());
    }
  }
}
