/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification.diff;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffEventData {

  public static final String KEY = "DIFF_REPORT";

  private final int status;
  private final String contentType;
  private final String report;

  public DiffEventData(
      @JsonProperty("status") int status,
      @JsonProperty("contentType") String contentType,
      @JsonProperty("report") String report) {
    this.status = status;
    this.contentType = contentType;
    this.report = report;
  }

  public int getStatus() {
    return status;
  }

  public String getContentType() {
    return contentType;
  }

  public String getReport() {
    return report;
  }
}
