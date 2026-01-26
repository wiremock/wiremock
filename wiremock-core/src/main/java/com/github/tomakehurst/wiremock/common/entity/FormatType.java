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
package com.github.tomakehurst.wiremock.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class FormatType {

  public static final FormatType JSON = new FormatType("json");
  public static final FormatType HTML = new FormatType("html");
  public static final FormatType TEXT = new FormatType("text");
  public static final FormatType XML = new FormatType("xml");
  public static final FormatType YAML = new FormatType("yaml");
  public static final FormatType CSV = new FormatType("csv");
  public static final FormatType BASE64 = new FormatType("base64");

  private final String type;

  public FormatType(String type) {
    this.type = type;
  }

  @JsonCreator
  public static FormatType fromString(String value) {
    return new FormatType(value);
  }

  @JsonValue
  public String value() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FormatType that = (FormatType) o;

    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return type;
  }

  public static FormatType[] values() {
    return new FormatType[] {JSON, HTML, TEXT, XML, YAML, CSV, BASE64};
  }
}
