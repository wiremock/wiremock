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

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class AdvancedPathPatternSerializationTest {

  @Test
  void matchesXpathWithOnlyAValueSerializesCorrectly() throws JSONException {
    String expectedJson = "{\"matchesXPath\" : \"//AccountId\"}";
    StringValuePattern pattern = matchingXPath("//AccountId");
    String serializedPattern = Json.write(pattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  @Test
  void matchingXpathSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "        \"matchesXPath\" : {\n"
            + "            \"expression\" : \"//AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "}";
    StringValuePattern pattern = matchingXPath("//AccountId", equalTo("123"));
    String serializedPattern = Json.write(pattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  // TODO: This is not correct, the submatcher should not be there
  @Test
  void nestedMatchingXpathWithPatternSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "    \"matchesJsonPath\" : {\n"
            + "        \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "        \"submatcher\" : {\n"
            + "           \"matchesXPath\" : {\n"
            + "               \"expression\" : \"//AccountId\",\n"
            + "               \"equalTo\" : \"123\"\n"
            + "           }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingJsonPath(
            "$.LinkageDetails.AccountId", matchingXPath("//AccountId", equalTo("123")));
    String serializedPattern = Json.write(pattern);
    System.out.println(serializedPattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  // TODO: This should fail - the submatcher shouldn't be there
  @Test
  void nestedMatchingXpathWithNameSpacesSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "    \"matchesJsonPath\" : {\n"
            + "        \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "        \"submatcher\" : {\n"
            + "           \"matchesXPath\" : \"//AccountId\",\n"
            + "           \"xPathNamespaces\" : {\n"
            + "               \"one\" : \"https://example.com/one\",\n"
            + "               \"two\" : \"https://example.com/two\"\n"
            + "            }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingJsonPath(
            "$.LinkageDetails.AccountId",
            matchingXPath(
                "//AccountId",
                Map.of("one", "https://example.com/one", "two", "https://example.com/two")));
    String serializedPattern = Json.write(pattern);
    System.out.println(serializedPattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  @Test
  void matchesJsonPathWithOnlyAValueSerializesCorrectly() throws JSONException {
    String expectedJson = "{\"matchesJsonPath\" : \"$.LinkageDetails.AccountId\"}";
    StringValuePattern pattern = matchingJsonPath("$.LinkageDetails.AccountId");
    String serializedPattern = Json.write(pattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  @Test
  void matchingJsonPathSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "        \"matchesJsonPath\" : {\n"
            + "            \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "            \"equalTo\" : \"123\"\n"
            + "        }\n"
            + "}";
    StringValuePattern pattern = matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123"));
    String serializedPattern = Json.write(pattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  // TODO: This is not correct, the submatcher should not be there
  @Test
  void nestedMatchingJsonPathWithOnlyValueSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"submatcher\" : {\n"
            + "           \"matchesJsonPath\" : \"$.LinkageDetails.AccountId\"\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath("//AccountId", matchingJsonPath("$.LinkageDetails.AccountId"));
    String serializedPattern = Json.write(pattern);
    System.out.println(serializedPattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  // TODO: This is not correct, the submatcher should not be there
  @Test
  void nestedMatchingJsonPathWithPatternSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"submatcher\" : {\n"
            + "           \"matchesJsonPath\" : {\n"
            + "               \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "               \"equalTo\" : \"123\"\n"
            + "           }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath(
            "//AccountId", matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123")));
    String serializedPattern = Json.write(pattern);
    System.out.println(serializedPattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }

  // TODO: This is not correct, the submatcher should not be there
  @Test
  void nestedMatchingJsonPathSerializesCorrectly() throws JSONException {
    String expectedJson =
        "{\n"
            + "    \"matchesXPath\" : {\n"
            + "        \"expression\" : \"//AccountId\",\n"
            + "        \"submatcher\" : {\n"
            + "           \"matchesJsonPath\" : {\n"
            + "               \"expression\" : \"$.LinkageDetails.AccountId\",\n"
            + "               \"equalTo\" : \"123\"\n"
            + "           }\n"
            + "        }\n"
            + "    }\n"
            + "}";
    StringValuePattern pattern =
        matchingXPath(
            "//AccountId", matchingJsonPath("$.LinkageDetails.AccountId", equalTo("123")));
    String serializedPattern = Json.write(pattern);
    System.out.println(serializedPattern);
    JSONAssert.assertEquals(expectedJson, serializedPattern, true);
  }
}
