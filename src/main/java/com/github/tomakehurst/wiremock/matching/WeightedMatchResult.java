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
package com.github.tomakehurst.wiremock.matching;

/** The type Weighted match result. */
public class WeightedMatchResult {

  private final MatchResult matchResult;
  private final double weighting;

  /**
   * Weight weighted match result.
   *
   * @param matchResult the match result
   * @param weighting the weighting
   * @return the weighted match result
   */
  public static WeightedMatchResult weight(MatchResult matchResult, double weighting) {
    return new WeightedMatchResult(matchResult, weighting);
  }

  /**
   * Weight weighted match result.
   *
   * @param matchResult the match result
   * @return the weighted match result
   */
  public static WeightedMatchResult weight(MatchResult matchResult) {
    return new WeightedMatchResult(matchResult);
  }

  /**
   * Instantiates a new Weighted match result.
   *
   * @param matchResult the match result
   */
  public WeightedMatchResult(MatchResult matchResult) {
    this(matchResult, 1.0);
  }

  /**
   * Instantiates a new Weighted match result.
   *
   * @param matchResult the match result
   * @param weighting the weighting
   */
  public WeightedMatchResult(MatchResult matchResult, double weighting) {
    this.matchResult = matchResult;
    this.weighting = weighting;
  }

  /**
   * Is exact match boolean.
   *
   * @return the boolean
   */
  public boolean isExactMatch() {
    return matchResult.isExactMatch();
  }

  /**
   * Gets distance.
   *
   * @return the distance
   */
  public double getDistance() {
    return weighting * matchResult.getDistance();
  }

  /**
   * Gets weighting.
   *
   * @return the weighting
   */
  public double getWeighting() {
    return weighting;
  }

  /**
   * Gets match result.
   *
   * @return the match result
   */
  public MatchResult getMatchResult() {
    return matchResult;
  }
}
