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

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.NamedValueMatcher;

public class DiffDescriptionLine extends DiffLine<Request> {

  private final MatchResult.DiffDescription diffDescription;

  public DiffDescriptionLine(
      String requestAttribute,
      NamedValueMatcher<Request> pattern,
      Request value,
      String printedPatternValue) {
    super(requestAttribute, pattern, value, printedPatternValue);
    this.diffDescription = pattern.match(value).getDiffDescription();
  }

  @Override
  public String getPrintedPatternValue() {
    if (diffDescription != null) {
      return this.diffDescription.expected;
    }
    return super.getPrintedPatternValue();
  }

  @Override
  public Object getActual() {
    if (diffDescription != null) {
      return this.diffDescription.actual;
    }
    return super.getActual();
  }

  @Override
  public String getMessage() {
    if (diffDescription != null) {
      return this.diffDescription.errorMessage;
    }
    return super.getMessage();
  }
}
