/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Strings;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import me.jvt.http.mediatype.MediaType;

public class ContentTypeHeader extends HttpHeader {

  public static final String KEY = "Content-Type";
  private static final String CHARSET_QUOTES = "\"([^\"]*)\"";

  private final Optional<MediaType> maybeMediaType;

  public ContentTypeHeader(String stringValue) {
    super(KEY, stringValue);
    maybeMediaType = Optional.of(MediaType.valueOf(stringValue));
  }

  private ContentTypeHeader() {
    super(KEY);
    maybeMediaType = Optional.empty();
  }

  public static ContentTypeHeader absent() {
    return new ContentTypeHeader();
  }

  public ContentTypeHeader or(String stringValue) {
    return isPresent() ? this : new ContentTypeHeader(stringValue);
  }

  public String mimeTypePart() {
    if (maybeMediaType.isEmpty()) {
      return null;
    }

    MediaType mediaType = maybeMediaType.get();
    return mediaType.getType() + "/" + mediaType.getSubtype();
  }

  public Optional<String> encodingPart() {
    if (maybeMediaType.isEmpty()) {
      return Optional.empty();
    }

    Map<String, String> parameters = maybeMediaType.get().getParameters();
    if (!parameters.containsKey("charset")) {
      return Optional.empty();
    }

    String charset = parameters.get("charset").replaceAll(CHARSET_QUOTES, "$1");

    return Optional.of(charset);
  }

  public Charset charset() {
    if (isPresent() && encodingPart().isPresent()) {
      return Charset.forName(encodingPart().get());
    }

    return Strings.DEFAULT_CHARSET;
  }
}
