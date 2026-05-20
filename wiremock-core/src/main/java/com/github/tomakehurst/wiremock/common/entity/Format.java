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

public class Format {

  public static final Format JSON = new Format("json");
  public static final Format HTML = new Format("html");
  public static final Format TEXT = new Format("text");
  public static final Format XML = new Format("xml");
  public static final Format YAML = new Format("yaml");
  public static final Format CSV = new Format("csv");
  public static final Format BINARY = new Format("binary");

  private final String type;

  public Format(String type) {
    this.type = type;
  }

  @JsonCreator
  public static Format fromString(String value) {
    if (value == null) {
      return null;
    }
    return switch (value.toLowerCase()) {
      case "json" -> JSON;
      case "html" -> HTML;
      case "text" -> TEXT;
      case "xml" -> XML;
      case "yaml" -> YAML;
      case "csv" -> CSV;
      case "binary" -> BINARY;
      default -> new Format(value);
    };
  }

  public static Format fromMimeType(String mimeType) {
    if (mimeType == null) {
      return TEXT;
    }

    MimeType parsed = MimeType.parse(mimeType);
    return fromString(parsed.getType());
  }

  public static Format detectFormat(String data) {
    if (data == null || data.isEmpty()) {
      return Format.TEXT;
    }

    String trimmed = data.trim();
    if (trimmed.isEmpty()) {
      return Format.TEXT;
    }

    char firstChar = trimmed.charAt(0);
    if (firstChar == '{' || firstChar == '[') {
      return JSON;
    }

    if (firstChar == '<') {
      if (trimmed.regionMatches(true, 0, "<!doctype html", 0, 14)
          || trimmed.regionMatches(true, 0, "<html", 0, 5)) {
        return HTML;
      }
      return XML;
    }

    if (trimmed.contains(":")
        && (trimmed.contains("\n-") || trimmed.matches("(?s).*\\n\\s+-\\s+.*"))) {
      return YAML;
    }

    return Format.TEXT;
  }

  @JsonValue
  public String value() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Format that = (Format) o;

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

  public static Format[] values() {
    return new Format[] {JSON, HTML, TEXT, XML, YAML, CSV, BINARY};
  }
}
