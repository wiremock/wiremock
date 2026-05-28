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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChannelProviderDriver implements ChannelProviderDriver {

  public static final String TYPE = "in-memory";

  private final Map<String, InboundMessageSink> sinks = new ConcurrentHashMap<>();

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void createChannel(ChannelProvider provider, String channelName, InboundMessageSink sink) {
    sinks.put(key(provider.getName(), channelName), sink);
  }

  @Override
  public void send(ChannelProvider provider, String channelName, Message message) {
    // Delivery is handled by ChannelProviderRegistry which records to the journal.
    // The in-memory driver has no additional transport to perform.
  }

  /**
   * Simulates an inbound message arriving on the named channel. Routes directly into WireMock's
   * stub-matching pipeline via the sink registered at channel-creation time.
   */
  public void receive(String providerName, String channelName, Message message) {
    InboundMessageSink sink = sinks.get(key(providerName, channelName));
    if (sink == null) {
      throw new IllegalStateException(
          "No channel registered with name '"
              + channelName
              + "' on provider '"
              + providerName
              + "'");
    }
    sink.receive(message);
  }

  private static String key(String providerName, String channelName) {
    return providerName + "/" + channelName;
  }
}
