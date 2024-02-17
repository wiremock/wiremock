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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Set;
import java.util.stream.Collectors;

public class Methods implements NamedValueMatcher<RequestMethod> {

  private final Set<RequestMethod> onOf;

  public Methods(@JsonProperty("onOf") Set<RequestMethod> onOf) {
    this.onOf = onOf.stream().filter(e -> !RequestMethod.ANY.equals(e)).collect(Collectors.toSet());
  }

  public Set<RequestMethod> getOnOf() {
    return onOf;
  }

  @Override
  public String getName() {
    return "oneOf: " + onOf.toString();
  }

  @Override
  public String getExpected() {
    return getName();
  }

  @Override
  public MatchResult match(RequestMethod value) {
    return MatchResult.of(this.onOf.contains(value));
  }
}
