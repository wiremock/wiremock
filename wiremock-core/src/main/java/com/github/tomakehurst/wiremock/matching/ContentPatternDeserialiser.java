/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class ContentPatternDeserialiser extends ValueDeserializer<ContentPattern<?>> {

  @Override
  public ContentPattern<?> deserialize(JsonParser parser, DeserializationContext context)
      throws JacksonException {
    JsonNode rootNode = parser.readValueAsTree();

    if (rootNode.has("binaryEqualTo")) {
      return deserializeBinaryEqualTo(rootNode);
    }

    return new StringValuePatternJsonDeserializer().buildStringValuePattern(rootNode);
  }

  private BinaryEqualToPattern deserializeBinaryEqualTo(JsonNode rootNode) {
    String operand = rootNode.findValue("binaryEqualTo").textValue();

    return new BinaryEqualToPattern(operand);
  }
}
