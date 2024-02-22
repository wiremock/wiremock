/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
import static java.util.stream.Collectors.toUnmodifiableList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MatchResult implements Comparable<MatchResult> {

  private final Queue<SubEvent> subEvents;

  public MatchResult() {
    this.subEvents = new LinkedBlockingQueue<>();
  }

  public MatchResult(List<SubEvent> subEvents) {
    this.subEvents = new LinkedBlockingQueue<>(subEvents);
  }

  protected void appendSubEvent(SubEvent subEvent) {
    subEvents.add(subEvent);
  }

  public List<SubEvent> getSubEvents() {
    return subEvents.stream().collect(toUnmodifiableList());
  }

  @JsonCreator
  public static MatchResult partialMatch(@JsonProperty("distance") double distance) {
    return new EagerMatchResult(distance);
  }

  public static MatchResult exactMatch(SubEvent... subEvents) {
    return exactMatch(List.of(subEvents));
  }

  public static MatchResult exactMatch(List<SubEvent> subEvents) {
    return new EagerMatchResult(0, subEvents);
  }

  public static MatchResult noMatch(SubEvent... subEvents) {
    return noMatch(List.of(subEvents));
  }

  public static MatchResult noMatch(List<SubEvent> subEvents) {
    return new EagerMatchResult(1, subEvents);
  }

  public static MatchResult of(boolean isMatch, SubEvent... subEvents) {
    return of(isMatch, List.of(subEvents));
  }

  public static MatchResult of(boolean isMatch, List<SubEvent> subEvents) {
    return isMatch ? exactMatch(subEvents) : noMatch(subEvents);
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

      private final Lazy<Boolean> exactMatch =
          Lazy.lazy(() -> matchResults.stream().allMatch(ARE_EXACT_MATCH));
      private final Lazy<Double> distance =
          Lazy.lazy(
              () -> {
                double totalDistance = 0;
                double sizeWithWeighting = 0;
                for (WeightedMatchResult matchResult : matchResults) {
                  totalDistance += matchResult.getDistance();
                  sizeWithWeighting += matchResult.getWeighting();
                }

                return (totalDistance / sizeWithWeighting);
              });

      private final Lazy<List<SubEvent>> subEvents =
          Lazy.lazy(
              () -> {
                isExactMatch(); // TODO: Find a less icky way to do this
                return matchResults.stream()
                    .flatMap(
                        weightedResult -> weightedResult.getMatchResult().getSubEvents().stream())
                    .collect(Collectors.toList());
              });

      @Override
      public boolean isExactMatch() {
        return exactMatch.get();
      }

      @Override
      public double getDistance() {
        return distance.get();
      }

      @Override
      public List<SubEvent> getSubEvents() {
        return subEvents.get();
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
