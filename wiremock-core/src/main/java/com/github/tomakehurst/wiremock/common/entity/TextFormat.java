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

public class TextFormat {

  public static final TextFormat JSON = new TextFormat("json");
  public static final TextFormat HTML = new TextFormat("html");
  public static final TextFormat TEXT = new TextFormat("text");
  public static final TextFormat XML = new TextFormat("xml");
  public static final TextFormat YAML = new TextFormat("yaml");
  public static final TextFormat CSV = new TextFormat("csv");

  private final String type;

  public TextFormat(String type) {
    this.type = type;
  }

  @JsonCreator
  public static TextFormat fromString(String value) {
    return new TextFormat(value);
  }

  public static TextFormat fromMimeType(String mimeType) {
    if (mimeType == null) {
      return TEXT;
    }

    MimeType parsed = MimeType.parse(mimeType);
    return fromString(parsed.getType());
  }

  @JsonValue
  public String value() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TextFormat that = (TextFormat) o;

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

  public static TextFormat[] values() {
    return new TextFormat[] {JSON, HTML, TEXT, XML, YAML, CSV};
  }
}
