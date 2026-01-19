/*
 * Copyright (C) 2024-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.tomakehurst.wiremock.common.Json;
import java.io.IOException;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class JsonMergeHelper extends HandlebarsHelper<Object> {

  @Override
  public String apply(Object baseJsonString, Options options) throws IOException {
    if (!(baseJsonString instanceof String))
      return handleError("Base JSON parameter must be a string");

    JsonNode baseJson;
    try {
      baseJson = Json.read((String) baseJsonString, JsonNode.class);
    } catch (Exception e) {
      return handleError("Base JSON is not valid JSON ('" + baseJsonString + "')", e);
    }
    if (!(baseJson instanceof ObjectNode)) {
      return handleError("Base JSON is not a JSON object ('" + baseJsonString + "')");
    }

    Object jsonToMergeString;
    if (options.tagType == TagType.SECTION) {
      jsonToMergeString = options.fn().toString();
    } else {
      jsonToMergeString = options.params.length > 0 ? options.params[0] : null;
    }
    if (!(jsonToMergeString instanceof String))
      return handleError("JSON to merge must be a string");

    JsonNode jsonToMerge;
    try {
      jsonToMerge = Json.read((String) jsonToMergeString, JsonNode.class);
    } catch (Exception e) {
      return handleError("JSON to merge is not valid JSON ('" + jsonToMergeString + "')", e);
    }
    if (!(jsonToMerge instanceof ObjectNode)) {
      return handleError("JSON to merge is not a JSON object ('" + jsonToMergeString + "')");
    }

    boolean removeNulls =
        options.hash.containsKey("removeNulls") && (boolean) options.hash.get("removeNulls");

    merge((ObjectNode) baseJson, (ObjectNode) jsonToMerge, removeNulls);
    return Json.getJsonMapper().writeValueAsString(baseJson);
  }

  private void merge(ObjectNode base, ObjectNode other, boolean removeNulls) {
    for (Map.Entry<String, JsonNode> child : other.properties()) {
      String fieldName = child.getKey();
      JsonNode childNodeToMerge = child.getValue();
      if (childNodeToMerge instanceof ObjectNode) {
        JsonNode baseChildNode = base.get(fieldName);
        if (baseChildNode instanceof ObjectNode) {
          merge((ObjectNode) baseChildNode, (ObjectNode) childNodeToMerge, removeNulls);
        } else {
          base.replace(fieldName, childNodeToMerge);
        }
      } else {
        if (removeNulls && childNodeToMerge.isNull()) {
          base.remove(fieldName);
        } else {
          base.replace(fieldName, childNodeToMerge);
        }
      }
    }
  }
}
