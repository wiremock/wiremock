/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.common.Lazy.lazy;
import static com.github.tomakehurst.wiremock.common.Pair.pair;

import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.common.Pair;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.ArrayList;
import java.util.List;

public class WeightedAggregateMatchResult extends MatchResult {

  private final List<WeightedMatchResult> matchResults;

  private final Lazy<Pair<Boolean, List<SubEvent>>> resultAndEvents;

  public WeightedAggregateMatchResult(List<WeightedMatchResult> matchResults) {
    this.matchResults = matchResults;
    resultAndEvents =
        lazy(
            () -> {
              final List<SubEvent> subEvents = new ArrayList<>(matchResults.size());
              return pair(
                  matchResults.stream()
                      .allMatch(
                          weightedMatchResult -> {
                            final boolean exactMatch = weightedMatchResult.isExactMatch();
                            subEvents.addAll(weightedMatchResult.getMatchResult().getSubEvents());
                            return exactMatch;
                          }),
                  subEvents);
            });
  }

  @Override
  public boolean isExactMatch() {
    return resultAndEvents.get().a;
  }

  @Override
  public double getDistance() {
    double totalDistance = 0;
    double sizeWithWeighting = 0;
    for (WeightedMatchResult matchResult : matchResults) {
      totalDistance += matchResult.getDistance();
      sizeWithWeighting += matchResult.getWeighting();
    }

    return (totalDistance / sizeWithWeighting);
  }

  @Override
  public List<SubEvent> getSubEvents() {
    return resultAndEvents.get().b;
  }
}
