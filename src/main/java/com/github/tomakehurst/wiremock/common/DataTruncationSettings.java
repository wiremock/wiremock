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

/** The type Data truncation settings. */
public class DataTruncationSettings {

  /** The constant NO_TRUNCATION. */
  public static final DataTruncationSettings NO_TRUNCATION =
      new DataTruncationSettings(Limit.UNLIMITED);

  /** The constant DEFAULTS. */
  public static final DataTruncationSettings DEFAULTS = NO_TRUNCATION;

  private final Limit maxResponseBodySize;

  /**
   * Instantiates a new Data truncation settings.
   *
   * @param maxResponseBodySize the max response body size
   */
  public DataTruncationSettings(Limit maxResponseBodySize) {
    this.maxResponseBodySize = maxResponseBodySize;
  }

  /**
   * Gets max response body size.
   *
   * @return the max response body size
   */
  public Limit getMaxResponseBodySize() {
    return maxResponseBodySize;
  }
}
