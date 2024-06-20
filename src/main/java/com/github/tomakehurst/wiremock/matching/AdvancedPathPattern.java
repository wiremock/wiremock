/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

@JsonDeserialize(using = AdvancedPathPattern.AdvancedPathPatternDeserializer.class)
public class AdvancedPathPattern {

  public final String expression;

  @JsonUnwrapped public final StringValuePattern submatcher;

  public AdvancedPathPattern(String expression, StringValuePattern submatcher) {
    this.expression = expression;
    this.submatcher = submatcher;
  }

  public static class AdvancedPathPatternDeserializer extends StdDeserializer<AdvancedPathPattern> {

    protected AdvancedPathPatternDeserializer() {
      super(AdvancedPathPattern.class);
    }

    @Override
    public AdvancedPathPattern deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode rootNode = p.readValueAsTree();
      if (rootNode.isTextual()) {
        return new AdvancedPathPattern(rootNode.asText(), null);
      }
      String expression = ((ObjectNode) rootNode).remove("expression").textValue();
      StringValuePattern submatcher = ctxt.readTreeAsValue(rootNode, StringValuePattern.class);
      return new AdvancedPathPattern(expression, submatcher);
    }
  }
}
