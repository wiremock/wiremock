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
package com.github.tomakehurst.wiremock.matching;

import static java.util.regex.Pattern.DOTALL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public abstract class AbstractRegexPattern extends StringValuePattern {

  private final ConcurrentMap<String, Pattern> compiledPatterns;

  protected AbstractRegexPattern(String regex) {
    super(regex);
    compiledPatterns = new ConcurrentHashMap<>();
  }

  // TODO: remove me
  @Override
  public MatchResult match(String value) {
    return match(value, null);
  }

  @Override
  public MatchResult match(String value, ServeContext context) {
    Pattern resolvedPattern = getPattern(context);
    return MatchResult.of(value != null && resolvedPattern.matcher(value).matches());
  }

  protected Pattern getPattern(ServeContext context) {
    String regex = isTemplated() ? resolveExpectedValue(context) : expectedValue;
    return compiledPatterns.computeIfAbsent(regex, compiled -> Pattern.compile(compiled, DOTALL));
  }
}
