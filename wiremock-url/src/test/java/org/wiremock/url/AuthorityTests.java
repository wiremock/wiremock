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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class AuthorityTests {

  static Stream<AuthorityParseTestCase> validAuthorities() {
    return Stream.of(
        testCase(
            "user:password@www.example.com:8080",
            expectation("user:password", "www.example.com", "8080")),
        testCase("user:pass@example.com:21", expectation("user:pass", "example.com", "21")),
        testCase("example.com:00080", expectation(null, "example.com", "00080")),
        testCase("example.com", expectation(null, "example.com", null)),
        testCase("[::1]", expectation(null, "[::1]", null)),
        testCase("[2001:db8::1]", expectation(null, "[2001:db8::1]", null)),
        testCase("[v7.fe80::1234]", expectation(null, "[v7.fe80::1234]", null)),
        testCase("%61", expectation(null, "%61", null)),
        testCase("localhost", expectation(null, "localhost", null)),
        testCase("example.com", expectation(null, "example.com", null)),
        testCase("www.example.com", expectation(null, "www.example.com", null)),
        testCase("192.168.1.1", expectation(null, "192.168.1.1", null)),
        testCase("10.0.0.1", expectation(null, "10.0.0.1", null)),
        testCase("127.0.0.1", expectation(null, "127.0.0.1", null)),
        testCase("[::1]", expectation(null, "[::1]", null)),
        testCase("[2001:db8::1]", expectation(null, "[2001:db8::1]", null)),
        testCase("test-server", expectation(null, "test-server", null)),
        testCase("test_server", expectation(null, "test_server", null)),
        testCase("server123", expectation(null, "server123", null)),
        testCase("a", expectation(null, "a", null)),
        testCase("a.b.c.d.e", expectation(null, "a.b.c.d.e", null)),
        testCase("test%20server", expectation(null, "test%20server", null)),
        testCase("caf%C3%A9.com", expectation(null, "caf%C3%A9.com", null)),
        testCase("me@localhost", expectation("me", "localhost", null)),
        testCase("me@example.com", expectation("me", "example.com", null)),
        testCase("me@www.example.com", expectation("me", "www.example.com", null)),
        testCase("me@192.168.1.1", expectation("me", "192.168.1.1", null)),
        testCase("me@10.0.0.1", expectation("me", "10.0.0.1", null)),
        testCase("me@127.0.0.1", expectation("me", "127.0.0.1", null)),
        testCase("me@[::1]", expectation("me", "[::1]", null)),
        testCase("me@[2001:db8::1]", expectation("me", "[2001:db8::1]", null)),
        testCase("me@test-server", expectation("me", "test-server", null)),
        testCase("me@test_server", expectation("me", "test_server", null)),
        testCase("me@server123", expectation("me", "server123", null)),
        testCase("me@a", expectation("me", "a", null)),
        testCase("me@a.b.c.d.e", expectation("me", "a.b.c.d.e", null)),
        testCase("me@test%20server", expectation("me", "test%20server", null)),
        testCase("me@caf%C3%A9.com", expectation("me", "caf%C3%A9.com", null)),
        testCase("localhost:8080", expectation(null, "localhost", "8080")),
        testCase("example.com:8080", expectation(null, "example.com", "8080")),
        testCase("www.example.com:8080", expectation(null, "www.example.com", "8080")),
        testCase("192.168.1.1:8080", expectation(null, "192.168.1.1", "8080")),
        testCase("10.0.0.1:8080", expectation(null, "10.0.0.1", "8080")),
        testCase("127.0.0.1:8080", expectation(null, "127.0.0.1", "8080")),
        testCase("[::1]:8080", expectation(null, "[::1]", "8080")),
        testCase("[2001:db8::1]:8080", expectation(null, "[2001:db8::1]", "8080")),
        testCase("test-server:8080", expectation(null, "test-server", "8080")),
        testCase("test_server:8080", expectation(null, "test_server", "8080")),
        testCase("server123:8080", expectation(null, "server123", "8080")),
        testCase("a:8080", expectation(null, "a", "8080")),
        testCase("a.b.c.d.e:8080", expectation(null, "a.b.c.d.e", "8080")),
        testCase("test%20server:8080", expectation(null, "test%20server", "8080")),
        testCase("caf%C3%A9.com:8080", expectation(null, "caf%C3%A9.com", "8080")),
        testCase("me@localhost:8080", expectation("me", "localhost", "8080")),
        testCase("me@example.com:8080", expectation("me", "example.com", "8080")),
        testCase("me@www.example.com:8080", expectation("me", "www.example.com", "8080")),
        testCase("me@192.168.1.1:8080", expectation("me", "192.168.1.1", "8080")),
        testCase("me@10.0.0.1:8080", expectation("me", "10.0.0.1", "8080")),
        testCase("me@127.0.0.1:8080", expectation("me", "127.0.0.1", "8080")),
        testCase("me@[::1]:8080", expectation("me", "[::1]", "8080")),
        testCase("me@[2001:db8::1]:8080", expectation("me", "[2001:db8::1]", "8080")),
        testCase("me@test-server:8080", expectation("me", "test-server", "8080")),
        testCase("me@test_server:8080", expectation("me", "test_server", "8080")),
        testCase("me@server123:8080", expectation("me", "server123", "8080")),
        testCase("me@a:8080", expectation("me", "a", "8080")),
        testCase("me@a.b.c.d.e:8080", expectation("me", "a.b.c.d.e", "8080")),
        testCase("me@test%20server:8080", expectation("me", "test%20server", "8080")),
        testCase("me@caf%C3%A9.com:8080", expectation("me", "caf%C3%A9.com", "8080")));
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    List<String> validAuthorities =
        validAuthorities()
            .map(authorityParseTestCase -> authorityParseTestCase.stringForm)
            .toList();
    return CharSequenceParserInvariantTests.generateInvariantTests(
        AuthorityParser.INSTANCE, validAuthorities);
  }

  @ParameterizedTest
  @MethodSource("validAuthorities")
  void parses_valid_url(AuthorityParseTestCase urlTest) {
    Authority authority = Authority.parse(urlTest.stringForm);
    assertThat(authority.userInfo()).isEqualTo(urlTest.expectation.userInfo);
    assertThat(authority.host()).isEqualTo(urlTest.expectation.host);
    assertThat(authority.port()).isEqualTo(urlTest.expectation.port);
  }

  static AuthorityParseTestCase testCase(String stringForm, AuthorityExpectation expectation) {
    return new AuthorityParseTestCase(stringForm, expectation);
  }

  static AuthorityExpectation expectation(
      @Nullable String userInfoStr, String hostStr, @Nullable String portStr) {
    UserInfo userInfo = userInfoStr == null ? null : UserInfo.parse(userInfoStr);
    Host host = Host.parse(hostStr);
    Port port = portStr == null ? null : Port.parse(portStr);
    return new AuthorityExpectation(userInfo, host, port);
  }

  record AuthorityParseTestCase(String stringForm, AuthorityExpectation expectation) {}

  record AuthorityExpectation(@Nullable UserInfo userInfo, Host host, @Nullable Port port) {}
}
