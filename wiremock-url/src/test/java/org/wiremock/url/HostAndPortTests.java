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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.url.AuthorityTests.AuthorityParseTestCase;

class HostAndPortTests {

  @Test
  void withoutPortRemovesPort() {
    HostAndPort hostAndPort = HostAndPort.parse("example.com:80");
    assertThat(hostAndPort.withoutPort()).isEqualTo(HostAndPort.parse("example.com"));
  }

  @Test
  void withoutPortDoesNothingIfNoPort() {
    var hostAndPort = HostAndPort.parse("example.com");
    assertThat(hostAndPort.withoutPort()).isSameAs(hostAndPort);
  }

  @Test
  void withPortNullRemovesPort() {
    HostAndPort hostAndPort = HostAndPort.parse("example.com:80");
    assertThat(hostAndPort.withPort(null)).isEqualTo(HostAndPort.parse("example.com"));
  }

  @Test
  void withPortNullDoesNothingIfNoPort() {
    var hostAndPort = HostAndPort.parse("example.com");
    assertThat(hostAndPort.withPort(null)).isSameAs(hostAndPort);
  }

  @ParameterizedTest
  @ValueSource(strings = {"example.com", "example.com:80"})
  void withPortChangesPort(String original) {
    assertThat(HostAndPort.parse(original).withPort(Port.of(8080)))
        .isEqualTo(HostAndPort.parse("example.com:8080"));
  }

  @Test
  void withPortDoesNothingIfNoChangeInPort() {
    var hostAndPort = HostAndPort.parse("example.com:8080");
    assertThat(hostAndPort.withPort(Port.of(8080))).isSameAs(hostAndPort);
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    List<String> authorities =
        validHostAndPorts.stream().map(AuthorityParseTestCase::stringForm).toList();
    return StringParserInvariantTests.generateInvariantTests(
        HostAndPortParser.INSTANCE, authorities);
  }

  private static final List<AuthorityParseTestCase> validHostAndPorts =
      AuthorityTests.validHostAndPorts;

  @ParameterizedTest
  @FieldSource("validHostAndPorts")
  void parses_valid_host_and_port(AuthorityParseTestCase urlTest) {
    HostAndPort hostAndPort = HostAndPort.parse(urlTest.stringForm());
    //noinspection removal
    assertThat(hostAndPort.getUserInfo()).isNull();
    assertThat(hostAndPort.getHost()).isEqualTo(urlTest.expectation().host());
    assertThat(hostAndPort.getPort()).isEqualTo(urlTest.expectation().port());
  }

  private static final List<AuthorityParseTestCase> invalidHostAndPorts =
      AuthorityTests.validAuthoritiesWithUserInfo;

  @ParameterizedTest
  @FieldSource("invalidHostAndPorts")
  void rejects_invalid_host_and_port(AuthorityParseTestCase urlTest) {
    assertThatThrownBy(() -> HostAndPort.parse(urlTest.stringForm()))
        .isInstanceOf(IllegalHostAndPort.class)
        .hasMessage("Illegal host and port: `" + urlTest.stringForm() + "`");
  }

  @Nested
  class HostAndPortNormalise {

    @Test
    void normalise_returns_same_instance_when_already_normalised() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority normalised = hostAndPort.normalise();
      assertThat(normalised).isSameAs(hostAndPort);
    }

    @Test
    void normalise_normalises_host() {
      HostAndPort hostAndPort = HostAndPort.parse("EXAMPLE.COM:8080");
      Authority normalised = hostAndPort.normalise();
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com:8080"));
      assertThat(normalised).isNotSameAs(hostAndPort);
    }

    @Test
    void normalise_normalises_port() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:00080");
      Authority normalised = hostAndPort.normalise();
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com:80"));
      assertThat(normalised).isNotSameAs(hostAndPort);
    }

    @Test
    void normalise_with_scheme_removes_default_port() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:80");
      Authority normalised = hostAndPort.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @Test
    void normalise_with_scheme_keeps_non_default_port() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority normalised = hostAndPort.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com:8080"));
      assertThat(normalised.getPort()).isEqualTo(Port.of(8080));
    }

    @Test
    void normalise_with_scheme_returns_same_instance_when_already_normalised() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com:8080");
      Authority normalised = hostAndPort.normalise(Scheme.http);
      assertThat(normalised).isSameAs(hostAndPort);
    }

    @Test
    void normalise_with_scheme_normalises_host_and_removes_default_port() {
      HostAndPort hostAndPort = HostAndPort.parse("EXAMPLE.COM:80");
      Authority normalised = hostAndPort.normalise(Scheme.http);
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @Test
    void normalise_with_scheme_normalises_host_and_port() {
      HostAndPort hostAndPort = HostAndPort.parse("EXAMPLE.COM:00443");
      Authority normalised = hostAndPort.normalise(Scheme.https);
      assertThat(normalised).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(normalised.getPort()).isNull();
    }

    @Test
    void normalise_without_port_returns_same_instance() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com");
      Authority normalised = hostAndPort.normalise();
      assertThat(normalised).isSameAs(hostAndPort);
    }

    @Test
    void normalise_with_scheme_without_port_returns_same_instance() {
      HostAndPort hostAndPort = HostAndPort.parse("example.com");
      Authority normalised = hostAndPort.normalise(Scheme.http);
      assertThat(normalised).isSameAs(hostAndPort);
    }
  }
}
