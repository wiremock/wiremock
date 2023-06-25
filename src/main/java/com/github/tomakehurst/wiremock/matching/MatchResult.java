/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MatchResult implements Comparable<MatchResult> {

  @JsonCreator
  public static MatchResult partialMatch(@JsonProperty("distance") double distance) {
    return new EagerMatchResult(distance);
  }

  public static MatchResult exactMatch() {
    return new EagerMatchResult(0);
  }

  public static MatchResult noMatch() {
    return new EagerMatchResult(1);
  }

  public static MatchResult of(boolean isMatch) {
    return isMatch ? exactMatch() : noMatch();
  }

  public static MatchResult aggregate(MatchResult... matches) {
    return aggregate(asList(matches));
  }

  public static MatchResult aggregate(final List<MatchResult> matchResults) {
    return aggregateWeighted(
        matchResults.stream().map(WeightedMatchResult::new).collect(Collectors.toList()));
  }

  public static MatchResult aggregateWeighted(WeightedMatchResult... matchResults) {
    return aggregateWeighted(asList(matchResults));
  }

  public static MatchResult aggregateWeighted(final List<WeightedMatchResult> matchResults) {
    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return matchResults.stream().allMatch(ARE_EXACT_MATCH);
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
    };
  }

  @JsonIgnore
  public abstract boolean isExactMatch();

  public abstract double getDistance();

  @Override
  public int compareTo(MatchResult other) {
    return Double.compare(other.getDistance(), getDistance());
  }

  public static final Predicate<WeightedMatchResult> ARE_EXACT_MATCH =
      WeightedMatchResult::isExactMatch;
}
