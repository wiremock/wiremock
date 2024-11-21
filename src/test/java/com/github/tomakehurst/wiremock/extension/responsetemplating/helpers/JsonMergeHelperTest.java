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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.is;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.json.JsonSource;

public class JsonMergeHelperTest extends HandlebarsHelperTestBase {

  private final Handlebars handlebars =
      new Handlebars()
          .with(EscapingStrategy.NOOP)
          .registerHelper("jsonMerge", new JsonMergeHelper());

  private String resolveInlineMerge(Object baseJson, Object jsonToMerge) throws IOException {
    Map<String, Object> context = new HashMap<>();
    context.put("baseJson", baseJson);
    context.put("jsonToMerge", jsonToMerge);
    return handlebars.compileInline("{{jsonMerge baseJson jsonToMerge}}").apply(context);
  }

  private String resolveBlockMerge(Object baseJson, String block) throws IOException {
    return handlebars
        .compileInline("{{#jsonMerge baseJson}}" + block + "{{/jsonMerge}}")
        .apply(Map.of("baseJson", baseJson));
  }

  @Test
  void helperIsAccessibleFromResponseBody() {
    String responseTemplate =
        "{{ jsonMerge '{\"id\":456,\"name\":\"bob\"}' '{\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}' }}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is("{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}"));
  }

  @ParameterizedTest
  @CsvSource({
    "'{ \"id\": 456, \"name\": \"bob\" }',"
        + "'{ \"roles\": [ \"admin\", \"user\" ], \"dob\": \"2024-06-18\" }',"
        + "'{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}'",
    "'{ \"id\": 456, \"name\": \"bob\", \"roles\": [ \"viewer\" ] }',"
        + "'{ \"roles\": [ \"admin\", \"user\" ], \"dob\": \"2024-06-18\" }',"
        + "'{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}'",
    "'{}',"
        + "'{ \"roles\": [ \"admin\", \"user\" ], \"dob\": \"2024-06-18\" }',"
        + "'{\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}'",
    "'{ \"id\": 456, \"name\": \"bob\" }'," + "'{}'," + "'{\"id\":456,\"name\":\"bob\"}'",
    "'{ \"id\": 456, \"name\": \"bob\", \"data\": { \"field\": \"value\" } }',"
        + "'{ \"data\": [ 123, true ] }',"
        + "'{\"id\":456,\"name\":\"bob\",\"data\":[123,true]}'",
  })
  void mergesJsonObjects(String baseJson, String jsonToMerge, String expectedOutput)
      throws IOException {
    assertThat(resolveInlineMerge(baseJson, jsonToMerge), is(expectedOutput));
  }

  @ParameterizedTest
  @JsonSource({
    // have to double wrap arrays because @JsonSource unwraps them.
    "[[ { \"id\": 456, \"name\": \"bob\" }, { \"id\": 123, \"name\": \"alice\" }, { \"id\": 321, \"name\": \"sam\" } ]]",
    "{ \"id\": 456, \"name\": \"bob\" }",
    "true",
    "null",
    "123",
  })
  void returnsAnErrorWhenBaseJsonIsNotAString(Object baseJson) throws IOException {
    assertThat(
        resolveInlineMerge(baseJson, "{\"id\":321,\"name\":\"sam\"}"),
        is("[ERROR: Base JSON parameter must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 }",
        "",
        " ",
      })
  void returnsAnErrorWhenBaseJsonIsNotValidJson(String baseJson) throws IOException {
    assertThat(
        resolveInlineMerge(baseJson, "{\"id\":321,\"name\":\"sam\"}"),
        is("[ERROR: Base JSON is not valid JSON ('" + baseJson + "')]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "[ { \"id\": 123, \"name\": \"alice\" } ]",
        "\"name\"",
        "true",
        "null",
        "123",
      })
  void returnsAnErrorWhenBaseJsonIsNotJsonObject(Object baseJson) throws IOException {
    assertThat(
        resolveInlineMerge(baseJson, "{\"id\":321,\"name\":\"sam\"}"),
        is("[ERROR: Base JSON is not a JSON object ('" + baseJson + "')]"));
  }

  @ParameterizedTest
  @CsvSource({
    "'{\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}',"
        + "'{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"],\"dob\":\"2024-06-18\"}'",
    "'{}'," + "'{\"id\":456,\"name\":\"bob\",\"roles\":[\"viewer\"]}'",
    "'{\"data\":[123, true]}',"
        + "'{\"id\":456,\"name\":\"bob\",\"roles\":[\"viewer\"],\"data\":[123,true]}'",
  })
  void jsonToMergeCanBeSpecifiedInAHandlebarsBlock(String block, String expectedOutput)
      throws IOException {
    assertThat(
        resolveBlockMerge("{\"id\":456,\"name\":\"bob\",\"roles\":[\"viewer\"]}", block),
        is(expectedOutput));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 }",
        "",
        " ",
      })
  void returnsAnErrorWhenJsonToMergeInAHandlebarsBlockDoesNotResolveToValidJson(String block)
      throws IOException {
    assertThat(
        resolveBlockMerge("{\"id\":456,\"name\":\"bob\"}", block),
        is("[ERROR: JSON to merge is not valid JSON ('" + block + "')]"));
  }

  @ParameterizedTest
  @JsonSource({
    // have to double wrap arrays because @JsonSource unwraps them.
    "[[ { id: 456, name: 'bob' } ]]",
    "{ id: 456, name: 'bob' }",
    "true",
    "null",
    "123",
  })
  void returnsAnErrorWhenJsonToMergeIsNotAString(Object jsonToMerge) throws IOException {
    assertThat(
        resolveInlineMerge("{\"id\":456,\"name\":\"bob\"}", jsonToMerge),
        is("[ERROR: JSON to merge must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 }",
        "",
        " ",
      })
  void returnsAnErrorWhenJsonToMergeIsNotValidJson(String jsonToMerge) throws IOException {
    assertThat(
        resolveInlineMerge("{\"id\":456,\"name\":\"bob\"}", jsonToMerge),
        is("[ERROR: JSON to merge is not valid JSON ('" + jsonToMerge + "')]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "[{\"id\":123,\"name\":\"alice\"}]",
        "\"name\"",
        "true",
        "null",
        "123",
      })
  void returnsAnErrorWhenJsonToMergeIsNotAnObject(String jsonToMerge) throws IOException {
    assertThat(
        resolveInlineMerge("{\"id\":456,\"name\":\"bob\"}", jsonToMerge),
        is("[ERROR: JSON to merge is not a JSON object ('" + jsonToMerge + "')]"));
  }

  @Test
  void jsonObjectsAreMergedRecursively() throws IOException {
    String baseJson =
        "{ \"id\": 456, \"name\": \"bob\", \"data\": { \"nestedObject\": { \"value\": \"my value\", \"veryNestedArray\": [ true, false, 123 ], \"anOldField\": \"with an old value\" }, \"someNumber\": 456 } }";
    String jsonToMerge =
        "{ \"data\": { \"nestedObject\": { \"value\": \"new value\", \"veryNestedArray\": [ \"newItem\" ], \"aNewField\": \"with a new value\" }, \"newNestedObject\": { \"someBoolean\": true } } }";
    String output = resolveInlineMerge(baseJson, jsonToMerge);
    String expectedOutput =
        "{ \"id\": 456, \"name\": \"bob\", \"data\": { \"nestedObject\": { \"value\": \"new value\", \"veryNestedArray\": [ \"newItem\" ], \"anOldField\": \"with an old value\", \"aNewField\": \"with a new value\" }, \"someNumber\": 456, \"newNestedObject\": { \"someBoolean\": true } } }";
    assertThat(output, jsonEquals(expectedOutput));
  }
}
