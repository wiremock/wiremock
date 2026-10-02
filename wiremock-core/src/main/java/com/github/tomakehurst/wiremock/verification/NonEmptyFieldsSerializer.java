/*
 * Copyright (C) 2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Serializes any object using the current mapper, then strips fields that carry no meaningful
 * information: empty containers ({@code {}} or {@code []}), {@code false} booleans, and integers
 * equal to {@code -1} (the conventional sentinel for "not set"). Use via
 * {@code @JsonSerialize(using = NonEmptyFieldsSerializer.class)} on a getter or field — it applies
 * only at that specific point of use, leaving the target class itself unmodified.
 */
public class NonEmptyFieldsSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
      throws IOException {
    ObjectMapper mapper = (ObjectMapper) gen.getCodec();
    ObjectNode node = mapper.valueToTree(value);
    stripUnmeaningfulFields(node);
    gen.writeTree(node);
  }

  private static void stripUnmeaningfulFields(ObjectNode node) {
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      JsonNode v = fields.next().getValue();
      if (isUnmeaningful(v)) {
        fields.remove();
      }
    }
  }

  private static boolean isUnmeaningful(JsonNode v) {
    return (v.isContainerNode() && v.isEmpty())
        || (v.isBoolean() && !v.booleanValue())
        || (v.isIntegralNumber() && v.intValue() == -1);
  }
}
