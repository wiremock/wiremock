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

import static java.util.Collections.min;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.MultiValue;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(using = MultiValuePatternDeserializer.class)
public abstract class MultiValuePattern implements NamedValueMatcher<MultiValue> {

  protected static final String INCLUDES_MATCH_MULTI_VALUE_PATTERN_JSON_KEY = "includes";
  protected static final String EXACT_MATCH_MULTI_VALUE_PATTERN_JSON_KEY = "hasExactly";

  public static MultiValuePattern of(StringValuePattern valuePattern) {
    return new SingleMatchMultiValuePattern(valuePattern);
  }

  public static MultiValuePattern absent() {
    return new SingleMatchMultiValuePattern(WireMock.absent());
  }

  protected static MatchResult getBestMatch(
      final StringValuePattern valuePattern, List<String> values) {
    List<MatchResult> allResults =
        values.stream().map(valuePattern::match).collect(Collectors.toList());
    return min(allResults, Comparator.comparingDouble(MatchResult::getDistance));
  }
}
