/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StubMappings {

  ServeEvent serveFor(ServeEvent request);

  void addMapping(StubMapping mapping);

  void removeMapping(StubMapping mapping);

  void editMapping(StubMapping stubMapping);

  void reset();

  void resetScenarios();

  List<StubMapping> getAll();

  Optional<StubMapping> get(UUID id);

  List<Scenario> getAllScenarios();

  List<StubMapping> findByMetadata(StringValuePattern pattern);

  default StubMappings withFailIfMultipleMappingsMatch(boolean failIfMultipleMappingsMatch) {
    return this;
  }
}
