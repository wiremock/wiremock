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
package com.github.tomakehurst.wiremock.recording;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated(forRemoval = true)
public class ScenarioProcessor {

  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated(forRemoval = true)
  public void putRepeatedRequestsInScenarios(List<StubMapping> stubMappings) {
    Map<RequestPattern, List<StubMapping>> stubsGroupedByRequest =
        stubMappings.stream()
            .collect(
                Collectors.groupingBy(
                    StubMapping::getRequest,
                    LinkedHashMap::new,
                    Collectors.toCollection(LinkedList::new)));

    Map<RequestPattern, Collection<StubMapping>> groupsWithMoreThanOneStub =
        stubsGroupedByRequest.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (entry1, entry2) -> entry1,
                    LinkedHashMap::new));

    int scenarioIndex = 0;
    for (Map.Entry<RequestPattern, Collection<StubMapping>> entry :
        groupsWithMoreThanOneStub.entrySet()) {
      scenarioIndex++;
      final List<StubMapping> stubsInScenario = new LinkedList<>(entry.getValue());
      Collections.reverse(stubsInScenario);
      putStubsInScenario(scenarioIndex, stubsInScenario);
    }
  }

  private void putStubsInScenario(int scenarioIndex, List<StubMapping> stubMappings) {
    StubMapping firstScenario = stubMappings.get(0);
    String scenarioName =
        "scenario-"
            + scenarioIndex
            + "-"
            + Urls.urlToPathParts(
                URI.create(
                    getFirstNonNull(
                        firstScenario.getRequest().getUrl(),
                        firstScenario.getRequest().getUrlPath())));

    int count = 1;
    for (StubMapping stub : stubMappings) {
      stub.setScenarioName(scenarioName);
      if (count == 1) {
        stub.setRequiredScenarioState(Scenario.STARTED);
      } else {
        stub.setRequiredScenarioState(scenarioName + "-" + count);
      }

      if (count < stubMappings.size()) {
        stub.setNewScenarioState(scenarioName + "-" + (count + 1));
      }

      count++;
    }
  }
}
