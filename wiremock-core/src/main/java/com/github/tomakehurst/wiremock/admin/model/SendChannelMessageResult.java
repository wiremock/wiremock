/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.verification.LoggedMessageChannel;
import java.util.Collections;
import java.util.List;

/** Result model for sending a message to channels. */
public class SendChannelMessageResult {

  private final List<LoggedMessageChannel> channels;

  @JsonCreator
  public SendChannelMessageResult(@JsonProperty("channels") List<LoggedMessageChannel> channels) {
    this.channels = channels != null ? channels : Collections.emptyList();
  }

  public List<LoggedMessageChannel> getChannels() {
    return channels;
  }

  public int getChannelsMessaged() {
    return channels.size();
  }
}
