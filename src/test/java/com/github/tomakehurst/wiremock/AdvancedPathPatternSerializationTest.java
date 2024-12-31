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

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

public class AdvancedPathPatternSerializationTest {

  @Test
  void matchesXpathWithOnlyAValueSerializesCorrectly() {
    String expectedJson = "{\"matchesXPath\" : \"//AccountId\"}";
    StringValuePattern pattern = matchingXPath("//AccountId");
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void matchingXpathWithPatternSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "        \"matchesXPath\" : {\n"
            + "            \"expression\" : \"//AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "}";
    StringValuePattern pattern = matchingXPath("//AccountId", equalTo("123"));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void matchingXpathWithNameSpacesSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "           \"matchesXPath\" : \"//AccountId\",\n"
            + "           \"xPathNamespaces\" : {\n"
            + "               \"one\" : \"https://example.com/one\",\n"
            + "               \"two\" : \"https://example.com/two\"\n"
            + "            }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath(
            "//AccountId",
            Map.of("one", "https://example.com/one", "two", "https://example.com/two"));

    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void nestedMatchingXpathWithPatternSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "    \"matchesJsonPath\" : {\n"
            + "        \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "        \"matchesXPath\" : {\n"
            + "            \"expression\" : \"//AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingJsonPath(
            "$.LinkageDetails.AccountId", matchingXPath("//AccountId", equalTo("123")));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void nestedMatchingXpathWithNameSpacesSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "    \"matchesJsonPath\" : {\n"
            + "        \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "        \"matchesXPath\" : \"//AccountId\",\n"
            + "        \"xPathNamespaces\" : {\n"
            + "            \"one\" : \"https://example.com/one\",\n"
            + "            \"two\" : \"https://example.com/two\"\n"
            + "         }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingJsonPath(
            "$.LinkageDetails.AccountId",
            matchingXPath(
                "//AccountId",
                Map.of("one", "https://example.com/one", "two", "https://example.com/two")));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void matchesJsonPathWithOnlyAValueSerializesCorrectly() {
    String expectedJson = "{\"matchesJsonPath\" : \"$.LinkageDetails.AccountId\"}";
    StringValuePattern pattern = matchingJsonPath("$.LinkageDetails.AccountId");
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void matchingJsonPathSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "        \"matchesJsonPath\" : {\n"
            + "            \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "}";
    StringValuePattern pattern = matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123"));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void nestedMatchingJsonPathWithOnlyValueSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"matchesJsonPath\" : \"$.LinkageDetails.AccountId\"\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath("//AccountId", matchingJsonPath("$.LinkageDetails.AccountId"));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void nestedMatchingJsonPathWithPatternSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"matchesJsonPath\" : {\n"
            + "            \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath(
            "//AccountId", matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123")));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }

  @Test
  void nestedMatchingJsonPathSerializesCorrectly() {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"matchesJsonPath\" : {\n"
            + "            \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath(
            "//AccountId", matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123")));
    assertThat(Json.write(pattern), jsonEquals(expectedJson));
  }
}
