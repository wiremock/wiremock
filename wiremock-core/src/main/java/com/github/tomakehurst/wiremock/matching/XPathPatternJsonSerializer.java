/*
 * Copyright (C) 2017-2026 Thomas Akehurst
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

import java.util.Map;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.util.NameTransformer;

public class XPathPatternJsonSerializer extends PathPatternJsonSerializer<MatchesXPathPattern> {

  @Override
  public ValueSerializer<MatchesXPathPattern> unwrappingSerializer(NameTransformer unwrapper) {
    return new UnwrappedXPathPatternJsonSerializer();
  }

  @Override
  protected void serializeAdditionalFields(
      MatchesXPathPattern value, JsonGenerator gen, SerializationContext serializers) {
    if (value.getXPathNamespaces() != null && !value.getXPathNamespaces().isEmpty()) {
      gen.writeObjectPropertyStart("xPathNamespaces");
      for (Map.Entry<String, String> namespace : value.getXPathNamespaces().entrySet()) {
        gen.writeStringProperty(namespace.getKey(), namespace.getValue());
      }
      gen.writeEndObject();
    }
  }
}
