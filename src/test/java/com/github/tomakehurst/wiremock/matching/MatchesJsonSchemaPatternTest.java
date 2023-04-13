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
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.file;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.common.Json;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;

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
    MatchesJsonSchemaPattern pattern = new MatchesJsonSchemaPattern(schema, V4);

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

  private static String stringify(String json) {
    return "\"" + json.replace("\n", "").replace("\"", "\\\"") + "\"";
  }
}
