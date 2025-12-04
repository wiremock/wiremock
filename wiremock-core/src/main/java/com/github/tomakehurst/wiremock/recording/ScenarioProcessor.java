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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.wiremock.url.UrlReference;

class ScenarioProcessor {

  List<StubMapping> putRepeatedRequestsInScenarios(List<StubMapping> stubMappings) {
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
                toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (entry1, entry2) -> entry1,
                    LinkedHashMap::new));

    final List<StubMapping> stubsInScenario = new LinkedList<>();
    int scenarioIndex = 0;
    for (Map.Entry<RequestPattern, Collection<StubMapping>> entry :
        groupsWithMoreThanOneStub.entrySet()) {
      scenarioIndex++;
      final List<StubMapping> batch = new LinkedList<>(entry.getValue());
      Collections.reverse(batch);

      stubsInScenario.addAll(putStubsInScenario(scenarioIndex, batch));
    }

    Map<UUID, StubMapping> stubsInScenarioById =
        stubsInScenario.stream().collect(toMap(StubMapping::getId, Function.identity()));

    return stubMappings.stream()
        .map(originalStub -> stubsInScenarioById.getOrDefault(originalStub.getId(), originalStub))
        .toList();
  }

  private List<StubMapping> putStubsInScenario(int scenarioIndex, List<StubMapping> stubMappings) {
    StubMapping firstScenario = stubMappings.get(0);
    String scenarioName =
        "scenario-"
            + scenarioIndex
            + "-"
            + Urls.urlToPathParts(
                UrlReference.parse(
                    getFirstNonNull(
                        firstScenario.getRequest().getUrl(),
                        firstScenario.getRequest().getUrlPath())));

    return IntStream.range(1, stubMappings.size() + 1)
        .mapToObj(
            i ->
                stubMappings
                    .get(i - 1)
                    .transform(
                        stub -> {
                          stub.setScenarioName(scenarioName);
                          if (i == 1) {
                            stub.setRequiredScenarioState(Scenario.STARTED);
                          } else {
                            stub.setRequiredScenarioState(scenarioName + "-" + i);
                          }
                          if (i < stubMappings.size()) {
                            stub.setNewScenarioState(scenarioName + "-" + (i + 1));
                          }
                        }))
        .collect(toList());
  }
}
