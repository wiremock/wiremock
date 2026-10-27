/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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

import static java.util.stream.Collectors.toList;

import com.github.tomakehurst.wiremock.admin.NotFoundException;
import com.github.tomakehurst.wiremock.store.ScenariosStore;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

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
    return store.getAll().collect(toList());
  }

  @Override
  public void onStubMappingAdded(StubMapping mapping) {
    if (mapping.isInScenario()) {
      addToScenario(mapping);
    }
  }

  @Override
  public void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping) {
    if (oldMapping.isInScenario()
        && !oldMapping.getScenarioName().equals(newMapping.getScenarioName())) {
      removeFromScenario(oldMapping);
    }

    if (newMapping.isInScenario()) {
      addToScenario(newMapping);
    }
  }

  @Override
  public void onStubMappingRemoved(StubMapping mapping) {
    if (mapping.isInScenario()) {
      removeFromScenario(mapping);
    }
  }

  @Override
  public void onStubServed(StubMapping mapping) {
    if (mapping.isInScenario()) {
      if (mapping.modifiesScenarioState()) {
        store.compute(
            mapping.getScenarioName(),
            current ->
                Optional.of(transition(current.orElseThrow(IllegalStateException::new), mapping)));
      } else if (store.get(mapping.getScenarioName()).isEmpty()) {
        throw new IllegalStateException();
      }
    }
  }

  @Override
  public void reset() {
    store
        .getAllKeys()
        .collect(toList())
        .forEach(name -> store.compute(name, current -> current.map(Scenario::reset)));
  }

  @Override
  public void resetSingle(String name) {
    setSingleScenarioState(name, Scenario::reset);
  }

  @Override
  public void setSingle(String name, String state) {
    setSingleScenarioState(name, scenario -> scenario.setState(state));
  }

  private void setSingleScenarioState(String name, UnaryOperator<Scenario> fn) {
    store.compute(
        name,
        current ->
            Optional.of(
                fn.apply(
                    current.orElseThrow(
                        () -> new NotFoundException("Scenario " + name + " does not exist")))));
  }

  private static Scenario transition(Scenario scenario, StubMapping mapping) {
    return mapping.getRequiredScenarioState() == null
            || scenario.getState().equals(mapping.getRequiredScenarioState())
        ? scenario.setState(mapping.getNewScenarioState())
        : scenario;
  }

  private void addToScenario(StubMapping mapping) {
    String scenarioName = mapping.getScenarioName();
    store.compute(
        scenarioName,
        current ->
            Optional.of(
                current
                    .orElseGet(() -> Scenario.inStartedState(scenarioName))
                    .withStubMapping(mapping)));
  }

  private void removeFromScenario(StubMapping mapping) {
    store.compute(
        mapping.getScenarioName(),
        current ->
            current
                .map(scenario -> scenario.withoutStubMapping(mapping))
                .filter(scenario -> !scenario.getMappings().isEmpty()));
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public ScenarioSnapshot snapshot() {
    return new ScenarioSnapshot(store.getAll());
  }
}
