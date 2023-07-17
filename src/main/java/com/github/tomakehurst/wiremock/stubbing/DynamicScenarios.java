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

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.store.ScenariosStore;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class DynamicScenarios extends AbstractScenarios {
  public DynamicScenarios(ScenariosStore store) {
    super(store);
  }

  @Override
  public void onStubMappingAdded(StubMapping mapping) {
    super.onStubMappingAdded(mapping);

    ResponseDefinition responseDefinition =
        ResponseDefinitionBuilder.like(mapping.getResponse())
            .but()
            .withTransformer(ResponseTemplateTransformer.NAME, "dynamicScenarioKey", "IDDQD")
            .build();

    mapping.setResponse(responseDefinition);
  }

  @Override
  public void onStubServed(StubMapping mapping, Request request) {
    if (!canHandle(mapping)) {
      return;
    }

    String scenarioPrefix = mapping.getScenarioPrefix();
    Scenario scenario = getStore().get(scenarioPrefix).orElseThrow(IllegalStateException::new);

    if (mapping.modifiesScenarioState()) {
      Optional<ListOrSingle<String>> expressionResult =
          Optional.ofNullable(mapping.getScenarioKeyPattern())
              .map(er -> er.getExpressionResult(request.getBodyAsString()));

      if (mapping.getRequiredScenarioState() == null && expressionResult.isPresent()) {
        String scenarioName = scenarioName(scenarioPrefix, expressionResult.get());
        Scenario newScenario =
            scenario.setState(mapping.getNewScenarioState()).setName(scenarioName);
        getStore().put(scenarioName, newScenario);
      } else {
        final Scenario dynamicScenario;

        if (expressionResult.isPresent()) {
          String scenarioName = scenarioName(scenarioPrefix, expressionResult.get());
          dynamicScenario = getStore().get(scenarioName).orElse(scenario.setName(scenarioName));
        } else {
          dynamicScenario =
              findAllStartsWith(scenarioName(scenarioPrefix, null))
                  .findFirst()
                  .orElseThrow(IllegalStateException::new);
        }

        if (dynamicScenario.getState().equals(mapping.getRequiredScenarioState())) {
          Scenario newScenario = dynamicScenario.setState(mapping.getNewScenarioState());
          getStore().put(dynamicScenario.getName(), newScenario);
        }
      }
    }
  }

  @Override
  public boolean canHandle(StubMapping mapping) {
    return mapping.isInDynamicScenario();
  }

  @Override
  String getScenarioName(StubMapping mapping) {
    return mapping.getScenarioPrefix();
  }

  @Override
  public boolean mappingMatchesScenarioState(StubMapping mapping, Request request) {
    if (mapping.getScenarioKeyPattern() != null) {
      String bodyAsString = request.getBodyAsString();
      return Optional.of(mapping.getScenarioKeyPattern())
          .filter(pathPattern -> pathPattern.match(bodyAsString).isExactMatch())
          .map(pathPattern -> pathPattern.getExpressionResult(bodyAsString))
          .map(expressionResult -> scenarioName(getScenarioName(mapping), expressionResult))
          .map(this::getByName)
          .map(Scenario::getState)
          .map(mapping.getRequiredScenarioState()::equals)
          .orElse(false);
    } else {
      return findAllStartsWith(scenarioName(getScenarioName(mapping), null))
          .map(Scenario::getState)
          .anyMatch(mapping.getRequiredScenarioState()::equals);
    }
  }

  private String scenarioName(String prefix, @Nullable ListOrSingle<String> expressionResult) {
    String suffix =
        Optional.ofNullable(expressionResult)
            .filter(er -> er.size() > 0)
            .map(ListOrSingle::getFirst)
            .orElse("");

    return prefix + "-" + suffix;
  }

  private Stream<Scenario> findAllStartsWith(String startsWith) {
    return getAll().stream().filter(scenario -> scenario.getName().startsWith(startsWith));
  }
}
