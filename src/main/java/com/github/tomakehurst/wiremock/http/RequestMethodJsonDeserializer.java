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
package com.github.tomakehurst.wiremock.http;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsNoneOf;
import com.github.tomakehurst.wiremock.matching.MultiRequestMethodPattern.IsOneOf;
import java.io.IOException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RequestMethodJsonDeserializer extends JsonDeserializer<RequestMethod> {

  @Override
  public RequestMethod deserialize(JsonParser parser, DeserializationContext context)
      throws IOException, JacksonException {
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
      requestMethod = RequestMethod.fromString(rootNode.asText());
    }

    return requestMethod;
  }

  private static Set<RequestMethod> toRequestMethodSet(ArrayNode itemsNode) {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(itemsNode.elements(), Spliterator.ORDERED), false)
        .map(JsonNode::asText)
        .map(RequestMethod::fromString)
        .collect(Collectors.toSet());
  }
}
