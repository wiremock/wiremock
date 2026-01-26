/*
 * Copyright (C) 2023-2026 Thomas Akehurst
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

public class EncodingType {

  public static final EncodingType TEXT = new EncodingType("text");
  public static final EncodingType BINARY = new EncodingType("binary");
  public static final EncodingType MULTIPART = new EncodingType("multipart");

  private final String type;

  public EncodingType(String type) {
    this.type = type;
  }

  @JsonCreator
  public static EncodingType fromString(String value) {
    return new EncodingType(value);
  }

  @JsonValue
  public String value() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EncodingType that = (EncodingType) o;

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

  public static EncodingType[] values() {
    return new EncodingType[] {TEXT, BINARY, MULTIPART};
  }
}
