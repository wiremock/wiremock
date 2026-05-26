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
import java.util.HashMap;
import java.util.Map;

public class ChannelProviderRegistry {

  private final Map<String, ChannelProviderDriver> drivers = new HashMap<>();
  private final Map<String, ChannelProvider> providers = new HashMap<>();

  public ChannelProviderRegistry() {
    registerDriver(new InMemoryChannelProviderDriver());
  }

  public void registerDriver(ChannelProviderDriver driver) {
    drivers.put(driver.getType(), driver);
  }

  public void registerProvider(ChannelProvider provider) {
    if (!drivers.containsKey(provider.getDriverType())) {
      throw new IllegalArgumentException(
          "No driver registered for type: " + provider.getDriverType());
    }
    providers.put(provider.getName(), provider);
  }

  public void createChannel(FixedChannel channel) {
    ChannelProvider provider = requireProvider(channel.getProviderName());
    ChannelProviderDriver driver = drivers.get(provider.getDriverType());
    driver.createChannel(provider, channel.getName());
  }

  public void send(String providerName, String channelName, Message message) {
    ChannelProvider provider = requireProvider(providerName);
    ChannelProviderDriver driver = drivers.get(provider.getDriverType());
    driver.send(provider, channelName, message);
  }

  private ChannelProvider requireProvider(String providerName) {
    ChannelProvider provider = providers.get(providerName);
    if (provider == null) {
      throw new IllegalArgumentException("No channel provider registered with name: " + providerName);
    }
    return provider;
  }
}
