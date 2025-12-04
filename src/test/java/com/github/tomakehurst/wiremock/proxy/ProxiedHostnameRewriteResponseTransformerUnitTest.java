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
package com.github.tomakehurst.wiremock.proxy;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.ImmutableRequest;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.collect.Streams;
import java.net.URI;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("HttpUrlsUsage")
public class ProxiedHostnameRewriteResponseTransformerUnitTest {

  @Test
  void replacesMultipleCorrectlyProxyDefaultPortOriginDefaultPortSpecificDirectionOne() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "http://origin.example.com:80 http://origin.example.com origin.example.com:80 origin.example.com origin.example.com origin.example.com:80 http://origin.example.com http://origin.example.com:80",
            "http://proxy.example.com:80 http://proxy.example.com proxy.example.com:80 proxy.example.com proxy.example.com proxy.example.com:80 http://proxy.example.com http://proxy.example.com:80"));
  }

  @Test
  void replacesMultipleCorrectlyProxyDefaultPortOriginDefaultPortSpecificDirectionTwo() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "origin.example.com origin.example.com:80 http://origin.example.com http://origin.example.com:80 http://origin.example.com:80 http://origin.example.com origin.example.com:80 origin.example.com",
            "proxy.example.com proxy.example.com:80 http://proxy.example.com http://proxy.example.com:80 http://proxy.example.com:80 http://proxy.example.com proxy.example.com:80 proxy.example.com"));
  }

  @Test
  void replacesMultipleCorrectlyProxyDefaultPortOriginCustomPortSpecificDirectionOne() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080 origin.example.com:8080 origin.example.com origin.example.com origin.example.com:8080 http://origin.example.com:8080",
            "http://proxy.example.com proxy.example.com proxy.example.com proxy.example.com proxy.example.com http://proxy.example.com"));
  }

  @Test
  void replacesMultipleCorrectlyProxyDefaultPortOriginCustomPortSpecificDirectionTwo() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "origin.example.com origin.example.com:8080 http://origin.example.com:8080 http://origin.example.com:8080 origin.example.com:8080 origin.example.com",
            "proxy.example.com proxy.example.com http://proxy.example.com http://proxy.example.com proxy.example.com proxy.example.com"));
  }

  @Test
  void replacesMultipleCorrectlyProxyCustomPortOriginDefaultPortSpecificDirectionOne() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "http://origin.example.com:80 http://origin.example.com origin.example.com:80 origin.example.com origin.example.com origin.example.com:80 http://origin.example.com http://origin.example.com:80",
            "http://proxy.example.com:8080 http://proxy.example.com:8080 proxy.example.com:8080 proxy.example.com:8080 proxy.example.com:8080 proxy.example.com:8080 http://proxy.example.com:8080 http://proxy.example.com:8080"));
  }

  @Test
  void replacesMultipleCorrectlyProxyCustomPortOriginDefaultPortSpecificDirectionTwo() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "origin.example.com origin.example.com:80 http://origin.example.com http://origin.example.com:80 http://origin.example.com:80 http://origin.example.com origin.example.com:80 origin.example.com",
            "proxy.example.com:8080 proxy.example.com:8080 http://proxy.example.com:8080 http://proxy.example.com:8080 http://proxy.example.com:8080 http://proxy.example.com:8080 proxy.example.com:8080 proxy.example.com:8080"));
  }

  @Test
  void replacesMultipleCorrectlyProxyCustomPortOriginCustomPortSpecificDirectionOne() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080 origin.example.com:8080 origin.example.com origin.example.com origin.example.com:8080 http://origin.example.com:8080",
            "http://proxy.example.com:8080 proxy.example.com:8080 proxy.example.com proxy.example.com proxy.example.com:8080 http://proxy.example.com:8080"));
  }

  @Test
  void replacesMultipleCorrectlyProxyCustomPortOriginCustomPortSpecificDirectionTwo() {
    assertExpectedBody(
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "origin.example.com origin.example.com:8080 http://origin.example.com:8080 http://origin.example.com:8080 origin.example.com:8080 origin.example.com",
            "proxy.example.com proxy.example.com:8080 http://proxy.example.com:8080 http://proxy.example.com:8080 proxy.example.com:8080 proxy.example.com"));
  }

  @ParameterizedTest
  @MethodSource("allThatDoNotChange")
  void doesNotRewriteBodyWhenShouldNot(BodyRewriteTestCase testCase) {
    assertExpectedBody(testCase);
  }

  @ParameterizedTest
  @MethodSource("allThatDoChange")
  void rewritesBodyAsExpected(BodyRewriteTestCase testCase) {
    assertExpectedBody(testCase);
  }

  static Stream<BodyRewriteTestCase> simpleChanges() {
    return Stream.of(
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "http://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "ws://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:80",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "https://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "wss://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:443",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "http://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "ws://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:80",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "https://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "wss://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "http://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:80"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:443",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "http://proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:80",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "ws://proxy.example.com"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "http://origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "ws://origin.example.com",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com",
            "//origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:80",
            "//origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "https://origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "wss://origin.example.com",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com",
            "//origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "ws://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:443",
            "//origin.example.com",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "http://proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com:8080"),
        testCase(
            "http://proxy.example.com:8080",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "ws://proxy.example.com:8080"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "http://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "ws://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:80",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "https://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "wss://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:443",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "http://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "ws://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:80",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "https://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "wss://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "https://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:443"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:443",
            "//origin.example.com",
            "//proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "https://proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:443",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "wss://proxy.example.com"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "http://origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "http://origin.example.com:80",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "origin.example.com",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "origin.example.com:80",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "ws://origin.example.com",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com",
            "//origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "http://origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "http://origin.example.com:80",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "origin.example.com",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "origin.example.com:80",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "ws://origin.example.com",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "ws://origin.example.com:80",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:80",
            "//origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:8080",
            "http://origin.example.com:8080",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:8080",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:8080",
            "origin.example.com:8080",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "http://origin.example.com:8080",
            "ws://origin.example.com:8080",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "https://origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "https://origin.example.com:443",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "origin.example.com",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "origin.example.com:443",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "wss://origin.example.com",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com",
            "//origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "https://origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "https://origin.example.com:443",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "origin.example.com",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "origin.example.com:443",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "wss://origin.example.com",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "wss://origin.example.com:443",
            "wss://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:443",
            "//origin.example.com",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:4434",
            "https://origin.example.com:4434",
            "https://proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:4434",
            "origin.example.com",
            "proxy.example.com"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:4434",
            "origin.example.com:4434",
            "proxy.example.com:4434"),
        testCase(
            "https://proxy.example.com:4434",
            "https://origin.example.com:4434",
            "wss://origin.example.com:4434",
            "wss://proxy.example.com:4434"));
  }

  @ParameterizedTest
  @MethodSource("simpleChanges")
  void rewritesBodyAsExpectedForSimpleChange(BodyRewriteTestCase testCase) {
    assertExpectedBody(testCase);
  }

  private static void assertExpectedBody(BodyRewriteTestCase testCase) {
    var transformer = new ProxiedHostnameRewriteResponseTransformer();

    var serveEvent =
        ServeEvent.of(
                ImmutableRequest.create()
                    .withMethod(GET)
                    .withAbsoluteUrl(testCase.proxyUrl)
                    .build())
            .withResponseDefinition(aResponse().proxiedFrom(testCase.originUrl).build());

    var response =
        Response.response()
            .body(testCase.originResponseBody)
            .headers(new HttpHeaders(HttpHeader.httpHeader("Content-Type", "text/plain")))
            .build();

    var transformed = transformer.transform(response, serveEvent);

    assertThat(transformed.getBodyAsString()).isEqualTo(testCase.expectedResponseBody);
  }

  @Nested
  class CheckTestInputs {

    @Test
    void proxyUrlsAreUnique() {
      assertUnique(ProxiedHostnameRewriteResponseTransformerUnitTest::proxyUrls);
    }

    @Test
    void originUrlsAreUnique() {
      assertUnique(ProxiedHostnameRewriteResponseTransformerUnitTest::originUrls);
    }

    @Test
    void originResponseBodiesAreUnique() {
      assertUnique(ProxiedHostnameRewriteResponseTransformerUnitTest::originResponseBodies);
    }

    @Test
    void testsAreUnique() {
      assertUnique(() -> bodyRewriteTestCases().map(BodyRewriteTestCase::getInput));
    }

    @Test
    void simpleTestsAreUnique() {
      assertUnique(() -> simpleChanges().map(BodyRewriteTestCase::getInput));
    }

    @Test
    void simpleTestsAreAllInAllThatDoChange() {
      assertThat(allThatDoChange().map(BodyRewriteTestCase::getInput).collect(Collectors.toSet()))
          .containsAll(simpleChanges().map(BodyRewriteTestCase::getInput).toList());
    }

    @Test
    void expectedNumberOfTests() {
      assertThat(bodyRewriteTestCases())
          .hasSize(
              (proxyUrls().toList().size()
                      * originUrls().toList().size()
                      * originResponseBodies().toList().size())
                  + shouldNeverChange().toList().size());
    }

    private void assertUnique(Supplier<Stream<?>> items) {
      assertThat(items.get().collect(Collectors.toSet())).hasSameSizeAs(items.get().toList());
    }
  }

  static Stream<BodyRewriteTestCase> bodyRewriteTestCases() {
    return Stream.concat(allThatDoNotChange(), allThatDoChange());
  }

  private static Stream<BodyRewriteTestCase> allThatDoChange() {
    return changes().flatMap(SourceTestCase::toChangesTests).sorted();
  }

  static Stream<BodyRewriteTestCase> allThatDoNotChange() {
    return Streams.concat(
            shouldNeverChange(), doesNotChange().flatMap(SourceTestCase::toDoesNotChangeTests))
        .sorted();
  }

  static Stream<BodyRewriteTestCase> shouldNeverChange() {
    return proxyUrls()
        .flatMap(
            proxyUrl ->
                originUrls()
                    .flatMap(
                        originUrl ->
                            Stream.of(
                                    "notorigin.example.com",
                                    "notorigin.example.com:80",
                                    "notorigin.example.com:8080",
                                    "notorigin.example.com:443",
                                    "notorigin.example.com:4434",
                                    "notorigin.example.com ",
                                    "notorigin.example.com:80 ",
                                    "notorigin.example.com:8080 ",
                                    "notorigin.example.com:443 ",
                                    "notorigin.example.com:4434 ",
                                    "origin.example.com.au",
                                    "origin.example.comp")
                                .map(
                                    originResponseBody ->
                                        testCase(
                                            proxyUrl,
                                            originUrl,
                                            originResponseBody,
                                            originResponseBody))));
  }

  static Stream<SourceTestCase> changes() {
    return Stream.of(
        testCase("http://origin.example.com", " http://origin.example.com "),
        testCase("http://origin.example.com", " http://origin.example.com:80 "),
        testCase("http://origin.example.com", "http://origin.example.com"),
        testCase("http://origin.example.com", "http://origin.example.com:80"),
        testCase("http://origin.example.com", " ws://origin.example.com "),
        testCase("http://origin.example.com", " ws://origin.example.com:80 "),
        testCase("http://origin.example.com", "ws://origin.example.com"),
        testCase("http://origin.example.com", "ws://origin.example.com:80"),
        testCase("http://origin.example.com", "origin.example.com"),
        testCase("http://origin.example.com", "origin.example.com:80"),
        testCase("http://origin.example.com", " origin.example.com "),
        testCase("http://origin.example.com", " origin.example.com:80 "),
        testCase("http://origin.example.com", "//origin.example.com"),
        testCase("http://origin.example.com", " //origin.example.com "),
        testCase("http://origin.example.com:80", " http://origin.example.com "),
        testCase("http://origin.example.com:80", " http://origin.example.com:80 "),
        testCase("http://origin.example.com:80", "http://origin.example.com"),
        testCase("http://origin.example.com:80", "http://origin.example.com:80"),
        testCase("http://origin.example.com:80", " ws://origin.example.com "),
        testCase("http://origin.example.com:80", " ws://origin.example.com:80 "),
        testCase("http://origin.example.com:80", "ws://origin.example.com"),
        testCase("http://origin.example.com:80", "ws://origin.example.com:80"),
        testCase("http://origin.example.com:80", "origin.example.com"),
        testCase("http://origin.example.com:80", "origin.example.com:80"),
        testCase("http://origin.example.com:80", " origin.example.com "),
        testCase("http://origin.example.com:80", " origin.example.com:80 "),
        testCase("http://origin.example.com:80", "//origin.example.com"),
        testCase("http://origin.example.com:80", " //origin.example.com "),
        testCase("http://origin.example.com:8080", " http://origin.example.com:8080 "),
        testCase("http://origin.example.com:8080", "http://origin.example.com:8080"),
        testCase("http://origin.example.com:8080", " ws://origin.example.com:8080 "),
        testCase("http://origin.example.com:8080", "ws://origin.example.com:8080"),
        testCase("http://origin.example.com:8080", "origin.example.com:8080"),
        testCase("http://origin.example.com:8080", " origin.example.com:8080 "),
        testCase("http://origin.example.com:8080", "origin.example.com"),
        testCase("http://origin.example.com:8080", " origin.example.com "),
        testCase("https://origin.example.com", " https://origin.example.com "),
        testCase("https://origin.example.com", " https://origin.example.com:443 "),
        testCase("https://origin.example.com", "https://origin.example.com"),
        testCase("https://origin.example.com", "https://origin.example.com:443"),
        testCase("https://origin.example.com", " wss://origin.example.com "),
        testCase("https://origin.example.com", " wss://origin.example.com:443 "),
        testCase("https://origin.example.com", "wss://origin.example.com"),
        testCase("https://origin.example.com", "wss://origin.example.com:443"),
        testCase("https://origin.example.com", "origin.example.com:443"),
        testCase("https://origin.example.com", " origin.example.com:443 "),
        testCase("https://origin.example.com", "origin.example.com"),
        testCase("https://origin.example.com", " origin.example.com "),
        testCase("https://origin.example.com", "//origin.example.com"),
        testCase("https://origin.example.com", " //origin.example.com "),
        testCase("https://origin.example.com:443", " https://origin.example.com "),
        testCase("https://origin.example.com:443", " https://origin.example.com:443 "),
        testCase("https://origin.example.com:443", "https://origin.example.com"),
        testCase("https://origin.example.com:443", "https://origin.example.com:443"),
        testCase("https://origin.example.com:443", " wss://origin.example.com "),
        testCase("https://origin.example.com:443", " wss://origin.example.com:443 "),
        testCase("https://origin.example.com:443", "wss://origin.example.com"),
        testCase("https://origin.example.com:443", "wss://origin.example.com:443"),
        testCase("https://origin.example.com:443", "origin.example.com"),
        testCase("https://origin.example.com:443", "origin.example.com:443"),
        testCase("https://origin.example.com:443", " origin.example.com "),
        testCase("https://origin.example.com:443", " origin.example.com:443 "),
        testCase("https://origin.example.com:443", "//origin.example.com"),
        testCase("https://origin.example.com:443", " //origin.example.com "),
        testCase("https://origin.example.com:4434", " https://origin.example.com:4434 "),
        testCase("https://origin.example.com:4434", "https://origin.example.com:4434"),
        testCase("https://origin.example.com:4434", " wss://origin.example.com:4434 "),
        testCase("https://origin.example.com:4434", "wss://origin.example.com:4434"),
        testCase("https://origin.example.com:4434", "origin.example.com:4434"),
        testCase("https://origin.example.com:4434", " origin.example.com:4434 "),
        testCase("https://origin.example.com:4434", "origin.example.com"),
        testCase("https://origin.example.com:4434", " origin.example.com "));
  }

  static Stream<SourceTestCase> doesNotChange() {
    return Stream.of(
        testCase("http://origin.example.com", " http://origin.example.com:8080 "),
        testCase("http://origin.example.com", " https://origin.example.com "),
        testCase("http://origin.example.com", " https://origin.example.com:443 "),
        testCase("http://origin.example.com", " https://origin.example.com:4434 "),
        testCase("http://origin.example.com", "http://origin.example.com:8080"),
        testCase("http://origin.example.com", "https://origin.example.com"),
        testCase("http://origin.example.com", "https://origin.example.com:443"),
        testCase("http://origin.example.com", "https://origin.example.com:4434"),
        testCase("http://origin.example.com", " ws://origin.example.com:8080 "),
        testCase("http://origin.example.com", " wss://origin.example.com "),
        testCase("http://origin.example.com", " wss://origin.example.com:443 "),
        testCase("http://origin.example.com", " wss://origin.example.com:4434 "),
        testCase("http://origin.example.com", "ws://origin.example.com:8080"),
        testCase("http://origin.example.com", "wss://origin.example.com"),
        testCase("http://origin.example.com", "wss://origin.example.com:443"),
        testCase("http://origin.example.com", "wss://origin.example.com:4434"),
        testCase("http://origin.example.com", "origin.example.com:8080"),
        testCase("http://origin.example.com", "origin.example.com:443"),
        testCase("http://origin.example.com", "origin.example.com:4434"),
        testCase("http://origin.example.com", " origin.example.com:8080 "),
        testCase("http://origin.example.com", " origin.example.com:443 "),
        testCase("http://origin.example.com", " origin.example.com:4434 "),
        testCase("http://origin.example.com:80", " http://origin.example.com:8080 "),
        testCase("http://origin.example.com:80", " https://origin.example.com "),
        testCase("http://origin.example.com:80", " https://origin.example.com:443 "),
        testCase("http://origin.example.com:80", " https://origin.example.com:4434 "),
        testCase("http://origin.example.com:80", "http://origin.example.com:8080"),
        testCase("http://origin.example.com:80", "https://origin.example.com"),
        testCase("http://origin.example.com:80", "https://origin.example.com:443"),
        testCase("http://origin.example.com:80", "https://origin.example.com:4434"),
        testCase("http://origin.example.com:80", " ws://origin.example.com:8080 "),
        testCase("http://origin.example.com:80", " wss://origin.example.com "),
        testCase("http://origin.example.com:80", " wss://origin.example.com:443 "),
        testCase("http://origin.example.com:80", " wss://origin.example.com:4434 "),
        testCase("http://origin.example.com:80", "ws://origin.example.com:8080"),
        testCase("http://origin.example.com:80", "wss://origin.example.com"),
        testCase("http://origin.example.com:80", "wss://origin.example.com:443"),
        testCase("http://origin.example.com:80", "wss://origin.example.com:4434"),
        testCase("http://origin.example.com:80", "origin.example.com:8080"),
        testCase("http://origin.example.com:80", "origin.example.com:443"),
        testCase("http://origin.example.com:80", "origin.example.com:4434"),
        testCase("http://origin.example.com:80", " origin.example.com:8080 "),
        testCase("http://origin.example.com:80", " origin.example.com:443 "),
        testCase("http://origin.example.com:80", " origin.example.com:4434 "),
        testCase("http://origin.example.com:8080", " http://origin.example.com "),
        testCase("http://origin.example.com:8080", " http://origin.example.com:80 "),
        testCase("http://origin.example.com:8080", " https://origin.example.com "),
        testCase("http://origin.example.com:8080", " https://origin.example.com:443 "),
        testCase("http://origin.example.com:8080", " https://origin.example.com:4434 "),
        testCase("http://origin.example.com:8080", "http://origin.example.com"),
        testCase("http://origin.example.com:8080", "http://origin.example.com:80"),
        testCase("http://origin.example.com:8080", "https://origin.example.com"),
        testCase("http://origin.example.com:8080", "https://origin.example.com:443"),
        testCase("http://origin.example.com:8080", "https://origin.example.com:4434"),
        testCase("http://origin.example.com:8080", " ws://origin.example.com "),
        testCase("http://origin.example.com:8080", " ws://origin.example.com:80 "),
        testCase("http://origin.example.com:8080", " wss://origin.example.com "),
        testCase("http://origin.example.com:8080", " wss://origin.example.com:443 "),
        testCase("http://origin.example.com:8080", " wss://origin.example.com:4434 "),
        testCase("http://origin.example.com:8080", "ws://origin.example.com"),
        testCase("http://origin.example.com:8080", "ws://origin.example.com:80"),
        testCase("http://origin.example.com:8080", "wss://origin.example.com"),
        testCase("http://origin.example.com:8080", "wss://origin.example.com:443"),
        testCase("http://origin.example.com:8080", "wss://origin.example.com:4434"),
        testCase("http://origin.example.com:8080", "origin.example.com:80"),
        testCase("http://origin.example.com:8080", "origin.example.com:443"),
        testCase("http://origin.example.com:8080", "origin.example.com:4434"),
        testCase("http://origin.example.com:8080", " origin.example.com:80 "),
        testCase("http://origin.example.com:8080", " origin.example.com:443 "),
        testCase("http://origin.example.com:8080", " origin.example.com:4434 "),
        testCase("http://origin.example.com:8080", "//origin.example.com"),
        testCase("http://origin.example.com:8080", " //origin.example.com "),
        testCase("https://origin.example.com", " http://origin.example.com "),
        testCase("https://origin.example.com", " http://origin.example.com:80 "),
        testCase("https://origin.example.com", " http://origin.example.com:8080 "),
        testCase("https://origin.example.com", " https://origin.example.com:4434 "),
        testCase("https://origin.example.com", "http://origin.example.com"),
        testCase("https://origin.example.com", "http://origin.example.com:80"),
        testCase("https://origin.example.com", "http://origin.example.com:8080"),
        testCase("https://origin.example.com", "https://origin.example.com:4434"),
        testCase("https://origin.example.com", " ws://origin.example.com "),
        testCase("https://origin.example.com", " ws://origin.example.com:80 "),
        testCase("https://origin.example.com", " ws://origin.example.com:8080 "),
        testCase("https://origin.example.com", " wss://origin.example.com:4434 "),
        testCase("https://origin.example.com", "ws://origin.example.com"),
        testCase("https://origin.example.com", "ws://origin.example.com:80"),
        testCase("https://origin.example.com", "ws://origin.example.com:8080"),
        testCase("https://origin.example.com", "wss://origin.example.com:4434"),
        testCase("https://origin.example.com", "origin.example.com:80"),
        testCase("https://origin.example.com", "origin.example.com:8080"),
        testCase("https://origin.example.com", "origin.example.com:4434"),
        testCase("https://origin.example.com", " origin.example.com:80 "),
        testCase("https://origin.example.com", " origin.example.com:8080 "),
        testCase("https://origin.example.com", " origin.example.com:4434 "),
        testCase("https://origin.example.com:443", " http://origin.example.com "),
        testCase("https://origin.example.com:443", " http://origin.example.com:80 "),
        testCase("https://origin.example.com:443", " http://origin.example.com:8080 "),
        testCase("https://origin.example.com:443", " https://origin.example.com:4434 "),
        testCase("https://origin.example.com:443", "http://origin.example.com"),
        testCase("https://origin.example.com:443", "http://origin.example.com:80"),
        testCase("https://origin.example.com:443", "http://origin.example.com:8080"),
        testCase("https://origin.example.com:443", "https://origin.example.com:4434"),
        testCase("https://origin.example.com:443", " ws://origin.example.com "),
        testCase("https://origin.example.com:443", " ws://origin.example.com:80 "),
        testCase("https://origin.example.com:443", " ws://origin.example.com:8080 "),
        testCase("https://origin.example.com:443", " wss://origin.example.com:4434 "),
        testCase("https://origin.example.com:443", "ws://origin.example.com"),
        testCase("https://origin.example.com:443", "ws://origin.example.com:80"),
        testCase("https://origin.example.com:443", "ws://origin.example.com:8080"),
        testCase("https://origin.example.com:443", "wss://origin.example.com:4434"),
        testCase("https://origin.example.com:443", "origin.example.com:80"),
        testCase("https://origin.example.com:443", "origin.example.com:8080"),
        testCase("https://origin.example.com:443", "origin.example.com:4434"),
        testCase("https://origin.example.com:443", " origin.example.com:80 "),
        testCase("https://origin.example.com:443", " origin.example.com:8080 "),
        testCase("https://origin.example.com:443", " origin.example.com:4434 "),
        testCase("https://origin.example.com:4434", " http://origin.example.com "),
        testCase("https://origin.example.com:4434", " http://origin.example.com:80 "),
        testCase("https://origin.example.com:4434", " http://origin.example.com:8080 "),
        testCase("https://origin.example.com:4434", " https://origin.example.com "),
        testCase("https://origin.example.com:4434", " https://origin.example.com:443 "),
        testCase("https://origin.example.com:4434", "http://origin.example.com"),
        testCase("https://origin.example.com:4434", "http://origin.example.com:80"),
        testCase("https://origin.example.com:4434", "http://origin.example.com:8080"),
        testCase("https://origin.example.com:4434", "https://origin.example.com"),
        testCase("https://origin.example.com:4434", "https://origin.example.com:443"),
        testCase("https://origin.example.com:4434", " ws://origin.example.com "),
        testCase("https://origin.example.com:4434", " ws://origin.example.com:80 "),
        testCase("https://origin.example.com:4434", " ws://origin.example.com:8080 "),
        testCase("https://origin.example.com:4434", " wss://origin.example.com "),
        testCase("https://origin.example.com:4434", " wss://origin.example.com:443 "),
        testCase("https://origin.example.com:4434", "ws://origin.example.com"),
        testCase("https://origin.example.com:4434", "ws://origin.example.com:80"),
        testCase("https://origin.example.com:4434", "ws://origin.example.com:8080"),
        testCase("https://origin.example.com:4434", "wss://origin.example.com"),
        testCase("https://origin.example.com:4434", "wss://origin.example.com:443"),
        testCase("https://origin.example.com:4434", "origin.example.com:80"),
        testCase("https://origin.example.com:4434", "origin.example.com:8080"),
        testCase("https://origin.example.com:4434", "origin.example.com:443"),
        testCase("https://origin.example.com:4434", " origin.example.com:80 "),
        testCase("https://origin.example.com:4434", " origin.example.com:8080 "),
        testCase("https://origin.example.com:4434", " origin.example.com:443 "),
        testCase("https://origin.example.com:4434", "//origin.example.com"),
        testCase("https://origin.example.com:4434", " //origin.example.com "));
  }

  static Stream<String> proxyUrls() {
    return Stream.of(
        "http://proxy.example.com",
        "http://proxy.example.com:80",
        "http://proxy.example.com:8080",
        "https://proxy.example.com",
        "https://proxy.example.com:443",
        "https://proxy.example.com:4434");
  }

  static Stream<String> originUrls() {
    return Stream.of(
        "http://origin.example.com",
        "http://origin.example.com:80",
        "http://origin.example.com:8080",
        "https://origin.example.com",
        "https://origin.example.com:443",
        "https://origin.example.com:4434");
  }

  static Stream<String> originResponseBodies() {
    return Stream.of(
        " http://origin.example.com ",
        " http://origin.example.com:80 ",
        " http://origin.example.com:8080 ",
        " https://origin.example.com ",
        " https://origin.example.com:443 ",
        " https://origin.example.com:4434 ",
        "http://origin.example.com",
        "http://origin.example.com:80",
        "http://origin.example.com:8080",
        "https://origin.example.com",
        "https://origin.example.com:443",
        "https://origin.example.com:4434",
        " ws://origin.example.com ",
        " ws://origin.example.com:80 ",
        " ws://origin.example.com:8080 ",
        " wss://origin.example.com ",
        " wss://origin.example.com:443 ",
        " wss://origin.example.com:4434 ",
        "ws://origin.example.com",
        "ws://origin.example.com:80",
        "ws://origin.example.com:8080",
        "wss://origin.example.com",
        "wss://origin.example.com:443",
        "wss://origin.example.com:4434",
        "origin.example.com",
        "origin.example.com:80",
        "origin.example.com:8080",
        "origin.example.com:443",
        "origin.example.com:4434",
        " origin.example.com ",
        " origin.example.com:80 ",
        " origin.example.com:8080 ",
        " origin.example.com:443 ",
        " origin.example.com:4434 ",
        "//origin.example.com",
        " //origin.example.com ");
  }

  static SourceTestCase testCase(String originUrl, String originResponseBody) {
    return new SourceTestCase(originUrl, originResponseBody);
  }

  record SourceTestCase(String originUrl, String originResponseBody) {

    Stream<BodyRewriteTestCase> toChangesTests() {
      return proxyUrls()
          .map(
              proxyUrlStr -> {
                URI originUrl = URI.create(this.originUrl);
                URI proxyUrl = URI.create(proxyUrlStr);
                int originDefaultPort = getDefaultPort(originUrl.getScheme());
                var proxyDefaultPort = getDefaultPort(proxyUrl.getScheme());
                var originWsScheme = getWebSocketScheme(originUrl);
                var proxyWsScheme = getWebSocketScheme(proxyUrl);
                String expectedResponseBody =
                    originResponseBody
                        .replace(originUrl.getScheme(), proxyUrl.getScheme())
                        .replace(originWsScheme, proxyWsScheme);

                if (originUrl.getPort() == -1 || originUrl.getPort() == originDefaultPort) {
                  if (proxyUrl.getPort() == -1 || proxyUrl.getPort() == proxyDefaultPort) {
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "origin.example.com:" + originDefaultPort,
                            "proxy.example.com:" + proxyDefaultPort);
                    expectedResponseBody =
                        expectedResponseBody.replace("origin.example.com", "proxy.example.com");
                  } else {
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "origin.example.com:" + originDefaultPort,
                            "proxy.example.com:" + proxyUrl.getPort());
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "://origin.example.com", "://proxy.example.com:" + proxyUrl.getPort());
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "//origin.example.com",
                            proxyUrl.getScheme() + "://proxy.example.com:" + proxyUrl.getPort());
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "origin.example.com", "proxy.example.com:" + proxyUrl.getPort());
                  }
                } else {
                  if (proxyUrl.getPort() == -1 || proxyUrl.getPort() == proxyDefaultPort) {
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "origin.example.com:" + originUrl.getPort(), "proxy.example.com");
                  } else {
                    expectedResponseBody =
                        expectedResponseBody.replace(
                            "origin.example.com:" + originUrl.getPort(),
                            "proxy.example.com:" + proxyUrl.getPort());
                  }
                  expectedResponseBody =
                      expectedResponseBody.replace("origin.example.com", "proxy.example.com");
                }
                return testCase(
                    proxyUrlStr, this.originUrl, originResponseBody, expectedResponseBody);
              });
    }

    Stream<BodyRewriteTestCase> toDoesNotChangeTests() {
      return proxyUrls()
          .map(proxyUrl -> testCase(proxyUrl, originUrl, originResponseBody, originResponseBody));
    }
  }

  static BodyRewriteTestCase testCase(
      String proxyUrl, String originUrl, String originResponseBody, String expectedResponseBody) {
    return new BodyRewriteTestCase(
        proxyUrl + "/", originUrl, originResponseBody, expectedResponseBody);
  }

  record BodyRewriteTestCase(
      String proxyUrl, String originUrl, String originResponseBody, String expectedResponseBody)
      implements Comparable<BodyRewriteTestCase> {

    private static final Comparator<BodyRewriteTestCase> ORDER =
        Comparator.comparing(BodyRewriteTestCase::proxyUrl)
            .thenComparing(BodyRewriteTestCase::originUrl)
            .thenComparing(BodyRewriteTestCase::originResponseBody);

    @Override
    public int compareTo(@NonNull BodyRewriteTestCase other) {
      return ORDER.compare(this, other);
    }

    public BodyRewriteTestCaseInput getInput() {
      return new BodyRewriteTestCaseInput(proxyUrl, originUrl, originResponseBody);
    }
  }

  record BodyRewriteTestCaseInput(String proxyUrl, String originUrl, String originResponseBody) {}

  private static String getWebSocketScheme(URI proxyUrl) {
    return proxyUrl.getScheme().equals("https") ? "wss" : "ws";
  }

  private static int getDefaultPort(String scheme) {
    return switch (scheme) {
      case "http", "ws" -> 80;
      case "https", "wss" -> 443;
      default -> -1;
    };
  }
}
