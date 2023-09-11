/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import java.util.Set;

public class RequestMethodMatcher implements NamedValueMatcher<Set<RequestMethod>> {

  private final Set<RequestMethod> requestMethods;

  public RequestMethodMatcher(Set<RequestMethod> requestMethods) {
    this.requestMethods = requestMethods;
  }

  @Override
  public String getName() {
    return requestMethods.toString();
  }

  @Override
  public String getExpected() {
    return getName();
  }

  @Override
  public MatchResult match(Set<RequestMethod> method) {
    return MatchResult.of(
        this.requestMethods.contains(RequestMethod.ANY)
            || this.requestMethods.contains(method.iterator().next()));
  }

  public MatchResult match(RequestMethod method) {
    return match(Set.of(method));
  }
}
