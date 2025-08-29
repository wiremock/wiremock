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
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Message;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** The type Sub event. */
public class SubEvent {

  /** The constant NON_MATCH_TYPE. */
  public static final String NON_MATCH_TYPE = "REQUEST_NOT_MATCHED";

  /** The constant JSON_ERROR. */
  public static final String JSON_ERROR = "JSON_ERROR";

  /** The constant XML_ERROR. */
  public static final String XML_ERROR = "XML";

  /** The constant INFO. */
  public static final String INFO = "INFO";

  /** The constant WARNING. */
  public static final String WARNING = "WARNING";

  /** The constant ERROR. */
  public static final String ERROR = "ERROR";

  private static final List<String> STANDARD_TYPES =
      Arrays.asList(NON_MATCH_TYPE, JSON_ERROR, XML_ERROR, INFO, WARNING, ERROR);

  private final String type;

  private final Long timeOffsetNanos;

  private final Map<String, Object> data;

  /**
   * Info sub event.
   *
   * @param message the message
   * @return the sub event
   */
  public static SubEvent info(String message) {
    return message(INFO, message);
  }

  /**
   * Warning sub event.
   *
   * @param message the message
   * @return the sub event
   */
  public static SubEvent warning(String message) {
    return message(WARNING, message);
  }

  /**
   * Error sub event.
   *
   * @param message the message
   * @return the sub event
   */
  public static SubEvent error(String message) {
    return message(ERROR, message);
  }

  /**
   * Message sub event.
   *
   * @param type the type
   * @param message the message
   * @return the sub event
   */
  public static SubEvent message(String type, String message) {
    return new SubEvent(type, null, new Message(message));
  }

  /**
   * Instantiates a new Sub event.
   *
   * @param type the type
   * @param data the data
   */
  public SubEvent(String type, Object data) {
    this(type, null, data);
  }

  /**
   * Instantiates a new Sub event.
   *
   * @param type the type
   * @param timeOffsetMillis the time offset millis
   * @param data the data
   */
  public SubEvent(String type, Long timeOffsetMillis, Object data) {
    this(type, timeOffsetMillis, Json.objectToMap(data));
  }

  /**
   * Instantiates a new Sub event.
   *
   * @param type the type
   * @param timeOffsetNanos the time offset nanos
   * @param data the data
   */
  public SubEvent(
      @JsonProperty("type") String type,
      @JsonProperty("timeOffsetNanos") Long timeOffsetNanos,
      @JsonProperty("data") Map<String, Object> data) {
    this.type = type;
    this.timeOffsetNanos = timeOffsetNanos;
    this.data = data;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Gets time offset nanos.
   *
   * @return the time offset nanos
   */
  public Long getTimeOffsetNanos() {
    return timeOffsetNanos;
  }

  /**
   * Gets data.
   *
   * @return the data
   */
  public Map<String, Object> getData() {
    return data;
  }

  /**
   * Gets data as.
   *
   * @param <T> the type parameter
   * @param dataType the data type
   * @return the data as
   */
  public <T> T getDataAs(Class<T> dataType) {
    return Json.mapToObject(data, dataType);
  }

  /**
   * Is equivalent standard typed event to boolean.
   *
   * @param other the other
   * @return the boolean
   */
  public boolean isEquivalentStandardTypedEventTo(SubEvent other) {
    return isStandardType()
        && other.isStandardType()
        && type.equals(other.type)
        && data.equals(other.data);
  }

  /**
   * Is standard type boolean.
   *
   * @return the boolean
   */
  boolean isStandardType() {
    return STANDARD_TYPES.contains(type);
  }
}
