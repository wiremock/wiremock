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
package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A data transfer object (DTO) for setting the state of a scenario.
 *
 * <p>This is used as the request body for the admin API endpoint that updates a scenario to a new
 * state.
 *
 * @see com.github.tomakehurst.wiremock.stubbing.Scenario
 */
public class ScenarioState {

  private final String state;

  /**
   * Constructs a new ScenarioState.
   *
   * @param state The name of the new state for the scenario. The {@code @JsonProperty} annotation
   *     maps this from the "state" field in a JSON object.
   */
  public ScenarioState(@JsonProperty("state") String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }
}
