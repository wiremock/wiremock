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

class QueryParamKeyTests {

  @Nested
  class Parse {

    static final List<String> validKeys =
        List.of(
            // Empty and simple keys
            "",
            "key",
            "123",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "key-name",
            "key.name",
            "key_name",
            "key~name",
            "Key123",
            "test-key_123.name~test",

            // Sub-delimiters (!$'()*+,;) - note & and = are not allowed in keys
            "key!name",
            "key$name",
            "key'name",
            "key(name)",
            "key*name",
            "key+name",
            "key,name",
            "key;name",

            // Colon and at-sign
            "time:12:30:00",
            "user@example.com",
            "http://example.com",
            "/api/v1/users",

            // Forward slash and question mark
            "/path/to/resource",
            "what?when?where",
            "/search?q",

            // Percent-encoded characters
            "%20",
            "search%20term",
            "%2Fapi%2Fv1",
            "%C3%A9ric",
            "caf%C3%A9",
            "%25",
            "%3D", // encoded =
            "key%3Dname",

            // Characters that extend beyond RFC 3986
            "{key}",
            "[1,2,3]",
            "<key>",
            "key|name",
            "key\\name",
            "key^name",
            "key`name",

            // Spaces and special characters (permissive)
            "search term",
            "na[me]",
            "<key>",

            // Complex combinations
            "http://example.com:8080/path?q",
            "%7B%22key%22:%22value%22%7D",
            "jQuery.ajax",

            // No separators
            "justtext",
            "noseparators123",

            // Invalid percent encoding (still accepted - permissive parser)
            "%",
            "%2",
            "%GG",
            "%ZZkey");

    @ParameterizedTest
    @FieldSource("validKeys")
    void parses_valid_keys(String keyString) {
      QueryParamKey key = QueryParamKey.parse(keyString);
      assertThat(key.toString()).isEqualTo(keyString);
    }

    @ParameterizedTest
    @ValueSource(strings = {"key#fragment", "test#test", "#", "key#"})
    void rejects_keys_with_hash(String illegalKey) {
      assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> QueryParamKey.parse(illegalKey))
          .withMessage("Illegal segment: `" + illegalKey + "`")
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalKey);
    }

    @ParameterizedTest
    @ValueSource(strings = {"key&other", "test&test", "&", "key&"})
    void rejects_keys_with_ampersand(String illegalKey) {
      assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> QueryParamKey.parse(illegalKey))
          .withMessage("Illegal segment: `" + illegalKey + "`")
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalKey);
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=value", "test=test", "=", "key="})
    void rejects_keys_with_equals(String illegalKey) {
      assertThatExceptionOfType(IllegalSegment.class)
          .isThrownBy(() -> QueryParamKey.parse(illegalKey))
          .withMessage("Illegal segment: `" + illegalKey + "`")
          .extracting(IllegalSegment::getIllegalValue)
          .isEqualTo(illegalKey);
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          QueryParamKeyParser.INSTANCE, validKeys);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<QueryParamKey>> normalisationCases =
        Stream.of(
                // Characters that need encoding
                Pair.of("search term", "search%20term"),
                Pair.of("hello world", "hello%20world"),
                Pair.of("test\"quote", "test%22quote"),
                Pair.of("test<tag>", "test%3Ctag%3E"),
                Pair.of("test`backtick", "test%60backtick"),
                Pair.of("data{key}", "data%7Bkey%7D"),
                Pair.of("café", "caf%C3%A9"),
                Pair.of("héllo", "h%C3%A9llo"),
                // Percent encoding normalisation
                Pair.of("%ff", "%FF"),
                Pair.of("%fF", "%FF"),
                Pair.of("%Ff", "%FF"),
                Pair.of("%41", "A"),
                Pair.of("%5A", "Z"),
                Pair.of("%5a", "Z"))
            .map(
                testCase ->
                    new NormalisationCase<>(
                        QueryParamKey.parse(testCase.getLeft()),
                        QueryParamKey.parse(testCase.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_key_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases);
    }

    static final List<String> alreadyNormalisedKeys =
        List.of(
            "",
            "key",
            "key-name",
            "key_name.test~123",
            "key!name",
            "time:12:30",
            "/api/v1",
            "search%20term",
            "caf%C3%A9",
            "test%22quote");

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedKeys.stream().map(QueryParamKey::parse).toList());
    }
  }

  @Nested
  class Codec {

    static final List<String> keysWithoutPercentEncoding =
        List.of("", "key", "test-key", "key_name.test", "time:12:30", "/api/v1");

    @ParameterizedTest
    @FieldSource("keysWithoutPercentEncoding")
    void returns_same_string_for_key_without_percent_encoding(String keyString) {
      QueryParamKey key = QueryParamKey.parse(keyString);
      assertThat(key.decode()).isEqualTo(keyString);
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
            new CodecCase("key%3Dvalue", "key=value"),
            new CodecCase("a%26b", "a&b"));

    @ParameterizedTest
    @FieldSource("encodeCases")
    void encodes_correctly(CodecCase testCase) {
      var encoded = QueryParamKey.encode(testCase.decoded());
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
            new CodecCase("key%3Dvalue", "key=value"),
            new CodecCase("a%26b", "a&b"));

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_correctly(CodecCase testCase) {
      var key = QueryParamKey.parse(testCase.encoded());
      assertThat(key.decode()).isEqualTo(testCase.decoded());
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      var decoded =
          Stream.concat(decodeCases.stream(), encodeCases.stream())
              .map(CodecCase::decoded)
              .collect(Collectors.toSet())
              .stream()
              .sorted();

      return generateEncodeDecodeInvariantTests(QueryParamKeyParser.INSTANCE, decoded);
    }
  }

  @Nested
  class Equality {

    @Test
    void keys_with_same_content_are_equal() {
      QueryParamKey key1 = QueryParamKey.parse("test");
      QueryParamKey key2 = QueryParamKey.parse("test");
      assertThat(key1).isEqualTo(key2);
    }

    @Test
    void keys_with_different_content_are_not_equal() {
      QueryParamKey key1 = QueryParamKey.parse("test1");
      QueryParamKey key2 = QueryParamKey.parse("test2");
      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void keys_with_different_case_are_not_equal() {
      QueryParamKey key1 = QueryParamKey.parse("test");
      QueryParamKey key2 = QueryParamKey.parse("TEST");
      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    void key_is_equal_to_itself() {
      QueryParamKey key = QueryParamKey.parse("test");
      assertThat(key).isEqualTo(key);
    }

    @Test
    void key_is_not_equal_to_null() {
      QueryParamKey key = QueryParamKey.parse("test");
      assertThat(key).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void key_is_not_equal_to_different_type() {
      QueryParamKey key = QueryParamKey.parse("test");
      assertThat(key).isNotEqualTo("test");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_keys_have_same_hash_code() {
      QueryParamKey key1 = QueryParamKey.parse("test");
      QueryParamKey key2 = QueryParamKey.parse("test");
      assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      QueryParamKey key = QueryParamKey.parse("test-key");
      int hashCode1 = key.hashCode();
      int hashCode2 = key.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_key() {
      String keyString = "test-key";
      QueryParamKey key = QueryParamKey.parse(keyString);
      assertThat(key.toString()).isEqualTo(keyString);
    }

    @Test
    void to_string_preserves_case() {
      String keyString = "Test-Key";
      QueryParamKey key = QueryParamKey.parse(keyString);
      assertThat(key.toString()).isEqualTo(keyString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "search%20term";
      QueryParamKey key = QueryParamKey.parse(encoded);
      assertThat(key.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      QueryParamKey original = QueryParamKey.parse("test/key");
      String stringForm = original.toString();
      QueryParamKey parsed = QueryParamKey.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @Nested
  class LengthAndEmpty {

    @Test
    void length_returns_string_length() {
      QueryParamKey key = QueryParamKey.parse("test");
      assertThat(key.length()).isEqualTo(4);
    }

    @Test
    void length_includes_percent_encoding() {
      QueryParamKey key = QueryParamKey.parse("a%20b");
      assertThat(key.length()).isEqualTo(5);
    }

    @Test
    void is_empty_returns_true_for_empty_key() {
      QueryParamKey key = QueryParamKey.parse("");
      assertThat(key.isEmpty()).isTrue();
    }

    @Test
    void is_empty_returns_false_for_non_empty_key() {
      QueryParamKey key = QueryParamKey.parse("test");
      assertThat(key.isEmpty()).isFalse();
    }
  }
}
