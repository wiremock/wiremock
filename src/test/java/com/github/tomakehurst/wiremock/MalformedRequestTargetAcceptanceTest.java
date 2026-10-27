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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MalformedRequestTargetAcceptanceTest {

  private static WireMockServer serverWithNoStubs;
  private static WireMockServer serverStubbingPathA;

  @BeforeAll
  static void startServers() {
    serverWithNoStubs = new WireMockServer(wireMockConfig().dynamicPort());
    serverWithNoStubs.start();

    serverStubbingPathA = new WireMockServer(wireMockConfig().dynamicPort());
    serverStubbingPathA.start();
    serverStubbingPathA.stubFor(get(urlEqualTo("/a")).willReturn(ok()));
  }

  @AfterAll
  static void stopServers() {
    serverWithNoStubs.stop();
    serverStubbingPathA.stop();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"OPTIONS * HTTP/1.1", "GET http://localhost/a HTTP/1.1", "GET /a#frag HTTP/1.1"})
  void aServerWithNoStubsRespondsNotFoundToUnusualRequestTargets(String requestLine)
      throws IOException {
    assertThat(statusLineOf(serverWithNoStubs, requestLine), is("HTTP/1.1 404 Not Found"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GET http://localhost/a HTTP/1.1", "GET /a#frag HTTP/1.1"})
  void aServerNormalisesUnusualRequestTargetsToThePathBeforeMatching(String requestLine)
      throws IOException {
    assertThat(statusLineOf(serverStubbingPathA, requestLine), is("HTTP/1.1 200 OK"));
  }

  @Test
  void anAsteriskRequestTargetNeverReachesWireMock() throws IOException {
    WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());
    server.start();
    try {
      server.stubFor(any(anyUrl()).willReturn(ok()));

      statusLineOf(server, "OPTIONS * HTTP/1.1");

      assertThat(server.getAllServeEvents(), is(empty()));
    } finally {
      server.stop();
    }
  }

  private static String statusLineOf(WireMockServer server, String requestLine) throws IOException {
    try (Socket socket = new Socket("localhost", server.port())) {
      socket
          .getOutputStream()
          .write(
              (requestLine + "\r\nHost: localhost\r\nConnection: close\r\n\r\n")
                  .getBytes(StandardCharsets.ISO_8859_1));
      return new String(socket.getInputStream().readAllBytes(), StandardCharsets.ISO_8859_1)
          .lines()
          .findFirst()
          .orElse("");
    }
  }
}
