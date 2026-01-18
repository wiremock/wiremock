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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

public class AuthorityTests {

  @Nested
  class Parse {

    static List<AuthorityParseTestCase> validHostAndPorts =
        List.of(
            testCase("example.com:00080", expectation(null, "example.com", "00080")),
            testCase("example.com", expectation(null, "example.com", null)),
            testCase("[::1]", expectation(null, "[::1]", null)),
            testCase("[2001:db8::1]", expectation(null, "[2001:db8::1]", null)),
            testCase("[v7.fe80::1234]", expectation(null, "[v7.fe80::1234]", null)),
            testCase("%61", expectation(null, "%61", null)),
            testCase("localhost", expectation(null, "localhost", null)),
            testCase("www.example.com", expectation(null, "www.example.com", null)),
            testCase("127.0.0.1", expectation(null, "127.0.0.1", null)),
            testCase("test-server", expectation(null, "test-server", null)),
            testCase("test_server", expectation(null, "test_server", null)),
            testCase("server123", expectation(null, "server123", null)),
            testCase("a", expectation(null, "a", null)),
            testCase("a.b.c.d.e", expectation(null, "a.b.c.d.e", null)),
            testCase("test%20server", expectation(null, "test%20server", null)),
            testCase("caf%C3%A9.com", expectation(null, "caf%C3%A9.com", null)),
            testCase("localhost:8080", expectation(null, "localhost", "8080")),
            testCase("example.com:8080", expectation(null, "example.com", "8080")),
            testCase("www.example.com:8080", expectation(null, "www.example.com", "8080")),
            testCase("127.0.0.1:8080", expectation(null, "127.0.0.1", "8080")),
            testCase("[::1]:8080", expectation(null, "[::1]", "8080")),
            testCase("[2001:db8::1]:8080", expectation(null, "[2001:db8::1]", "8080")),
            testCase("test-server:8080", expectation(null, "test-server", "8080")),
            testCase("test_server:8080", expectation(null, "test_server", "8080")),
            testCase("server123:8080", expectation(null, "server123", "8080")),
            testCase("a:8080", expectation(null, "a", "8080")),
            testCase("a.b.c.d.e:8080", expectation(null, "a.b.c.d.e", "8080")),
            testCase("test%20server:8080", expectation(null, "test%20server", "8080")),
            testCase("caf%C3%A9.com:8080", expectation(null, "caf%C3%A9.com", "8080")));

    static final List<AuthorityParseTestCase> validAuthoritiesWithUserInfo =
        List.of(
            testCase(
                "user:password@www.example.com:8080",
                expectation("user:password", "www.example.com", "8080")),
            testCase("user:pass@example.com:21", expectation("user:pass", "example.com", "21")),
            testCase("me@localhost", expectation("me", "localhost", null)),
            testCase("me@example.com", expectation("me", "example.com", null)),
            testCase("me@www.example.com", expectation("me", "www.example.com", null)),
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
            testCase("me@localhost:8080", expectation("me", "localhost", "8080")),
            testCase("me@example.com:8080", expectation("me", "example.com", "8080")),
            testCase("me@www.example.com:8080", expectation("me", "www.example.com", "8080")),
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

    static List<AuthorityParseTestCase> validAuthorities =
        Stream.concat(validHostAndPorts.stream(), validAuthoritiesWithUserInfo.stream()).toList();

    @ParameterizedTest
    @FieldSource("validAuthorities")
    void parses_valid_authority(AuthorityParseTestCase urlTest) {
      Authority authority = Authority.parse(urlTest.stringForm);
      assertThat(authority.getUserInfo()).isEqualTo(urlTest.expectation.userInfo);
      assertThat(authority.getHost()).isEqualTo(urlTest.expectation.host);
      assertThat(authority.getPort()).isEqualTo(urlTest.expectation.port);
    }

    @ParameterizedTest
    @FieldSource("validHostAndPorts")
    void parses_valid_host_and_port(AuthorityParseTestCase urlTest) {
      Authority authority = Authority.parse(urlTest.stringForm);
      assertThat(authority).isInstanceOf(HostAndPort.class);
      HostAndPort hostAndPort = HostAndPort.parse(urlTest.stringForm);
      assertThat(hostAndPort).isEqualTo(authority);
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority.getHost()).isEqualTo(urlTest.expectation.host);
      assertThat(authority.getPort()).isEqualTo(urlTest.expectation.port);
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      List<String> authorities =
          validAuthorities.stream()
              .map(authorityParseTestCase -> authorityParseTestCase.stringForm)
              .toList();
      return StringParserInvariantTests.generateInvariantTests(
          AuthorityParser.INSTANCE, authorities);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          " ", // space
          "example.com:abc", // non-numeric port
          "example.com:-80", // negative port
          "user name@example.com", // unencoded space in userinfo
          "user@ex ample.com", // space in host
          "example.com:8080:9090", // multiple ports
          "[::1", // unclosed IPv6 bracket
          "::1]", // IPv6 without opening bracket
          "user@@example.com", // double @
          "user#name@example.com", // invalid char in userinfo
          "example?.com", // invalid char in host
        })
    void throws_exception_for_illegal_authority(String illegalAuthority) {
      assertThatExceptionOfType(IllegalAuthority.class)
          .isThrownBy(() -> Authority.parse(illegalAuthority))
          .withMessage("Illegal authority: `" + illegalAuthority + "`")
          .extracting(IllegalAuthority::getIllegalValue)
          .isEqualTo(illegalAuthority);
    }

    @Test
    void rejects_illegal_port() {
      // Intger.MAX_VALUE + 1
      var authorityWithIllegalPort = "example.com:2147483648";
      var exception =
          assertThatExceptionOfType(IllegalAuthority.class)
              .isThrownBy(() -> Authority.parse(authorityWithIllegalPort))
              .withMessage("Illegal authority: `" + authorityWithIllegalPort + "`")
              .actual();

      assertThat(exception.getIllegalValue()).isEqualTo(authorityWithIllegalPort);
      var cause = assertThat(exception.getCause()).asInstanceOf(type(IllegalPort.class)).actual();
      assertThat(cause.getMessage())
          .isEqualTo(
              "Illegal port [2147483648]; Port value must be an integer between 1 and 2147483647");
      assertThat(cause.getCause()).isNull();
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

  @Nested
  class Update {

    private static final List<AuthorityChangeTestCase> withoutPortTestCases =
        List.of(
            changeTest("example.com:80", "example.com"),
            changeTest("user@example.com:80", "user@example.com"),
            changeTest("example.com:", "example.com"),
            changeTest("user@example.com:", "user@example.com"));

    @ParameterizedTest
    @FieldSource("withoutPortTestCases")
    void withoutPortRemovesPort(AuthorityChangeTestCase testCase) {
      assertThat(testCase.original.withoutPort()).isEqualTo(testCase.expected);
    }

    @ParameterizedTest
    @FieldSource("withoutPortTestCases")
    void withPortNullRemovesPort(AuthorityChangeTestCase testCase) {
      assertThat(testCase.original.withPort(null)).isEqualTo(testCase.expected);
    }

    private static final List<AuthorityChangeTestCase> unchangedWithoutPortTestCases =
        List.of(
            changeTest("example.com", "example.com"),
            changeTest("user@example.com", "user@example.com"));

    @ParameterizedTest
    @FieldSource("unchangedWithoutPortTestCases")
    void withoutPortDoesNothingIfNoPort(AuthorityChangeTestCase testCase) {
      assertThat(testCase.original.withoutPort()).isSameAs(testCase.original);
    }

    @ParameterizedTest
    @FieldSource("unchangedWithoutPortTestCases")
    void withPortNullDoesNothingIfNoPort(AuthorityChangeTestCase testCase) {
      assertThat(testCase.original.withPort(null)).isSameAs(testCase.original);
    }

    private static final List<AuthorityChangeTestCase> withPortChangesPortTestCases =
        List.of(
            changeTest("example.com", "example.com:8080"),
            changeTest("user@example.com", "user@example.com:8080"),
            changeTest("example.com:", "example.com:8080"),
            changeTest("user@example.com:", "user@example.com:8080"),
            changeTest("example.com:80", "example.com:8080"),
            changeTest("user@example.com:80", "user@example.com:8080"));

    @ParameterizedTest
    @FieldSource("withPortChangesPortTestCases")
    void withPortChangesPort(AuthorityChangeTestCase testCase) {
      assertThat(testCase.original.withPort(Port.of(8080))).isEqualTo(testCase.expected);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "example.com:8080",
          "user@example.com:8080",
        })
    void withPortDoesNothingIfNoChangeInPort(String original) {
      var authority = Authority.parse(original);
      assertThat(authority.withPort(Port.of(8080))).isSameAs(authority);
    }

    record AuthorityChangeTestCase(Authority original, Authority expected) {}

    private static AuthorityChangeTestCase changeTest(String original, String expected) {
      return new AuthorityChangeTestCase(Authority.parse(original), Authority.parse(expected));
    }
  }

  @Nested
  class AuthorityEquality {

    @Test
    void authorities_with_same_components_are_equal() {
      Authority authority1 = Authority.parse("user@example.com:8080");
      Authority authority2 = Authority.parse("user@example.com:8080");
      assertThat(authority1).isEqualTo(authority2);
    }

    @Test
    void authorities_with_different_hosts_are_not_equal() {
      Authority authority1 = Authority.parse("user@example.com:8080");
      Authority authority2 = Authority.parse("user@different.com:8080");
      assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void authorities_with_different_ports_are_not_equal() {
      Authority authority1 = Authority.parse("example.com:8080");
      Authority authority2 = Authority.parse("example.com:9090");
      assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void authorities_with_different_userInfo_are_not_equal() {
      Authority authority1 = Authority.parse("user1@example.com:8080");
      Authority authority2 = Authority.parse("user2@example.com:8080");
      assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void authority_with_port_and_without_port_are_not_equal() {
      Authority authority1 = Authority.parse("example.com:8080");
      Authority authority2 = Authority.parse("example.com");
      assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void authority_with_empty_port_and_without_port_are_not_equal() {
      Authority authority1 = Authority.parse("example.com:");
      Authority authority2 = Authority.parse("example.com");
      assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void authority_is_equal_to_itself() {
      Authority authority = Authority.parse("user@example.com:8080");
      assertThat(authority).isEqualTo(authority);
    }

    @Test
    void authority_is_not_equal_to_null() {
      Authority authority = Authority.parse("example.com");
      assertThat(authority).isNotEqualTo(null);
    }

    @Test
    void authority_is_not_equal_to_different_type() {
      Authority authority = Authority.parse("example.com");
      assertThat(authority).isNotEqualTo("example.com");
    }
  }

  @Nested
  class AuthorityHashCode {

    @Test
    void equal_authorities_have_same_hash_code() {
      Authority authority1 = Authority.parse("user@example.com:8080");
      Authority authority2 = Authority.parse("user@example.com:8080");
      assertThat(authority1.hashCode()).isEqualTo(authority2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      Authority authority = Authority.parse("user@example.com:8080");
      int hashCode1 = authority.hashCode();
      int hashCode2 = authority.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }
  }

  @Nested
  class HostAndPortEquality {

    @Test
    void hostAndPorts_with_same_components_are_equal() {
      HostAndPort hostAndPort1 = HostAndPort.parse("example.com:8080");
      HostAndPort hostAndPort2 = HostAndPort.parse("example.com:8080");
      assertThat(hostAndPort1).isEqualTo(hostAndPort2);
    }

    @Test
    void hostAndPorts_with_different_hosts_are_not_equal() {
      HostAndPort hostAndPort1 = HostAndPort.parse("example.com:8080");
      HostAndPort hostAndPort2 = HostAndPort.parse("different.com:8080");
      assertThat(hostAndPort1).isNotEqualTo(hostAndPort2);
    }

    @Test
    void hostAndPorts_with_different_ports_are_not_equal() {
      HostAndPort hostAndPort1 = HostAndPort.parse("example.com:8080");
      HostAndPort hostAndPort2 = HostAndPort.parse("example.com:9090");
      assertThat(hostAndPort1).isNotEqualTo(hostAndPort2);
    }

    @Test
    void hostAndPort_with_port_and_without_port_are_not_equal() {
      HostAndPort hostAndPort1 = HostAndPort.parse("example.com:8080");
      HostAndPort hostAndPort2 = HostAndPort.parse("example.com");
      assertThat(hostAndPort1).isNotEqualTo(hostAndPort2);
    }

    @Test
    void hostAndPort_is_equal_to_itself() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      assertThat(hostAndPort).isEqualTo(hostAndPort);
    }

    @Test
    void hostAndPort_is_not_equal_to_null() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com");
      assertThat(hostAndPort).isNotEqualTo(null);
    }

    @Test
    void hostAndPort_is_not_equal_to_different_type() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com");
      assertThat(hostAndPort).isNotEqualTo("example.com");
    }

    @Test
    void hostAndPort_equals_authority_with_same_host_and_port() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority authority = Authority.parse("example.com:8080");
      assertThat(hostAndPort).isEqualTo(authority);
      assertThat(authority).isEqualTo(hostAndPort);
    }

    @Test
    void hostAndPort_not_equals_authority_with_userInfo() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority authority = Authority.parse("user@example.com:8080");
      assertThat(hostAndPort).isNotEqualTo(authority);
      assertThat(authority).isNotEqualTo(hostAndPort);
    }
  }

  @Nested
  class HostAndPortHashCode {

    @Test
    void equal_hostAndPorts_have_same_hash_code() {
      HostAndPort hostAndPort1 = HostAndPort.parse("example.com:8080");
      HostAndPort hostAndPort2 = HostAndPort.parse("example.com:8080");
      assertThat(hostAndPort1.hashCode()).isEqualTo(hostAndPort2.hashCode());
    }

    @Test
    void hash_code_is_consistent() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      int hashCode1 = hostAndPort.hashCode();
      int hashCode2 = hostAndPort.hashCode();
      assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void hostAndPort_and_authority_with_same_components_have_same_hash_code() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority authority = Authority.parse("example.com:8080");
      assertThat(hostAndPort.hashCode()).isEqualTo(authority.hashCode());
    }
  }

  @Nested
  class AuthorityOfMethods {

    @Test
    void of_with_host_only() {
      Host host = Host.parse("example.com");
      Authority authority = Authority.of(host);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isNull();
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("example.com"));
    }

    @Test
    void of_with_host_and_port() {
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      Authority authority = Authority.of(host, port);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isEqualTo(port);
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("example.com:8080"));
    }

    @Test
    void of_with_host_and_null_port() {
      Host host = Host.parse("example.com");
      Authority authority = Authority.of(host, null);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isNull();
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("example.com"));
    }

    @Test
    void of_with_userInfo_and_host() {
      UserInfo userInfo = UserInfo.parse("user:password");
      Host host = Host.parse("example.com");
      Authority authority = Authority.of(userInfo, host);
      assertThat(authority.getUserInfo()).isEqualTo(userInfo);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("user:password@example.com:"));
    }

    @Test
    void of_with_null_userInfo_and_host() {
      Host host = Host.parse("example.com");
      Authority authority = Authority.of(null, host);
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("example.com"));
    }

    @Test
    void of_with_all_components() {
      UserInfo userInfo = UserInfo.parse("user:password");
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      Authority authority = Authority.of(userInfo, host, port);
      assertThat(authority.getUserInfo()).isEqualTo(userInfo);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isEqualTo(port);
      assertThat(authority).isEqualTo(Authority.parse("user:password@example.com:8080"));
    }

    @Test
    void of_with_all_components_and_null_port() {
      UserInfo userInfo = UserInfo.parse("user:password");
      Host host = Host.parse("example.com");
      Authority authority = Authority.of(userInfo, host, null);
      assertThat(authority.getUserInfo()).isEqualTo(userInfo);
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isNull();
      assertThat(authority).isEqualTo(Authority.parse("user:password@example.com:"));
    }

    @Test
    void of_with_all_components_and_null_userInfo() {
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      Authority authority = Authority.of(null, host, port);
      assertThat(authority.getUserInfo()).isNull();
      assertThat(authority.getHost()).isEqualTo(host);
      assertThat(authority.getPort()).isEqualTo(port);
      assertThat(authority).isEqualTo(Authority.parse("example.com:8080"));
    }

    @Test
    void of_returns_hostAndPort_when_no_userInfo() {
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      Authority authority = Authority.of(null, host, port);
      assertThat(authority).isInstanceOf(HostAndPort.class);
    }
  }

  @Nested
  class HostAndPortOfMethods {

    @Test
    void of_with_host_only() {
      Host host = Host.parse("example.com");
      HostAndPort hostAndPort = HostAndPort.of(host);
      assertThat(hostAndPort.getHost()).isEqualTo(host);
      assertThat(hostAndPort.getPort()).isNull();
      assertThat(hostAndPort.getUserInfo()).isNull();
      assertThat(hostAndPort).isEqualTo(HostAndPort.parse("example.com"));
    }

    @Test
    void of_with_host_and_port() {
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      HostAndPort hostAndPort = HostAndPort.of(host, port);
      assertThat(hostAndPort.getHost()).isEqualTo(host);
      assertThat(hostAndPort.getPort()).isEqualTo(port);
      assertThat(hostAndPort.getUserInfo()).isNull();
      assertThat(hostAndPort).isEqualTo(HostAndPort.parse("example.com:8080"));
    }

    @Test
    void of_with_host_and_null_port() {
      Host host = Host.parse("example.com");
      HostAndPort hostAndPort = HostAndPort.of(host, null);
      assertThat(hostAndPort.getHost()).isEqualTo(host);
      assertThat(hostAndPort.getPort()).isNull();
      assertThat(hostAndPort.getUserInfo()).isNull();
      assertThat(hostAndPort).isEqualTo(HostAndPort.parse("example.com"));
    }

    @Test
    void of_equals_authority_of_with_same_components() {
      Host host = Host.parse("example.com");
      Port port = Port.of(8080);
      HostAndPort hostAndPort = HostAndPort.of(host, port);
      Authority authority = Authority.of(host, port);
      assertThat(hostAndPort).isEqualTo(authority);
      assertThat(authority).isEqualTo(hostAndPort);
    }
  }

  @Nested
  class AuthorityNormalise {

    @Test
    void normalise_with_scheme_removes_default_port() {
      Authority authority = Authority.parse("example.com:80");
      Authority normalised = authority.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(Authority.parse("example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @Test
    void normalise_with_scheme_keeps_non_default_port() {
      Authority authority = Authority.parse("example.com:8080");
      Authority normalised = authority.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(Authority.parse("example.com:8080"));
      assertThat(normalised.getPort()).isEqualTo(Port.of(8080));
    }

    @Test
    void normalise_with_scheme_returns_same_instance_when_already_normalised() {
      Authority authority = Authority.parse("example.com:8080");
      Authority normalised = authority.normalise(Scheme.http);
      assertThat(normalised).isSameAs(authority);
    }

    @Test
    void normalise_with_scheme_normalises_host_and_removes_default_port() {
      Authority authority = Authority.parse("EXAMPLE.COM:80");
      Authority normalised = authority.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(Authority.parse("example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @Test
    void normalise_with_userInfo_and_scheme_removes_default_port() {
      Authority authority = Authority.parse("user:password@example.com:80");
      Authority normalised = authority.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(Authority.parse("user:password@example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.com", "example.com:8080"})
    void is_normal_form_with_http_scheme_returns_true(String authorityString) {
      var authority = Authority.parse(authorityString);
      assertThat(authority.isNormalForm(Scheme.http)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.com:", "example.com:80"})
    void is_normal_form_with_http_scheme_returns_false(String authorityString) {
      var authority = Authority.parse(authorityString);
      assertThat(authority.isNormalForm(Scheme.http)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "example.com",
          "example.com:123",
        })
    void is_normal_form_with_file_scheme_returns_true(String authorityString) {
      var authority = Authority.parse(authorityString);
      assertThat(authority.isNormalForm(Scheme.file)).isTrue();
    }

    @Test
    void is_normal_form_with_file_scheme_returns_false() {
      var authority = Authority.parse("example.com:");
      assertThat(authority.isNormalForm(Scheme.file)).isFalse();
    }

    static final List<NormalisationCase<Authority>> normalisationCases =
        Stream.of(
                Pair.of("EXAMPLE.COM:8080", "example.com:8080"),
                Pair.of("EXAMPLE.COM:08080", "example.com:8080"),
                Pair.of("EXAMPLE.COM:", "example.com"),
                Pair.of("example.com:08080", "example.com:8080"),
                Pair.of("example.com:", "example.com"),
                Pair.of("user@EXAMPLE.COM:8080", "user@example.com:8080"),
                Pair.of("user@EXAMPLE.COM:08080", "user@example.com:8080"),
                Pair.of("user@EXAMPLE.COM:", "user@example.com"),
                Pair.of("user@example.com:08080", "user@example.com:8080"),
                Pair.of("user@example.com:", "user@example.com"),
                Pair.of("us%65r@example.com:", "user@example.com"),
                Pair.of("us%65r%2f@example.com:", "user%2F@example.com"))
            .map(
                it ->
                    new NormalisationCase<>(
                        Authority.parse(it.getLeft()), Authority.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_authority_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(normalisationCases);
    }

    static final List<Authority> alreadyNormalisedAuthorities =
        Stream.of(
                "example.com",
                "example.com:8080",
                "user@example.com:8080",
                "user@example.com",
                "user:@example.com:8080",
                "user:@example.com",
                ":@example.com:8080",
                ":@example.com",
                ":password@example.com:8080",
                ":password@example.com",
                "user:password@example.com:8080",
                "userwithencodedcolon%3A@example.com:8080",
                "user:password@example.com")
            .map(Authority::parse)
            .toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedAuthorities);
    }
  }
}
