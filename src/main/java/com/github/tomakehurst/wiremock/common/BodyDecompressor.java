/*
 * Copyright (C) 2023 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common;

import static com.google.common.net.HttpHeaders.*;

import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Response;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import java.util.List;

public class BodyDecompressor {

  private static final List<CaseInsensitiveKey> EXCLUDED_HEADERS =
      ImmutableList.of(
          CaseInsensitiveKey.from(CONTENT_ENCODING),
          CaseInsensitiveKey.from(CONTENT_LENGTH),
          CaseInsensitiveKey.from(TRANSFER_ENCODING));
  private final byte[] bytes;
  private final Predicate<HttpHeader> predicate;

  public BodyDecompressor(Response response) {
    this(response.getBody(), response.getHeaders());
  }

  public BodyDecompressor(byte[] body, HttpHeaders headers) {
    if (body != null && body.length > 0 && headers != null) {
      HttpHeader contentEncoding = headers.getHeader(CONTENT_ENCODING);
      for (Compression algorithm : Compression.values()) {
        if (contentEncoding.containsValue(algorithm.contentEncodingValue)) {
          // Once we decompress the body, we need to remove these headers as they are no longer
          // correct.
          bytes = algorithm.decompress(body);
          predicate = (header) -> !EXCLUDED_HEADERS.contains(header.caseInsensitiveKey());
          return;
        }
      }
    }
    predicate = (header) -> true;
    bytes = body;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public boolean shouldRetain(HttpHeader header) {
    return predicate.test(header);
  }
}
