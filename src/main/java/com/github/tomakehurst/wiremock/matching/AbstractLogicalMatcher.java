/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import static java.util.Arrays.asList;

import java.util.List;
import java.util.stream.Collectors;

/** The type Abstract logical matcher. */
public abstract class AbstractLogicalMatcher extends StringValuePattern {

  /** The Operands. */
  protected final List<StringValuePattern> operands;

  /**
   * Instantiates a new Abstract logical matcher.
   *
   * @param operands the operands
   */
  public AbstractLogicalMatcher(StringValuePattern... operands) {
    this(asList(operands));
  }

  /**
   * Instantiates a new Abstract logical matcher.
   *
   * @param operands the operands
   */
  public AbstractLogicalMatcher(List<StringValuePattern> operands) {
    super(checkAtLeast2OperandsAndReturnFirstExpected(operands));
    this.operands = operands;
  }

  private static String checkAtLeast2OperandsAndReturnFirstExpected(
      List<StringValuePattern> operands) {
    if (operands.size() < 2) {
      throw new IllegalArgumentException("Must be constructed with at least two matchers");
    }

    return operands.stream()
        .findFirst()
        .map(ContentPattern::getExpected)
        .orElseThrow(() -> new IllegalArgumentException("Matchers must have expected values"));
  }

  @Override
  public String getExpected() {
    return operands.stream()
        .map(contentPattern -> contentPattern.getName() + " " + contentPattern.getExpected())
        .collect(Collectors.joining(" " + getOperationName() + " "));
  }

  /**
   * Gets operation name.
   *
   * @return the operation name
   */
  protected abstract String getOperationName();
}
