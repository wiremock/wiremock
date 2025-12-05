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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
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
              [{"id":123,"name":"bob"}]""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    // Should not error - order parameter is optional
    assertThat(output, not(containsString("ERROR")));
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
              [{"id":123,"name":"bob"}]""");
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    // Should not error about missing [*]
    assertThat(output, not(containsString("must include [*]")));
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
            {"users":[{"name":"bob"}]}""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output =
        handleBars.compileInline("{{ jsonSort input '$.users[*].name' }}").apply(context);
    // Should not error - valid nested array path
    assertThat(output, not(containsString("ERROR")));
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
  void errorsIfSortFieldIsMissingFromObjects() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    Map<String, String> context = new HashMap<>();
    context.put("input", """
              [{"id":123,"name":"bob"}]""");
    String output =
        handleBars.compileInline("{{ jsonSort input '$[*].missingField' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: All objects in the array must have the sort field specified by JSONPath expression ('$[*].missingField')]"));
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
    assertThat(output, containsString("same comparable type"));
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
    assertThat(output, containsString("Number of sort values"));
    assertThat(output, containsString("does not match array size"));
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
  void errorsIfArrayContainsNullSortValues() throws IOException {
    Handlebars handleBars = getHandlebarsWithJsonSort();
    String input = """
      [{"name":null},{"name":"bob"}]""";
    Map<String, String> context = new HashMap<>();
    context.put("input", input);
    String output = handleBars.compileInline("{{ jsonSort input '$[*].name' }}").apply(context);
    assertThat(
        output,
        is(
            "[ERROR: All objects in the array must have the sort field specified by JSONPath expression ('$[*].name')]"));
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

  private Handlebars getHandlebarsWithJsonSort() {
    return new Handlebars()
        .with(EscapingStrategy.NOOP)
        .registerHelper("jsonSort", new JsonSortHelper());
  }
}
