/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.store.ScenariosStore;
import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class AbstractScenarios implements Scenarios {

  private final ScenariosStore store;

  public AbstractScenarios(ScenariosStore store) {
    this.store = store;
  }

  ScenariosStore getStore() {
    return store;
  }

  @Override
  public Scenario getByName(String name) {
    return getStore().get(name).orElse(null);
  }

  @Override
  public List<Scenario> getAll() {
    return ImmutableList.copyOf(getStore().getAll().collect(toList()));
  }

  @Override
  public void onStubMappingAdded(StubMapping mapping) {
    if (canHandle(mapping)) {
      String scenarioName = getScenarioName(mapping);
      Scenario scenario =
          getFirstNonNull(
                  getStore().get(scenarioName).orElse(null), Scenario.inStartedState(scenarioName))
              .withStubMapping(mapping);
      getStore().put(scenarioName, scenario);
    }
  }

  @Override
  public void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping) {
    String oldScenarioName = oldMapping.getScenarioName();
    String newScenarioName = newMapping.getScenarioName();

    if (canHandle(oldMapping) && !oldScenarioName.equals(newScenarioName)) {
      Scenario scenarioForOldMapping =
          getStore()
              .get(oldScenarioName)
              .map(scenario -> scenario.withoutStubMapping(oldMapping))
              .orElseThrow(IllegalStateException::new);

      if (scenarioForOldMapping.getMappings().isEmpty()) {
        getStore().remove(scenarioForOldMapping.getId());
      } else {
        getStore().put(oldScenarioName, scenarioForOldMapping);
      }
    }

    if (canHandle(newMapping)) {
      Scenario scenario =
          getFirstNonNull(
                  getStore().get(newScenarioName).orElse(null),
                  Scenario.inStartedState(newScenarioName))
              .withStubMapping(newMapping);
      getStore().put(newScenarioName, scenario);
    }
  }

  @Override
  public void onStubMappingRemoved(StubMapping mapping) {
    if (canHandle(mapping)) {
      final String scenarioName = getScenarioName(mapping);
      Scenario scenario =
          getStore()
              .get(scenarioName)
              .orElseThrow(IllegalStateException::new)
              .withoutStubMapping(mapping);

      if (scenario.getMappings().isEmpty()) {
        getStore().remove(scenarioName);
      } else {
        getStore().put(scenarioName, scenario);
      }
    }
  }

  @Override
  public void onStubServed(StubMapping mapping, Request request) {
    if (!canHandle(mapping)) {
      return;
    }

    final String scenarioName = getScenarioName(mapping);
    Scenario scenario = getStore().get(scenarioName).orElseThrow(IllegalStateException::new);
    if (mapping.modifiesScenarioState()
        && (mapping.getRequiredScenarioState() == null
            || scenario.getState().equals(mapping.getRequiredScenarioState()))) {
      Scenario newScenario = scenario.setState(mapping.getNewScenarioState());
      getStore().put(scenarioName, newScenario);
    }
  }

  @Override
  public void reset() {
    getStore()
        .getAll()
        .map(Scenario::reset)
        .forEach(scenario -> getStore().put(scenario.getId(), scenario));
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
    Scenario scenario = getByName(name);

    if (scenario == null) {
      throw new NotFoundException("Scenario " + name + " does not exist");
    }

    getStore().put(name, fn.apply(scenario));
  }

  @Override
  public void clear() {
    getStore().clear();
  }

  @Override
  public boolean mappingMatchesScenarioState(StubMapping mapping, Request request) {
    String currentScenarioState = getByName(getScenarioName(mapping)).getState();
    return mapping.getRequiredScenarioState().equals(currentScenarioState);
  }

  String getScenarioName(StubMapping mapping) {
    return mapping.getScenarioName();
  }
}
