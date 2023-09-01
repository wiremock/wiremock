/*
 * Copyright (C) 2012-2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.ParameterUtils.checkState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.List;
import java.util.stream.Collectors;

public class MultiValue {

  protected final String key;
  protected final List<String> values;

  public MultiValue(String key, List<String> values) {
    this.key = key;
    this.values = values;
  }

  public String getKey() {
    return key;
  }

  public List<String> getValues() {
    return values;
  }

  @JsonIgnore
  public boolean isPresent() {
    return !values.isEmpty();
  }

  public String key() {
    return key;
  }

  public String firstValue() {
    checkPresent();
    return values.get(0);
  }

  public List<String> values() {
    checkPresent();
    return values;
  }

  private void checkPresent() {
    checkState(isPresent(), "No value for " + key);
  }

  @JsonIgnore
  public boolean isSingleValued() {
    return values.size() == 1;
  }

  public boolean containsValue(String expectedValue) {
    return values.contains(expectedValue);
  }

  public boolean hasValueMatching(final StringValuePattern valuePattern) {
    return (valuePattern.nullSafeIsAbsent() && !isPresent()) || anyValueMatches(valuePattern);
  }

  private boolean anyValueMatches(final StringValuePattern valuePattern) {
    return values.stream().anyMatch(headerValue -> valuePattern.match(headerValue).isExactMatch());
  }

  @Override
  public String toString() {
    return values.stream().map(value -> key + ": " + value).collect(Collectors.joining("\n"));
  }
}
