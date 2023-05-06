/*
 * Copyright (C) 2017-2022 Thomas Akehurst
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

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.store.ScenariosStore;
import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class AbstractScenarios implements Scenarios {

  private final ScenariosStore store;

  public AbstractScenarios(ScenariosStore store) {
    this.store = store;
  }

  @Override
  public Scenario getByName(String name) {
    return store.get(name).orElse(null);
  }

  @Override
  public List<Scenario> getAll() {
    return ImmutableList.copyOf(store.getAll().collect(toList()));
  }

  @Override
  public void onStubMappingAdded(StubMapping mapping) {
    if (mapping.isInScenario()) {
      String scenarioName = mapping.getScenarioName();
      Scenario scenario =
          firstNonNull(store.get(scenarioName).orElse(null), Scenario.inStartedState(scenarioName))
              .withStubMapping(mapping);
      store.put(scenarioName, scenario);
    }
  }

  @Override
  public void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping) {
    if (oldMapping.isInScenario()
        && !oldMapping.getScenarioName().equals(newMapping.getScenarioName())) {
      Scenario scenarioForOldMapping =
          store
              .get(oldMapping.getScenarioName())
              .map(scenario -> scenario.withoutStubMapping(oldMapping))
              .orElseThrow(IllegalStateException::new);

      if (scenarioForOldMapping.getMappings().isEmpty()) {
        store.remove(scenarioForOldMapping.getId());
      } else {
        store.put(oldMapping.getScenarioName(), scenarioForOldMapping);
      }
    }

    if (newMapping.isInScenario()) {
      String scenarioName = newMapping.getScenarioName();
      Scenario scenario =
          firstNonNull(store.get(scenarioName).orElse(null), Scenario.inStartedState(scenarioName))
              .withStubMapping(newMapping);
      store.put(scenarioName, scenario);
    }
  }

  @Override
  public void onStubMappingRemoved(StubMapping mapping) {
    if (mapping.isInScenario()) {
      final String scenarioName = mapping.getScenarioName();
      Scenario scenario =
          store
              .get(scenarioName)
              .orElseThrow(IllegalStateException::new)
              .withoutStubMapping(mapping);

      if (scenario.getMappings().isEmpty()) {
        store.remove(scenarioName);
      } else {
        store.put(scenarioName, scenario);
      }
    }
  }

  @Override
  public void onStubServed(StubMapping mapping) {
    if (mapping.isInScenario()) {
      final String scenarioName = mapping.getScenarioName();
      Scenario scenario = store.get(scenarioName).orElseThrow(IllegalStateException::new);
      if (mapping.modifiesScenarioState()
          && (mapping.getRequiredScenarioState() == null
              || scenario.getState().equals(mapping.getRequiredScenarioState()))) {
        Scenario newScenario = scenario.setState(mapping.getNewScenarioState());
        store.put(scenarioName, newScenario);
      }
    }
  }

  @Override
  public void reset() {
    store.getAll().map(Scenario::reset).forEach(scenario -> store.put(scenario.getId(), scenario));
  }

  @Override
  public void resetSingle(String name) {
    setSingleScenarioState(name, Scenario::reset);
  }

  @Override
  public void setSingle(String name, String state) {
    setSingleScenarioState(name, scenario -> scenario.setState(state));
  }

  private void setSingleScenarioState(
      String name, java.util.function.Function<Scenario, Scenario> fn) {
    Scenario scenario =
        store
            .get(name)
            .orElseThrow(() -> new NotFoundException("Scenario " + name + " does not exist"));

    store.put(name, fn.apply(scenario));
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public boolean mappingMatchesScenarioState(StubMapping mapping) {
    String currentScenarioState = getByName(mapping.getScenarioName()).getState();
    return mapping.getRequiredScenarioState().equals(currentScenarioState);
  }
}
