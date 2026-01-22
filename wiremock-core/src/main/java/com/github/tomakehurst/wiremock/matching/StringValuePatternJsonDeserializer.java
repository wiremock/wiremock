/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.client.WireMock.JsonSchemaVersion;
import com.github.tomakehurst.wiremock.common.DateTimeUnit;
import com.github.tomakehurst.wiremock.common.Json;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.xmlunit.diff.ComparisonType;

public class StringValuePatternJsonDeserializer extends JsonDeserializer<StringValuePattern> {

  private static final Map<String, Class<? extends StringValuePattern>> PATTERNS =
      Map.ofEntries(
          Map.entry("equalTo", EqualToPattern.class),
          Map.entry("equalToNumber", EqualToNumberPattern.class),
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
          Map.entry("matchesPathTemplate", PathTemplatePattern.class),
          Map.entry("greaterThanNumber", GreaterThanNumberPattern.class),
          Map.entry("greaterThanEqualNumber", GreaterThanEqualNumberPattern.class),
          Map.entry("lessThanNumber", LessThanNumberPattern.class),
          Map.entry("lessThanEqualNumber", LessThanEqualNumberPattern.class));

  private static Map.Entry<String, JsonNode> findMainFieldEntry(JsonNode rootNode) {
    List<Map.Entry<String, JsonNode>> list = getListFromNode(rootNode);
    return list.stream()
        .filter(input -> PATTERNS.containsKey(input.getKey()))
        .findFirst()
        .orElseThrow(NoSuchElementException::new);
  }

  private static EqualToXmlPattern.NamespaceAwareness deserializeNamespaceAwareness(
      JsonNode rootNode) {
    String namespaceAwarenessString =
        fromNullableTextNode(rootNode.findValue("namespaceAwareness"));
    return namespaceAwarenessString == null
        ? null
        : EqualToXmlPattern.NamespaceAwareness.valueOf(namespaceAwarenessString);
  }

  private static Map<String, String> toNamespaceMap(JsonNode namespacesNode) {
    Map<String, String> map = new LinkedHashMap<>();
    for (Iterator<Map.Entry<String, JsonNode>> fields = namespacesNode.fields();
        fields.hasNext(); ) {
      Map.Entry<String, JsonNode> field = fields.next();
      map.put(field.getKey(), field.getValue().textValue());
    }

    return map;
  }

  private static Boolean fromNullable(JsonNode node) {
    return node == null ? null : node.asBoolean();
  }

  private static String fromNullableTextNode(JsonNode node) {
    return node == null ? null : node.asText();
  }

  private static Set<ComparisonType> comparisonTypeSetFromArray(JsonNode node) {
    if (node == null || !node.isArray()) {
      return null;
    }

    Set<ComparisonType> comparisonTypes = new LinkedHashSet<>();
    for (JsonNode itemNode : node) {
      comparisonTypes.add(ComparisonType.valueOf(itemNode.textValue()));
    }

    return comparisonTypes;
  }

  private static Constructor<? extends StringValuePattern> findConstructor(
      Class<? extends StringValuePattern> clazz) {
    return findConstructor(clazz, String.class);
  }

  @SuppressWarnings("unchecked")
  private static Constructor<? extends StringValuePattern> findConstructor(
      Class<? extends StringValuePattern> clazz, Class<?> parameterType) {
    Optional<Constructor<?>> optionalConstructor =
        Arrays.stream(clazz.getDeclaredConstructors())
            .filter(
                input ->
                    input.getParameterTypes().length == 1
                        && input.getGenericParameterTypes()[0].equals(parameterType))
            .findFirst();

    if (optionalConstructor.isEmpty()) {
      throw new IllegalStateException(
          "Constructor for "
              + clazz.getSimpleName()
              + " must have a single "
              + parameterType.getSimpleName().toLowerCase()
              + " argument constructor");
    }

    return (Constructor<? extends StringValuePattern>) optionalConstructor.get();
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent") // exceptions are handled in main try-catch
  private static String getSingleArgumentConstructorJsonPropertyName(
      Constructor<? extends StringValuePattern> constructor) {
    try {
      Parameter parameter = Arrays.stream(constructor.getParameters()).findFirst().get();
      return Arrays.stream(parameter.getAnnotationsByType(JsonProperty.class))
          .findFirst()
          .get()
          .value();
    } catch (Exception e) {
      throw new IllegalStateException(
          "Constructor for "
              + constructor.getDeclaringClass().getSimpleName()
              + " must have a single argument constructor with @JsonProperty annotation");
    }
  }

  private static Class<? extends StringValuePattern> findPatternClass(JsonNode rootNode)
      throws JsonMappingException {
    for (Map.Entry<String, JsonNode> node : getListFromNode(rootNode)) {
      Class<? extends StringValuePattern> patternClass = PATTERNS.get(node.getKey());
      if (patternClass != null) {
        return patternClass;
      }
    }

    throw new JsonMappingException(rootNode + " is not a valid match operation");
  }

  private static List<Map.Entry<String, JsonNode>> getListFromNode(JsonNode rootNode) {
    List<Map.Entry<String, JsonNode>> list = new LinkedList<>();
    rootNode.fields().forEachRemaining(list::add);
    return list;
  }

  @Override
  public StringValuePattern deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonNode rootNode = parser.readValueAsTree();
    return buildStringValuePattern(rootNode);
  }

  public StringValuePattern buildStringValuePattern(JsonNode rootNode) throws JsonMappingException {
    Class<? extends StringValuePattern> patternClass = findPatternClass(rootNode);
    if (patternClass.equals(AbsentPattern.class)) {
      return AbsentPattern.ABSENT;
    } else if (patternClass.equals(EqualToJsonPattern.class)) {
      return deserializeEqualToJson(rootNode);
    } else if (patternClass.equals(MatchesJsonSchemaPattern.class)) {
      return deserializeMatchesJsonSchema(rootNode);
    } else if (patternClass.equals(EqualToXmlPattern.class)) {
      return deserializeEqualToXml(rootNode);
    } else if (patternClass.equals(MatchesJsonPathPattern.class)) {
      return deserialiseMatchesJsonPathPattern(rootNode);
    } else if (patternClass.equals(MatchesXPathPattern.class)) {
      return deserialiseMatchesXPathPattern(rootNode);
    } else if (patternClass.equals(EqualToPattern.class)) {
      return deserializeEqualTo(rootNode);
    } else if (AbstractDateTimePattern.class.isAssignableFrom(patternClass)) {
      final Map.Entry<String, JsonNode> mainFieldEntry = findMainFieldEntry(rootNode);
      String matcherName = mainFieldEntry.getKey();
      return deserialiseDateTimePattern(rootNode, matcherName);
    } else if (AbstractNumberPattern.class.isAssignableFrom(patternClass)) {
      return deserializeNumberPattern(rootNode, patternClass);
    } else if (patternClass.equals(LogicalAnd.class)) {
      return deserializeAnd(rootNode);
    } else if (patternClass.equals(LogicalOr.class)) {
      return deserializeOr(rootNode);
    } else if (patternClass.equals(NotPattern.class)) {
      return deserializeNot(rootNode);
    }

    final Map.Entry<String, JsonNode> mainFieldEntry = findMainFieldEntry(rootNode);
    if (!mainFieldEntry.getValue().isTextual()) {
      throw new JsonMappingException(
          mainFieldEntry.getKey() + " operand must be a non-null string");
    }
    String operand = mainFieldEntry.getValue().textValue();
    try {
      Constructor<? extends StringValuePattern> constructor = findConstructor(patternClass);
      return constructor.newInstance(operand);
    } catch (Exception e) {
      return throwUnchecked(e, StringValuePattern.class);
    }
  }

  private EqualToPattern deserializeEqualTo(JsonNode rootNode) throws JsonMappingException {
    if (!rootNode.has("equalTo")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode equalToNode = rootNode.findValue("equalTo");
    if (!equalToNode.isTextual()) {
      throw new JsonMappingException("equalTo operand must be a non-null string");
    }

    String operand = equalToNode.textValue();
    Boolean ignoreCase = fromNullable(rootNode.findValue("caseInsensitive"));
    Boolean templated = fromNullable(rootNode.findValue("templated"));

    return new EqualToPattern(operand, ignoreCase, templated);
  }

  private EqualToJsonPattern deserializeEqualToJson(JsonNode rootNode) throws JsonMappingException {
    if (!rootNode.has("equalToJson")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode operand = rootNode.findValue("equalToJson");

    Boolean ignoreArrayOrder = fromNullable(rootNode.findValue("ignoreArrayOrder"));
    Boolean ignoreExtraElements = fromNullable(rootNode.findValue("ignoreExtraElements"));

    // Allow either a JSON value or a string containing JSON
    if (operand.isTextual()) {
      return new EqualToJsonPattern(operand.textValue(), ignoreArrayOrder, ignoreExtraElements);
    } else {
      return new EqualToJsonPattern(operand, ignoreArrayOrder, ignoreExtraElements);
    }
  }

  private MatchesJsonSchemaPattern deserializeMatchesJsonSchema(JsonNode rootNode)
      throws JsonMappingException {
    if (!rootNode.has("matchesJsonSchema")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode operand = rootNode.findValue("matchesJsonSchema");

    JsonSchemaVersion schemaVersion;
    try {
      String schemaVersionString = fromNullableTextNode(rootNode.findValue("schemaVersion"));
      schemaVersion =
          schemaVersionString != null
              ? JsonSchemaVersion.valueOf(schemaVersionString)
              : JsonSchemaVersion.DEFAULT;
    } catch (Exception e) {
      throw new JsonMappingException(
          "schemaVersion must be one of " + Json.write(JsonSchemaVersion.values()));
    }

    // Allow either a JSON value or a string containing JSON
    if (operand.isTextual()) {
      return new MatchesJsonSchemaPattern(operand.textValue(), schemaVersion);
    } else {
      return new MatchesJsonSchemaPattern(operand, schemaVersion);
    }
  }

  private EqualToXmlPattern deserializeEqualToXml(JsonNode rootNode) throws JsonMappingException {
    if (!rootNode.has("equalToXml")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode operand = rootNode.findValue("equalToXml");

    Boolean enablePlaceholders = fromNullable(rootNode.findValue("enablePlaceholders"));
    String placeholderOpeningDelimiterRegex =
        fromNullableTextNode(rootNode.findValue("placeholderOpeningDelimiterRegex"));
    String placeholderClosingDelimiterRegex =
        fromNullableTextNode(rootNode.findValue("placeholderClosingDelimiterRegex"));
    Set<ComparisonType> exemptedComparisons =
        comparisonTypeSetFromArray(rootNode.findValue("exemptedComparisons"));
    Boolean ignoreOrderOfSameNode = fromNullable(rootNode.findValue("ignoreOrderOfSameNode"));
    EqualToXmlPattern.NamespaceAwareness namespaceAwareness =
        deserializeNamespaceAwareness(rootNode);
    return new EqualToXmlPattern(
        operand.textValue(),
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        exemptedComparisons,
        ignoreOrderOfSameNode,
        namespaceAwareness);
  }

  private MatchesJsonPathPattern deserialiseMatchesJsonPathPattern(JsonNode rootNode)
      throws JsonMappingException {
    if (!rootNode.has("matchesJsonPath")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode outerPatternNode = rootNode.findValue("matchesJsonPath");
    if (outerPatternNode.isTextual()) {
      return new MatchesJsonPathPattern(outerPatternNode.textValue());
    }

    if (!outerPatternNode.has("expression")) {
      throw new JsonMappingException("expression is required in the advanced matchesJsonPath form");
    }

    String expression = outerPatternNode.findValue("expression").textValue();
    StringValuePattern valuePattern = buildStringValuePattern(outerPatternNode);

    return new MatchesJsonPathPattern(expression, valuePattern);
  }

  private MatchesXPathPattern deserialiseMatchesXPathPattern(JsonNode rootNode)
      throws JsonMappingException {
    if (!rootNode.has("matchesXPath")) {
      throw new JsonMappingException(rootNode + " is not a valid match operation");
    }

    JsonNode namespacesNode = rootNode.findValue("xPathNamespaces");

    Map<String, String> namespaces =
        namespacesNode != null ? toNamespaceMap(namespacesNode) : Collections.emptyMap();

    JsonNode outerPatternNode = rootNode.findValue("matchesXPath");
    if (outerPatternNode.isTextual()) {
      return new MatchesXPathPattern(outerPatternNode.textValue(), namespaces);
    }

    if (!outerPatternNode.has("expression")) {
      throw new JsonMappingException("expression is required in the advanced matchesXPath form");
    }

    String expression = outerPatternNode.findValue("expression").textValue();
    StringValuePattern valuePattern = buildStringValuePattern(outerPatternNode);

    return new MatchesXPathPattern(expression, namespaces, valuePattern);
  }

  private StringValuePattern deserialiseDateTimePattern(JsonNode rootNode, String matcherName)
      throws JsonMappingException {
    JsonNode dateTimeNode = rootNode.findValue(matcherName);
    JsonNode formatNode = rootNode.findValue("actualFormat");
    JsonNode truncateExpectedNode = rootNode.findValue("truncateExpected");
    JsonNode truncateActualNode = rootNode.findValue("truncateActual");
    JsonNode applyTruncationLastNode = rootNode.findValue("applyTruncationLast");
    JsonNode expectedOffsetAmountNode = rootNode.findValue("expectedOffset");
    JsonNode expectedOffsetUnitNode = rootNode.findValue("expectedOffsetUnit");

    switch (matcherName) {
      case "before":
        return new BeforeDateTimePattern(
            dateTimeNode.textValue(),
            formatNode != null ? formatNode.textValue() : null,
            truncateExpectedNode != null ? truncateExpectedNode.textValue() : null,
            truncateActualNode != null ? truncateActualNode.textValue() : null,
            applyTruncationLastNode != null && applyTruncationLastNode.booleanValue(),
            expectedOffsetAmountNode != null ? expectedOffsetAmountNode.intValue() : null,
            expectedOffsetUnitNode != null
                ? DateTimeUnit.valueOf(expectedOffsetUnitNode.textValue().toUpperCase())
                : null);
      case "after":
        return new AfterDateTimePattern(
            dateTimeNode.textValue(),
            formatNode != null ? formatNode.textValue() : null,
            truncateExpectedNode != null ? truncateExpectedNode.textValue() : null,
            truncateActualNode != null ? truncateActualNode.textValue() : null,
            applyTruncationLastNode != null && applyTruncationLastNode.booleanValue(),
            expectedOffsetAmountNode != null ? expectedOffsetAmountNode.intValue() : null,
            expectedOffsetUnitNode != null
                ? DateTimeUnit.valueOf(expectedOffsetUnitNode.textValue().toUpperCase())
                : null);
      case "equalToDateTime":
        return new EqualToDateTimePattern(
            dateTimeNode.textValue(),
            formatNode != null ? formatNode.textValue() : null,
            truncateExpectedNode != null ? truncateExpectedNode.textValue() : null,
            truncateActualNode != null ? truncateActualNode.textValue() : null,
            applyTruncationLastNode != null && applyTruncationLastNode.booleanValue(),
            expectedOffsetAmountNode != null ? expectedOffsetAmountNode.intValue() : null,
            expectedOffsetUnitNode != null
                ? DateTimeUnit.valueOf(expectedOffsetUnitNode.textValue().toUpperCase())
                : null);
      default:
        throw new JsonMappingException(rootNode + " is not a valid match operation");
    }
  }

  private static StringValuePattern deserializeNumberPattern(
      JsonNode rootNode, Class<? extends StringValuePattern> patternClass)
      throws JsonMappingException {
    Constructor<? extends StringValuePattern> constructor =
        findConstructor(patternClass, Number.class);
    String propertyName = getSingleArgumentConstructorJsonPropertyName(constructor);
    if (!rootNode.hasNonNull(propertyName)) {
      throw new JsonMappingException(propertyName + " has to be a numeric value");
    }
    try {
      Number propertyValue = Double.parseDouble(rootNode.get(propertyName).asText());
      return constructor.newInstance(propertyValue);
    } catch (NumberFormatException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new JsonMappingException(propertyName + " has to be a numeric value");
    }
  }

  private LogicalAnd deserializeAnd(JsonNode node) throws JsonMappingException {
    JsonNode operandsNode = node.get("and");
    if (!operandsNode.isArray()) {
      throw new JsonMappingException("and field must be an array of matchers");
    }

    try (JsonParser parser = Json.getObjectMapper().treeAsTokens(node.get("and"))) {
      List<StringValuePattern> operands =
          parser.readValueAs(new TypeReference<List<StringValuePattern>>() {});
      return new LogicalAnd(operands);
    } catch (IOException e) {
      return throwUnchecked(e, LogicalAnd.class);
    }
  }

  private LogicalOr deserializeOr(JsonNode node) throws JsonMappingException {
    JsonNode operandsNode = node.get("or");
    if (!operandsNode.isArray()) {
      throw new JsonMappingException("and field must be an array of matchers");
    }

    try (JsonParser parser = Json.getObjectMapper().treeAsTokens(node.get("or"))) {
      List<StringValuePattern> operands =
          parser.readValueAs(new TypeReference<List<StringValuePattern>>() {});
      return new LogicalOr(operands);
    } catch (IOException e) {
      return throwUnchecked(e, LogicalOr.class);
    }
  }

  private StringValuePattern deserializeNot(JsonNode rootNode) throws JsonMappingException {
    if (!rootNode.has("not")) {
      throw new JsonMappingException(rootNode + " is not a valid not operation");
    }

    JsonNode notNode = rootNode.findValue("not");

    try (JsonParser parser = Json.getObjectMapper().treeAsTokens(notNode)) {
      StringValuePattern unexpectedPattern =
          parser.readValueAs(new TypeReference<StringValuePattern>() {});
      return new NotPattern(unexpectedPattern);
    } catch (IOException e) {
      return throwUnchecked(e, NotPattern.class);
    }
  }
}
