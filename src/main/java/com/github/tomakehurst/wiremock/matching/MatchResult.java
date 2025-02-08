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
package com.github.tomakehurst.wiremock.matching;

import static java.util.Arrays.asList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.wiremock.annotations.Beta;

public abstract class MatchResult implements Comparable<MatchResult> {

  private final Queue<SubEvent> subEvents;
  private final List<DiffDescription> diffDescriptions;

  public MatchResult() {
    this(List.of(), List.of());
  }

  public MatchResult(List<SubEvent> subEvents) {
    this(subEvents, List.of());
  }

  public MatchResult(List<SubEvent> subEvents, DiffDescription diffDescription) {
    this(subEvents, List.of(diffDescription));
  }

  public MatchResult(List<SubEvent> subEvents, List<DiffDescription> diffDescriptions) {
    this.subEvents = new LinkedBlockingQueue<>(subEvents);
    this.diffDescriptions = diffDescriptions;
  }

  protected void appendSubEvent(SubEvent subEvent) {
    subEvents.add(subEvent);
  }

  public List<SubEvent> getSubEvents() {
    return new ArrayList<>(subEvents);
  }

  public List<DiffDescription> getDiffDescriptions() {
    return this.diffDescriptions;
  }

  @JsonCreator
  public static MatchResult partialMatch(@JsonProperty("distance") double distance) {
    return partialMatch(distance, List.of());
  }

  public static MatchResult partialMatch(double distance, SubEvent... subEvents) {
    return partialMatch(distance, List.of(subEvents));
  }

  public static MatchResult partialMatch(double distance, List<SubEvent> subEvents) {
    return new EagerMatchResult(distance, subEvents);
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
    return new WeightedAggregateMatchResult(matchResults);
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

  @Beta(
      justification =
          "Add self-description callbacks for use in Diff - https://github.com/wiremock/wiremock/issues/2758")
  public static class DiffDescription {
    private final String expected;
    private final String actual;
    private final String errorMessage;

    public DiffDescription(String expected, String actual, String errorMessage) {
      this.expected = expected;
      this.actual = actual;
      this.errorMessage = errorMessage;
    }

    public String getExpected() {
      return expected;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public String getActual() {
      return actual;
    }
  }
}
