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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserInfoTests {

  @Nested
  class ParseMethod {

    static final List<String> validUserInfo =
        List.of(
            // Simple usernames
            "user",
            "admin",
            "john",
            "alice123",

            // Username with password
            "user:password",
            "admin:secret123",
            "john:passw0rd",

            // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
            "user-name",
            "user.name",
            "user_name",
            "user~name",
            "User123",
            "test-user_123.name~test",

            // Sub-delimiters (!$&'()*+,;=)
            "user!name",
            "user$name",
            "user&name",
            "user'name",
            "user(name)",
            "user*name",
            "user+name",
            "user,name",
            "user;name",
            "user=name",

            // Percent-encoded characters
            "%20", // space
            "user%20name", // user name
            "user%40example", // user@example
            "%C3%A9", // é
            "caf%C3%A9", // café

            // With colons (common in password-based auth)
            ":",
            ":::",
            "user:",
            ":password",
            "user:pass:extra",

            // Complex combinations
            "john.doe:secret123",
            "user%20one:p%40ssword",
            "admin_123:!secret$",
            "test+user:pass=123",

            // Empty (valid according to regex)
            "");

    @ParameterizedTest
    @FieldSource("validUserInfo")
    void parses_valid_userinfo(String userInfoString) {
      UserInfo userInfo = UserInfo.parse(userInfoString);
      assertThat(userInfo.toString()).isEqualTo(userInfoString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "user name", // unencoded space
          "user@name", // @ not allowed (must be percent-encoded)
          "user#name", // # not allowed
          "user/name", // / not allowed
          "user?name", // ? not allowed
          "user[name]", // brackets not allowed
          "user<name>", // angle brackets not allowed
          "user\\name", // backslash not allowed
          "user|name", // pipe not allowed
          "user\"name", // quote not allowed
          "%", // incomplete encoding
          "%2", // incomplete encoding
          "%GG", // invalid hex
          "user%ZZname" // invalid hex
        })
    void rejects_illegal_userinfo(String illegalUserInfo) {
      assertThatExceptionOfType(IllegalUserInfo.class)
          .isThrownBy(() -> UserInfo.parse(illegalUserInfo))
          .withMessage("Illegal user info: `" + illegalUserInfo + "`")
          .withNoCause()
          .extracting(IllegalUserInfo::getIllegalValue)
          .isEqualTo(illegalUserInfo);
    }
  }

  @Nested
  class DecodeMethod {

    record DecodeCase(String input, String expected) {}

    static final List<String> userInfoWithoutPercentEncoding =
        List.of("", "user", "user:password", "admin:secret123", "john.doe:pass");

    static final List<DecodeCase> decodeCases =
        List.of(
            new DecodeCase("user%20name", "user name"),
            new DecodeCase("user%40example", "user@example"),
            new DecodeCase("caf%C3%A9", "café"),
            new DecodeCase("user:pass%20word", "user:pass word"),
            new DecodeCase("user%20one:p%40ssword", "user one:p@ssword"),
            new DecodeCase("%C3%A9:%C3%A9", "é:é"),
            new DecodeCase("user%2Fname:pass", "user/name:pass"),
            new DecodeCase("test:pass%3Aword", "test:pass:word"));

    @ParameterizedTest
    @FieldSource("userInfoWithoutPercentEncoding")
    void returns_same_string_for_userinfo_without_percent_encoding(String userInfoString) {
      UserInfo userInfo = UserInfo.parse(userInfoString);
      assertThat(userInfo.decode()).isEqualTo(userInfoString);
    }

    @ParameterizedTest
    @FieldSource("decodeCases")
    void decodes_percent_encoded_userinfo_correctly(DecodeCase testCase) {
      UserInfo userInfo = UserInfo.parse(testCase.input());
      assertThat(userInfo.decode()).isEqualTo(testCase.expected());
    }
  }

  @Nested
  class Equality {

    record EqualityCase(String userInfo1, String userInfo2, boolean shouldBeEqual) {}

    static final List<EqualityCase> equalityCases =
        List.of(
            new EqualityCase("user:password", "user:password", true),
            new EqualityCase("user1:password", "user2:password", false),
            new EqualityCase("user:password1", "user:password2", false),
            new EqualityCase("user:password", "USER:PASSWORD", false));

    @ParameterizedTest
    @FieldSource("equalityCases")
    void equality_comparison(EqualityCase testCase) {
      UserInfo userInfo1 = UserInfo.parse(testCase.userInfo1());
      UserInfo userInfo2 = UserInfo.parse(testCase.userInfo2());
      if (testCase.shouldBeEqual()) {
        assertThat(userInfo1).isEqualTo(userInfo2);
      } else {
        assertThat(userInfo1).isNotEqualTo(userInfo2);
      }
    }

    @Test
    void userinfo_is_equal_to_itself() {
      UserInfo userInfo = UserInfo.parse("user:password");
      assertThat(userInfo).isEqualTo(userInfo);
    }

    @Test
    void userinfo_is_not_equal_to_null() {
      UserInfo userInfo = UserInfo.parse("user:password");
      assertThat(userInfo).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void userinfo_is_not_equal_to_different_type() {
      UserInfo userInfo = UserInfo.parse("user:password");
      assertThat(userInfo).isNotEqualTo("user:password");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_userinfo_have_same_hash_code() {
      UserInfo userInfo1 = UserInfo.parse("user:password");
      UserInfo userInfo2 = UserInfo.parse("user:password");
      assertThat(userInfo1.hashCode()).isEqualTo(userInfo2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      UserInfo userInfo = UserInfo.parse("admin:secret123");
      int hashCode1 = userInfo.hashCode();
      int hashCode2 = userInfo.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    static final List<String> toStringTestCases =
        List.of("user:password", "User:Password", "user%20name:pass%20word", "john.doe:secret123");

    @ParameterizedTest
    @FieldSource("toStringTestCases")
    void to_string_returns_original_userinfo(String userInfoString) {
      UserInfo userInfo = UserInfo.parse(userInfoString);
      assertThat(userInfo.toString()).isEqualTo(userInfoString);
    }

    @ParameterizedTest
    @FieldSource("toStringTestCases")
    void to_string_result_can_be_parsed_back(String userInfoString) {
      UserInfo original = UserInfo.parse(userInfoString);
      String stringForm = original.toString();
      UserInfo parsed = UserInfo.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }

  @Nested
  class UsernameAndPassword {

    record ExtractionCase(String input, String expectedUsername, String expectedPassword) {}

    static final List<ExtractionCase> extractionCases =
        List.of(
            new ExtractionCase("john:secret", "john", "secret"),
            new ExtractionCase("john", "john", null),
            new ExtractionCase("john:", "john", ""),
            new ExtractionCase(":password", "", "password"),
            new ExtractionCase("user:pass:word:123", "user", "pass:word:123"));

    @ParameterizedTest
    @FieldSource("extractionCases")
    void extracts_username_and_password_correctly(ExtractionCase testCase) {
      UserInfo userInfo = UserInfo.parse(testCase.input());
      assertThat(userInfo.getUsername().toString()).isEqualTo(testCase.expectedUsername());
      if (testCase.expectedPassword() == null) {
        assertThat(userInfo.getPassword()).isNull();
      } else {
        assertThat(userInfo.getPassword()).isNotNull();
        assertThat(userInfo.getPassword().toString()).isEqualTo(testCase.expectedPassword());
      }
    }
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return StringParserInvariantTests.generateInvariantTests(
        UserInfoParser.INSTANCE, ParseMethod.validUserInfo);
  }
}
