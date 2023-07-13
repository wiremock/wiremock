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
package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;

public class PathTemplatePattern extends StringValuePattern {

  private final PathTemplate pathTemplate;

  public PathTemplatePattern(@JsonProperty("matchesPathTemplate") String expectedValue) {
    super(expectedValue);
    this.pathTemplate = new PathTemplate(expectedValue);
  }

  public String getMatchesPathTemplate() {
    return expectedValue;
  }

  @JsonIgnore
  public PathTemplate getPathTemplate() {
    return pathTemplate;
  }

  @Override
  public MatchResult match(String path) {
    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return pathTemplate.matches(path);
      }

      @Override
      public double getDistance() {
        if (isExactMatch()) {
          return 0;
        }

        String expected = pathTemplate.withoutVariables();
        return Strings.normalisedLevenshteinDistance(expected, path);
      }
    };
  }
}
