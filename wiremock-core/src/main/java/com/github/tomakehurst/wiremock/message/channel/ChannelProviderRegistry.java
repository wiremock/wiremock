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

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.message.FixedChannel;
import com.github.tomakehurst.wiremock.store.ChannelProviderStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChannelProviderRegistry {

  private final Map<String, ChannelProviderDriver> drivers = new HashMap<>();
  private final ChannelProviderStore providerStore;

  public ChannelProviderRegistry(ChannelProviderStore providerStore) {
    this.providerStore = providerStore;
    registerDriver(new InMemoryChannelProviderDriver());
  }

  public void registerDriver(ChannelProviderDriver driver) {
    drivers.put(driver.getType(), driver);
  }

  public List<ChannelProvider> listAllProviders() {
    return providerStore.getAll().collect(Collectors.toList());
  }

  public Optional<ChannelProvider> getProvider(String name) {
    return providerStore.get(name);
  }

  public void registerProvider(ChannelProvider provider) {
    if (!drivers.containsKey(provider.getDriverType())) {
      throw new InvalidInputException(
          Errors.single(10, "No driver registered for type: " + provider.getDriverType()));
    }
    providerStore.put(provider);
  }

  public void removeProvider(String name) {
    providerStore.remove(name);
  }

  public FixedChannel createChannel(
      FixedChannelDefinition channelDefinition, InboundMessageSink sink) {
    ChannelProvider provider = requireProvider(channelDefinition.getProviderName());
    ChannelProviderDriver driver = drivers.get(provider.getDriverType());
    driver.createChannel(provider, channelDefinition.getName(), sink);
    return new FixedChannel(driver, provider, channelDefinition.getName());
  }

  private ChannelProvider requireProvider(String providerName) {
    return providerStore
        .get(providerName)
        .orElseThrow(
            () ->
                new InvalidInputException(
                    Errors.single(
                        10, "No channel provider registered with name: " + providerName)));
  }
}
