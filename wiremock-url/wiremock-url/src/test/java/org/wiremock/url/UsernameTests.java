/*
 * Copyright (C) 2025-2025 Thomas Akehurst
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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

class UsernameTests {

  @Nested
  class Parse {

    static final List<String> validUsernames =
        List.of(
            // Empty username
            "",

            // Simple usernames
            "user",
            "admin",
            "john",
            "alice123",

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
            "user%3Aname", // user:name (colon must be encoded)

            // Complex combinations
            "john.doe",
            "user%20one",
            "admin_123",
            "test+user");

    @ParameterizedTest
    @FieldSource("validUsernames")
    void parses_valid_usernames(String usernameString) {
      Username username = Username.parse(usernameString);
      assertThat(username.toString()).isEqualTo(usernameString);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "user name", // unencoded space
          "user@name", // @ not allowed (must be percent-encoded)
          "user:name", // : not allowed (must be percent-encoded)
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
          "%2G", // incomplete encoding
          "%GG", // invalid hex
          "user%ZZname" // invalid hex
        })
    void rejects_illegal_username(String illegalUsername) {
      assertThatExceptionOfType(IllegalUsername.class)
          .isThrownBy(() -> Username.parse(illegalUsername))
          .withMessage("Illegal username: `" + illegalUsername + "`")
          .withNoCause()
          .extracting(IllegalUsername::getIllegalValue)
          .isEqualTo(illegalUsername);
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          UsernameParser.INSTANCE, validUsernames);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<UserInfo>> normalisationCases =
        Stream.of(Pair.of("us%65r", "user"), Pair.of("user%2f", "user%2F"))
            .map(
                it ->
                    new NormalisationCase<>(
                        UserInfo.parse(it.getLeft()), UserInfo.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_user_info_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases);
    }

    static final List<UserInfo> alreadyNormalised =
        Stream.of("user", "user%2F", "user_with_encoded_%3A_colon").map(UserInfo::parse).toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(alreadyNormalised);
    }
  }

  @Nested
  class Codec {

    static final List<String> usernamesWithoutPercentEncoding =
        List.of("", "user", "admin", "user-name", "user.name", "User123");

    @ParameterizedTest
    @FieldSource("usernamesWithoutPercentEncoding")
    void encode_returns_same_string_for_username_without_percent_encoding(String usernameString) {
      Username username = Username.encode(usernameString);
      assertThat(username.toString()).isEqualTo(usernameString);
      assertThat(username.decode()).isEqualTo(usernameString);
    }

    @ParameterizedTest
    @FieldSource("usernamesWithoutPercentEncoding")
    void decode_returns_same_string_for_username_without_percent_encoding(String usernameString) {
      Username username = Username.parse(usernameString);
      assertThat(username.decode()).isEqualTo(usernameString);
    }

    static final List<CodecCase> codecCases =
        List.of(
            new CodecCase("user%20name", "user name"),
            new CodecCase("user%3Aname", "user:name"),
            new CodecCase("user%25name", "user%name"),
            new CodecCase("%C3%A9", "é"),
            new CodecCase("caf%C3%A9", "café"),
            new CodecCase("user%40example", "user@example"),
            new CodecCase("user%3Aname", "user:name"),
            new CodecCase("%20", " "),
            new CodecCase("hello%20world", "hello world"),
            new CodecCase("test%2Fuser", "test/user"));

    @ParameterizedTest
    @FieldSource("codecCases")
    void encodes_percent_encoded_username_correctly(CodecCase testCase) {
      Username username = Username.encode(testCase.decoded());
      assertThat(username.toString()).isEqualTo(testCase.encoded());
    }

    @ParameterizedTest
    @FieldSource("codecCases")
    void decodes_percent_encoded_username_correctly(CodecCase testCase) {
      Username username = Username.parse(testCase.encoded());
      assertThat(username.decode()).isEqualTo(testCase.decoded());
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
      return generateEncodeDecodeInvariantTests(
          UsernameParser.INSTANCE,
          Stream.of("foo", "bar", "test123", "hello world", "user@example", "café", "こんにちは"));
    }
  }

  @Nested
  class Equality {

    @Test
    void usernames_with_same_value_are_equal() {
      Username username1 = Username.parse("user");
      Username username2 = Username.parse("user");
      assertThat(username1).isEqualTo(username2);
    }

    @Test
    void usernames_with_different_values_are_not_equal() {
      Username username1 = Username.parse("user1");
      Username username2 = Username.parse("user2");
      assertThat(username1).isNotEqualTo(username2);
    }

    @Test
    void usernames_with_different_case_are_not_equal() {
      Username username1 = Username.parse("user");
      Username username2 = Username.parse("USER");
      assertThat(username1).isNotEqualTo(username2);
    }

    @Test
    void username_is_equal_to_itself() {
      Username username = Username.parse("user");
      assertThat(username).isEqualTo(username);
    }

    @Test
    void username_is_not_equal_to_null() {
      Username username = Username.parse("user");
      assertThat(username).isNotEqualTo(null);
    }

    @Test
    @SuppressWarnings("AssertBetweenInconvertibleTypes")
    void username_is_not_equal_to_different_type() {
      Username username = Username.parse("user");
      assertThat(username).isNotEqualTo("user");
    }
  }

  @Nested
  class HashCode {

    @Test
    void equal_usernames_have_same_hash_code() {
      Username username1 = Username.parse("user");
      Username username2 = Username.parse("user");
      assertThat(username1.hashCode()).isEqualTo(username2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Username username = Username.parse("admin123");
      int hashCode1 = username.hashCode();
      int hashCode2 = username.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class ToStringMethod {

    @Test
    void to_string_returns_original_username() {
      String usernameString = "user-name";
      Username username = Username.parse(usernameString);
      assertThat(username.toString()).isEqualTo(usernameString);
    }

    @Test
    void to_string_preserves_case() {
      String usernameString = "UserName";
      Username username = Username.parse(usernameString);
      assertThat(username.toString()).isEqualTo(usernameString);
    }

    @Test
    void to_string_preserves_percent_encoding() {
      String encoded = "user%20name";
      Username username = Username.parse(encoded);
      assertThat(username.toString()).isEqualTo(encoded);
    }

    @Test
    void to_string_result_can_be_parsed_back() {
      Username original = Username.parse("user.name");
      String stringForm = original.toString();
      Username parsed = Username.parse(stringForm);
      assertThat(parsed).isEqualTo(original);
      assertThat(parsed.toString()).isEqualTo(stringForm);
    }
  }
}
