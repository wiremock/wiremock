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

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.google.common.base.Objects;
import java.util.List;

@JsonDeserialize(as = SingleMatchMultiValuePattern.class)
public class SingleMatchMultiValuePattern extends MultiValuePattern {

  private final StringValuePattern valuePattern;

  @JsonCreator
  public SingleMatchMultiValuePattern(StringValuePattern valuePattern) {
    this.valuePattern = valuePattern;
  }

  @Override
  public MatchResult match(MultiValue multiValue) {
    List<String> values = multiValue.isPresent() ? multiValue.values() : singletonList(null);
    return getBestMatch(valuePattern, values);
  }

  @JsonValue
  public StringValuePattern getValuePattern() {
    return valuePattern;
  }

  @Override
  public String getName() {
    return valuePattern.getName();
  }

  @Override
  public String getExpected() {
    return valuePattern.getExpected();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SingleMatchMultiValuePattern that = (SingleMatchMultiValuePattern) o;
    return Objects.equal(valuePattern, that.valuePattern);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(valuePattern);
  }
}
