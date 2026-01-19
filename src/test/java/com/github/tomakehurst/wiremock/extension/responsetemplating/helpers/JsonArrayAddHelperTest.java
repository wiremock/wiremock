/*
 * Copyright (C) 2024-2026 Thomas Akehurst
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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;

public class JsonArrayAddHelperTest extends HandlebarsHelperTestBase {

  @Test
  void helperIsAccessibleFromResponseBody() {
    String responseTemplate =
        "{{ jsonArrayAdd '[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]' '{\"id\":321,\"name\":\"sam\"}' }}";
    final ResponseDefinition responseDefinition =
        transform(transformer, mockRequest(), aResponse().withBody(responseTemplate));

    assertThat(
        responseDefinition.getBody(),
        is(
            "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]"));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[]'," + "'{\"id\":321,\"name\":\"sam\"}'," + "'[{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'\"name\"',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},\"name\"]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'true',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},true]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'null',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},null]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'123',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},123]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]]'",
  })
  void addsAnItemToAnArrayAndReturnsString(
      String inputArray, String itemToAdd, String expectedOutput) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item }}")
            .apply(Map.of("array", inputArray, "item", itemToAdd));
    assertThat(output, is(expectedOutput));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]'",
    "'[]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"}]',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[]',"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]'",
    "'[]'," + "'[]'," + "'[]'",
    "'[]'," + "'[null]'," + "'[null]'",
  })
  void addsMultipleItemsToAnArrayAndReturnsString(
      String inputArray, String itemToAdd, String expectedOutput) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item flatten=true }}")
            .apply(Map.of("array", inputArray, "item", itemToAdd));
    assertThat(output, is(expectedOutput));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"name\":\"bob\"}]',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "false,"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "false,"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "false,"
        + "'[{\"id\":123,\"name\":\"alice\"},{\"id\":666,\"name\":\"jason\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"}]',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "false,"
        + "'[{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "true,"
        + "'[{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"}]',"
        + "true,"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"}]'",
    "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]',"
        + "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"},{\"id\":987,\"name\":\"sue\"}]',"
        + "true,"
        + "'[{\"id\":666,\"name\":\"jason\"},{\"id\":789,\"name\":\"max\"},{\"id\":987,\"name\":\"sue\"}]'",
  })
  void theNumberOfItemsInTheOutputArrayCanBeLimited(
      String inputArray, String itemToAdd, String flatten, String expectedOutput)
      throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item flatten=" + flatten + " maxItems=3 }}")
            .apply(Map.of("array", inputArray, "item", itemToAdd));
    assertThat(output, is(expectedOutput));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"id\":321,\"name\":\"sam\"}",
        "[{\"id\":321,\"name\":\"sam\"}]",
      })
  void theNumberOfItemsInTheOutputArrayCanBeLimitedTo0(String itemToAdd) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item maxItems=0 }}")
            .apply(Map.of("array", "[{\"id\":456,\"name\":\"bob\"}]", "item", itemToAdd));
    assertThat(output, is("[]"));
  }

  private static Stream<Arguments> provideInputsForReturnsAnErrorWhenInputJsonIsNotAString() {
    final JsonMapper mapper = JsonMapper.builder().build();
    final JsonNodeFactory nodeFactory = mapper.getNodeFactory();

    return Stream.of(
        Arguments.of(
            mapper.readTree(
                "[ { \"id\": 456, \"name\": \"bob\" }, { \"id\": 123, \"name\": \"alice\" }, { \"id\": 321, \"name\": \"sam\" } ]"),
            mapper.readTree("{ \"id\": 456, \"name\": \"bob\" }"),
            nodeFactory.booleanNode(true),
            nodeFactory.nullNode(),
            nodeFactory.numberNode(123)));
  }

  @MethodSource("provideInputsForReturnsAnErrorWhenInputJsonIsNotAString")
  @ParameterizedTest
  void returnsAnErrorWhenInputJsonIsNotAString(Object inputJson) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    Map<String, Object> context = new HashMap<>();
    context.put("array", inputJson);
    context.put("item", "{\"id\":321,\"name\":\"sam\"}");
    String output = handleBars.compileInline("{{ jsonArrayAdd array item }}").apply(context);
    assertThat(output, is("[ERROR: Base JSON must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[{\"id\":456}",
        "",
        " ",
        // This is actually valid JSON but JSONPath library throws an exception and
        // it's simpler to use that exception than handle the scenario ourselves.
        "null",
      })
  void returnsAnErrorWhenInputJsonIsNotValidJson(String inputJson) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item }}")
            .apply(Map.of("array", inputJson, "item", "{\"id\":321,\"name\":\"sam\"}"));
    assertThat(output, is("[ERROR: Base JSON is not valid JSON ('" + inputJson + "')]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"id\":456,\"name\":\"bob\"}",
        "\"name\"",
        "true",
        "123",
      })
  void returnsAnErrorWhenInputJsonIsNotJsonArray(String inputJson) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item }}")
            .apply(Map.of("array", inputJson, "item", "{\"id\":321,\"name\":\"sam\"}"));
    assertThat(output, is("[ERROR: Target JSON is not a JSON array ('" + inputJson + "')]"));
  }

  @ParameterizedTest
  @CsvSource({
    "'{\"id\":321,\"name\":\"sam\"}',"
        + "false,"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]'",
    "'\"name\"',"
        + "false,"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},\"name\"]'",
    "'true'," + "false," + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},true]'",
    "'null'," + "false," + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},null]'",
    "'123'," + "false," + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},123]'",
    "'[{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]',"
        + "true,"
        + "'[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"},{\"id\":666,\"name\":\"jason\"}]'",
  })
  void itemToAddCanBeSpecifiedInAHandlebarsBlock(
      String block, boolean flatten, String expectedOutput) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline(
                "{{#jsonArrayAdd array flatten=" + flatten + "}}" + block + "{{/jsonArrayAdd}}")
            .apply(
                Map.of("array", "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]"));
    assertThat(output, is(expectedOutput));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 }",
        "",
        " ",
      })
  void returnsAnErrorWhenItemToAddInAHandlebarsBlockDoesNotResolveToValidJson(String block)
      throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{#jsonArrayAdd array}}" + block + "{{/jsonArrayAdd}}")
            .apply(
                Map.of("array", "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]"));
    assertThat(output, is("[ERROR: Item-to-add JSON is not valid JSON ('" + block + "')]"));
  }

  private static Stream<Arguments> provideInputsForReturnsAnErrorWhenItemToAddIsNotAString() {
    final JsonMapper mapper = JsonMapper.builder().build();
    final JsonNodeFactory nodeFactory = mapper.getNodeFactory();

    return Stream.of(
        Arguments.of(
            mapper.readTree("[ { \"id\": 456, \"name\": \"bob\" } ]"),
            mapper.readTree("{ \"id\": 456, \"name\": \"bob\" }"),
            nodeFactory.booleanNode(true),
            nodeFactory.nullNode(),
            nodeFactory.numberNode(123)));
  }

  @MethodSource("provideInputsForReturnsAnErrorWhenItemToAddIsNotAString")
  @ParameterizedTest
  void returnsAnErrorWhenItemToAddIsNotAString(Object item) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    Map<String, Object> context = new HashMap<>();
    context.put("array", "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]");
    context.put("item", item);
    String output = handleBars.compileInline("{{ jsonArrayAdd array item }}").apply(context);
    assertThat(output, is("[ERROR: Item-to-add JSON must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid json",
        "[ { \"id\": 456 }",
        "",
        " ",
      })
  void returnsAnErrorWhenItemToAddIsNotValidJson(String item) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item }}")
            .apply(
                Map.of(
                    "array",
                    "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]",
                    "item",
                    item));
    assertThat(output, is("[ERROR: Item-to-add JSON is not valid JSON ('" + item + "')]"));
  }

  private static Stream<Arguments> provideInputsForReturnsAnErrorWhenMaxItemsIsNotAnInteger() {
    final JsonMapper mapper = JsonMapper.builder().build();
    final JsonNodeFactory nodeFactory = mapper.getNodeFactory();

    return Stream.of(
        Arguments.of(
            nodeFactory.numberNode(1.23),
            nodeFactory.booleanNode(true),
            nodeFactory.stringNode("not a number"),
            nodeFactory.stringNode("1"),
            nodeFactory.objectNode(),
            nodeFactory.arrayNode()));
  }

  @MethodSource("provideInputsForReturnsAnErrorWhenMaxItemsIsNotAnInteger")
  @ParameterizedTest
  void returnsAnErrorWhenMaxItemsIsNotAnInteger(Object maxItems) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item maxItems=max }}")
            .apply(
                Map.of(
                    "array",
                    "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]",
                    "item",
                    "{\"id\":321,\"name\":\"sam\"}",
                    "max",
                    maxItems));
    assertThat(output, is("[ERROR: maxItems option must be an integer]"));
  }

  @Test
  void returnsAnErrorWhenMaxItemsIsANegativeInteger() throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item maxItems=-1 }}")
            .apply(
                Map.of(
                    "array",
                    "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]",
                    "item",
                    "{\"id\":321,\"name\":\"sam\"}"));
    assertThat(output, is("[ERROR: maxItems option integer must be positive]"));
  }

  private static Stream<Arguments> provideInputsForReturnsAnErrorWhenFlattenOptionIsNotABoolean() {
    final JsonMapper mapper = JsonMapper.builder().build();
    final JsonNodeFactory nodeFactory = mapper.getNodeFactory();

    return Stream.of(
        Arguments.of(
            nodeFactory.numberNode(1.23),
            nodeFactory.stringNode("true"),
            nodeFactory.stringNode("not a number"),
            nodeFactory.numberNode(1),
            nodeFactory.objectNode(),
            nodeFactory.arrayNode()));
  }

  @MethodSource("provideInputsForReturnsAnErrorWhenFlattenOptionIsNotABoolean")
  @ParameterizedTest
  void returnsAnErrorWhenFlattenOptionIsNotABoolean(Object flatten) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd array item flatten=flat }}")
            .apply(
                Map.of(
                    "array",
                    "[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]",
                    "item",
                    "{\"id\":321,\"name\":\"sam\"}",
                    "flat",
                    flatten));
    assertThat(output, is("[ERROR: flatten option must be a boolean]"));
  }

  @ParameterizedTest
  @CsvSource({
    "'{\"expired\":false,\"users\":[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"}]}',"
        + "'{\"id\":321,\"name\":\"sam\"}',"
        + "'$.users',"
        + "'{\"expired\":false,\"users\":[{\"id\":456,\"name\":\"bob\"},{\"id\":123,\"name\":\"alice\"},{\"id\":321,\"name\":\"sam\"}]}'",
    "'[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]',"
        + "'\"sam\"',"
        + "'$[0].names',"
        + "'[{\"id\":456,\"names\":[\"bob\",\"jason\",\"sam\"]},{\"id\":123,\"names\":[\"alice\"]}]'",
  })
  void jsonPathCanBeProvidedToSspecifyANestedArrayToAddTheItemTo(
      String inputJson, String itemToAdd, String jsonPath, String expectedOutput)
      throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd inputJson item jsonPath=jPath }}")
            .apply(Map.of("inputJson", inputJson, "item", itemToAdd, "jPath", jsonPath));
    assertThat(output, is(expectedOutput));
  }

  private static Stream<Arguments> provideInputsForReturnsAnErrorWhenJsonpathOptionIsNotAString() {
    final JsonMapper mapper = JsonMapper.builder().build();
    final JsonNodeFactory nodeFactory = mapper.getNodeFactory();

    return Stream.of(
        Arguments.of(
            nodeFactory.numberNode(1),
            nodeFactory.numberNode(1.23),
            nodeFactory.booleanNode(true),
            nodeFactory.objectNode(),
            nodeFactory.arrayNode()));
  }

  @MethodSource("provideInputsForReturnsAnErrorWhenJsonpathOptionIsNotAString")
  @ParameterizedTest
  void returnsAnErrorWhenJsonpathOptionIsNotAString(Object jsonPath) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{ jsonArrayAdd inputJson item jsonPath=jPath }}")
            .apply(
                Map.of(
                    "inputJson",
                    "[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]",
                    "item",
                    "\"sam\"",
                    "jPath",
                    jsonPath));
    assertThat(output, is("[ERROR: jsonPath option must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "invalid jsonpath",
        "$.[",
        "",
        " ",
      })
  void returnsAnErrorWhenJsonpathOptionIsNotValidExpression(String jsonPath) throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{jsonArrayAdd inputJson item jsonPath=jPath}}")
            .apply(
                Map.of(
                    "inputJson",
                    "[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]",
                    "item",
                    "\"sam\"",
                    "jPath",
                    jsonPath));
    assertThat(
        output,
        is("[ERROR: jsonPath option is not valid JSONPath expression ('" + jsonPath + "')]"));
  }

  @ParameterizedTest
  @CsvSource({
    "'[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]',"
        + "'$[0].doesNotExist'",
    "'[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]',"
        + "'$[0].doesNotExist.myArray'",
    "'[{\"id\":456,\"names\":[\"bob\",\"jason\"]},{\"id\":123,\"names\":[\"alice\"]}]',"
        + "'$[0].id'",
    "'{\"id\":\"a string\"}'," + "'$.id'",
    "'{\"nullable\":null}'," + "'$.nullable'",
  })
  void returnsAnErrorWhenJsonpathDoesNotResolveToAnArray(String inputJson, String jsonPath)
      throws IOException {
    Handlebars handleBars =
        new Handlebars()
            .with(EscapingStrategy.NOOP)
            .registerHelper("jsonArrayAdd", new JsonArrayAddHelper());
    String output =
        handleBars
            .compileInline("{{jsonArrayAdd inputJson item jsonPath=jPath}}")
            .apply(Map.of("inputJson", inputJson, "item", "\"sam\"", "jPath", jsonPath));
    assertThat(
        output,
        is(
            "[ERROR: Target JSON is not a JSON array (root: '"
                + inputJson
                + "', jsonPath: '"
                + jsonPath
                + "')]"));
  }
}
