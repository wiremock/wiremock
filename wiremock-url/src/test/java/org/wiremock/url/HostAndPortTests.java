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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
}
