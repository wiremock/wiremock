/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

/** The interface Stub mappings. */
public interface StubMappings {

  /**
   * Serve for serve event.
   *
   * @param request the request
   * @return the serve event
   */
  ServeEvent serveFor(ServeEvent request);

  /**
   * Add mapping.
   *
   * @param mapping the mapping
   */
  void addMapping(StubMapping mapping);

  /**
   * Remove mapping.
   *
   * @param mapping the mapping
   */
  void removeMapping(StubMapping mapping);

  /**
   * Edit mapping.
   *
   * @param stubMapping the stub mapping
   */
  void editMapping(StubMapping stubMapping);

  /** Reset. */
  void reset();

  /** Reset scenarios. */
  void resetScenarios();

  /**
   * Gets all.
   *
   * @return the all
   */
  List<StubMapping> getAll();

  /**
   * Get optional.
   *
   * @param id the id
   * @return the optional
   */
  Optional<StubMapping> get(UUID id);

  /**
   * Gets all scenarios.
   *
   * @return the all scenarios
   */
  List<Scenario> getAllScenarios();

  /**
   * Find by metadata list.
   *
   * @param pattern the pattern
   * @return the list
   */
  List<StubMapping> findByMetadata(StringValuePattern pattern);
}
