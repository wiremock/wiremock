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

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class PercentEncodedCharSequenceParserInvariantTests {

  @TestFactory
  Stream<DynamicTest> passwordParser() {
    return generateEncodeDecodeInvariantTests(
        PasswordParser.INSTANCE,
        Stream.of("foo", "bar", "test123", "hello world", "user@example", "café", "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> usernameParser() {
    return generateEncodeDecodeInvariantTests(
        UsernameParser.INSTANCE,
        Stream.of("foo", "bar", "test123", "hello world", "user@example", "café", "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> userInfoParser() {
    return generateEncodeDecodeInvariantTests(
        UserInfoParser.INSTANCE,
        Stream.of(
            "foo", "foo:bar", "test123", "hello world", "user@example:password", "café", "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> hostParser() {
    return generateEncodeDecodeInvariantTests(
        HostParser.INSTANCE,
        Stream.of("foo", "example.com", "test123", "hello world", "café", "example.org", "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> fragmentParser() {
    return generateEncodeDecodeInvariantTests(
        FragmentParser.INSTANCE,
        Stream.of(
            "foo",
            "bar",
            "test123",
            "hello world",
            "section-1",
            "café",
            "path/to/section",
            "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> queryParser() {
    return generateEncodeDecodeInvariantTests(
        QueryParser.INSTANCE,
        Stream.of(
            "foo",
            "bar",
            "key=value",
            "hello world",
            "q=café",
            "param1=value1&param2=value2",
            "こんにちは"));
  }

  @TestFactory
  Stream<DynamicTest> pathParser() {
    return generateEncodeDecodeInvariantTests(
        PathParser.INSTANCE,
        Stream.of(
            "foo",
            "/bar",
            "/path/to/resource",
            "hello world",
            "/café",
            "/path/with spaces",
            "こんにちは"));
  }

  static <T extends PercentEncoded> Stream<DynamicTest> generateEncodeDecodeInvariantTests(
      PercentEncodedCharSequenceParser<T> parser, Stream<String> inputs) {
    return inputs.map(
        input ->
            DynamicTest.dynamicTest(
                "encode(\"" + input + "\").decode() == \"" + input + "\"",
                () -> {
                  T encoded = parser.encode(input);
                  String decoded = encoded.decode();
                  assertThat(decoded).isEqualTo(input);
                }));
  }
}
