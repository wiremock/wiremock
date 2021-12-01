/*
 * Copyright (C) 2021 Wilfried Kohl
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

public class ScenarioStateParam {

  private String scenarioName;
  private String scenarioState;

  @JsonCreator
  public ScenarioStateParam(
      @JsonProperty("scenarioName") String scenarioName,
      @JsonProperty("scenarioState") String scenarioState) {
    this.scenarioName = scenarioName;
    this.scenarioState = scenarioState;
  }

  public String getScenarioName() {
    return this.scenarioName;
  }

  public String getScenarioState() {
    return this.scenarioState;
  }

  public void setScenarioName(String scenarioName) {
    this.scenarioName = scenarioName;
  }

  public void setScenarioState(String scenarioState) {
    this.scenarioState = scenarioState;
  }
}
