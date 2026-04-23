/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.message;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@JsonInclude(NON_EMPTY)
public class HttpRequestTrigger implements MessageTrigger {

  private final RequestPattern requestPattern;

  @JsonCreator
  public HttpRequestTrigger(@JsonProperty("requestPattern") RequestPattern requestPattern) {
    this.requestPattern = requestPattern;
  }

  public static HttpRequestTrigger forRequestPattern(RequestPattern requestPattern) {
    return new HttpRequestTrigger(requestPattern);
  }

  public RequestPattern getRequestPattern() {
    return requestPattern;
  }

  public boolean matches(
      ServeEvent serveEvent, Map<String, RequestMatcherExtension> customMatchers) {
    if (serveEvent == null || serveEvent.getRequest() == null) {
      return false;
    }
    return matches(serveEvent.getRequest(), customMatchers);
  }

  public boolean matches(Request request, Map<String, RequestMatcherExtension> customMatchers) {
    if (requestPattern == null) {
      return true;
    }
    MatchResult matchResult = requestPattern.match(request, customMatchers);
    return matchResult.isExactMatch();
  }

  public boolean matches(ServeEvent serveEvent) {
    return matches(serveEvent, Collections.emptyMap());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    HttpRequestTrigger that = (HttpRequestTrigger) o;
    return Objects.equals(requestPattern, that.requestPattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestPattern);
  }

  @Override
  public String toString() {
    return "HttpRequestTrigger{" + "requestPattern=" + requestPattern + '}';
  }
}
