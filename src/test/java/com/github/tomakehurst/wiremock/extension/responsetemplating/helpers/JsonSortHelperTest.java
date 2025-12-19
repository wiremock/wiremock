/*
 * Copyright (C) 2025 Thomas Akehurst
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.jknack.handlebars.EscapingStrategy;
import com.github.jknack.handlebars.Handlebars;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.json.JsonSource;

public class JsonSortHelperTest extends HandlebarsHelperTestBase {

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
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, Object> context = new HashMap<>();
    context.put("input", inputJson);
    String output = handleBars.compileInline("{{ jsonSort input '$.name' }}").apply(context);
    assertThat(output, is("[ERROR: Input JSON must be a string]"));
  }

  @Test
  void errorsIfJsonpathIsNotProvided() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              {"id":123,"name":"bob"}""");
    String output = handleBars.compileInline("{{ jsonSort input }}").apply(context);
    assertThat(output, is("[ERROR: A single JSONPath expression parameter must be supplied]"));
  }

  @Test
  void errorsIfMoreThanOneParameterIsProvided() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              {"id":123,"name":"bob"}""");
    String output =
        handleBars.compileInline("{{ jsonSort input '$.name' '$.name' }}").apply(context);
    assertThat(output, is("[ERROR: A single JSONPath expression parameter must be supplied]"));
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
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, Object> context = new HashMap<>();
    context.put("input", """
            {"id":123,"name":"bob"}""");
    context.put("jsonPath", jsonPath);
    String output = handleBars.compileInline("{{ jsonSort input jsonPath }}").apply(context);
    assertThat(output, is("[ERROR: JSONPath parameter must be a string]"));
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
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", inputJson);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is("[ERROR: Input JSON string is not valid JSON ('" + inputJson + "')]"));
  }

  @Test
  void errorsIfOrderParameterIsNotAString() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].name' order=123 }}").apply(context);
    assertThat(output, is("[ERROR: order parameter must be a string]"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"invalid", "ascending", "descending", "up", "down", "ASC", "DESC", " ", ""})
  void errorsIfOrderParameterIsNotAscOrDesc(String orderValue) throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output =
        handleBars
            .compileInline("{{ jsonSort input '$[*].name' order='" + orderValue + "' }}")
            .apply(context);
    assertThat(output, is("[ERROR: order parameter must be 'asc' or 'desc']"));
  }

  @Test
  void orderParameterIsOptionalAndDefaultsToAscending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"},{"id":456,"name":"alice"}]""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    // Should not error - order parameter is optional
    assertThat(output, is("[{\"id\":456,\"name\":\"alice\"},{\"id\":123,\"name\":\"bob\"}]"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"$.name", "$.users.name", "$", "name"})
  void errorsIfJsonPathDoesNotIncludeArrayWildcard(String jsonPath) throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output =
        handleBars.compileInline("{{ jsonSort input '" + jsonPath + "' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: JSONPath must include [*] to specify array location (e.g., '$[*].name' or '$.users[*].name')]"));
  }

  @Test
  void acceptsValidJsonPathWithArrayWildcard() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"},{"id":456,"name":"alice"}]""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].id' }}").apply(context);
    // Should not error - order parameter is optional
    assertThat(output, is("[{\"id\":123,\"name\":\"bob\"},{\"id\":456,\"name\":\"alice\"}]"));
  }

  @Test
  void errorsIfInputJsonIsNotAnArray() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              {"id":123,"name":"bob"}""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is("[ERROR: JSONPath does not reference an array ('$')]"));
  }

  @Test
  void acceptsNestedArrayJsonPath() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            {"users":[{"name":"fred"},{"name":"bob"}]}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$.users[*].name' }}").apply(context);
    // Should not error - valid nested array path
    assertThat(output, is("{\"users\":[{\"name\":\"bob\"},{\"name\":\"fred\"}]}"));
  }

  @Test
  void errorsIfJsonPathExpressionIsInvalid() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].[' }}").apply(context);
    assertThat(output, is("[ERROR: Invalid JSONPath expression ('$[*].[')]"));
  }

  @Test
  void errorsIfJsonPathDoesNotMatchAnyValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // JsonPath $.users[*].name but input has no 'users' field
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output =
        handleBars.compileInline("{{ jsonSort input '$.users[*].name' }}").apply(context);
    assertThat(
        output, is("[ERROR: JSONPath expression did not match any values ('$.users[*].name')]"));
  }

  @Test
  void errorsIfJsonPathDoesNotReferenceAnArray() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Input has 'user' as an object, not an array
    String input = """
            {"user":{"name":"bob"}}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$.user[*].name' }}").apply(context);
    assertThat(output, is("[ERROR: JSONPath does not reference an array ('$.user')]"));
  }

  @Test
  void errorsIfRootIsNotAnArrayWhenUsingRootPath() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Root is an object, not an array
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              {"id":123,"name":"bob"}""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is("[ERROR: JSONPath does not reference an array ('$')]"));
  }

  @Test
  void handlesComplexJsonPathGracefully() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Just verify we don't crash with complex JSON
    String input = """
            {"data":{"nested":{"users":[{"name":"bob"}]}}}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars
            .compileInline("{{ jsonSort input '$.data.nested.users[*].name' }}")
            .apply(context);
    // Should work or give a clear error, not crash
    assertThat(output, is(input));
  }

  @Test
  void returnsEmptyArrayUnchanged() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", "[]");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is("[]"));
  }

  @Test
  void returnsSingleElementArrayUnchanged() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"id":123,"name":"alice"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(input));
  }

  @Test
  void sortsArrayWhenSortFieldIsMissingFromAllObjects() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
      [{"id":3},{"id":1},{"id":2}]""");
    // Missing field returns null for all objects
    // All nulls are equal, so stable sort maintains original order
    String expected = """
      [{"id":3},{"id":1},{"id":2}]""";
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].missingField' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayWithSomeObjectsMissingSortField() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
      [{"id":1,"name":"alice"},{"id":2},{"id":3,"name":"bob"}]""");
    // Objects without 'name' sort first (nulls first is default)
    String expected = """
      [{"id":2},{"id":1,"name":"alice"},{"id":3,"name":"bob"}]""";
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void errorsIfSortFieldIsUnsupportedType() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"data":{"nested":"value"}},{"data":{"other":"value"}}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].data' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: All sort field values must be of the same comparable type (Number, String, or Boolean)]"));
  }

  @Test
  void errorsIfSortFieldHasMixedTypes() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"value":"text"},{"value":123}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].value' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: All sort field values must be of the same comparable type (Number, String, or Boolean)]"));
  }

  @Test
  void errorsIfSortValuesDoNotMatchArraySize() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Input has 2 objects, but nested arrays contain 3 total names
    // $[*].users[*].name will return 3 values for an array of size 2
    String input =
        """
              [{"users":[{"name":"bob"},{"name":"alice"}]},{"users":[{"name":"charlie"}]}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].users[*].name' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: Number of sort values (3) does not match array size (2). JSONPath contains 2 wildcards [*] but only single-level array sorting is supported]"));
  }

  @Test
  void sortsArrayByStringFieldAscending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
            [{"id":456,"name":"bob"},{"id":123,"name":"alice"},{"id":789,"name":"charlie"}]""";
    String expected =
        """
            [{"id":123,"name":"alice"},{"id":456,"name":"bob"},{"id":789,"name":"charlie"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayByNumericFieldAscending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"id":456},{"id":123},{"id":789},{"id":321}]""";
    String expected = """
            [{"id":123},{"id":321},{"id":456},{"id":789}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].id' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayByNumericFieldDescending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"id":123},{"id":456},{"id":789}]""";
    String expected = """
            [{"id":789},{"id":456},{"id":123}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].id' order='desc' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayByBooleanField() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
            [{"active":true},{"active":false},{"active":true}]""";
    String expected = """
            [{"active":false},{"active":true},{"active":true}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].active' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsNestedArrayByField() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
            {"users":[{"name":"charlie"},{"name":"alice"},{"name":"bob"}]}""";
    String expected =
        """
            {"users":[{"name":"alice"},{"name":"bob"},{"name":"charlie"}]}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$.users[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayByDateFieldInIso8601Format() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // ISO 8601 format dates (same as {{now}} helper output)
    String input =
        """
            [{"id":1,"created":"2025-03-15T14:30:00Z"},{"id":2,"created":"2025-01-10T09:15:00Z"},{"id":3,"created":"2025-12-01T18:45:00Z"}]""";
    String expected =
        """
            [{"id":2,"created":"2025-01-10T09:15:00Z"},{"id":1,"created":"2025-03-15T14:30:00Z"},{"id":3,"created":"2025-12-01T18:45:00Z"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].created' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayByDateFieldDescending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // ISO 8601 format dates sorted in descending order (most recent first)
    String input =
        """
            [{"id":1,"created":"2025-03-15T14:30:00Z"},{"id":2,"created":"2025-01-10T09:15:00Z"},{"id":3,"created":"2025-12-01T18:45:00Z"}]""";
    String expected =
        """
            [{"id":3,"created":"2025-12-01T18:45:00Z"},{"id":1,"created":"2025-03-15T14:30:00Z"},{"id":2,"created":"2025-01-10T09:15:00Z"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].created' order='desc' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsComplexObjectsByDateFieldWithMixedDataTypes() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Objects with mixed data types (string, number, boolean) and nested objects
    String input =
        """
            [{"id":1,"name":"alice","active":true,"score":95.5,"created":"2025-06-20T10:00:00Z","metadata":{"department":"engineering","level":3}},{"id":2,"name":"bob","active":false,"score":87.2,"created":"2025-02-15T14:30:00Z","metadata":{"department":"sales","level":2}},{"id":3,"name":"charlie","active":true,"score":92.0,"created":"2025-09-10T08:45:00Z","metadata":{"department":"engineering","level":4}}]""";
    String expected =
        """
            [{"id":2,"name":"bob","active":false,"score":87.2,"created":"2025-02-15T14:30:00Z","metadata":{"department":"sales","level":2}},{"id":1,"name":"alice","active":true,"score":95.5,"created":"2025-06-20T10:00:00Z","metadata":{"department":"engineering","level":3}},{"id":3,"name":"charlie","active":true,"score":92.0,"created":"2025-09-10T08:45:00Z","metadata":{"department":"engineering","level":4}}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].created' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsLargeIntegersCorrectly() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Values beyond double precision: 2^53 + 1, 2^53, 2^53 + 2
    // With the old doubleValue() approach, these would sort INCORRECTLY
    String input =
        """
      [{"id":9007199254740993},{"id":9007199254740992},{"id":9007199254740994}]""";
    String expected =
        """
      [{"id":9007199254740992},{"id":9007199254740993},{"id":9007199254740994}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].id' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsMixedIntegerAndFloatNumbers() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"val":2},{"val":1.5},{"val":1},{"val":2.7}]""";
    String expected = """
      [{"val":1},{"val":1.5},{"val":2},{"val":2.7}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].val' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsVeryLargeNegativeNumbers() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"id":-9007199254740993},{"id":-9007199254740992},{"id":-9007199254740994}]""";
    String expected =
        """
      [{"id":-9007199254740994},{"id":-9007199254740993},{"id":-9007199254740992}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].id' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void maintainsSortStabilityForEqualValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Multiple objects with same name - should maintain original order
    String input =
        """
      [{"name":"alice","id":1},{"name":"alice","id":2},{"name":"alice","id":3}]""";
    String expected =
        """
      [{"name":"alice","id":1},{"name":"alice","id":2},{"name":"alice","id":3}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsUnicodeStringsCorrectly() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"name":"Zoë"},{"name":"Émilie"},{"name":"André"}]""";
    // Sorts by Unicode code point: A(U+0041) < Z(U+005A) < É(U+00C9)
    String expected = """
      [{"name":"André"},{"name":"Zoë"},{"name":"Émilie"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void errorsWhenInputIsJsonNull() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", "null");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    // Should give a clear error that null cannot be sorted
    assertThat(output, is("[ERROR: Cannot sort a JSON null value - input must be a JSON array]"));
  }

  @Test
  void providesHelpfulErrorMessageForMultipleWildcards() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Root array has 2 elements, but $[*].users[*].name returns 3 values (bob, alice, charlie)
    // because of the TWO wildcards flattening nested arrays
    String input =
        """
      [{"users":[{"name":"bob"},{"name":"alice"}]},{"users":[{"name":"charlie"}]}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].users[*].name' }}").apply(context);

    // Should mention multiple wildcards in the error message
    assertThat(
        output,
        is(
            "[ERROR: Number of sort values (3) does not match array size (2). JSONPath contains 2 wildcards [*] but only single-level array sorting is supported]"));
  }

  @Test
  void sortsSimpleArrayOfStrings() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      ["charlie","alice","bob"]""";
    String expected = """
      ["alice","bob","charlie"]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    // Note: $[*] gets the array elements themselves (not a field within objects)
    String output = handleBars.compileInline("{{ jsonSort input '$[*]' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsSimpleArrayOfNumbers() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [456,123,789,321]""";
    String expected = """
      [123,321,456,789]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*]' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsMixedPositiveAndNegativeNumbers() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"val":5},{"val":-3},{"val":0},{"val":-10},{"val":2}]""";
    String expected = """
      [{"val":-10},{"val":-3},{"val":0},{"val":2},{"val":5}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].val' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsStringsByCaseSensitiveUnicodeOrder() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"name":"zebra"},{"name":"Apple"},{"name":"banana"}]""";
    // Uppercase 'A' (U+0041) < lowercase 'b' (U+0062) < lowercase 'z' (U+007A)
    String expected = """
      [{"name":"Apple"},{"name":"banana"},{"name":"zebra"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsStringFieldDescending() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"name":"alice"},{"name":"charlie"},{"name":"bob"}]""";
    String expected = """
      [{"name":"charlie"},{"name":"bob"},{"name":"alice"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].name' order='desc' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsNestedArrayAtSpecificIndex() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"items":[{"price":30},{"price":10},{"price":20}]},{"items":[{"price":100},{"price":50}]}]""";
    // Only sorts items[0], leaves items[1] unchanged
    String expected =
        """
      [{"items":[{"price":10},{"price":20},{"price":30}]},{"items":[{"price":100},{"price":50}]}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[0].items[*].price' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayWithBracketNotationPropertyAccess() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      {"my-key":{"items":[{"id":3},{"id":1},{"id":2}]}}""";
    String expected = """
      {"my-key":{"items":[{"id":1},{"id":2},{"id":3}]}}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[\"my-key\"].items[*].id' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void errorsWhenJsonPathUsesFilterInsteadOfWildcard() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"id":1,"name":"alice"},{"id":2,"name":"bob"},{"id":3,"name":"charlie"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    // Filter expressions are not supported - must use [*] wildcard
    String output =
        handleBars.compileInline("{{ jsonSort input '$[?(@.id > 2)].name' }}").apply(context);

    // Should error because filter syntax [?(...)] is not supported
    assertThat(
        output,
        is(
            "[ERROR: JSONPath must include [*] to specify array location (e.g., '$[*].name' or '$.users[*].name')]"));
  }

  @Test
  void maintainsOrderForNestedFieldAccess() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Verify that nested field access maintains array order
    String input =
        """
      [{"id":1,"data":{"value":"c"}},{"id":2,"data":{"value":"a"}},{"id":3,"data":{"value":"b"}}]""";
    String expected =
        """
      [{"id":2,"data":{"value":"a"}},{"id":3,"data":{"value":"b"}},{"id":1,"data":{"value":"c"}}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].data.value' }}").apply(context);

    // If order is maintained, this should sort correctly
    assertThat(output, is(expected));
  }

  @Test
  void sortsWithNullsFirstByDefault() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"id":5,"name":"charlie"},{"id":2,"name":null},{"id":4,"name":"alice"},{"id":1,"name":null},{"id":3,"name":"bob"}]""";
    // Stable sort: nulls maintain original order (id:2 before id:1)
    // Note: Jackson omits null field values in output
    String expected =
        """
      [{"id":2},{"id":1},{"id":4,"name":"alice"},{"id":3,"name":"bob"},{"id":5,"name":"charlie"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsWithNullsLast() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"id":4,"name":"charlie"},{"id":2,"name":null},{"id":3,"name":"alice"},{"id":1,"name":"bob"}]""";
    // Nulls sort last; note that null 'name' field is omitted in output
    String expected =
        """
      [{"id":3,"name":"alice"},{"id":1,"name":"bob"},{"id":4,"name":"charlie"},{"id":2}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].name' nulls='last' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayWithAllNullValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // With all nulls, objects still have 'id' field to show they exist
    String input = """
      [{"id":3,"name":null},{"id":1,"name":null},{"id":2,"name":null}]""";
    // Objects maintain their other fields even though 'name' is null
    String expected = """
      [{"id":3},{"id":1},{"id":2}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsNumbersWithNullsFirst() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input =
        """
      [{"id":"a","score":100},{"id":"b","score":null},{"id":"c","score":50},{"id":"d","score":null}]""";
    String expected =
        """
      [{"id":"b"},{"id":"d"},{"id":"c","score":50},{"id":"a","score":100}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].score' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void errorsIfNullsParameterIsInvalid() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
      [{"name":"alice"}]""");
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].name' nulls='middle' }}").apply(context);
    assertThat(output, is("[ERROR: nulls parameter must be 'first' or 'last']"));
  }

  @Test
  void sortsStringsWithEmoji() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Emoji sorted by UTF-16 code units:
    // 🎉(U+1F389) < 💻(U+1F4BB) < 😀(U+1F600) < 🚀(U+1F680)
    String input =
        """
      [{"name":"😀 smile"},{"name":"🎉 party"},{"name":"💻 laptop"},{"name":"🚀 rocket"}]""";
    String expected =
        """
      [{"name":"🎉 party"},{"name":"💻 laptop"},{"name":"😀 smile"},{"name":"🚀 rocket"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsMixedAsciiAccentedAndEmoji() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Java String comparison: ASCII (a,b) < accented (Á,É) < emoji (😀)
    // Order: a(U+0061) < b(U+0062) < Á(U+00C1) < É(U+00C9) < 😀(U+1F600)
    String input =
        """
      [{"name":"😀"},{"name":"Álvaro"},{"name":"alice"},{"name":"Élodie"},{"name":"bob"}]""";
    String expected =
        """
      [{"name":"alice"},{"name":"bob"},{"name":"Álvaro"},{"name":"Élodie"},{"name":"😀"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsComplexEmoji() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Flag emoji use regional indicator symbols (U+1F1E6-1F1FF)
    // 🇫🇷: U+1F1EB,U+1F1F7  🇬🇧: U+1F1EC,U+1F1E7  🇯🇵: U+1F1EF,U+1F1F5  🇺🇸: U+1F1FA,U+1F1F8
    String input = """
      [{"flag":"🇬🇧"},{"flag":"🇺🇸"},{"flag":"🇯🇵"},{"flag":"🇫🇷"}]""";
    String expected =
        """
      [{"flag":"🇫🇷"},{"flag":"🇬🇧"},{"flag":"🇯🇵"},{"flag":"🇺🇸"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].flag' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsRightToLeftText() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Arabic (U+0600-06FF), Hebrew (U+0590-05FF), ASCII (U+0041-007A)
    // Order by first code point: hello(h=U+0068) < שלום(ש=U+05E9) < مرحبا(م=U+0645)
    String input = """
      [{"name":"مرحبا"},{"name":"שלום"},{"name":"hello"}]""";
    String expected = """
      [{"name":"hello"},{"name":"שלום"},{"name":"مرحبا"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsArrayWithDuplicateValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // "bob" appears twice (id:1 and id:3), should maintain the original order
    String input =
        """
      [{"id":1,"name":"bob"},{"id":2,"name":"alice"},{"id":3,"name":"bob"},{"id":4,"name":"charlie"}]""";
    // Stable sort: alice, bob(id:1), bob(id:3), charlie
    String expected =
        """
      [{"id":2,"name":"alice"},{"id":1,"name":"bob"},{"id":3,"name":"bob"},{"id":4,"name":"charlie"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(output, is(expected));
  }

  @Test
  void sortsNumbersWithDuplicateValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    // Score 50 appears three times
    String input =
        """
      [{"id":"a","score":100},{"id":"b","score":50},{"id":"c","score":50},{"id":"d","score":25},{"id":"e","score":50}]""";
    // Stable sort: 25, 50(b), 50(c), 50(e), 100
    String expected =
        """
      [{"id":"d","score":25},{"id":"b","score":50},{"id":"c","score":50},{"id":"e","score":50},{"id":"a","score":100}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].score' }}").apply(context);
    assertThat(output, is(expected));
  }

  private Handlebars getHandlebarsWithJsonSort() {
    return new Handlebars()
        .with(EscapingStrategy.NOOP)
        .registerHelper("jsonSort", new JsonSortHelper());
  }
}
