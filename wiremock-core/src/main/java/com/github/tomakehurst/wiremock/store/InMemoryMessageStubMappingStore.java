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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.websocket.message.MessageStubMapping;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryMessageStubMappingStore implements MessageStubMappingStore {

  private final Map<UUID, MessageStubMapping> mappings = new ConcurrentHashMap<>();

  @Override
  public Stream<MessageStubMapping> getAll() {
    return mappings.values().stream();
  }

  @Override
  public Optional<MessageStubMapping> get(UUID id) {
    return Optional.ofNullable(mappings.get(id));
  }

  @Override
  public void add(MessageStubMapping mapping) {
    mappings.put(mapping.getId(), mapping);
  }

  @Override
  public void remove(UUID id) {
    mappings.remove(id);
  }

  @Override
  public void clear() {
    mappings.clear();
  }
}
