/*
 * Copyright (C) 2022-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.store;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.wiremock.annotations.Beta;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Beta(justification = "Externalized State API: https://github.com/wiremock/wiremock/issues/2144")
public class InMemoryScenariosStore implements ScenariosStore {

  private final ConcurrentHashMap<String, Scenario> scenarioMap = new ConcurrentHashMap<>();

  @Override
  public Stream<String> getAllKeys() {
    return scenarioMap.keySet().stream();
  }

  @Override
  public Stream<Scenario> getAll() {
    return scenarioMap.values().stream();
  }

  @Override
  public Optional<Scenario> get(String key) {
    return Optional.ofNullable(scenarioMap.get(key));
  }

  @Override
  public void put(String key, Scenario content) {
    scenarioMap.put(key, content);
  }

  @Override
  public void remove(String key) {
    scenarioMap.remove(key);
  }

  @Override
  public void clear() {
    scenarioMap.clear();
  }
}
