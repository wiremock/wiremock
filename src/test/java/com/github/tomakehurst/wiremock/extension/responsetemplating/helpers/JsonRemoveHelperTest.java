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
import static org.hamcrest.MatcherAssert.assertThat;
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

public class JsonRemoveHelperTest extends HandlebarsHelperTestBase {

  @Test
  void helperIsAccessibleFromResponseBody() {
    String responseTemplate = "{{ jsonRemove '{\"id\":456,\"name\":\"bob\"}' '$.name' }}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(responseDefinition.getBody(), is("{\"id\":456}"));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]',"
        + "'$.[?(@.id == 123)]',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":123,\"name\":\"sam\"}]',"
        + "'$[?(@.id == 123)]',"
        + "'[{\"id\":456,\"name\":\"bob\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]',"
        + "'$.[?(@.id == 123)].name',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123},{\"id\":321,\"name\":\"sam\"}]'",
    "'{\"id\":456,\"name\":\"bob\"}'," + "'$.name'," + "'{\"id\":456}'",
    "'[{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"]},{\"id\":123,\"name\":\"alice\",\"roles\":[\"admin\",\"user\"]}]',"
        + "'$.[?(@.name == \"alice\")].roles[?(@ == \"admin\")]',"
        + "'[{\"id\":456,\"name\":\"bob\",\"roles\":[\"admin\",\"user\"]},{\"id\":123,\"name\":\"alice\",\"roles\":[\"user\"]}]'",
  })
  void removesElementsFromStringInputAndReturnsString(
      String inputJson, String jsonPath, String expectedOutput) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '" + jsonPath + "' }}")
            .apply(Map.of("input", inputJson));
    assertThat(output, is(expectedOutput));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]'," + "'$.[?(@.id == 321)]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]'," + "'$.name'",
    "'{\"id\":456}'," + "'$.name'",
    "'true'," + "'$.name'",
    "'null'," + "'$.name'",
    "'123'," + "'$.name'",
  })
  void noOpIfJsonpathResultIsNotFoundForInputJson(String inputJson, String jsonPath)
      throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '" + jsonPath + "' }}")
            .apply(Map.of("input", inputJson));
    assertThat(output, is(inputJson));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 } ",
        "",
        " ",
      })
  void errorsIfInputJsonStringIsNotValidJson(String inputJson) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '$.name' }}")
            .apply(Map.of("input", inputJson));
    assertThat(output, is("[ERROR: Input JSON string is not valid JSON ('" + inputJson + "')]"));
  }

  @ParameterizedTest
  @JsonSource({
    // have to double wrap arrays because @JsonSource unwraps them.
    "[[ { id: 456, name: 'bob' }, { id: 123, name: 'alice' }, { id: 321, name: 'sam' } ]]",
    "{ id: 456, name: 'bob' }",
    "true",
    "null",
    "123",
  })
  void errorsIfInputJsonIsNotAString(Object inputJson) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    Map<String, Object> context = new HashMap<>();
    context.put("input", inputJson);
    String output = handleBars.compileInline("{{ jsonRemove input '$.name' }}").apply(context);
    assertThat(output, is("[ERROR: Input JSON must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "$name",
        "not json path",
        "",
        " ",
      })
  void errorsIfJsonpathExpressionIsNotAValidJsonpath(String jsonPath) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '" + jsonPath + "' }}")
            .apply(Map.of("input", "{\"id\":123,\"name\":\"bob\"}"));
    assertThat(
        output,
        is("[ERROR: JSONPath parameter is not a valid JSONPath expression ('" + jsonPath + "')]"));
  }

  @ParameterizedTest
  @JsonSource({
    "{}",
    // have to double wrap arrays because @JsonSource unwraps them.
    "[[]]",
    "true",
    "null",
    "123",
  })
  void errorsIfJsonpathExpressionIsNotAString(Object jsonPath) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    Map<String, Object> context = new HashMap<>();
    context.put("input", "{\"id\":123,\"name\":\"bob\"}");
    context.put("jsonPath", jsonPath);
    String output = handleBars.compileInline("{{ jsonRemove input jsonPath }}").apply(context);
    assertThat(output, is("[ERROR: JSONPath parameter must be a string]"));
  }

  @Test
  void errorsIfJsonpathExpressionCannotBeAppliedToADeleteOperation() throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '$' }}")
            .apply(Map.of("input", "{\"id\":123,\"name\":\"bob\"}"));
    assertThat(
        output, is("[ERROR: Delete operation cannot be applied to JSONPath expression ('$')]"));
  }

  @Test
  void errorsIfJsonpathIsNotProvided() throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input }}")
            .apply(Map.of("input", "{\"id\":123,\"name\":\"bob\"}"));
    assertThat(output, is("[ERROR: A single JSONPath expression parameter must be supplied]"));
  }

  @Test
  void errorsIfMoreThanOneParameterIsProvided() throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonRemove", new JsonRemoveHelper());
    String output =
        handleBars
            .compileInline("{{ jsonRemove input '$.name' '$.name' }}")
            .apply(Map.of("input", "{\"id\":123,\"name\":\"bob\"}"));
    assertThat(output, is("[ERROR: A single JSONPath expression parameter must be supplied]"));
  }
}
