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
import java.util.Arrays;

public class CompressionType {

  public static final CompressionType NONE = new CompressionType("none");
  public static final CompressionType BROTLI = new CompressionType("brotli");
  public static final CompressionType GZIP = new CompressionType("gzip");
  public static final CompressionType DEFLATE = new CompressionType("deflate");

  private final String type;

  public CompressionType(String type) {
    this.type = type;
  }

  @JsonCreator
  public static CompressionType fromString(String value) {
    return Arrays.stream(values())
        .filter(t -> t.type.equalsIgnoreCase(value))
        .findFirst()
        .orElseGet(() -> new CompressionType(value));
  }

  @JsonValue
  public String value() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompressionType that = (CompressionType) o;

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

  public static CompressionType[] values() {
    return new CompressionType[] {NONE, BROTLI, GZIP, DEFLATE};
  }
}
