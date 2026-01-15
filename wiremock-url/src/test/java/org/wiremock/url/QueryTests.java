/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.wiremock.url.PercentEncodedStringParserInvariantTests.generateEncodeDecodeInvariantTests;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class QueryTests {

  @Nested
  class Parse {

    static final List<String> validQueries =
        List.of(
            // Empty and simple queries
            "",
            "q=search",
            "key=value",
            "name=John",
            "id=123",

            // Multiple parameters
            "q=search&page=1",
            "name=John&age=30&city=NYC",
            "a=1&b=2&c=3",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "query-name=value",
            "query.name=value",
            "query_name=value",
            "query~name=value",
            "Query123=Value456",
            "test-param_123.name~test=result",

            // Sub-delimiters (!$&'()*+,;=)
            "query!name=value",
            "query$name=value",
            "key&key2=value",
            "query'name=value",
            "query(name)=value",
            "query*name=value",
            "query+name=value",
            "query,name=value",
            "query;name=value",
            "key=val=ue",

            // Colon and at-sign
            "time=12:30:00",
            "email=user@example.com",
            "url=http://example.com",
            "path=/api/v1/users",

            // Forward slash and question mark
            "path=/path/to/resource",
            "query=what?when?where",
            "url=/search?q=test",
            "nested=a=b?c=d",

            // Percent-encoded characters
            "%20=value", // space key
            "query=%20", // space value
            "q=search%20term", // search term
            "path=%2Fapi%2Fv1", // /api/v1
            "name=%C3%A9ric", // éric
            "caf%C3%A9=coffee", // café=coffee
            "percent=%25", // %

            // Empty values
            "key=",
            "key1=&key2=",
            "=value",
            "=",

            // Characters that extend beyond RFC 3986
            "key={value}",
            "data=[1,2,3]",
            "path=<value>",
            "pipe=val|ue",
            "back=val\\ue",
            "caret=val^ue",
            "grave=val`ue",

            // Spaces and special characters (permissive)
            "q=search term", // unencoded space
            "na[me]=value", // brackets
            "key=<value>", // angle brackets

            // Complex combinations
            "q=test&page=1&limit=10",
            "filter=name:John&sort=date:desc",
            "url=http://example.com:8080/path?q=test",
            "data=%7B%22key%22:%22value%22%7D", // {"key":"value"}
            "callback=jQuery.ajax&_=1234567890",

            // No separators
            "justtext",
            "noseparators123",

            // Edge cases
            "?nested=question",
            "key=value&&&",
            "===",
            "&&&",

            // Invalid percent encoding (still accepted - permissive parser)
            "q=%", // incomplete
            "key=%2", // incomplete
            "val=%GG", // invalid hex
            "query=%ZZvalue"); // invalid hex

    @ParameterizedTest
    @FieldSource("validQueries")
    void parses_valid_queries(String queryString) {
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=val#ue", "query#fragment", "#", "test#test"})
    void rejects_queries_with_hash(String illegalQuery) {
      assertThatExceptionOfType(IllegalQuery.class)
          .isThrownBy(() -> Query.parse(illegalQuery))
          .withMessage("Illegal query: `" + illegalQuery + "`")
          .extracting(IllegalQuery::getIllegalValue)
          .isEqualTo(illegalQuery);
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          QueryParser.INSTANCE, Parse.validQueries);
    }
  }

  @Nested
  class Normalise {

    record NormalisationCase(String input, String expected) {}

    static final List<NormalisationCase> normalisationCases =
        List.of(
            new NormalisationCase("q=search term", "q=search%20term"),
            new NormalisationCase("key=value test", "key=value%20test"),
            new NormalisationCase("q=test\"quote", "q=test%22quote"),
            new NormalisationCase("data={value}", "data=%7Bvalue%7D"),
            new NormalisationCase("q=test<tag>", "q=test%3Ctag%3E"),
            new NormalisationCase("name=café", "name=caf%C3%A9"),
            new NormalisationCase("%ff", "%FF"),
            new NormalisationCase("%fF", "%FF"),
            new NormalisationCase("%Ff", "%FF"),
            new NormalisationCase("%41", "A"),
            new NormalisationCase("%5A", "Z"),
            new NormalisationCase("%5a", "Z"),
            new NormalisationCase("key=}value{", "key=%7Dvalue%7B"));

    static final List<String> alreadyNormalisedQueries =
        List.of(
            "",
            "q=search",
            "key=value",
            "a=1&b=2",
            "query-name=value",
            "time=12:30:00",
            "path=/api/v1",
            "q=search%20term",
            "name=%C3%A9ric",
            "q=test'quote");

    @TestFactory
    Stream<DynamicTest> normalises_query_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream()
              .map(
                  testCase ->
                      new NormalisableInvariantTests.NormalisationCase<>(
                          Query.parse(testCase.input()), Query.parse(testCase.expected())))
              .toList());
    }

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedQueries.stream().map(Query::parse).toList());
    }
  }

  @Nested
  class Codec {

    static final List<String> queriesWithoutPercentEncoding =
        List.of("", "q=search", "key=value", "a=1&b=2", "time=12:30:00", "path=/api/v1");

    static final List<CodecCase> decodeCases =
        List.of(
            new CodecCase("q=search%20term", "q=search term"),
            new CodecCase("name=%C3%A9ric", "name=éric"),
            new CodecCase("caf%C3%A9=coffee", "café=coffee"),
            new CodecCase("percent=%25", "percent=%"),
            new CodecCase("path=%2Fapi%2Fv1", "path=/api/v1"),
            new CodecCase("data=%7B%22key%22:%22value%22%7D", "data={\"key\":\"value\"}"),
            new CodecCase("query=%20", "query= "),
            new CodecCase("%20=value", " =value"),
            new CodecCase("q=hello%20world%21", "q=hello world!"));

    @ParameterizedTest
    @FieldSource("queriesWithoutPercentEncoding")
    void returns_same_string_for_query_without_percent_encoding(String queryString) {
      Query query = Query.parse(queryString);
      assertThat(query.decode()).isEqualTo(queryString);
    }

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_percent_encoded_query_correctly(CodecCase testCase) {
      Query query = Query.parse(testCase.encoded());
      assertThat(query.decode()).isEqualTo(testCase.decoded());
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      return generateEncodeDecodeInvariantTests(
          QueryParser.INSTANCE,
          Stream.of(
              "foo",
              "bar",
              "key=value",
              "hello world",
              "q=café",
              "param1=value1&param2=value2",
              "こんにちは"));
    }
  }

  @Nested
  class Equality {

    @Test
    void queries_with_same_value_are_equal() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("q=search");
      assertThat(query1).isEqualTo(query2);
    }

    @Test
    void queries_with_different_values_are_not_equal() {
      Query query1 = Query.parse("q=search1");
      Query query2 = Query.parse("q=search2");
      assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void queries_with_different_case_are_not_equal() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("Q=SEARCH");
      assertThat(query1).isNotEqualTo(query2);
    }

    @Test
    void query_is_equal_to_itself() {
      Query query = Query.parse("q=search");
      assertThat(query).isEqualTo(query);
    }

    @Test
    void query_is_not_equal_to_null() {
      Query query = Query.parse("q=search");
      assertThat(query).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void query_is_not_equal_to_different_type() {
      Query query = Query.parse("q=search");
      assertThat(query).isNotEqualTo("q=search");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_queries_have_same_hash_code() {
      Query query1 = Query.parse("q=search");
      Query query2 = Query.parse("q=search");
      assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Query query = Query.parse("q=search&page=1");
      int hashCode1 = query.hashCode();
      int hashCode2 = query.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_query() {
      String queryString = "q=search&page=1";
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @Test
    void to_string_preserves_case() {
      String queryString = "Query=Search";
      Query query = Query.parse(queryString);
      assertThat(query.toString()).isEqualTo(queryString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "q=search%20term";
      Query query = Query.parse(encoded);
      assertThat(query.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Query original = Query.parse("q=test&page=1&limit=10");
      String stringForm = original.toString();
      Query parsed = Query.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }
}
