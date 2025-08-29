/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.Optional;

/** The type Content type header. */
public class ContentTypeHeader extends HttpHeader {

  /** The constant KEY. */
  public static final String KEY = "Content-Type";

  private String[] parts;

  /**
   * Instantiates a new Content type header.
   *
   * @param stringValue the string value
   */
  public ContentTypeHeader(String stringValue) {
    super(KEY, stringValue);
    parts = stringValue != null ? stringValue.split(";") : new String[0];
  }

  private ContentTypeHeader() {
    super(KEY);
  }

  /**
   * Absent content type header.
   *
   * @return the content type header
   */
  public static ContentTypeHeader absent() {
    return new ContentTypeHeader();
  }

  /**
   * Or content type header.
   *
   * @param stringValue the string value
   * @return the content type header
   */
  public ContentTypeHeader or(String stringValue) {
    return isPresent() ? this : new ContentTypeHeader(stringValue);
  }

  /**
   * Mime type part string.
   *
   * @return the string
   */
  public String mimeTypePart() {
    return parts != null && parts.length > 0 ? parts[0] : null;
  }

  /**
   * Encoding part optional.
   *
   * @return the optional
   */
  public Optional<String> encodingPart() {
    for (int i = 1; i < parts.length; i++) {
      if (parts[i].matches("\\s*charset\\s*=.*")) {
        return Optional.of(parts[i].split("=")[1].replace("\"", ""));
      }
    }

    return Optional.empty();
  }

  /**
   * Charset charset.
   *
   * @return the charset
   */
  public Charset charset() {
    if (isPresent() && encodingPart().isPresent()) {
      return Charset.forName(encodingPart().get());
    }

    return UTF_8;
  }
}
