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
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Map;

public class SubEvent {

  public static final String NON_MATCH_TYPE = "REQUEST_NOT_MATCHED";
  private final String type;

  private final long timeOffsetNanos;

  private final Map<String, Object> data;

  public SubEvent(
      @JsonProperty("type") String type,
      @JsonProperty("timeOffsetNanos") long timeOffsetNanos,
      @JsonProperty("data") Map<String, Object> data) {
    this.type = type;
    this.timeOffsetNanos = timeOffsetNanos;
    this.data = data;
  }

  public SubEvent(String type, long timeOffsetMillis, Object data) {
    this(type, timeOffsetMillis, Json.objectToMap(data));
  }

  public String getType() {
    return type;
  }

  public long getTimeOffsetNanos() {
    return timeOffsetNanos;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public <T> T getDataAs(Class<T> dataType) {
    return Json.mapToObject(data, dataType);
  }
}
