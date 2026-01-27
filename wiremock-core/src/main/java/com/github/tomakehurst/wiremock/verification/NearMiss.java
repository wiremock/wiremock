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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.ServeContext;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.diff.Diff;

public class NearMiss implements Comparable<NearMiss> {

  private final LoggedRequest request;
  private final StubMapping mapping;
  private final RequestPattern requestPattern;
  private final MatchResult matchResult;
  private final String scenarioState;
  @JsonIgnore private final ServeContext serveContext;

  @JsonCreator
  public NearMiss(
      @JsonProperty("request") LoggedRequest request,
      @JsonProperty("stubMapping") StubMapping mapping,
      @JsonProperty("requestPattern") RequestPattern requestPattern,
      @JsonProperty("matchResult") MatchResult matchResult,
      @JsonProperty("scenarioState") String scenarioState) {
    this(request, mapping, requestPattern, matchResult, scenarioState, null);
  }

  public NearMiss(
      LoggedRequest request, StubMapping mapping, MatchResult matchResult, String scenarioState) {
    this(request, mapping, null, matchResult, scenarioState, null);
  }

  public NearMiss(LoggedRequest request, RequestPattern requestPattern, MatchResult matchResult) {
    this(request, null, requestPattern, matchResult, null, null);
  }

  public NearMiss(
      LoggedRequest request,
      StubMapping mapping,
      MatchResult matchResult,
      String scenarioState,
      ServeContext serveContext) {
    this(request, mapping, null, matchResult, scenarioState, serveContext);
  }

  public NearMiss(
      LoggedRequest request,
      RequestPattern requestPattern,
      MatchResult matchResult,
      ServeContext serveContext) {
    this(request, null, requestPattern, matchResult, null, serveContext);
  }

  private NearMiss(
      LoggedRequest request,
      StubMapping mapping,
      RequestPattern requestPattern,
      MatchResult matchResult,
      String scenarioState,
      ServeContext serveContext) {
    this.request = request;
    this.mapping = mapping;
    this.requestPattern = requestPattern;
    this.matchResult = matchResult;
    this.scenarioState = scenarioState;
    this.serveContext = serveContext;
  }

  public LoggedRequest getRequest() {
    return request;
  }

  public StubMapping getStubMapping() {
    return mapping;
  }

  public RequestPattern getRequestPattern() {
    return requestPattern;
  }

  public MatchResult getMatchResult() {
    return matchResult;
  }

  @Override
  public int compareTo(NearMiss o) {
    return o.getMatchResult().compareTo(matchResult);
  }

  @JsonIgnore
  public Diff getDiff() {
    if (requestPattern != null) {
      return new Diff(requestPattern, request, serveContext);
    }

    return new Diff(getStubMapping(), request, scenarioState, serveContext);
  }

  @Override
  public String toString() {
    return getDiff().toString();
  }
}
