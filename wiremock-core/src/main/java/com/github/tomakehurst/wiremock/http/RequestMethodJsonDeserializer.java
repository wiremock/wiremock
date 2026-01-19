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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsNoneOf;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsOneOf;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ArrayNode;

public class RequestMethodJsonDeserializer extends ValueDeserializer<RequestMethod> {

  @Override
  public RequestMethod deserialize(JsonParser parser, DeserializationContext context)
      throws JacksonException {
    JsonNode rootNode = parser.readValueAsTree();
    RequestMethod requestMethod;
    if (rootNode.has(IsOneOf.NAME)) {
      ArrayNode itemsNode = (ArrayNode) rootNode.get(IsOneOf.NAME);
      Set<RequestMethod> methods = toRequestMethodSet(itemsNode);
      requestMethod = new IsOneOf(methods);
    } else if (rootNode.has(IsNoneOf.NAME)) {
      ArrayNode itemsNode = (ArrayNode) rootNode.get(IsNoneOf.NAME);
      Set<RequestMethod> methods = toRequestMethodSet(itemsNode);
      requestMethod = new IsNoneOf(methods);
    } else {
      requestMethod = RequestMethod.fromString(rootNode.asString());
    }

    return requestMethod;
  }

  private static Set<RequestMethod> toRequestMethodSet(ArrayNode itemsNode) {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(itemsNode.values().iterator(), Spliterator.ORDERED),
            false)
        .map(JsonNode::asString)
        .map(RequestMethod::fromString)
        .collect(Collectors.toSet());
  }
}
