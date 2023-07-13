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
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ScenarioState;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

public class SetScenarioStateTask implements AdminTask {

  @Override
  public ResponseDefinition execute(Admin admin, ServeEvent serveEvent, PathParams pathParams) {
    String name = pathParams.get("name");
    String body = serveEvent.getRequest().getBodyAsString();

    try {
      setOrResetScenarioState(admin, name, body);
    } catch (NotFoundException e) {
      return ResponseDefinitionBuilder.jsonResponse(Errors.single(404, e.getMessage()), 404);
    }

    return ResponseDefinition.okEmptyJson();
  }

  private void setOrResetScenarioState(Admin admin, String name, String body) {
    if (body != null && !body.isEmpty()) {
      ScenarioState scenarioState = Json.read(body, ScenarioState.class);
      admin.setScenarioState(name, scenarioState.getState());
    } else {
      admin.resetScenario(name);
    }
  }
}
