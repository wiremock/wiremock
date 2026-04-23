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
package org.wiremock.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openjdk.jmh.annotations.*;
import org.wiremock.url.whatwg.SimpleFailureWhatWGUrlTestCase;
import org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

/**
 * JMH benchmark comparing the performance of {@code java.net.URI} and {@code org.wiremock.url.Uri}
 * parsing.
 *
 * <p>Run with: {@code ./gradlew :wiremock-url:jmh}
 *
 * <p>This benchmark tests two scenarios:
 *
 * <ul>
 *   <li><strong>Valid parsing</strong>: URIs successfully parseable by both implementations
 *   <li><strong>Error handling</strong>: URIs rejected by both implementations (exception handling
 *       performance)
 * </ul>
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(
    value = 2,
    jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ParsePerformanceBenchmark {

  private static final Set<String> allUris =
      WhatWGUrlTestManagement.testData.stream()
          .flatMap(
              test -> {
                if (test instanceof SuccessWhatWGUrlTestCase success) {
                  return Stream.of(
                      success.input(), success.base(), success.href(), success.origin());
                } else if (test instanceof SimpleFailureWhatWGUrlTestCase failure) {
                  return Stream.of(failure.input(), failure.base());
                } else {
                  return Stream.of(test.input());
                }
              })
          .filter(t -> t != null && !t.isEmpty() && !t.equals("null"))
          .collect(Collectors.toSet());

  private static final List<String> parseableByBoth =
      allUris.stream()
          .filter(
              uriStr -> {
                try {
                  new URI(uriStr);
                } catch (URISyntaxException e) {
                  return false;
                }
                try {
                  Uri.parse(uriStr);
                } catch (IllegalUri e) {
                  return false;
                }
                return true;
              })
          .toList();

  private static final List<String> notParseableByEither =
      allUris.stream()
          .filter(
              uriStr -> {
                try {
                  new URI(uriStr);
                  return false;
                } catch (URISyntaxException ignored) {
                  // means we can't parse it
                }
                try {
                  Uri.parse(uriStr);
                  return false;
                } catch (IllegalUri ignored) {
                  // means we can't parse it
                }
                return true;
              })
          .toList();

  @State(Scope.Thread)
  public static class ParseableBenchmarkState {
    private int index = 0;

    @Setup(Level.Invocation)
    public void setUp() {
      index = (index + 1) % parseableByBoth.size();
    }

    public String getCurrentUri() {
      return parseableByBoth.get(index);
    }
  }

  /**
   * Benchmark for parsing URIs using {@code java.net.URI.create}.
   *
   * @param state the benchmark state containing the current URI to parse
   * @return the parsed URI (to prevent dead code elimination)
   */
  @Benchmark
  public URI benchmarkJavaNetUri(ParseableBenchmarkState state) {
    return URI.create(state.getCurrentUri());
  }

  /**
   * Benchmark for parsing URIs using {@code org.wiremock.url.Uri.parse}.
   *
   * @param state the benchmark state containing the current URI to parse
   * @return the parsed Uri (to prevent dead code elimination)
   */
  @Benchmark
  public Uri benchmarkWireMockUri(ParseableBenchmarkState state) {
    return Uri.parse(state.getCurrentUri());
  }

  @State(Scope.Thread)
  public static class InvalidBenchmarkState {
    private int index = 0;

    @Setup(Level.Invocation)
    public void setUp() {
      index = (index + 1) % notParseableByEither.size();
    }

    public String getCurrentUri() {
      return notParseableByEither.get(index);
    }
  }

  /**
   * Benchmark for error handling when parsing invalid URIs using {@code java.net.URI}.
   *
   * <p>Tests the performance of exception handling when URIs are rejected by java.net.URI.
   *
   * @param state the benchmark state containing the current invalid URI
   * @return null (exception is expected)
   */
  @Benchmark
  public URI benchmarkJavaNetUriErrorHandling(InvalidBenchmarkState state) {
    try {
      return new URI(state.getCurrentUri());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Benchmark for error handling when parsing invalid URIs using {@code
   * org.wiremock.url.Uri.parse}.
   *
   * <p>Tests the performance of exception handling when URIs are rejected by Uri.
   *
   * @param state the benchmark state containing the current invalid URI
   * @return null (exception is expected)
   */
  @Benchmark
  public Uri benchmarkWireMockUriErrorHandling(InvalidBenchmarkState state) {
    try {
      return Uri.parse(state.getCurrentUri());
    } catch (IllegalUri e) {
      return null;
    }
  }
}
