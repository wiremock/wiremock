/*
 * Copyright (C) 2022 Thomas Akehurst
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

public interface Scenarios {
  Scenario getByName(String name);

  List<Scenario> getAll();

  void onStubMappingAdded(StubMapping mapping);

  void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping);

  void onStubMappingRemoved(StubMapping mapping);

  void onStubServed(StubMapping mapping);

  void reset();

  void resetSingle(String name);

  void setSingle(String name, String state);

  void clear();

  boolean mappingMatchesScenarioState(StubMapping mapping);
}
