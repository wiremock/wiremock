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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.diff.Diff;

/** The type Near miss. */
public class NearMiss implements Comparable<NearMiss> {

  private final LoggedRequest request;
  private final StubMapping mapping;
  private final RequestPattern requestPattern;
  private final MatchResult matchResult;
  private final String scenarioState;

  /**
   * Instantiates a new Near miss.
   *
   * @param request the request
   * @param mapping the mapping
   * @param requestPattern the request pattern
   * @param matchResult the match result
   * @param scenarioState the scenario state
   */
  @JsonCreator
  public NearMiss(
      @JsonProperty("request") LoggedRequest request,
      @JsonProperty("stubMapping") StubMapping mapping,
      @JsonProperty("requestPattern") RequestPattern requestPattern,
      @JsonProperty("matchResult") MatchResult matchResult,
      @JsonProperty("scenarioState") String scenarioState) {
    this.request = request;
    this.mapping = mapping;
    this.requestPattern = requestPattern;
    this.matchResult = matchResult;
    this.scenarioState = scenarioState;
  }

  /**
   * Instantiates a new Near miss.
   *
   * @param request the request
   * @param mapping the mapping
   * @param matchResult the match result
   * @param scenarioState the scenario state
   */
  public NearMiss(
      LoggedRequest request, StubMapping mapping, MatchResult matchResult, String scenarioState) {
    this(request, mapping, null, matchResult, scenarioState);
  }

  /**
   * Instantiates a new Near miss.
   *
   * @param request the request
   * @param requestPattern the request pattern
   * @param matchResult the match result
   */
  public NearMiss(LoggedRequest request, RequestPattern requestPattern, MatchResult matchResult) {
    this(request, null, requestPattern, matchResult, null);
  }

  /**
   * Gets request.
   *
   * @return the request
   */
  public LoggedRequest getRequest() {
    return request;
  }

  /**
   * Gets stub mapping.
   *
   * @return the stub mapping
   */
  public StubMapping getStubMapping() {
    return mapping;
  }

  /**
   * Gets request pattern.
   *
   * @return the request pattern
   */
  public RequestPattern getRequestPattern() {
    return requestPattern;
  }

  /**
   * Gets match result.
   *
   * @return the match result
   */
  public MatchResult getMatchResult() {
    return matchResult;
  }

  @Override
  public int compareTo(NearMiss o) {
    return o.getMatchResult().compareTo(matchResult);
  }

  /**
   * Gets diff.
   *
   * @return the diff
   */
  @JsonIgnore
  public Diff getDiff() {
    if (requestPattern != null) {
      return new Diff(requestPattern, request);
    }

    return new Diff(getStubMapping(), request, scenarioState);
  }

  @Override
  public String toString() {
    return getDiff().toString();
  }
}
