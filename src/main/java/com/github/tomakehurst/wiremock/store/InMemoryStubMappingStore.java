/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.stubbing.SortedConcurrentMappingSet;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.wiremock.annotations.Beta;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryStubMappingStore implements StubMappingStore {

  private final SortedConcurrentMappingSet mappings = new SortedConcurrentMappingSet();

  @Override
  public Optional<StubMapping> get(UUID id) {
    return mappings.stream().filter(stubMapping -> stubMapping.getId().equals(id)).findFirst();
  }

  @Override
  public void remove(StubMapping stubMapping) {
    mappings.remove(stubMapping);
  }

  @Override
  public void clear() {
    mappings.clear();
  }

  @Override
  public Stream<StubMapping> getAll() {
    return mappings.stream();
  }

  @Override
  public void add(StubMapping stubMapping) {
    mappings.add(stubMapping);
  }

  @Override
  public void replace(StubMapping existing, StubMapping updated) {
    mappings.replace(existing, updated);
  }
}
