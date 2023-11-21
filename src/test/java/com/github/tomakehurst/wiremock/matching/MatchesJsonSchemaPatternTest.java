/*
 * Copyright (C) 2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.JsonSchemaVersion.V4;
import static com.github.tomakehurst.wiremock.client.WireMock.JsonSchemaVersion.V6;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonSchema;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.jayway.jsonpath.JsonPath;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MatchesJsonSchemaPatternTest {

  @Test
  void distanceIsProportionateToNumberOfValidationErrors() {
    String schema = file("schema-validation/shop-order.schema.json");

    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema);

    MatchResult veryBadMatchResult = pattern.match("{}");
    assertThat(veryBadMatchResult.isExactMatch(), is(false));
    assertThat(veryBadMatchResult.getDistance(), closeTo(0.66, 0.01));

    MatchResult lessBadMatchResult =
        pattern.match(file("schema-validation/shop-order.slightly-wrong.json"));
    assertThat(lessBadMatchResult.isExactMatch(), is(false));
    assertThat(lessBadMatchResult.getDistance(), closeTo(0.33, 0.01));
  }

  private static List<String> invalidContent() {
    return Arrays.asList(null, "", "not json", "{");
  }

  @ParameterizedTest
  @MethodSource("invalidContent")
  void invalidContentGivesNoMatch(String content) {
    String schema = file("schema-validation/shop-order.schema.json");

    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema);

    MatchResult veryBadMatchResult = pattern.match(content);

    assertThat(veryBadMatchResult.isExactMatch(), is(false));
    assertThat(veryBadMatchResult.getDistance(), greaterThan(0.33));
  }

  @Test
  void serialisesToJsonCorrectlyWithDefaultSchemaVersion() {
    String schema = file("schema-validation/shop-order.schema.json");
    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema);

    String json = Json.write(pattern);
    String schemaString = JsonPath.read(json, "$.matchesJsonSchema");
    assertThat(schemaString, jsonEquals(schema));
  }

  @Test
  void serialisesToJsonCorrectlyWithProvidedSchemaVersion() {
    String schema = file("schema-validation/shop-order.schema.json");
    MatchesJsonSchemaPattern pattern =
        new MatchesJsonSchemaPattern(schema, WireMock.JsonSchemaVersion.V4);

    String json = Json.write(pattern);
    String schemaString = JsonPath.read(json, "$.matchesJsonSchema");
    assertThat(schemaString, jsonEquals(schema));
  }

  @Test
  void deserialisesFromJsonCorrectlyWithDefaultSchemaVersion() {
    String schemaJson =
        "{\n"
            + "    \"required\": [\n"
            + "      \"itemCatalogueId\",\n"
            + "      \"quantity\"\n"
            + "    ],\n"
            + "    \"properties\": {\n"
            + "      \"itemCatalogueId\": {\n"
            + "        \"type\": \"string\"\n"
            + "      },\n"
            + "      \"quantity\": {\n"
            + "        \"type\": \"integer\"\n"
            + "      },\n"
            + "      \"fastDelivery\": {\n"
            + "        \"type\": \"boolean\"\n"
            + "      }\n"
            + "    }\n"
            + "  }";

    String matcherJson = "{\n" + "  \"matchesJsonSchema\": " + stringify(schemaJson) + "\n" + "}";

    MatchesJsonSchemaPattern pattern = Json.read(matcherJson, MatchesJsonSchemaPattern.class);

    assertThat(pattern.getMatchesJsonSchema(), jsonEquals(schemaJson));
  }

  @Test
  void deserialisesFromJsonCorrectlyWithProvidedSchemaVersion() {
    String schemaJson =
        "{\n"
            + "    \"properties\": {\n"
            + "      \"itemCatalogueId\": {\n"
            + "        \"type\": \"string\"\n"
            + "      }\n"
            + "    }\n"
            + "  }";

    String matcherJson =
        "{\n"
            + "  \"matchesJsonSchema\": "
            + stringify(schemaJson)
            + ",\n"
            + "  \"schemaVersion\": \"V6\"\n"
            + "}";

    MatchesJsonSchemaPattern pattern = Json.read(matcherJson, MatchesJsonSchemaPattern.class);

    assertThat(pattern.getSchemaVersion(), is(V6));
  }

  private static final StringValuePattern stringSchema =
      matchingJsonSchema(
          "{" + "\"type\": \"string\"," + "\"minLength\": 2," + "\"maxLength\": 4" + "}");

  @ParameterizedTest
  @MethodSource("validStrings")
  void matchesAString(String toMatch) {
    MatchResult match = stringSchema.match(toMatch);
    assertThat(match.isExactMatch(), is(true));
  }

  private static Stream<Arguments> validStrings() {
    return Stream.of(
        Arguments.of("\"12\""),
        Arguments.of("\"123\""),
        Arguments.of("\"1234\""),
        Arguments.of("12"),
        Arguments.of("123"),
        Arguments.of("1234"));
  }

  @ParameterizedTest
  @MethodSource("invalidStrings")
  void doesNotMatchAnInvalidString(String toMatch) {
    MatchResult match = stringSchema.match(toMatch);

    assertThat(match.isExactMatch(), is(false));
    assertThat(match.getDistance(), is(1.0));
  }

  private static Stream<Arguments> invalidStrings() {
    return Stream.of(
        Arguments.of(""),
        Arguments.of("\"\""),
        Arguments.of("\"1\""),
        Arguments.of("\"12345\""),
        Arguments.of("1"),
        Arguments.of("12345"));
  }

  @ParameterizedTest
  @MethodSource("simpleRefSchemaMatchingExamples")
  void simpleRefMatches(String input) {
    String schema = file("schema-validation/has-ref.schema.json");

    MatchesJsonSchemaPattern pattern =
        new MatchesJsonSchemaPattern(schema, WireMock.JsonSchemaVersion.V4);

    MatchResult match = pattern.match(input);

    assertThat(match.isExactMatch(), is(true));
  }

  private static Stream<Arguments> simpleRefSchemaMatchingExamples() {
    return Stream.of(
        Arguments.of("{ \"things\": [] }"),
        Arguments.of("{ \"things\": [ 1 ] }"),
        Arguments.of("{ \"things\": [ 1, 2 ] }"));
  }

  @ParameterizedTest
  @MethodSource("simpleRefSchemaNonMatchingExamples")
  void simpleRefRejectsNonMatches(String input) {
    String schema = file("schema-validation/has-ref.schema.json");

    MatchesJsonSchemaPattern pattern =
        new MatchesJsonSchemaPattern(schema, WireMock.JsonSchemaVersion.V4);

    MatchResult match = pattern.match(input);

    assertThat(match.isExactMatch(), is(false));
  }

  private static Stream<Arguments> simpleRefSchemaNonMatchingExamples() {
    return Stream.of(
        Arguments.of("{}"),
        Arguments.of("{ \"not_things\": null }"),
        Arguments.of("{ \"not_things\": [] }"),
        Arguments.of("{ \"things\": null }"),
        Arguments.of("{ \"things\": {} }"),
        Arguments.of("{ \"things\": 1 }"),
        Arguments.of("{ \"things\": [ \"1\" ] }"));
  }

  @ParameterizedTest
  @Disabled
  @MethodSource("recursiveSchemaMatchingExamples")
  void recursiveRefExactMatchesCorrectlyMatched(String input) {
    String schema = file("schema-validation/recursive.schema.json");

    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema, V4);

    MatchResult match = pattern.match(input);

    assertThat(match.isExactMatch(), is(true));
  }

  private static Stream<Arguments> recursiveSchemaMatchingExamples() {
    return Stream.of(
        Arguments.of("{ \"name\": \"no_children\" }"),
        Arguments.of("{ \"name\": \"no_children\", \"children\": null }"),
        Arguments.of("{ \"name\": \"no_children\", \"children\": [] }"),
        Arguments.of(
            "{ \"name\": \"no_grandchildren\", \"children\": [{ \"name\": \"no_children\", \"children\": [] }] }"));
  }

  @ParameterizedTest
  @Disabled
  @MethodSource("recursiveSchemaNonMatchingExamples")
  void recursiveRefNonMatchesCorrectlyMatched(String input) {
    String schema = file("schema-validation/recursive.schema.json");

    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema, V4);

    MatchResult match = pattern.match(input);

    assertThat(match.isExactMatch(), is(false));
  }

  @Test
  void corercesNumericActualValueToJsonNumber() {
    String schema = file("schema-validation/numeric.schema.json");

    MatchesJsonSchemaPattern pattern =
        new MatchesJsonSchemaPattern(schema, WireMock.JsonSchemaVersion.V4);

    assertThat(pattern.match("5").isExactMatch(), is(true));
    assertThat(pattern.match("0").isExactMatch(), is(true));
    assertThat(pattern.match("100").isExactMatch(), is(true));
    assertThat(pattern.match("10a").isExactMatch(), is(false));
    assertThat(pattern.match("101").isExactMatch(), is(false));
  }

  @Test
  void coercesNumericActualValueToJsonString() {
    String schema = file("schema-validation/stringy.schema.json");

    MatchesJsonSchemaPattern pattern =
        new MatchesJsonSchemaPattern(schema, WireMock.JsonSchemaVersion.V4);

    assertThat(pattern.match("abcd").isExactMatch(), is(true));
    assertThat(pattern.match("abcde").isExactMatch(), is(true));
    assertThat(pattern.match("abcdef").isExactMatch(), is(false));
    assertThat(pattern.match("1").isExactMatch(), is(true));
    assertThat(pattern.match("12345").isExactMatch(), is(true));
    assertThat(pattern.match("123456").isExactMatch(), is(false));
  }

  private static Stream<Arguments> recursiveSchemaNonMatchingExamples() {
    return Stream.of(
        Arguments.of("{}"),
        Arguments.of("{ \"not_a_name\": null }"),
        Arguments.of("{ \"name\": \"invalid_child\", \"children\": [{}] }"),
        Arguments.of(
            "{ \"name\": \"invalid_grandchild\", \"children\": [{ \"name\": \"invalid_child\", \"children\": [{}] }] }"));
  }

  private static String stringify(String json) {
    return "\"" + json.replace("\n", "").replace("\"", "\\\"") + "\"";
  }
}
