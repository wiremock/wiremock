/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.List;

/**
 * A wrapper class for the result of a "get all scenarios" admin API call.
 *
 * <p>This serves as a data transfer object (DTO) to structure the JSON response containing a list
 * of all {@link Scenario} states.
 *
 * @see com.github.tomakehurst.wiremock.stubbing.Scenario
 */
public class GetScenariosResult {

  private final List<Scenario> scenarios;

  /**
   * Constructs a new GetScenariosResult.
   *
   * @param scenarios The list of {@link Scenario} objects to be wrapped. The {@code @JsonProperty}
   *     annotation maps this to the "scenarios" key in the JSON output.
   */
  @JsonCreator
  public GetScenariosResult(@JsonProperty("scenarios") List<Scenario> scenarios) {
    this.scenarios = scenarios;
  }

  public List<Scenario> getScenarios() {
    return scenarios;
  }
}
