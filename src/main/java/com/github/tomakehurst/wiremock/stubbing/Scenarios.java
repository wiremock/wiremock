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
package com.github.tomakehurst.wiremock.stubbing;

import java.util.List;

/** The interface Scenarios. */
public interface Scenarios {
  /**
   * Gets by name.
   *
   * @param name the name
   * @return the by name
   */
  Scenario getByName(String name);

  /**
   * Gets all.
   *
   * @return the all
   */
  List<Scenario> getAll();

  /**
   * On stub mapping added.
   *
   * @param mapping the mapping
   */
  void onStubMappingAdded(StubMapping mapping);

  /**
   * On stub mapping updated.
   *
   * @param oldMapping the old mapping
   * @param newMapping the new mapping
   */
  void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping);

  /**
   * On stub mapping removed.
   *
   * @param mapping the mapping
   */
  void onStubMappingRemoved(StubMapping mapping);

  /**
   * On stub served.
   *
   * @param mapping the mapping
   */
  void onStubServed(StubMapping mapping);

  /** Reset. */
  void reset();

  /**
   * Reset single.
   *
   * @param name the name
   */
  void resetSingle(String name);

  /**
   * Sets single.
   *
   * @param name the name
   * @param state the state
   */
  void setSingle(String name, String state);

  /** Clear. */
  void clear();

  /**
   * Mapping matches scenario state boolean.
   *
   * @param mapping the mapping
   * @return the boolean
   */
  boolean mappingMatchesScenarioState(StubMapping mapping);
}
