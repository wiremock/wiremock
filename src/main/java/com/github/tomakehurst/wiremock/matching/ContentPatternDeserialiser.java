/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.*;

public class ContentPatternDeserialiser extends StdDeserializer<ContentPattern<?>>
    implements ContextualDeserializer {

  private static final Map<String, Class<? extends ContentPattern<?>>> PATTERNS =
      new HashMap<>(
          Map.ofEntries(
              Map.entry("binaryEqualTo", BinaryEqualToPattern.class),
              Map.entry("equalTo", EqualToPattern.class),
              Map.entry("equalToJson", EqualToJsonPattern.class),
              Map.entry("matchesJsonPath", MatchesJsonPathPattern.class),
              Map.entry("matchesJsonSchema", MatchesJsonSchemaPattern.class),
              Map.entry("equalToXml", EqualToXmlPattern.class),
              Map.entry("matchesXPath", MatchesXPathPattern.class),
              Map.entry("contains", ContainsPattern.class),
              Map.entry("not", NotPattern.class),
              Map.entry("doesNotContain", NegativeContainsPattern.class),
              Map.entry("matches", RegexPattern.class),
              Map.entry("doesNotMatch", NegativeRegexPattern.class),
              Map.entry("before", BeforeDateTimePattern.class),
              Map.entry("after", AfterDateTimePattern.class),
              Map.entry("equalToDateTime", EqualToDateTimePattern.class),
              Map.entry("anything", AnythingPattern.class),
              Map.entry("absent", AbsentPattern.class),
              Map.entry("and", LogicalAnd.class),
              Map.entry("or", LogicalOr.class),
              Map.entry("matchesPathTemplate", PathTemplatePattern.class)));

  public ContentPatternDeserialiser() {
    super(ContentPattern.class);
  }

  private ContentPatternDeserialiser(Class<?> vc) {
    super(vc);
  }

  public static void registerPatterns(Map<String, Class<? extends ContentPattern<?>>> patterns) {
    patterns.forEach(PATTERNS::putIfAbsent);
  }

  @Override
  public ContentPattern<?> deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    Class<? extends ContentPattern<?>> patternClass = getPattern(rootNode);
    return context.readTreeAsValue(rootNode, patternClass);
  }

  private Class<? extends ContentPattern<?>> getPattern(JsonNode rootNode)
      throws JsonMappingException {
    Iterable<String> fieldNames = rootNode::fieldNames;
    for (String fieldName : fieldNames) {
      Class<? extends ContentPattern<?>> patternClass = PATTERNS.get(fieldName);
      if (patternClass != null && _valueClass.isAssignableFrom(patternClass)) {
        return patternClass;
      }
    }
    throw new JsonMappingException(rootNode + " is not a valid match operation");
  }

  @Override
  public JsonDeserializer<?> createContextual(
      DeserializationContext context, BeanProperty property) {
    return new ContentPatternDeserialiser(context.getContextualType().getRawClass());
  }
}
