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

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

public class SendMessageActionBuilder {

  private final String message;

  public SendMessageActionBuilder(String message) {
    this.message = message;
  }

  public SendMessageAction onOriginatingChannel() {
    return SendMessageAction.toOriginatingChannel(message);
  }

  public SendMessageAction onChannelsMatching(RequestPattern targetChannelPattern) {
    return SendMessageAction.toMatchingChannels(message, targetChannelPattern);
  }

  public SendMessageAction onChannelsMatching(RequestPatternBuilder targetChannelPatternBuilder) {
    return SendMessageAction.toMatchingChannels(message, targetChannelPatternBuilder.build());
  }
}
