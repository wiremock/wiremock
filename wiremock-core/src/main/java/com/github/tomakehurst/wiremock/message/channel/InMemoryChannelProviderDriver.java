/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.message.channel;

import com.github.tomakehurst.wiremock.message.Message;

public class InMemoryChannelProviderDriver implements ChannelProviderDriver {

  public static final String TYPE = "in-memory";

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void createChannel(ChannelProvider provider, String channelName) {
    // No-op for in-memory: channels are created on demand when messages are sent
  }

  @Override
  public void send(ChannelProvider provider, String channelName, Message message) {
    // Delivery is handled by ChannelProviderRegistry which records to the journal.
    // The in-memory driver has no additional transport to perform.
  }
}
