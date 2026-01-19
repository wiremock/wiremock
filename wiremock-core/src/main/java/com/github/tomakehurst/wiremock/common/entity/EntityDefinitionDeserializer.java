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
package com.github.tomakehurst.wiremock.common.entity;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class EntityDefinitionDeserializer extends StdDeserializer<EntityDefinition> {

  public EntityDefinitionDeserializer() {
    super(EntityDefinition.class);
  }

  @Override
  public EntityDefinition deserialize(JsonParser parser, DeserializationContext ctxt) {
    JsonNode node = parser.readValueAsTree();

    Class<? extends EntityDefinition> targetClass;
    if (node.isString()) {
      targetClass = StringEntityDefinition.class;
    } else if (node.isObject()) {
      JsonNode encodingNode = node.get("encoding");
      if (encodingNode != null
          && encodingNode.isString()
          && EncodingType.BINARY.value().equals(encodingNode.stringValue())) {
        targetClass = BinaryEntityDefinition.class;
      } else {
        // Default to TextEntityDefinition for text encoding or when encoding is not specified
        targetClass = TextEntityDefinition.class;
      }
    } else {
      targetClass = TextEntityDefinition.class;
    }

    return ctxt.readTreeAsValue(node, targetClass);
  }
}
