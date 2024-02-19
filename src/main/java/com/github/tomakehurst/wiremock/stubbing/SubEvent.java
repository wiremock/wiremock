/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.common.Message;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SubEvent {

  public static final String NON_MATCH_TYPE = "REQUEST_NOT_MATCHED";
  public static final String JSON_ERROR = "JSON_ERROR";
  public static final String XML_ERROR = "XML";
  public static final String INFO = "INFO";
  public static final String WARNING = "WARNING";
  public static final String ERROR = "ERROR";

  private static final List<String> STANDARD_TYPES =
      Arrays.asList(NON_MATCH_TYPE, JSON_ERROR, XML_ERROR, INFO, WARNING, ERROR);

  private final String type;

  private final Long timeOffsetNanos;

  private final Map<String, Object> data;

  public static SubEvent info(String message) {
    return message(INFO, message);
  }

  public static SubEvent warning(String message) {
    return message(WARNING, message);
  }

  public static SubEvent error(String message) {
    return message(ERROR, message);
  }

  public static SubEvent message(String type, String message) {
    return new SubEvent(type, null, new Message(message));
  }

  public SubEvent(String type, Object data) {
    this(type, null, data);
  }

  public SubEvent(String type, Long timeOffsetMillis, Object data) {
    this(type, timeOffsetMillis, Json.objectToMap(data));
  }

  public SubEvent(
      @JsonProperty("type") String type,
      @JsonProperty("timeOffsetNanos") Long timeOffsetNanos,
      @JsonProperty("data") Map<String, Object> data) {
    this.type = type;
    this.timeOffsetNanos = timeOffsetNanos;
    this.data = data;
  }

  public String getType() {
    return type;
  }

  public Long getTimeOffsetNanos() {
    return timeOffsetNanos;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public <T> T getDataAs(Class<T> dataType) {
    return Json.mapToObject(data, dataType);
  }

  public boolean isEquivalentStandardTypedEventTo(SubEvent other) {
    return isStandardType()
        && other.isStandardType()
        && type.equals(other.type)
        && data.equals(other.data);
  }

  boolean isStandardType() {
    return STANDARD_TYPES.contains(type);
  }
}
