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
package com.github.tomakehurst.wiremock.stubbing;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.stream.Stream;

/**
 * An immutable, point-in-time view of all scenarios' states. Matching a single request reads state
 * through one snapshot so that a scenario advanced concurrently cannot change the state part way
 * through evaluating candidate stubs (which could otherwise leave the request unmatched).
 */
public class ScenarioSnapshot {

  private final Map<String, Scenario> scenariosByName;

  ScenarioSnapshot(Stream<Scenario> all) {
    this.scenariosByName = all.collect(toMap(Scenario::getId, scenario -> scenario));
  }

  public boolean mappingMatchesScenarioState(StubMapping mapping) {
    Scenario scenario = scenariosByName.get(mapping.getScenarioName());
    String currentState = scenario == null ? null : scenario.getState();
    return mapping.getRequiredScenarioState().equals(currentState);
  }
}
