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
package org.wiremock.url;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class FragmentTests {

  @Nested
  class ParseMethod {

    static final List<String> validFragments =
        List.of(
            // Empty and simple fragments
            "",
            "section",
            "top",
            "introduction",
            "chapter-1",
            "heading123",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "section-name",
            "section.name",
            "section_name",
            "section~name",
            "Section123",
            "test-section_123.name~test",

            // Sub-delimiters (!$&'()*+,;=)
            "section!name",
            "section$name",
            "section&name",
            "section'name",
            "section(name)",
            "section*name",
            "section+name",
            "section,name",
            "section;name",
            "section=name",

            // Colon and at-sign
            "time:12:30",
            "user@example",
            "ref:v1.2.3",
            "id:123",

            // Forward slash and question mark
            "path/to/section",
            "section?detail",
            "part/1?view=full",
            "nested/section/subsection",

            // Percent-encoded characters
            "%20", // space
            "section%20name", // section name
            "caf%C3%A9", // café
            "%C3%A9ric", // éric
            "100%25", // 100%
            "path%2Fsection", // path/section

            // Characters that extend beyond RFC 3986
            "section{name}",
            "data[123]",
            "tag<value>",
            "section|name",
            "back\\slash",
            "caret^name",
            "grave`name",

            // Spaces and special characters (permissive)
            "section name", // unencoded space
            "section#nested", // hash
            "section[1]", // brackets
            "section<name>", // angle brackets

            // Complex combinations
            "api/v1/users/123",
            "section:subsection:detail",
            "user@domain.com/profile",
            "heading-1.2.3?expanded=true",
            "doc%20section/page%202",

            // IDs and references
            "L123",
            "line-456",
            "ref123",
            "footnote1",

            // Edge cases
            "//double//slashes",
            "trailing/slash/",
            "multiple?question?marks",
            "dots...",
            "dashes---",

            // JSON-like fragments (percent-encoded and unencoded)
            "%7B%22key%22:%22value%22%7D", // {"key":"value"}
            "data=%7B%7D", // data={}
            "data={}",

            // No separators
            "justonefragment",
            "noseparators123",

            // Invalid percent encoding (still accepted - permissive parser)
            "%", // incomplete
            "%2", // incomplete
            "%GG", // invalid hex
            "section%ZZname"); // invalid hex

    @ParameterizedTest
    @FieldSource("validFragments")
    void parses_valid_fragments(String fragmentString) {
      Fragment fragment = Fragment.parse(fragmentString);
      assertThat(fragment.toString()).isEqualTo(fragmentString);
    }
  }

  @Nested
  class NormaliseMethod {

    record NormalisationCase(String input, String expected) {}

    static final List<NormalisationCase> normalisationCases =
        List.of(
            // Characters that need encoding
            new NormalisationCase("section name", "section%20name"),
            new NormalisationCase("hello world", "hello%20world"),
            new NormalisationCase("test\"quote", "test%22quote"),
            new NormalisationCase("test<tag>", "test%3Ctag%3E"),
            new NormalisationCase("test`backtick", "test%60backtick"),
            new NormalisationCase("data{value}", "data%7Bvalue%7D"),
            new NormalisationCase("test{name}", "test%7Bname%7D"),
            new NormalisationCase("café", "caf%C3%A9"),
            new NormalisationCase("héllo", "h%C3%A9llo"),
            new NormalisationCase("%ff", "%FF"),
            new NormalisationCase("%fF", "%FF"),
            new NormalisationCase("%Ff", "%FF"),
            new NormalisationCase("%41", "A"),
            new NormalisationCase("%5A", "Z"),
            new NormalisationCase("%5a", "Z"),
            new NormalisationCase("\u0001control", "%01control"));

    static final List<String> alreadyNormalisedFragments =
        List.of(
            "",
            "section",
            "section-name",
            "section_name.test~123",
            "section!name",
            "path/to/section",
            "time:12:30",
            "section?detail",
            "section%20name",
            "caf%C3%A9",
            "test%22quote");

    @TestFactory
    Stream<DynamicTest> normalises_fragment_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().map(testCase -> new NormalisableInvariantTests.NormalisationCase<>(
              Fragment.parse(testCase.input()),
              Fragment.parse(testCase.expected())
          )).toList()
      );
    }

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedFragments.stream().map(Fragment::parse).toList()
      );
    }
  }

  @Nested
  class DecodeMethod {

    record DecodeCase(String input, String expected) {}

    static final List<String> fragmentsWithoutPercentEncoding =
        List.of(
            "",
            "section",
            "chapter-1",
            "section-name.test_123",
            "path/to/section",
            "section?detail",
            "time:12:30");

    static final List<DecodeCase> decodeCases =
        List.of(
            new DecodeCase("section%20name", "section name"),
            new DecodeCase("caf%C3%A9", "café"),
            new DecodeCase("%C3%A9ric", "éric"),
            new DecodeCase("100%25", "100%"),
            new DecodeCase("path%2Fsection", "path/section"),
            new DecodeCase("%7B%22key%22:%22value%22%7D", "{\"key\":\"value\"}"),
            new DecodeCase("hello%20world%21", "hello world!"),
            new DecodeCase("test%3Ctag%3E", "test<tag>"),
            new DecodeCase("test%60backtick", "test`backtick"));

    @ParameterizedTest
    @FieldSource("fragmentsWithoutPercentEncoding")
    void returns_same_string_for_fragment_without_percent_encoding(String fragmentString) {
      Fragment fragment = Fragment.parse(fragmentString);
      assertThat(fragment.decode()).isEqualTo(fragmentString);
    }

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_percent_encoded_fragment_correctly(DecodeCase testCase) {
      Fragment fragment = Fragment.parse(testCase.input());
      assertThat(fragment.decode()).isEqualTo(testCase.expected());
    }
  }

  @Nested
  class Equality {

    @Test
    void fragments_with_same_value_are_equal() {
      Fragment fragment1 = Fragment.parse("section");
      Fragment fragment2 = Fragment.parse("section");
      assertThat(fragment1).isEqualTo(fragment2);
    }

    @Test
    void fragments_with_different_values_are_not_equal() {
      Fragment fragment1 = Fragment.parse("section1");
      Fragment fragment2 = Fragment.parse("section2");
      assertThat(fragment1).isNotEqualTo(fragment2);
    }

    @Test
    void fragments_with_different_case_are_not_equal() {
      Fragment fragment1 = Fragment.parse("section");
      Fragment fragment2 = Fragment.parse("SECTION");
      assertThat(fragment1).isNotEqualTo(fragment2);
    }

    @Test
    void fragment_is_equal_to_itself() {
      Fragment fragment = Fragment.parse("section");
      assertThat(fragment).isEqualTo(fragment);
    }

    @Test
    void fragment_is_not_equal_to_null() {
      Fragment fragment = Fragment.parse("section");
      assertThat(fragment).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void fragment_is_not_equal_to_different_type() {
      Fragment fragment = Fragment.parse("section");
      assertThat(fragment).isNotEqualTo("section");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_fragments_have_same_hash_code() {
      Fragment fragment1 = Fragment.parse("section");
      Fragment fragment2 = Fragment.parse("section");
      assertThat(fragment1.hashCode()).isEqualTo(fragment2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Fragment fragment = Fragment.parse("section-name");
      int hashCode1 = fragment.hashCode();
      int hashCode2 = fragment.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_fragment() {
      String fragmentString = "section-name";
      Fragment fragment = Fragment.parse(fragmentString);
      assertThat(fragment.toString()).isEqualTo(fragmentString);
    }

    @Test
    void to_string_preserves_case() {
      String fragmentString = "Section-Name";
      Fragment fragment = Fragment.parse(fragmentString);
      assertThat(fragment.toString()).isEqualTo(fragmentString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "section%20name";
      Fragment fragment = Fragment.parse(encoded);
      assertThat(fragment.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Fragment original = Fragment.parse("section/subsection");
      String stringForm = original.toString();
      Fragment parsed = Fragment.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return StringParserInvariantTests.generateInvariantTests(
        FragmentParser.INSTANCE, ParseMethod.validFragments);
  }
}
