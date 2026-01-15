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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.wiremock.url.PercentEncodedStringParserInvariantTests.generateEncodeDecodeInvariantTests;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

public class PathTests {

  @Nested
  class Parse {

    static Stream<String> validPaths() {
      return Stream.of(
          // Empty and simple paths
          "",
          "/",
          "/path",
          "/path/to/resource",
          "relative/path",
          "relative",

          // Paths with segments
          "/one",
          "/one/two",
          "/one/two/three",
          "one/two/three",

          // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
          "/path-name",
          "/path.name",
          "/path_name",
          "/path~name",
          "/Path123",
          "/test-path_123.name~test",

          // Sub-delimiters (!$&'()*+,;=)
          "/path!name",
          "/path$name",
          "/path&name",
          "/path'name",
          "/path(name)",
          "/path*name",
          "/path+name",
          "/path,name",
          "/path;name",
          "/path=name",

          // Colon and at-sign
          "/path:name",
          "/path@name",
          "/user:pass@host",
          "/::",
          "/@@@",

          // Percent-encoded characters
          "/%20", // space
          "/path%20name", // path name
          "/path%2Fname", // path/name (encoded slash)
          "/%C3%A9", // é
          "/caf%C3%A9", // café
          "/100%25complete", // 100%complete

          // Characters allowed by RFC 3986 but often considered special
          "/path{brace}",
          "/path[bracket]",
          "/<angle>",
          "/path|pipe",
          "/path\\backslash",
          "/path^caret",
          "/path`grave",

          // Spaces and control characters (permissive parsing)
          "/path name", // unencoded space
          "/path\tname", // tab

          // Complex combinations
          "/api/v1/users",
          "/files/document.pdf",
          "/path/with/many/segments/123",
          "/path%20with%20spaces/and-dashes",
          "/user@example.com:8080/resource",
          "/path!$&'()*+,;=/segment",

          // Edge cases
          "//double//slashes",
          "/trailing/slash/",
          "/./dot",
          "/../dotdot",
          "/single-segment",

          // Relative paths
          ".",
          "..",
          "./relative",
          "../parent",
          "../../grandparent",

          // Invalid percent encoding (still accepted - permissive parser)
          "/%", // incomplete
          "/%2", // incomplete
          "/%GG", // invalid hex
          "/path%ZZname"); // invalid hex
    }

    @TestFactory
    Stream<DynamicTest> invariants() {
      return StringParserInvariantTests.generateInvariantTests(
          PathParser.INSTANCE, validPaths().toList());
    }

    static Stream<String> illegalPaths() {
      return Stream.of(
          // Query and fragment characters (not delimiters in path context for this parser)
          "/path?with?questions", "/path#with#hashes", "/path#fragment");
    }

    @ParameterizedTest
    @MethodSource("illegalPaths")
    void rejects_illegal_path(String illegalPath) {
      assertThatExceptionOfType(IllegalPath.class)
          .isThrownBy(() -> Path.parse(illegalPath))
          .withMessage("Illegal path: `" + illegalPath + "`")
          .extracting(IllegalPath::getIllegalValue)
          .isEqualTo(illegalPath);
    }
  }

  @Nested
  class Normalise {

    private static final List<Entry<Path, Path>> normaliseTestCases =
        List.of(
            entry("", ""),
            entry(".", ""),
            entry("..", ""),
            entry("a", "a"),
            entry("/", "/"),
            entry("/%2F/", "/%2F/"),
            entry("/%2f/", "/%2F/"),
            entry("/.", "/"),
            entry("/..", "/"),
            entry("/a", "/a"),
            entry("./", ""),
            entry("../", ""),
            entry("a/", "a/"),
            entry("//", "//"),
            entry("/./", "/"),
            entry("/../", "/"),
            entry("/../../../../", "/"),
            entry("/a/", "/a/"),
            entry("/foo/bar/../ton", "/foo/ton"),
            entry("//a//../..//foo", "///foo"),
            entry("/﻿/foo", "/%EF%BB%BF/foo"),
            entry("/foo%2Â©zbar", "/foo%252%C3%82%C2%A9zbar"),
            entry("/你好你好", "/%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD"),
            entry("/‮/foo/‭/bar", "/%E2%80%AE/foo/%E2%80%AD/bar"),
            entry("/\"quoted\"", "/%22quoted%22"),
            entry("/￿y", "/%EF%BF%BFy"),
            entry("/‥/foo", "/%E2%80%A5/foo"),
            entry(
                "/ !\"$%&'()*+,-./:;<=>@[\\]^_`{|}~",
                "/%20!%22$%25&'()*+,-./:;%3C=%3E@%5B%5C%5D%5E_%60%7B%7C%7D~"),
            entry("/�", "/%EF%BF%BD"),
            entry("/foo/../../..", "/"),
            entry("/foo/%2e./%2e%2e/.%2e/%2e.bar", "/..bar"),
            entry("%fF", "%FF"),
            entry("%Ff", "%FF"),
            entry("%41", "A"),
            entry("%5A", "Z"),
            entry("%5a", "Z"),
            entry("/foo/%2e", "/foo/"),
            entry("/foo/.", "/foo/"),
            entry("/./y:", "/y:"),
            entry("/foo/./", "/foo/"),
            entry("/a/../b", "/b"),
            entry("/foo/bar//../..", "/foo/"),
            entry("/aaa/bbb/%2e%2e", "/aaa/"),
            entry("/./Y:", "/Y:"),
            entry("", ""),
            entry("/foo/../../../ton", "/ton"),
            entry("/./Y", "/Y"),
            entry("/./.foo", "/.foo"),
            entry("/foo/%2E/html", "/foo/html"),
            entry("/foo/bar/../ton/../../a", "/a"),
            entry("/foo/bar/../", "/foo/"),
            entry("//a//../..//", "///"),
            entry("/./y", "/y"),
            entry("////../..", "//"),
            entry("/foo/bar/..", "/foo/"),
            entry("/././foo", "/foo"),
            entry("/foo/bar//..", "/foo/bar/"),
            entry("/a/b/c/./../../g", "/a/g"),
            entry("mid/content=5/../6", "mid/6"));

    @TestFactory
    Stream<DynamicTest> normalises_path_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normaliseTestCases.stream()
              .filter(testCase -> !testCase.getKey().equals(testCase.getValue()))
              .map(
                  testCase ->
                      new NormalisableInvariantTests.NormalisationCase<>(
                          testCase.getKey(), testCase.getValue()))
              .toList());
    }

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          normaliseTestCases.stream().map(Entry::getValue).collect(Collectors.toSet()));
    }
  }

  @Nested
  class Resolve {

    private static final List<Entry<Path, Path>> rfc3986TestCases =
        List.of(
            entry("g", "/b/c/g"),
            entry("./g", "/b/c/g"),
            entry("g/", "/b/c/g/"),
            entry("/g", "/g"),
            entry(";x", "/b/c/;x"),
            entry("g;x", "/b/c/g;x"),
            entry("", "/b/c/d;p"),
            entry(".", "/b/c/"),
            entry("./", "/b/c/"),
            entry("..", "/b/"),
            entry("../", "/b/"),
            entry("../g", "/b/g"),
            entry("../..", "/"),
            entry("../../", "/"),
            entry("../../g", "/g"),
            entry("../../../g", "/g"),
            entry("../../../../g", "/g"),
            entry("/./g", "/g"),
            entry("/../g", "/g"),
            entry("g.", "/b/c/g."),
            entry(".g", "/b/c/.g"),
            entry("g..", "/b/c/g.."),
            entry("..g", "/b/c/..g"),
            entry("./../g", "/b/g"),
            entry("./g/.", "/b/c/g/"),
            entry("g/./h", "/b/c/g/h"),
            entry("g/../h", "/b/c/h"),
            entry("g;x=1/./y", "/b/c/g;x=1/y"),
            entry("g;x=1/../y", "/b/c/y"));

    private static final Path original = Path.parse("/b/c/d;p");

    @ParameterizedTest
    @FieldSource("rfc3986TestCases")
    void resolvePath(Entry<Path, Path> testCase) {
      assertThat(original.resolve(testCase.getKey())).isEqualTo(testCase.getValue());
    }

    @Test
    void resolvePathSingle() {
      resolvePath(entry("", "/b/c/d;p"));
    }
  }

  @Nested
  class Codec {

    static final List<String> pathsWithoutPercentEncoding =
        List.of("", "/", "/path", "/path/to/resource", "relative/path", "/user:pass@host");

    static final List<CodecCase> codecCases =
        List.of(
            new CodecCase("/%20", "/ "),
            new CodecCase("/path%20name", "/path name"),
            new CodecCase("/path%2Fname", "/path/name"),
            new CodecCase("/%C3%A9", "/é"),
            new CodecCase("/caf%C3%A9", "/café"),
            new CodecCase("/100%25complete", "/100%complete"),
            new CodecCase("/path%20with%20spaces/and-dashes", "/path with spaces/and-dashes"),
            new CodecCase("/user%40example.com", "/user@example.com"),
            new CodecCase("/hello%20world", "/hello world"),
            new CodecCase("/test%3Avalue", "/test:value"));

    @ParameterizedTest
    @FieldSource("pathsWithoutPercentEncoding")
    void returns_same_string_for_path_without_percent_encoding(String pathString) {
      Path path = Path.parse(pathString);
      assertThat(path.decode()).isEqualTo(pathString);
    }

    @ParameterizedTest
    @FieldSource("codecCases")
    void decodes_percent_encoded_path_correctly(CodecCase testCase) {
      Path path = Path.parse(testCase.encoded());
      assertThat(path.decode()).isEqualTo(testCase.decoded());
    }

    @TestFactory
    Stream<DynamicTest> encode_decode_invariants() {
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
  }

  @Nested
  class EncodedSlash {

    @Test
    void parse_with_encoded_slash() {
      Path path = Path.parse("/%2F/");
      assertThat(path.toString()).isEqualTo("/%2F/");
    }

    @Test
    void decode_with_encoded_slash() {
      Path path = Path.parse("/%2F/");
      assertThat(path.decode()).isEqualTo("///");
    }

    @Test
    void encode_with_encoded_slash() {
      Path path = Path.encode("/%2F/");
      assertThat(path).isEqualTo(Path.parse("/%252F/"));
    }

    @Test
    void normalise_with_encoded_slash() {
      Path path = Path.parse("/%2F/");
      assertThat(path.normalise()).isEqualTo(path);
    }
  }

  static Entry<Path, Path> entry(String nonNormalised, String normalised) {
    return Map.entry(Path.parse(nonNormalised), Path.parse(normalised));
  }
}
