/*
 * Copyright (C) 2022-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

/** The type Limit. */
public class Limit {

  /** The constant UNLIMITED. */
  public static final Limit UNLIMITED = new Limit(null);

  private final Integer limit;

  /**
   * Instantiates a new Limit.
   *
   * @param limit the limit
   */
  public Limit(Integer limit) {
    this.limit = limit;
  }

  /**
   * Is exceeded by boolean.
   *
   * @param value the value
   * @return the boolean
   */
  public boolean isExceededBy(int value) {
    return limit != null && value > limit;
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  public Integer getValue() {
    return limit;
  }

  /**
   * Is unlimited boolean.
   *
   * @return the boolean
   */
  public boolean isUnlimited() {
    return limit == null;
  }
}
