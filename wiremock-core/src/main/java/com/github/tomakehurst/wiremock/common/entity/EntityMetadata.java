/*
 * Copyright (C) 2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ContentTypes.determineIsTextFromMimeType;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import java.nio.charset.Charset;
import java.util.Optional;

public class EntityMetadata {

  public static void copyFromHeaders(HttpHeaders headers, EntityMetadataBuilder<?> builder) {
    final ContentTypeHeader contentTypeHeader = headers.getContentTypeHeader();
    final String mimeType = contentTypeHeader.mimeTypePart();
    final Optional<Charset> charset = contentTypeHeader.charset();

    final HttpHeader contentEncoding = headers.getHeader("Content-Encoding");

    if (mimeType != null && determineIsTextFromMimeType(mimeType)) {
      builder.setFormat(Format.fromMimeType(mimeType));
    }

    charset.ifPresent(builder::setCharset);

    if (contentEncoding.isPresent()) {
      builder.setCompression(CompressionType.fromString(contentEncoding.firstValue()));
    }
  }
}
