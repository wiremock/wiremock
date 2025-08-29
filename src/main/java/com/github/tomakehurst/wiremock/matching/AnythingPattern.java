/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonProperty;

/** The type Anything pattern. */
public class AnythingPattern extends StringValuePattern {

  /**
   * Instantiates a new Anything pattern.
   *
   * @param expectedValue the expected value
   */
  public AnythingPattern(@JsonProperty("anything") String expectedValue) {
    super(expectedValue);
  }

  /** Instantiates a new Anything pattern. */
  public AnythingPattern() {
    this("(always)");
  }

  /**
   * Gets anything.
   *
   * @return the anything
   */
  public String getAnything() {
    return "anything";
  }

  @Override
  public MatchResult match(String value) {
    return MatchResult.exactMatch();
  }

  @Override
  public String toString() {
    return "anything";
  }
}
