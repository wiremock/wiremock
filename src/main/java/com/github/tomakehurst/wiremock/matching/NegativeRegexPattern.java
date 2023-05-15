/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

public class NegativeRegexPattern extends AbstractRegexPattern {

  public NegativeRegexPattern(@JsonProperty("doesNotMatch") String regex) {
    super(regex);
  }

  public String getDoesNotMatch() {
    return expectedValue;
  }

  @Override
  public MatchResult match(String value) {
    return invert(super.match(value));
  }

  private MatchResult invert(final MatchResult matchResult) {
    return new MatchResult() {

      @Override
      public boolean isExactMatch() {
        return !matchResult.isExactMatch();
      }

      @Override
      public double getDistance() {
        return 1.0 - matchResult.getDistance();
      }
    };
  }
}
