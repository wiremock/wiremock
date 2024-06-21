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
package com.github.tomakehurst.wiremock.verification.diff;

import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.MatchResult.DiffDescription;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;
import org.wiremock.annotations.Beta;

@Beta(
    justification =
        "Add self-description callbacks for use in Diff - https://github.com/wiremock/wiremock/issues/2758")
public class DiffDescriptionLine<T> extends DiffLine<T> {

  public DiffDescriptionLine(
      String requestAttribute, NamedValueMatcher<T> pattern, T value, String printedPatternValue) {
    super(requestAttribute, pattern, value, printedPatternValue);
  }

  @Override
  public String getPrintedPatternValue() {
    final DiffDescription diffDescription = getDiffDescription();
    if (diffDescription != null) {
      return diffDescription.expected;
    }
    return super.getPrintedPatternValue();
  }

  @Override
  public Object getActual() {
    final DiffDescription diffDescription = getDiffDescription();
    if (diffDescription != null) {
      return diffDescription.actual;
    }
    return super.getActual();
  }

  @Override
  public String getMessage() {
    final DiffDescription diffDescription = getDiffDescription();
    if (diffDescription != null) {
      return diffDescription.errorMessage;
    }
    return super.getMessage();
  }

  private DiffDescription getDiffDescription() {
    return this.getMatchResult().getDiffDescription();
  }

  public MatchResult getMatchResult() {
    return pattern.match(value);
  }
}
