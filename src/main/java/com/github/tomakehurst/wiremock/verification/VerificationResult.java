/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;

/** The type Verification result. */
public class VerificationResult extends JournalBasedResult {

  private final Integer count;

  /**
   * Instantiates a new Verification result.
   *
   * @param count the count
   * @param requestJournalDisabled the request journal disabled
   */
  @JsonCreator
  public VerificationResult(
      @JsonProperty("count") Integer count,
      @JsonProperty("requestJournalDisabled") boolean requestJournalDisabled) {
    super(requestJournalDisabled);
    this.count = count;
  }

  /**
   * From verification result.
   *
   * @param json the json
   * @return the verification result
   */
  public static VerificationResult from(String json) {
    return Json.read(json, VerificationResult.class);
  }

  /**
   * With count verification result.
   *
   * @param count the count
   * @return the verification result
   */
  public static VerificationResult withCount(int count) {
    return new VerificationResult(count, false);
  }

  /**
   * With request journal disabled verification result.
   *
   * @return the verification result
   */
  public static VerificationResult withRequestJournalDisabled() {
    return new VerificationResult(-1, true);
  }

  /**
   * Gets count.
   *
   * @return the count
   */
  public int getCount() {
    return count;
  }
}
