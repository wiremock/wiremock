/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import static java.lang.Math.min;

import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MemoizingMatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.ServeContext;
import com.github.tomakehurst.wiremock.stubbing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NearMissCalculator {

  public static final int NEAR_MISS_COUNT = 3;

  public static final Comparator<NearMiss> NEAR_MISS_ASCENDING_COMPARATOR = Comparable::compareTo;

  private final StubMappings stubMappings;
  private final RequestJournal requestJournal;
  private final Scenarios scenarios;
  private final Map<String, RequestMatcherExtension> customMatchers;
  private final WireMockServices services;

  public NearMissCalculator(
      StubMappings stubMappings, RequestJournal requestJournal, Scenarios scenarios) {
    this(stubMappings, requestJournal, scenarios, Map.of(), Extensions.NONE);
  }

  public NearMissCalculator(
      StubMappings stubMappings,
      RequestJournal requestJournal,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers) {
    this(stubMappings, requestJournal, scenarios, customMatchers, Extensions.NONE);
  }

  public NearMissCalculator(
      StubMappings stubMappings,
      RequestJournal requestJournal,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers,
      WireMockServices services) {
    this.stubMappings = stubMappings;
    this.requestJournal = requestJournal;
    this.scenarios = scenarios;
    this.customMatchers = customMatchers;
    this.services = services;
  }

  public List<NearMiss> findNearestTo(final LoggedRequest request) {
    List<StubMapping> allMappings = stubMappings.getAll();
    ServeContext serveContext = new ServeContext(services, request);

    return sortAndTruncate(
        allMappings.stream()
            .map(
                stubMapping -> {
                  MatchResult matchResult =
                      new MemoizingMatchResult(
                          stubMapping.getRequest().match(request, customMatchers, serveContext));
                  String actualScenarioState = getScenarioStateOrNull(stubMapping);
                  return new NearMiss(
                      request, stubMapping, matchResult, actualScenarioState, serveContext);
                })
            .collect(Collectors.toList()),
        allMappings.size());
  }

  private String getScenarioStateOrNull(StubMapping stubMapping) {
    if (!stubMapping.isInScenario()) {
      return null;
    }

    Scenario scenario = scenarios.getByName(stubMapping.getScenarioName());
    return scenario != null ? scenario.getState() : null;
  }

  public List<NearMiss> findNearestTo(final RequestPattern requestPattern) {
    List<ServeEvent> serveEvents = requestJournal.getAllServeEvents();
    return sortAndTruncate(
        serveEvents.stream()
            .map(
                serveEvent -> {
                  ServeContext serveContext = new ServeContext(services, serveEvent.getRequest());
                  MatchResult matchResult =
                      new MemoizingMatchResult(
                          requestPattern.match(
                              serveEvent.getRequest(), customMatchers, serveContext));
                  return new NearMiss(
                      serveEvent.getRequest(), requestPattern, matchResult, serveContext);
                })
            .collect(Collectors.toList()),
        serveEvents.size());
  }

  private static List<NearMiss> sortAndTruncate(List<NearMiss> nearMisses, int originalSize) {
    nearMisses.sort(NEAR_MISS_ASCENDING_COMPARATOR);
    return nearMisses.subList(0, min(NEAR_MISS_COUNT, originalSize));
  }
}
