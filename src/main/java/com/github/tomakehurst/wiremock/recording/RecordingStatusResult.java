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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The type Recording status result. */
public class RecordingStatusResult {

  private final RecordingStatus status;

  /**
   * Instantiates a new Recording status result.
   *
   * @param status the status
   */
  @JsonCreator
  public RecordingStatusResult(@JsonProperty("status") String status) {
    this(RecordingStatus.valueOf(status));
  }

  /**
   * Instantiates a new Recording status result.
   *
   * @param status the status
   */
  public RecordingStatusResult(RecordingStatus status) {
    this.status = status;
  }

  /**
   * Gets status.
   *
   * @return the status
   */
  public RecordingStatus getStatus() {
    return status;
  }
}
