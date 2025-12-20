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

import static com.github.tomakehurst.wiremock.common.entity.FullEntityDefinition.aMessage;

import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.FullEntityDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

public class SendMessageActionBuilder {

  private FullEntityDefinition.Builder fullEntityBuilder = aMessage();

  public SendMessageActionBuilder() {}

  public SendMessageActionBuilder withBody(String message) {
    this.fullEntityBuilder.withBody(message);
    return this;
  }

  public SendMessageActionBuilder withBody(Object data) {
    fullEntityBuilder.withBody(data);
    return this;
  }

  public SendMessageActionBuilder withBodyFromStore(String storeName, String key) {
    fullEntityBuilder.withDataStore(storeName);
    fullEntityBuilder.withDataRef(key);
    return this;
  }

  private EntityDefinition resolveBody() {
    return fullEntityBuilder.build();
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
}
