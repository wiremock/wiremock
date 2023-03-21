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
import com.github.tomakehurst.wiremock.http.MultiValue;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MultipleMatchMultiValuePattern extends MultiValuePattern {

  private static final String COMMA = ",";

  private static final String AND = " AND ";

  @Override
  public String getName() {
    return getValues().stream()
        .map(
            stringValuePattern ->
                stringValuePattern.getName() + " " + stringValuePattern.getExpected())
        .collect(Collectors.joining(AND));
  }

  @Override
  public String getExpected() {
    return getValues().stream()
        .map(StringValuePattern::getValue)
        .collect(Collectors.joining(COMMA));
  }

  @Override
  public MatchResult match(MultiValue value) {
    if (!value.isPresent()) {
      return MatchResult.of(false);
    }
    List<MatchResult> matchResults =
        getValues().stream()
            .map(stringValuePattern -> getBestMatch(stringValuePattern, value.values()))
            .collect(Collectors.toList());
    return MatchResult.aggregate(matchResults);
  }

  @JsonIgnore
  public abstract List<StringValuePattern> getValues();
}
