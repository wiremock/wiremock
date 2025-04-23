/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class MultiValuePatternDeserializer extends JsonDeserializer<MultiValuePattern> {

  @Override
  public MultiValuePattern deserialize(JsonParser parser, DeserializationContext ctxt)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    final ObjectMapper mapper = (ObjectMapper) parser.getCodec();
    if (rootNode.has(ExactMatchMultiValuePattern.JSON_KEY)) {
      return mapper.treeToValue(rootNode, ExactMatchMultiValuePattern.class);
    } else if (rootNode.has(IncludesMatchMultiValuePattern.JSON_KEY)) {
      return mapper.treeToValue(rootNode, IncludesMatchMultiValuePattern.class);
    } else {
      return mapper.treeToValue(rootNode, SingleMatchMultiValuePattern.class);
    }
  }
}
