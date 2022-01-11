/*
 * Copyright (C) 2016-2022 Thomas Akehurst
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

import java.util.regex.Pattern;

public abstract class AbstractRegexPattern extends StringValuePattern {

  protected final Pattern pattern;

  protected AbstractRegexPattern(String regex) {
    super(regex);
    pattern = Pattern.compile(regex, DOTALL);
  }

  @Override
  public MatchResult match(String value) {
    return MatchResult.of(value != null && pattern.matcher(value).matches());
  }
}
