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
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

@JsonDeserialize(using = StubMappingOrMappingsJsonDeserializer.class)
public interface StubMappingOrMappings {

  @JsonIgnore
  List<StubMapping> getMappingOrMappings();

  @JsonIgnore
  boolean isMulti();
}

class StubMappingOrMappingsJsonDeserializer extends StdDeserializer<StubMappingOrMappings> {

  protected StubMappingOrMappingsJsonDeserializer() {
    super(StubMappingOrMappings.class);
  }

  @Override
  public StubMappingOrMappings deserialize(JsonParser parser, DeserializationContext ctxt)
      throws JacksonException {
    JsonNode rootNode = parser.readValueAsTree();
    Class<? extends StubMappingOrMappings> clazz;
    if (rootNode.has("mappings")) {
      clazz = StubMappingCollection.class;
    } else {
      clazz = StubMapping.class;
    }
    return ctxt.readTreeAsValue(rootNode, clazz);
  }
}
