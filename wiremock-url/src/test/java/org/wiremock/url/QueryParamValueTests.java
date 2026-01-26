/*
 * Copyright (C) 2026 Thomas Akehurst
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

class QueryParamValueTests {

  @Nested
  class Parse {

    static final List<String> validValues =
        List.of(
            // Empty and simple values
            "",
            "value",
            "123",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "value-name",
            "value.name",
            "value_name",
            "value~name",
            "Value123",
            "test-value_123.name~test",

            // Sub-delimiters (!$'()*+,;=) - note & is not allowed in values
            "value!name",
            "value$name",
            "value'name",
            "value(name)",
            "value*name",
            "value+name",
            "value,name",
            "value;name",
            "value=name",
            "a=b=c",

            // Colon and at-sign
            "time:12:30:00",
            "user@example.com",
            "http://example.com",
            "/api/v1/users",

            // Forward slash and question mark
            "/path/to/resource",
            "what?when?where",
            "/search?q=test",

            // Percent-encoded characters
            "%20",
            "search%20term",
            "%2Fapi%2Fv1",
            "%C3%A9ric",
            "caf%C3%A9",
            "%25",

            // Characters that extend beyond RFC 3986
            "{value}",
            "[1,2,3]",
            "<value>",
            "val|ue",
            "val\\ue",
            "val^ue",
            "val`ue",

            // Spaces and special characters (permissive)
            "search term",
            "na[me]",
            "<value>",

            // Complex combinations
            "http://example.com:8080/path?q=test",
            "%7B%22key%22:%22value%22%7D",
            "jQuery.ajax",

            // No separators
            "justtext",
            "noseparators123",

            // Invalid percent encoding (still accepted - permissive parser)
            "%",
            "%2",
            "%GG",
            "%ZZvalue");

    @ParameterizedTest
    @FieldSource("validValues")
    void parses_valid_values(String valueString) {
      QueryParamValue value = QueryParamValue.parse(valueString);
      assertThat(value.toString()).isEqualTo(valueString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"value#fragment", "test#test", "#", "value#"})
    void rejects_values_with_hash(String illegalValue) {
      assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> QueryParamValue.parse(illegalValue))
          .withMessage("Illegal segment: `" + illegalValue + "`")
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"value&other", "test&test", "&", "value&"})
    void rejects_values_with_ampersand(String illegalValue) {
      assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> QueryParamValue.parse(illegalValue))
          .withMessage("Illegal segment: `" + illegalValue + "`")
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalValue);
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          QueryParamValueParser.INSTANCE, validValues);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<QueryParamValue>> normalisationCases =
        Stream.of(
                // Characters that need encoding
                Pair.of("search term", "search%20term"),
                Pair.of("hello world", "hello%20world"),
                Pair.of("test\"quote", "test%22quote"),
                Pair.of("test<tag>", "test%3Ctag%3E"),
                Pair.of("test`backtick", "test%60backtick"),
                Pair.of("data{value}", "data%7Bvalue%7D"),
                Pair.of("café", "caf%C3%A9"),
                Pair.of("héllo", "h%C3%A9llo"),
                // Percent encoding normalisation
                Pair.of("%ff", "%FF"),
                Pair.of("%fF", "%FF"),
                Pair.of("%Ff", "%FF"),
                Pair.of("%41", "A"),
                Pair.of("%5A", "Z"),
                Pair.of("%5a", "Z"),
                // Reserved characters that should be decoded in values
                Pair.of("c%3D2%263=4", "c=2%263=4"))
            .map(
                testCase ->
                    new NormalisationCase<>(
                        QueryParamValue.parse(testCase.getLeft()),
                        QueryParamValue.parse(testCase.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_value_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases);
    }

    static final List<String> alreadyNormalisedValues =
        List.of(
            "",
            "value",
            "value-name",
            "value_name.test~123",
            "value!name",
            "time:12:30",
            "/api/v1",
            "search%20term",
            "caf%C3%A9",
            "test%22quote",
            "c=2%263=4");

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedValues.stream().map(QueryParamValue::parse).toList());
    }
  }

  @Nested
  class Codec {

    static final List<String> valuesWithoutPercentEncoding =
        List.of("", "value", "test-value", "value_name.test", "time:12:30", "/api/v1");

    @ParameterizedTest
    @FieldSource("valuesWithoutPercentEncoding")
    void returns_same_string_for_value_without_percent_encoding(String valueString) {
      QueryParamValue value = QueryParamValue.parse(valueString);
      assertThat(value.decode()).isEqualTo(valueString);
    }

    static final List<CodecCase> encodeCases =
        List.of(
            new CodecCase("search%20term", "search term"),
            new CodecCase("caf%C3%A9", "café"),
            new CodecCase("%C3%A9ric", "éric"),
            new CodecCase("100%25", "100%"),
            new CodecCase("%7B%22key%22:%22value%22%7D", "{\"key\":\"value\"}"),
            new CodecCase("hello%20world!", "hello world!"),
            new CodecCase("test%3Ctag%3E", "test<tag>"),
            new CodecCase("test%60backtick", "test`backtick"),
            new CodecCase("c=2%263=4", "c=2&3=4"));

    @ParameterizedTest
    @FieldSource("encodeCases")
    void encodes_correctly(CodecCase testCase) {
      var encoded = QueryParamValue.encode(testCase.decoded());
      assertThat(encoded.toString()).isEqualTo(testCase.encoded());
      assertThat(encoded.decode()).isEqualTo(testCase.decoded());
    }

    static final List<CodecCase> decodeCases =
        List.of(
            new CodecCase("search%20term", "search term"),
            new CodecCase("caf%C3%A9", "café"),
            new CodecCase("%C3%A9ric", "éric"),
            new CodecCase("100%25", "100%"),
            new CodecCase("%7B%22key%22:%22value%22%7D", "{\"key\":\"value\"}"),
            new CodecCase("hello%20world%21", "hello world!"),
            new CodecCase("c%3D2%263=4", "c=2&3=4"));

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_correctly(CodecCase testCase) {
      var value = QueryParamValue.parse(testCase.encoded());
      assertThat(value.decode()).isEqualTo(testCase.decoded());
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      var decoded =
          Stream.concat(decodeCases.stream(), encodeCases.stream())
              .map(CodecCase::decoded)
              .collect(Collectors.toSet())
              .stream()
              .sorted();

      return generateEncodeDecodeInvariantTests(QueryParamValueParser.INSTANCE, decoded);
    }
  }

  @Nested
  class Equality {

    @Test
    void values_with_same_content_are_equal() {
      QueryParamValue value1 = QueryParamValue.parse("test");
      QueryParamValue value2 = QueryParamValue.parse("test");
      assertThat(value1).isEqualTo(value2);
    }

    @Test
    void values_with_different_content_are_not_equal() {
      QueryParamValue value1 = QueryParamValue.parse("test1");
      QueryParamValue value2 = QueryParamValue.parse("test2");
      assertThat(value1).isNotEqualTo(value2);
    }

    @Test
    void values_with_different_case_are_not_equal() {
      QueryParamValue value1 = QueryParamValue.parse("test");
      QueryParamValue value2 = QueryParamValue.parse("TEST");
      assertThat(value1).isNotEqualTo(value2);
    }

    @Test
    void value_is_equal_to_itself() {
      QueryParamValue value = QueryParamValue.parse("test");
      assertThat(value).isEqualTo(value);
    }

    @Test
    void value_is_not_equal_to_null() {
      QueryParamValue value = QueryParamValue.parse("test");
      assertThat(value).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void value_is_not_equal_to_different_type() {
      QueryParamValue value = QueryParamValue.parse("test");
      assertThat(value).isNotEqualTo("test");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_values_have_same_hash_code() {
      QueryParamValue value1 = QueryParamValue.parse("test");
      QueryParamValue value2 = QueryParamValue.parse("test");
      assertThat(value1.hashCode()).isEqualTo(value2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      QueryParamValue value = QueryParamValue.parse("test-value");
      int hashCode1 = value.hashCode();
      int hashCode2 = value.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_value() {
      String valueString = "test-value";
      QueryParamValue value = QueryParamValue.parse(valueString);
      assertThat(value.toString()).isEqualTo(valueString);
    }

    @Test
    void to_string_preserves_case() {
      String valueString = "Test-Value";
      QueryParamValue value = QueryParamValue.parse(valueString);
      assertThat(value.toString()).isEqualTo(valueString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "search%20term";
      QueryParamValue value = QueryParamValue.parse(encoded);
      assertThat(value.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      QueryParamValue original = QueryParamValue.parse("test/value");
      String stringForm = original.toString();
      QueryParamValue parsed = QueryParamValue.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @Nested
  class LengthAndEmpty {

    @Test
    void length_returns_string_length() {
      QueryParamValue value = QueryParamValue.parse("test");
      assertThat(value.length()).isEqualTo(4);
    }

    @Test
    void length_includes_percent_encoding() {
      QueryParamValue value = QueryParamValue.parse("a%20b");
      assertThat(value.length()).isEqualTo(5);
    }

    @Test
    void is_empty_returns_true_for_empty_value() {
      QueryParamValue value = QueryParamValue.parse("");
      assertThat(value.isEmpty()).isTrue();
    }

    @Test
    void is_empty_returns_false_for_non_empty_value() {
      QueryParamValue value = QueryParamValue.parse("test");
      assertThat(value.isEmpty()).isFalse();
    }
  }

  @Nested
  class Initialisation extends AbstractEncodableInitialisationTests {
    Initialisation() {
      super(
          "org.wiremock.url.QueryParamValue", EMPTY, "org.wiremock.url.QueryParamValueParser", "");
    }
  }
}
