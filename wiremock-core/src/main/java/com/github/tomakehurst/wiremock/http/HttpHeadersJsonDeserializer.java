/*
 * Copyright (C) 2012-2026 Thomas Akehurst
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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class HttpHeadersJsonDeserializer extends ValueDeserializer<HttpHeaders> {

  @Override
  public HttpHeaders deserialize(JsonParser parser, DeserializationContext context) {
    JsonNode rootNode = parser.readValueAsTree();
    List<HttpHeader> headers =
        rootNode.properties().stream()
            .map(entry -> createHttpHeader(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    return new HttpHeaders(headers);
  }

  private static HttpHeader createHttpHeader(String key, JsonNode fieldValue) {
    if (fieldValue.isArray()) {
      List<String> headerValues =
          fieldValue.values().stream().map(JsonNode::stringValue).collect(Collectors.toList());
      return new HttpHeader(key, headerValues);
    } else {
      return new HttpHeader(key, fieldValue.stringValue());
    }
  }

  public static <T> Iterable<T> all(final Iterator<T> underlyingIterator) {
    return () -> underlyingIterator;
  }
}
