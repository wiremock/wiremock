/*
 * Copyright (C) 2025 Thomas Akehurst
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
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class RequestMethodJsonSerializer extends JsonSerializer<RequestMethod> {

  @Override
  public void serialize(RequestMethod value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    if (value instanceof MultiRequestMethodPattern) {
      Set<String> methods =
          ((MultiRequestMethodPattern) value)
              .getMethods().stream().map(RequestMethod::value).collect(Collectors.toSet());

      gen.writeStartObject();
      gen.writeArrayFieldStart(value.getName());
      for (String method : methods) {
        gen.writeString(method);
      }
      gen.writeEndArray();
      gen.writeEndObject();
    } else {
      gen.writeString(value.value());
    }
  }
}
