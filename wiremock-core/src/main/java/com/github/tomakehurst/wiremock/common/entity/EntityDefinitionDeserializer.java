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
package com.github.tomakehurst.wiremock.common.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class EntityDefinitionDeserializer extends StdDeserializer<EntityDefinition> {

  public EntityDefinitionDeserializer() {
    super(EntityDefinition.class);
  }

  @Override
  public EntityDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    JsonNode node = parser.readValueAsTree();

    Class<? extends EntityDefinition> targetClass;
    if (node.isTextual()) {
      targetClass = StringEntityDefinition.class;
    } else {
      targetClass = FullEntityDefinition.class;
    }

    return ctxt.readTreeAsValue(node, targetClass);
  }
}
