/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MemoizingMatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** The type Near miss calculator. */
public class NearMissCalculator {

  /** The constant NEAR_MISS_COUNT. */
  public static final int NEAR_MISS_COUNT = 3;

  /** The constant NEAR_MISS_ASCENDING_COMPARATOR. */
  public static final Comparator<NearMiss> NEAR_MISS_ASCENDING_COMPARATOR = Comparable::compareTo;

  private final StubMappings stubMappings;
  private final RequestJournal requestJournal;
  private final Scenarios scenarios;
  private final Map<String, RequestMatcherExtension> customMatchers;

  /**
   * Instantiates a new Near miss calculator.
   *
   * @param stubMappings the stub mappings
   * @param requestJournal the request journal
   * @param scenarios the scenarios
   */
  public NearMissCalculator(
      StubMappings stubMappings, RequestJournal requestJournal, Scenarios scenarios) {
    this(stubMappings, requestJournal, scenarios, Map.of());
  }

  /**
   * Instantiates a new Near miss calculator.
   *
   * @param stubMappings the stub mappings
   * @param requestJournal the request journal
   * @param scenarios the scenarios
   * @param customMatchers the custom matchers
   */
  public NearMissCalculator(
      StubMappings stubMappings,
      RequestJournal requestJournal,
      Scenarios scenarios,
      Map<String, RequestMatcherExtension> customMatchers) {
    this.stubMappings = stubMappings;
    this.requestJournal = requestJournal;
    this.scenarios = scenarios;
    this.customMatchers = customMatchers;
  }

  /**
   * Find nearest to list.
   *
   * @param request the request
   * @return the list
   */
  public List<NearMiss> findNearestTo(final LoggedRequest request) {
    List<StubMapping> allMappings = stubMappings.getAll();

    return sortAndTruncate(
        allMappings.stream()
            .map(
                stubMapping -> {
                  MatchResult matchResult =
                      new MemoizingMatchResult(
                          stubMapping.getRequest().match(request, customMatchers));
                  String actualScenarioState = getScenarioStateOrNull(stubMapping);
                  return new NearMiss(request, stubMapping, matchResult, actualScenarioState);
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

  /**
   * Find nearest to list.
   *
   * @param requestPattern the request pattern
   * @return the list
   */
  public List<NearMiss> findNearestTo(final RequestPattern requestPattern) {
    List<ServeEvent> serveEvents = requestJournal.getAllServeEvents();
    return sortAndTruncate(
        serveEvents.stream()
            .map(
                serveEvent -> {
                  MatchResult matchResult =
                      new MemoizingMatchResult(
                          requestPattern.match(serveEvent.getRequest(), customMatchers));
                  return new NearMiss(serveEvent.getRequest(), requestPattern, matchResult);
                })
            .collect(Collectors.toList()),
        serveEvents.size());
  }

  private static List<NearMiss> sortAndTruncate(List<NearMiss> nearMisses, int originalSize) {
    nearMisses.sort(NEAR_MISS_ASCENDING_COMPARATOR);
    return nearMisses.subList(0, min(NEAR_MISS_COUNT, originalSize));
  }
}
