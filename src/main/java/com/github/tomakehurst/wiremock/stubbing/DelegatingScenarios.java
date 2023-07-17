/*
 * Copyright (C) 2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.scenario.ScenarioUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DelegatingScenarios implements Scenarios {

  private final List<Scenarios> delegates;

  public DelegatingScenarios(List<Scenarios> delegates) {
    this.delegates = delegates;
  }

  @Override
  public Scenario getByName(String name) {
    return delegates.stream()
        .map(scenarios -> scenarios.getByName(name))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Scenario> getAll() {
    return delegates.stream()
        .map(Scenarios::getAll)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  @Override
  public void onStubMappingAdded(StubMapping mapping) {
    findDelegate(mapping).ifPresent(scenarios -> scenarios.onStubMappingAdded(mapping));
  }

  @Override
  public void onStubMappingUpdated(StubMapping oldMapping, StubMapping newMapping) {
    delegates.stream()
        .filter(scenarios -> scenarios.canHandle(oldMapping) || scenarios.canHandle(newMapping))
        .forEach(scenarios -> scenarios.onStubMappingUpdated(oldMapping, newMapping));
  }

  @Override
  public void onStubMappingRemoved(StubMapping mapping) {
    findDelegate(mapping).ifPresent(scenarios -> scenarios.onStubMappingRemoved(mapping));
  }

  @Override
  public void onStubServed(StubMapping mapping, Request request) {
    findDelegate(mapping).ifPresent(scenarios -> scenarios.onStubServed(mapping, request));
  }

  @Override
  public void reset() {
    delegates.forEach(Scenarios::reset);
  }

  @Override
  public void resetSingle(String name) {
    findDelegate(name).resetSingle(name);
  }

  @Override
  public void setSingle(String name, String state) {
    findDelegate(name).setSingle(name, state);
  }

  @Override
  public void clear() {
    delegates.forEach(Scenarios::clear);
  }

  @Override
  public boolean mappingMatchesScenarioState(StubMapping mapping, Request request) {
    return delegates.stream()
        .anyMatch(
            scenarios ->
                scenarios.canHandle(mapping)
                    && scenarios.mappingMatchesScenarioState(mapping, request));
  }

  @Override
  public boolean canHandle(StubMapping mapping) {
    return delegates.stream().anyMatch(scenarios -> scenarios.canHandle(mapping));
  }

  private Optional<Scenarios> findDelegate(StubMapping mapping) {
    return delegates.stream().filter(scenarios -> scenarios.canHandle(mapping)).findFirst();
  }

  private Scenarios findDelegate(String name) {
    return delegates.stream()
        .filter(scenarios -> scenarios.getByName(name) != null)
        .findFirst()
        .orElseThrow(() -> ScenarioUtils.notFoundException(name));
  }
}
