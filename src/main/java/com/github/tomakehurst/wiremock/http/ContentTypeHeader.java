/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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

public class ContentTypeHeader extends HttpHeader {

  public static final String KEY = "Content-Type";

  private String[] parts;

  public ContentTypeHeader(String stringValue) {
    super(KEY, stringValue);
    parts = stringValue != null ? stringValue.split(";") : new String[0];
  }

  private ContentTypeHeader() {
    super(KEY);
  }

  public static ContentTypeHeader absent() {
    return new ContentTypeHeader();
  }

  public ContentTypeHeader or(String stringValue) {
    return isPresent() ? this : new ContentTypeHeader(stringValue);
  }

  public String mimeTypePart() {
    return parts != null && parts.length > 0 ? parts[0] : null;
  }

  public Optional<String> encodingPart() {
    for (int i = 1; i < parts.length; i++) {
      if (parts[i].matches("\\s*charset\\s*=.*")) {
        return Optional.of(parts[i].split("=")[1].replace("\"", ""));
      }
    }

    return Optional.empty();
  }

  public Charset charset() {
    if (isPresent() && encodingPart().isPresent()) {
      return Charset.forName(encodingPart().get());
    }

    return UTF_8;
  }
}
