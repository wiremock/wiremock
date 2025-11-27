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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class UserInfoTests {

  static Stream<String> validUserInfo() {
    return Stream.of(
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
  }

  static Stream<String> invalidUserInfo() {
    return Stream.of(
        // Control characters
        "\\n",
        "user\\nname",
        "user\\tname",

        // Invalid characters not in unreserved/sub-delims/pct-encoded
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

        // Invalid percent encoding
        "%", // incomplete
        "%2", // incomplete
        "%GG", // invalid hex
        "user%ZZname", // invalid hex
        "user%2", // incomplete at end
        "user%"); // incomplete at end
  }

  @ParameterizedTest
  @MethodSource("invalidUserInfo")
  void throws_exception_for_invalid_userinfo(String invalidUserInfo) {
    assertThatExceptionOfType(IllegalUserInfo.class)
        .isThrownBy(() -> UserInfo.parse(invalidUserInfo))
        .withMessage("Illegal user info: `" + invalidUserInfo + "`")
        .extracting(IllegalUserInfo::getIllegalValue)
        .isEqualTo(invalidUserInfo);
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return CharSequenceParserInvariantTests.generateInvariantTests(
        UserInfoParser.INSTANCE, validUserInfo().toList());
  }
}
