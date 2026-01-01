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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordTests {

  @Nested
  class ParseMethod {

    static final List<String> validPasswords =
        List.of(
            // Empty password
            "",

            // Simple passwords
            "password",
            "secret",
            "pass123",
            "MyPassw0rd",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "pass-word",
            "pass.word",
            "pass_word",
            "pass~word",
            "Pass123",
            "test-pass_123.word~test",

            // Sub-delimiters (!$&'()*+,;=)
            "pass!word",
            "pass$word",
            "pass&word",
            "pass'word",
            "pass(word)",
            "pass*word",
            "pass+word",
            "pass,word",
            "pass;word",
            "pass=word",

            // Colons (allowed in passwords)
            "pass:word",
            ":::",
            "time:12:30",

            // Percent-encoded characters
            "%20", // space
            "pass%20word", // pass word
            "pass%40example", // pass@example
            "%C3%A9", // é
            "caf%C3%A9", // café
            "pass%2Fword", // pass/word

            // Complex combinations
            "Passw0rd!123",
            "pass%20word",
            "secret_456",
            "test+pass",
            "my:secret:key");

    @ParameterizedTest
    @FieldSource("validPasswords")
    void parses_valid_passwords(String passwordString) {
      Password password = Password.parse(passwordString);
      assertThat(password.toString()).isEqualTo(passwordString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "pass word", // unencoded space
          "pass@word", // @ not allowed (must be percent-encoded)
          "pass#word", // # not allowed
          "pass/word", // / not allowed
          "pass?word", // ? not allowed
          "pass[word]", // brackets not allowed
          "pass<word>", // angle brackets not allowed
          "pass\\word", // backslash not allowed
          "pass|word", // pipe not allowed
          "pass\"word", // quote not allowed
          "%", // incomplete encoding
          "%2", // incomplete encoding
          "%GG", // invalid hex
          "pass%ZZword" // invalid hex
        })
    void throws_exception_for_invalid_passwords(String invalidPassword) {
      assertThatExceptionOfType(IllegalPassword.class)
          .isThrownBy(() -> Password.parse(invalidPassword))
          .withMessage("Illegal password: `" + invalidPassword + "`")
          .extracting(IllegalPassword::getIllegalValue)
          .isEqualTo(invalidPassword);
    }
  }

  @Nested
  class DecodeMethod {

    record DecodeCase(String input, String expected) {}

    static final List<String> passwordsWithoutPercentEncoding =
        List.of("", "password", "secret", "pass-word", "pass.word", "Pass123", "pass:word");

    static final List<DecodeCase> decodeCases =
        List.of(
            new DecodeCase("pass%20word", "pass word"),
            new DecodeCase("%C3%A9", "é"),
            new DecodeCase("caf%C3%A9", "café"),
            new DecodeCase("pass%40example", "pass@example"),
            new DecodeCase("pass%2Fword", "pass/word"),
            new DecodeCase("%20", " "),
            new DecodeCase("hello%20world", "hello world"),
            new DecodeCase("test%3Apass", "test:pass"));

    @ParameterizedTest
    @FieldSource("passwordsWithoutPercentEncoding")
    void returns_same_string_for_password_without_percent_encoding(String passwordString) {
      Password password = Password.parse(passwordString);
      assertThat(password.decode()).isEqualTo(passwordString);
    }

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_percent_encoded_password_correctly(DecodeCase testCase) {
      Password password = Password.parse(testCase.input());
      assertThat(password.decode()).isEqualTo(testCase.expected());
    }
  }

  @Nested
  class Equality {

    @Test
    void passwords_with_same_value_are_equal() {
      Password password1 = Password.parse("secret");
      Password password2 = Password.parse("secret");
      assertThat(password1).isEqualTo(password2);
    }

    @Test
    void passwords_with_different_values_are_not_equal() {
      Password password1 = Password.parse("secret1");
      Password password2 = Password.parse("secret2");
      assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    void passwords_with_different_case_are_not_equal() {
      Password password1 = Password.parse("secret");
      Password password2 = Password.parse("SECRET");
      assertThat(password1).isNotEqualTo(password2);
    }

    @Test
    void password_is_equal_to_itself() {
      Password password = Password.parse("secret");
      assertThat(password).isEqualTo(password);
    }

    @Test
    void password_is_not_equal_to_null() {
      Password password = Password.parse("secret");
      assertThat(password).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void password_is_not_equal_to_different_type() {
      Password password = Password.parse("secret");
      assertThat(password).isNotEqualTo("secret");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_passwords_have_same_hash_code() {
      Password password1 = Password.parse("secret");
      Password password2 = Password.parse("secret");
      assertThat(password1.hashCode()).isEqualTo(password2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Password password = Password.parse("MyPassw0rd!123");
      int hashCode1 = password.hashCode();
      int hashCode2 = password.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_password() {
      String passwordString = "pass-word";
      Password password = Password.parse(passwordString);
      assertThat(password.toString()).isEqualTo(passwordString);
    }

    @Test
    void to_string_preserves_case() {
      String passwordString = "PassWord";
      Password password = Password.parse(passwordString);
      assertThat(password.toString()).isEqualTo(passwordString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "pass%20word";
      Password password = Password.parse(encoded);
      assertThat(password.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Password original = Password.parse("pass.word");
      String stringForm = original.toString();
      Password parsed = Password.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return StringParserInvariantTests.generateInvariantTests(
        PasswordParser.INSTANCE, ParseMethod.validPasswords);
  }
}
