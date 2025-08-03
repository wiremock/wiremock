/*
 * Copyright (C) 2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.Set;

public abstract class MultiRequestMethodPattern extends RequestMethod {

  protected final Set<RequestMethod> methods;

  protected MultiRequestMethodPattern(String name, Set<RequestMethod> methods) {
    super(name);
    this.methods = methods;
  }

  public Set<RequestMethod> getMethods() {
    return methods;
  }

  @Override
  public String getExpected() {
    return getName() + ": " + methods.toString();
  }

  public static class IsOneOf extends MultiRequestMethodPattern {

    public static final String NAME = "oneOf";

    public IsOneOf(Set<RequestMethod> methods) {
      super(NAME, methods);
    }

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public MatchResult match(RequestMethod value) {
      return methods.contains(value) ? MatchResult.exactMatch() : MatchResult.noMatch();
    }
  }

  public static class IsNoneOf extends MultiRequestMethodPattern {

    public static final String NAME = "noneOf";

    public IsNoneOf(Set<RequestMethod> methods) {
      super(NAME, methods);
    }

    @Override
    public String getName() {
      return NAME;
    }

    @Override
    public MatchResult match(RequestMethod value) {
      return methods.contains(value) ? MatchResult.noMatch() : MatchResult.exactMatch();
    }
  }
}
