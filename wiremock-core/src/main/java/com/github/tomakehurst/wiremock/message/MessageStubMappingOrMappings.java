/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.List;

@JsonDeserialize(using = MessageStubMappingOrMappingsJsonDeserializer.class)
public interface MessageStubMappingOrMappings {

  @JsonIgnore
  List<MessageStubMapping> getMappingOrMappings();

  @JsonIgnore
  boolean isMulti();
}

class MessageStubMappingOrMappingsJsonDeserializer
    extends StdDeserializer<MessageStubMappingOrMappings> {

  protected MessageStubMappingOrMappingsJsonDeserializer() {
    super(MessageStubMappingOrMappings.class);
  }

  @Override
  public MessageStubMappingOrMappings deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    Class<? extends MessageStubMappingOrMappings> clazz;
    if (rootNode.has("messageMappings")) {
      clazz = MessageStubMappingCollection.class;
    } else {
      clazz = SingleMessageStubMappingWrapper.class;
    }
    return ctxt.readTreeAsValue(rootNode, clazz);
  }
}
