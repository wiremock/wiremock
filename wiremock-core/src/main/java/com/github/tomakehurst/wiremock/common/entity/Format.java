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
import com.github.tomakehurst.wiremock.common.ContentTypes;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;

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

  public static Format fromMimeType(MimeType mimeType) {
    if (mimeType == null) {
      return null;
    }

    // Primary type — covers "application/json" → "json", "text/xml" → "xml", etc.
    if (isWellKnownType(mimeType.getType())) {
      return fromString(mimeType.getType());
    }

    // Subtype after '+' — covers "application/vnd.api+json" → subType "json"
    String subType = mimeType.getSubType();
    if (subType != null && isWellKnownType(subType)) {
      return fromString(subType);
    }

    return ContentTypes.determineIsTextFromMimeType(mimeType.toString()) ? TEXT : BINARY;
  }

  public static Format fromContentTypeHeader(ContentTypeHeader contentTypeHeader) {
    if (contentTypeHeader == null || !contentTypeHeader.isPresent()) {
      return BINARY;
    }
    MimeType mimeType = contentTypeHeader.getMimeType();
    return mimeType != null ? fromMimeType(mimeType) : null;
  }

  private static boolean isWellKnownType(String value) {
    if (value == null) return false;
    return switch (value.toLowerCase()) {
      case "json", "html", "text", "xml", "yaml", "csv", "binary" -> true;
      default -> false;
    };
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
