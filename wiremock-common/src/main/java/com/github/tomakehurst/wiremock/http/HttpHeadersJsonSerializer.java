/*
 * Copyright (C) 2012-2025 Thomas Akehurst
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class HttpHeadersJsonSerializer extends JsonSerializer<HttpHeaders> {

  @Override
  public void serialize(HttpHeaders headers, JsonGenerator jgen, SerializerProvider provider)
      throws IOException {
    jgen.writeStartObject();
    for (HttpHeader header : headers.all()) {
      if (header.isSingleValued()) {
        jgen.writeStringField(header.key(), header.firstValue());
      } else {
        jgen.writeArrayFieldStart(header.key());
        for (String value : header.values()) {
          jgen.writeString(value);
        }
        jgen.writeEndArray();
      }
    }
    jgen.writeEndObject();
  }
}
