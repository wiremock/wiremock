/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.BiPredicate;

/**
 * Defines a strategy for matching a request count during verification.
 *
 * <p>This interface extends {@link BiPredicate} to test an actual count against an expected count.
 * It also provides a friendly name for use in verification failure messages.
 */
public interface CountMatchingMode extends BiPredicate<Integer, Integer> {

  /**
   * Gets a human-readable name for the matching mode.
   *
   * <p>This name is used in verification failure messages to improve readability, e.g., "at least",
   * "exactly", "less than".
   *
   * @return The friendly name of the mode.
   */
  String getFriendlyName();
}
