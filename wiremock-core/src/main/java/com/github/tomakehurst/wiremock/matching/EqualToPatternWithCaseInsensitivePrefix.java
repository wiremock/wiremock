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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.common.Strings.normalisedLevenshteinDistance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class EqualToPatternWithCaseInsensitivePrefix extends StringValuePattern {

  private final String prefix;
  private final String testValue;

  public EqualToPatternWithCaseInsensitivePrefix(
      @JsonProperty("prefix") String prefix, @JsonProperty("equalTo") String testValue) {
    super(prefix + testValue);
    Objects.requireNonNull(prefix, "prefix cannot be null");
    this.prefix = prefix;
    this.testValue = testValue;
  }

  @Override
  public MatchResult match(final String value, ServeContext context) {
    final String resolvedPrefix = resolvePrefix(context);
    final String resolvedTestValue = resolveTestValue(context);
    final String resolvedExpected = resolvedPrefix + resolvedTestValue;
    return new MatchResult() {
      @Override
      public boolean isExactMatch() {
        return value != null
            && value.length() >= resolvedPrefix.length()
            && value.substring(0, resolvedPrefix.length()).equalsIgnoreCase(resolvedPrefix)
            && Objects.equals(resolvedTestValue, value.substring(resolvedPrefix.length()));
      }

      @Override
      public double getDistance() {
        return normalisedLevenshteinDistance(resolvedExpected, value);
      }
    };
  }

  @Override
  public MatchResult match(final String value) {
    return match(value, null);
  }

  private String resolvePrefix(ServeContext context) {
    if (isTemplated() && context != null) {
      return context.renderTemplate(prefix);
    }
    return prefix;
  }

  private String resolveTestValue(ServeContext context) {
    if (isTemplated() && context != null) {
      return context.renderTemplate(testValue);
    }
    return testValue;
  }
}
