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
import java.util.stream.Collectors;
import org.wiremock.annotations.Beta;

/** The type Match result. */
public abstract class MatchResult implements Comparable<MatchResult> {

  private final Queue<SubEvent> subEvents;
  private final List<DiffDescription> diffDescriptions;

  /** Instantiates a new Match result. */
  public MatchResult() {
    this(List.of(), List.of());
  }

  /**
   * Instantiates a new Match result.
   *
   * @param subEvents the sub events
   */
  public MatchResult(List<SubEvent> subEvents) {
    this(subEvents, List.of());
  }

  /**
   * Instantiates a new Match result.
   *
   * @param subEvents the sub events
   * @param diffDescription the diff description
   */
  public MatchResult(List<SubEvent> subEvents, DiffDescription diffDescription) {
    this(subEvents, List.of(diffDescription));
  }

  /**
   * Instantiates a new Match result.
   *
   * @param subEvents the sub events
   * @param diffDescriptions the diff descriptions
   */
  public MatchResult(List<SubEvent> subEvents, List<DiffDescription> diffDescriptions) {
    this.subEvents = new LinkedBlockingQueue<>(subEvents);
    this.diffDescriptions = diffDescriptions;
  }

  /**
   * Append sub event.
   *
   * @param subEvent the sub event
   */
  protected void appendSubEvent(SubEvent subEvent) {
    subEvents.add(subEvent);
  }

  /**
   * Gets sub events.
   *
   * @return the sub events
   */
  public List<SubEvent> getSubEvents() {
    return new ArrayList<>(subEvents);
  }

  /**
   * Gets diff descriptions.
   *
   * @return the diff descriptions
   */
  public List<DiffDescription> getDiffDescriptions() {
    return this.diffDescriptions;
  }

  /**
   * Partial match match result.
   *
   * @param distance the distance
   * @return the match result
   */
  @JsonCreator
  public static MatchResult partialMatch(@JsonProperty("distance") double distance) {
    return partialMatch(distance, List.of());
  }

  /**
   * Partial match match result.
   *
   * @param distance the distance
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult partialMatch(double distance, SubEvent... subEvents) {
    return partialMatch(distance, List.of(subEvents));
  }

  /**
   * Partial match match result.
   *
   * @param distance the distance
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult partialMatch(double distance, List<SubEvent> subEvents) {
    return new EagerMatchResult(distance, subEvents);
  }

  /**
   * Exact match match result.
   *
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult exactMatch(SubEvent... subEvents) {
    return exactMatch(List.of(subEvents));
  }

  /**
   * Exact match match result.
   *
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult exactMatch(List<SubEvent> subEvents) {
    return new EagerMatchResult(0, subEvents);
  }

  /**
   * No match match result.
   *
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult noMatch(SubEvent... subEvents) {
    return noMatch(List.of(subEvents));
  }

  /**
   * No match match result.
   *
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult noMatch(List<SubEvent> subEvents) {
    return new EagerMatchResult(1, subEvents);
  }

  /**
   * Of match result.
   *
   * @param isMatch the is match
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult of(boolean isMatch, SubEvent... subEvents) {
    return of(isMatch, List.of(subEvents));
  }

  /**
   * Of match result.
   *
   * @param isMatch the is match
   * @param subEvents the sub events
   * @return the match result
   */
  public static MatchResult of(boolean isMatch, List<SubEvent> subEvents) {
    return isMatch ? exactMatch(subEvents) : noMatch(subEvents);
  }

  /**
   * Aggregate match result.
   *
   * @param matches the matches
   * @return the match result
   */
  public static MatchResult aggregate(MatchResult... matches) {
    return aggregate(asList(matches));
  }

  /**
   * Aggregate match result.
   *
   * @param matchResults the match results
   * @return the match result
   */
  public static MatchResult aggregate(final List<MatchResult> matchResults) {
    return aggregateWeighted(
        matchResults.stream().map(WeightedMatchResult::new).collect(Collectors.toList()));
  }

  /**
   * Aggregate weighted match result.
   *
   * @param matchResults the match results
   * @return the match result
   */
  public static MatchResult aggregateWeighted(WeightedMatchResult... matchResults) {
    return aggregateWeighted(asList(matchResults));
  }

  /**
   * Aggregate weighted match result.
   *
   * @param matchResults the match results
   * @return the match result
   */
  public static MatchResult aggregateWeighted(final List<WeightedMatchResult> matchResults) {
    return new WeightedAggregateMatchResult(matchResults);
  }

  /**
   * Is exact match boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public abstract boolean isExactMatch();

  /**
   * Gets distance.
   *
   * @return the distance
   */
  public abstract double getDistance();

  @Override
  public int compareTo(MatchResult other) {
    return Double.compare(other.getDistance(), getDistance());
  }

  /** The constant ARE_EXACT_MATCH. */
  public static final java.util.function.Predicate<WeightedMatchResult> ARE_EXACT_MATCH =
      WeightedMatchResult::isExactMatch;

  /** The type Diff description. */
  @Beta(
      justification =
          "Add self-description callbacks for use in Diff - https://github.com/wiremock/wiremock/issues/2758")
  public static class DiffDescription {
    private final String expected;
    private final String actual;
    private final String errorMessage;

    /**
     * Instantiates a new Diff description.
     *
     * @param expected the expected
     * @param actual the actual
     * @param errorMessage the error message
     */
    public DiffDescription(String expected, String actual, String errorMessage) {
      this.expected = expected;
      this.actual = actual;
      this.errorMessage = errorMessage;
    }

    /**
     * Gets expected.
     *
     * @return the expected
     */
    public String getExpected() {
      return expected;
    }

    /**
     * Gets error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
      return errorMessage;
    }

    /**
     * Gets actual.
     *
     * @return the actual
     */
    public String getActual() {
      return actual;
    }
  }
}
