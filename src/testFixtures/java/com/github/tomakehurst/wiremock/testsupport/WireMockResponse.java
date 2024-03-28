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
package com.github.tomakehurst.wiremock.testsupport;

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirst;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;

public class WireMockResponse {

  private final ClassicHttpResponse httpResponse;
  private final byte[] content;

  public WireMockResponse(CloseableHttpResponse httpResponse) {
    this.httpResponse = httpResponse;
    content = getEntityAsByteArrayAndCloseStream(httpResponse);
  }

  public int statusCode() {
    return httpResponse.getCode();
  }

  public String content() {
    if (content == null) {
      return null;
    }
    return new String(content, UTF_8);
  }

  public byte[] binaryContent() {
    return content;
  }

  public String firstHeader(String key) {
    return getFirst(headers().get(key), null);
  }

  public Multimap<String, String> headers() {
    ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();

    for (Header header : httpResponse.getHeaders()) {
      builder.put(header.getName(), header.getValue());
    }

    return builder.build();
  }

  public String statusMessage() {
    return httpResponse.getReasonPhrase();
  }
}
