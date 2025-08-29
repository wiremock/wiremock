/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import java.util.Collections;
import java.util.List;

/** The type Cookie. */
public class Cookie extends MultiValue {

  /**
   * Cookie cookie.
   *
   * @param values the values
   * @return the cookie
   */
  @JsonCreator
  public static Cookie cookie(ListOrSingle<String> values) {
    return new Cookie(null, values);
  }

  /**
   * Cookie cookie.
   *
   * @param value the value
   * @return the cookie
   */
  public static Cookie cookie(String value) {
    return new Cookie(null, value);
  }

  /**
   * Absent cookie.
   *
   * @return the cookie
   */
  public static Cookie absent() {
    return new Cookie(null, Collections.emptyList());
  }

  /**
   * Instantiates a new Cookie.
   *
   * @param value the value
   */
  public Cookie(String value) {
    super(null, singletonList(value));
  }

  /**
   * Instantiates a new Cookie.
   *
   * @param values the values
   */
  public Cookie(List<String> values) {
    this(null, values);
  }

  /**
   * Instantiates a new Cookie.
   *
   * @param name the name
   * @param value the value
   */
  public Cookie(String name, String... value) {
    super(name, asList(value));
  }

  /**
   * Instantiates a new Cookie.
   *
   * @param name the name
   * @param values the values
   */
  public Cookie(String name, List<String> values) {
    super(name, values);
  }

  /**
   * Is absent boolean.
   *
   * @return the boolean
   */
  @JsonIgnore
  public boolean isAbsent() {
    return !isPresent();
  }

  @JsonValue
  public ListOrSingle<String> getValues() {
    return new ListOrSingle<>(isPresent() ? values() : Collections.emptyList());
  }

  /**
   * Gets value.
   *
   * @return the value
   */
  @JsonIgnore
  public String getValue() {
    return firstValue();
  }

  @Override
  public String toString() {
    return isAbsent() ? "(absent)" : String.join("; ", getValues());
  }
}
