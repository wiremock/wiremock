/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Prioritisable;
import com.github.tomakehurst.wiremock.common.SortedConcurrentPrioritisableSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.wiremock.annotations.Beta;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public abstract class InMemoryMappingStore<T extends Prioritisable> {

  private final SortedConcurrentPrioritisableSet<T> mappings =
      new SortedConcurrentPrioritisableSet<>();

  public Optional<T> get(UUID id) {
    return mappings.stream().filter(mapping -> mapping.getId().equals(id)).findFirst();
  }

  public void remove(UUID id) {
    mappings.remove(id);
  }

  public void clear() {
    mappings.clear();
  }

  public Stream<T> getAll() {
    return mappings.stream();
  }

  public T add(T mapping) {
    return mappings.add(mapping);
  }

  public T replace(T existing, T updated) {
    return mappings.replace(existing, updated);
  }
}
