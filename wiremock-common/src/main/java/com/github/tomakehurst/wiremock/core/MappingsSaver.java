/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.List;
import java.util.UUID;

public interface MappingsSaver {
  void save(List<StubMapping> stubMappings);

  void save(StubMapping stubMapping);

  void remove(UUID stubMappingId);

  default void remove(List<UUID> stubMappingIds) {
    stubMappingIds.forEach(this::remove);
  }

  void removeAll();

  /** Saves the provided stubs and removes all others. */
  default void setAll(List<StubMapping> stubMappings) {
    removeAll();
    save(stubMappings);
  }
}
