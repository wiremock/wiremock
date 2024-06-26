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

import com.github.tomakehurst.wiremock.matching.MatchResult.DiffDescription;

public class DiffDescriptionLine<T> extends DiffLine<T> {

  private final DiffDescription diffDescription;
  private final Boolean isExactMatch;

  public DiffDescriptionLine(
      DiffDescription diffDescription, String requestAttribute, Boolean isExactMatch) {
    super(requestAttribute, null, null, diffDescription.getExpected());
    this.diffDescription = diffDescription;
    this.isExactMatch = isExactMatch;
  }

  @Override
  public Object getActual() {
    return this.diffDescription.getActual();
  }

  @Override
  public String getMessage() {
    return this.diffDescription.getErrorMessage();
  }

  @Override
  protected boolean isExactMatch() {
    return this.isExactMatch;
  }
}
