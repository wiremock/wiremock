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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize(as = IncludesMatchMultiValuePattern.class)
public class IncludesMatchMultiValuePattern extends MultipleMatchMultiValuePattern {

  public static final String JSON_KEY = "includes";
  public static final String INCLUDING_OPERATOR = " including ";

  @JsonProperty(JSON_KEY)
  private final List<StringValuePattern> stringValuePatterns;

  @JsonCreator
  public IncludesMatchMultiValuePattern(
      @JsonProperty(JSON_KEY) final List<StringValuePattern> stringValuePatterns) {
    this.stringValuePatterns = stringValuePatterns;
  }

  @Override
  public List<StringValuePattern> getValues() {
    return stringValuePatterns;
  }

  @Override
  public String getOperator() {
    return INCLUDING_OPERATOR;
  }
}
