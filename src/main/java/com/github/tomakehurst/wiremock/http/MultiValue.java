/*
 * Copyright (C) 2012-2025 Thomas Akehurst
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

/** The type Multi value. */
public class MultiValue {

  /** The Key. */
  protected final String key;

  /** The Values. */
  protected final List<String> values;

  /**
   * Instantiates a new Multi value.
   *
   * @param key the key
   * @param values the values
   */
  public MultiValue(String key, List<String> values) {
    this.key = key;
    this.values = values;
  }

  /**
   * Gets key.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Gets values.
   *
   * @return the values
   */
  public List<String> getValues() {
    return values;
  }

  /**
   * Is present boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isPresent() {
    return !values.isEmpty();
  }

  /**
   * Key string.
   *
   * @return the string
   */
  public String key() {
    return key;
  }

  /**
   * First value string.
   *
   * @return the string
   */
  public String firstValue() {
    checkPresent();
    return values.get(0);
  }

  /**
   * Values list.
   *
   * @return the list
   */
  public List<String> values() {
    checkPresent();
    return values;
  }

  private void checkPresent() {
    checkState(isPresent(), "No value for " + key);
  }

  /**
   * Is single valued boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isSingleValued() {
    return values.size() == 1;
  }

  /**
   * Contains value boolean.
   *
   * @param expectedValue the expected value
   * @return the boolean
   */
  public boolean containsValue(String expectedValue) {
    return values.contains(expectedValue);
  }

  /**
   * Has value matching boolean.
   *
   * @param valuePattern the value pattern
   * @return the boolean
   */
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
