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
package com.github.tomakehurst.wiremock.extension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.github.tomakehurst.wiremock.common.Json;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostServeActionDefinitionListDeserializer
    extends JsonDeserializer<List<PostServeActionDefinition>> {

  @Override
  public List<PostServeActionDefinition> deserialize(
      JsonParser parser, DeserializationContext context) throws IOException {

    JsonToken currentToken = parser.currentToken();

    if (currentToken == JsonToken.START_OBJECT) {
      return deserializeFromMap(parser);
    } else if (currentToken == JsonToken.START_ARRAY) {
      return deserializeFromArray(parser);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private List<PostServeActionDefinition> deserializeFromMap(JsonParser parser) throws IOException {
    Map<String, Object> map = parser.readValueAs(Map.class);

    List<PostServeActionDefinition> result = new ArrayList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      result.add(
          new PostServeActionDefinition(
              entry.getKey(), Parameters.from((Map<String, Object>) entry.getValue())));
    }
    return result;
  }

  private List<PostServeActionDefinition> deserializeFromArray(JsonParser parser)
      throws IOException {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> list = parser.readValueAs(List.class);

    List<PostServeActionDefinition> result = new ArrayList<>();
    for (Map<String, Object> item : list) {
      result.add(Json.mapToObject(item, PostServeActionDefinition.class));
    }
    return result;
  }
}
