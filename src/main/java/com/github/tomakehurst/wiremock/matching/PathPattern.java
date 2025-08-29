/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import java.util.Objects;

/** The type Path pattern. */
public abstract class PathPattern extends StringValuePattern {

  /** The Value pattern. */
  protected final StringValuePattern valuePattern;

  /**
   * Instantiates a new Path pattern.
   *
   * @param expectedValue the expected value
   * @param valuePattern the value pattern
   */
  protected PathPattern(String expectedValue, StringValuePattern valuePattern) {
    super(expectedValue);
    this.valuePattern = valuePattern;
  }

  /**
   * Gets value pattern.
   *
   * @return the value pattern
   */
  public StringValuePattern getValuePattern() {
    return valuePattern;
  }

  /**
   * Is simple boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isSimple() {
    return valuePattern == null;
  }

  @Override
  public MatchResult match(String value) {
    if (isSimple()) {
      return isSimpleMatch(value);
    }

    return isAdvancedMatch(value);
  }

  /**
   * Is simple match match result.
   *
   * @param value the value
   * @return the match result
   */
  protected abstract MatchResult isSimpleMatch(String value);

  /**
   * Is advanced match match result.
   *
   * @param value the value
   * @return the match result
   */
  protected abstract MatchResult isAdvancedMatch(String value);

  /**
   * Gets expression result.
   *
   * @param value the value
   * @return the expression result
   */
  public abstract ListOrSingle<String> getExpressionResult(String value);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    PathPattern that = (PathPattern) o;
    return Objects.equals(expectedValue, that.expectedValue)
        && Objects.equals(valuePattern, that.valuePattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), valuePattern);
  }

  /** The type Sub expression exception. */
  protected static class SubExpressionException extends RuntimeException {
    /**
     * Instantiates a new Sub expression exception.
     *
     * @param message the message
     */
    public SubExpressionException(String message) {
      super(message);
    }

    /**
     * Instantiates a new Sub expression exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SubExpressionException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
