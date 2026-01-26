/*
 * Copyright (C) 2014-2025 Thomas Akehurst
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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Objects;

@JsonDeserialize(as = StringEntityDefinition.class)
public class StringEntityDefinition extends EntityDefinition {
  private final String value;

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public StringEntityDefinition(String v) {
    value = v;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    StringEntityDefinition that = (StringEntityDefinition) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public EncodingType getEncoding() {
    return EncodingType.TEXT;
  }

  @Override
  public FormatType getFormat() {
    return FormatType.TEXT;
  }

  @Override
  public CompressionType getCompression() {
    return CompressionType.NONE;
  }

  @Override
  public Object getData() {
    return value;
  }
}
