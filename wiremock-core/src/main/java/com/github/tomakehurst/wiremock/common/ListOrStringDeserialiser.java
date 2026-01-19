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
package com.github.tomakehurst.wiremock.common;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ArrayNode;

public class ListOrStringDeserialiser<T> extends ValueDeserializer<ListOrSingle<T>> {

  @Override
  @SuppressWarnings("unchecked")
  public ListOrSingle<T> deserialize(JsonParser parser, DeserializationContext ctxt)
      throws JacksonException {
    JsonNode rootNode = parser.readValueAsTree();
    if (rootNode.isArray()) {
      ArrayNode arrayNode = (ArrayNode) rootNode;
      List<T> items = new ArrayList<>();
      for (JsonNode node : arrayNode.values()) {
        Object value = getValue(node);
        items.add((T) value);
      }

      return new ListOrSingle<>(items);
    }

    return new ListOrSingle<>((T) getValue(rootNode));
  }

  private static Object getValue(JsonNode node) {
    return node.isString()
        ? node.stringValue()
        : node.isNumber()
            ? node.numberValue()
            : node.isBoolean() ? node.booleanValue() : node.stringValue();
  }
}
