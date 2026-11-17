/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.message;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageHeadersJsonDeserializer extends JsonDeserializer<MessageHeaders> {

  @Override
  public MessageHeaders deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    Iterable<Map.Entry<String, JsonNode>> all = rootNode::fields;
    List<MessageHeader> headers =
        StreamSupport.stream(all.spliterator(), false)
            .map(entry -> createMessageHeader(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    return new MessageHeaders(headers);
  }

  private static MessageHeader createMessageHeader(String key, JsonNode fieldValue) {
    if (fieldValue.isArray()) {
      Iterable<JsonNode> all = fieldValue::elements;
      List<String> values =
          StreamSupport.stream(all.spliterator(), false)
              .map(JsonNode::textValue)
              .collect(Collectors.toList());
      return new MessageHeader(key, values);
    } else {
      return new MessageHeader(key, fieldValue.textValue());
    }
  }
}
