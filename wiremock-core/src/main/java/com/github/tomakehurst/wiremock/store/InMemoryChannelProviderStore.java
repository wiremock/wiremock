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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.message.channel.ChannelProvider;
import java.util.Optional;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryChannelProviderStore extends InMemoryStringKeyedStore<ChannelProvider>
    implements ChannelProviderStore {

  @Override
  public Optional<ChannelProvider> get(String name) {
    return getByKey(name);
  }

  @Override
  public void put(ChannelProvider provider) {
    putByKey(provider.getName(), provider);
  }

  @Override
  public void remove(String name) {
    removeByKey(name);
  }
}
