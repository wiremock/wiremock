/*
 * Copyright (C) 2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.common.DateTimeTruncation.FIRST_DAY_OF_MONTH;
import static com.github.tomakehurst.wiremock.common.DateTimeTruncation.LAST_DAY_OF_MONTH;
import static com.github.tomakehurst.wiremock.common.DateTimeUnit.DAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.testsupport.TestFiles;
import com.networknt.schema.*;
import java.util.Map;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContentPatternsJsonValidityTest {

  static JsonSchemaFactory schemaFactory;
  static SchemaValidatorsConfig config;
  static JsonSchema schema;

  @BeforeAll
  static void init() {
    config = SchemaValidatorsConfig.builder().build();

    schemaFactory =
        JsonSchemaFactory.getInstance(WireMock.JsonSchemaVersion.V202012.toVersionFlag());

    schema =
        schemaFactory.getSchema(
            SchemaLocation.of(TestFiles.fileUri("swagger/schemas/content-pattern.yaml").toString()),
            config);
  }

  @Test
  void equalToValidates() {
    assertThat(validate(equalTo("abc")), empty());
    assertThat(
        validate("{ \"equalTo\": \"thing\", \"caseInsensitive\": 3 }"), Matchers.not(empty()));
  }

  @Test
  void binaryEqualToValidates() {
    assertThat(validate(binaryEqualTo("abc".getBytes())), empty());
    assertThat(validate("{ \"binaryEqualTo\": \"not base 64\" }"), Matchers.not(empty()));
  }

  @Test
  void equalToJsonWithMinimalParametersValidates() {
    assertThat(validate(equalToJson("{}")), empty());
    assertThat(validate("{ \"equalToJson\": 5 }"), Matchers.not(empty()));
  }

  @Test
  void equalToJsonWithAllParametersValidates() {
    assertThat(validate(equalToJson("{}", false, true)), empty());
    assertThat(
        validate(
            "{ \"equalToJson\": \"{}\", \"ignoreExtraElements\": false, \"ignoreArrayOrder\": {} }"),
        Matchers.not(empty()));
  }

  @Test
  void simpleMatchesJsonPathValidates() {
    assertThat(validate(matchingJsonPath("$.id")), empty());
    assertThat(validate("{ \"matchesJsonPath\": 5 }"), Matchers.not(empty()));
  }

  @Test
  void matchesJsonPathWithSubMatcherValidates() {
    assertThat(validate(matchingJsonPath("$.id", equalTo("123"))), empty());
    assertThat(validate("{ \"matchesJsonPath\": 5 }"), Matchers.not(empty()));
  }

  @Test
  void equalToXmlWithMinimalParametersValidates() {
    assertThat(validate(equalToXml("<thing/>")), empty());
    assertThat(validate("{ \"equalToXml\": 5 }"), Matchers.not(empty()));
  }

  @Test
  void equalToXmlWithAllParametersValidates() {
    assertThat(validate(equalToXml("<thing/>", true, "[", "]", true)), empty());
    assertThat(
        validate(
            "{\n"
                + "  \"equalToXml\" : \"<thing/>\",\n"
                + "  \"enablePlaceholders\" : true,\n"
                + "  \"placeholderOpeningDelimiterRegex\" : 3,\n"
                + "  \"placeholderClosingDelimiterRegex\" : \"]\"\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void simpleMatchesXPathValidates() {
    assertThat(validate(matchingXPath("//Order/Quantity")), empty());
    assertThat(validate("{ \"matchesXPath\": 5 }"), Matchers.not(empty()));
  }

  @Test
  void matchesXPathWithSubMatcherValidates() {
    assertThat(validate(matchingXPath("//Order/Quantity", equalTo("123"))), empty());
    assertThat(
        validate(
            "{\n"
                + "  \"matchesXPath\": {\n"
                + "    \"expression\": true,\n"
                + "    \"equalTo\": \"123\"\n"
                + "  }\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void matchesXPathWithNamespacesValidates() {
    assertThat(
        validate(
            matchingXPath(
                "//Order/Quantity",
                Map.of("one", "https://example.com/one", "two", "https://example.com/two"))),
        empty());

    assertThat(
        validate(
            "{\n"
                + "  \"matchesXPath\" : \"//Order/Quantity\",\n"
                + "  \"xPathNamespaces\" : {\n"
                + "    \"one\": \"https://example.com/one\",\n"
                + "    \"two\": 543 \n"
                + "  }\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void matchesJsonSchemaValidates() {
    assertThat(validate(matchingJsonSchema("{ \"type\": \"string\" }")), empty());
    assertThat(validate("{ \"matchesJsonSchema\": true }"), Matchers.not(empty()));
  }

  @Test
  void matchesJsonSchemaWithVersionValidates() {
    assertThat(validate(matchingJsonSchema("{ \"type\": \"string\" }")), empty());
    assertThat(validate("{ \"matchesJsonSchema\": true }"), Matchers.not(empty()));
  }

  @Test
  void containsValidates() {
    assertThat(validate(containing("abc")), empty());
    assertThat(validate("{ \"contains\": true }"), Matchers.not(empty()));
  }

  @Test
  void doesNotContainValidates() {
    assertThat(validate(notContaining("abc")), empty());
    assertThat(validate("{ \"doesNotContain\": true }"), Matchers.not(empty()));
  }

  @Test
  void matchesValidates() {
    assertThat(validate(matching("abc")), empty());
    assertThat(validate("{ \"matches\": true }"), Matchers.not(empty()));
  }

  @Test
  void not_equalToValidates() {
    assertThat(validate(not(equalTo("abc"))), empty());

    assertThat(validate("{\n" + "  \"not\": true\n" + "}"), Matchers.not(empty()));
  }

  @Test
  void doesNotMatchValidates() {
    assertThat(validate(notMatching("abc")), empty());
    assertThat(validate("{ \"doesNotMatch\": true }"), Matchers.not(empty()));
  }

  @Test
  void beforeWithMinimalParametersValidates() {
    assertThat(validate(before("2018-05-05T00:11:22Z")), empty());
    assertThat(validate("{ \"before\": 55 }"), Matchers.not(empty()));
  }

  @Test
  void beforeWithAllParametersValidates() {
    assertThat(
        validate(
            before("2018-05-05T00:11:22Z")
                .actualFormat("yyyy-MM-dd")
                .expectedOffset(3, DAYS)
                .truncateExpected(FIRST_DAY_OF_MONTH)
                .truncateActual(LAST_DAY_OF_MONTH)),
        empty());

    assertThat(
        validate(
            "{\n"
                + "  \"before\" : \"now +3 days\",\n"
                + "  \"actualFormat\" : \"yyyy-MM-dd\",\n"
                + "  \"truncateExpected\" : true,\n"
                + "  \"truncateActual\" : \"last day of month\"\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void afterWithMinimalParametersValidates() {
    assertThat(validate(after("2018-05-05T00:11:22Z")), empty());
    assertThat(validate("{ \"after\": 55 }"), Matchers.not(empty()));
  }

  @Test
  void afterWithAllParametersValidates() {
    assertThat(
        validate(
            after("2018-05-05T00:11:22Z")
                .actualFormat("yyyy-MM-dd")
                .expectedOffset(3, DAYS)
                .truncateExpected(FIRST_DAY_OF_MONTH)
                .truncateActual(LAST_DAY_OF_MONTH)),
        empty());

    assertThat(
        validate(
            "{\n"
                + "  \"after\" : \"now +3 days\",\n"
                + "  \"actualFormat\" : \"yyyy-MM-dd\",\n"
                + "  \"truncateExpected\" : true,\n"
                + "  \"truncateActual\" : \"last day of month\"\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void equalToDateTimeWithMinimalParametersValidates() {
    assertThat(validate(equalToDateTime("2018-05-05T00:11:22Z")), empty());
    assertThat(validate("{ \"equalToDateTime\": 55 }"), Matchers.not(empty()));
  }

  @Test
  void equalToDateTimeWithAllParametersValidates() {
    assertThat(
        validate(
            equalToDateTime("2018-05-05T00:11:22Z")
                .actualFormat("yyyy-MM-dd")
                .expectedOffset(3, DAYS)
                .truncateExpected(FIRST_DAY_OF_MONTH)
                .truncateActual(LAST_DAY_OF_MONTH)),
        empty());

    assertThat(
        validate(
            "{\n"
                + "  \"equalToDateTime\" : \"now +3 days\",\n"
                + "  \"actualFormat\" : \"yyyy-MM-dd\",\n"
                + "  \"truncateExpected\" : true,\n"
                + "  \"truncateActual\" : \"last day of month\"\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void tmp() {
    System.out.println(
        Json.write(
            matchingXPath(
                "//Order/Quantity",
                Map.of("one", "https://example.com/one", "two", "https://example.com/two"))));
  }

  @Test
  void afterValidates() {
    assertThat(validate(after("2018-05-05T00:11:22Z")), empty());
    assertThat(validate("{ \"after\": 55 }"), Matchers.not(empty()));
  }

  @Test
  void absentValidates() {
    assertThat(validate(absent()), empty());
    assertThat(validate("{ \"absent\": 11 }"), Matchers.not(empty()));
  }

  @Test
  void and_containsValidates() {
    assertThat(validate(containing("abc").and(containing("123"))), empty());

    assertThat(
        validate(
            "{\n"
                + "  \"and\": [\n"
                + "    {\n"
                + "      \"contains\": \"abc\"\n"
                + "    },\n"
                + "    \"wrong\"\n"
                + "  ]\n"
                + "}"),
        Matchers.not(empty()));
  }

  @Test
  void or_containsValidates() {
    assertThat(validate(containing("abc").or(containing("123"))), empty());

    assertThat(
        validate(
            "{\n"
                + "  \"or\": [\n"
                + "    {\n"
                + "      \"contains\": \"abc\"\n"
                + "    },\n"
                + "    \"wrong\"\n"
                + "  ]\n"
                + "}"),
        Matchers.not(empty()));
  }

  private static Set<ValidationMessage> validate(Object obj) {
    return schema.validate(Json.write(obj), InputFormat.JSON);
  }

  private static Set<ValidationMessage> validate(String json) {
    return schema.validate(json, InputFormat.JSON);
  }
}
