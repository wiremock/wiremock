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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

public class PathTests {

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

  static Stream<String> invalidPaths() {
    return Stream.of(
        // Query and fragment characters (not delimiters in path context for this parser)
        "/path?with?questions", "/path#with#hashes", "/path#fragment");
  }

  @ParameterizedTest
  @MethodSource("invalidPaths")
  void throws_exception_for_invalid_userinfo(String invalidPath) {
    assertThatExceptionOfType(IllegalPath.class)
        .isThrownBy(() -> Path.parse(invalidPath))
        .withMessage("Illegal path: `" + invalidPath + "`")
        .extracting(IllegalPath::getIllegalValue)
        .isEqualTo(invalidPath);
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return CharSequenceParserInvariantTests.generateInvariantTests(
        PathParser.INSTANCE, validPaths().toList());
  }

  private static final List<Entry<Path, Path>> normaliseTestCases =
      List.of(
          entry("", ""),
          entry(".", ""),
          entry("..", ""),
          entry("a", "a"),
          entry("/", "/"),
          entry("/.", "/"),
          entry("/..", "/"),
          entry("/a", "/a"),
          entry("./", ""),
          entry("../", ""),
          entry("a/", "a/"),
          entry("//", "//"),
          entry("/./", "/"),
          entry("/../", "/"),
          entry("/a/", "/a/"),
          entry("/foo/bar/../ton", "/foo/ton"),
          entry("//a//../..//foo", "///foo"),
          entry("/﻿/foo", "/%EF%BB%BF/foo"),
          entry("/foo%2Â©zbar", "/foo%2%C3%82%C2%A9zbar"),
          entry("/你好你好", "/%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD"),
          entry("/‮/foo/‭/bar", "/%E2%80%AE/foo/%E2%80%AD/bar"),
          entry("/\"quoted\"", "/%22quoted%22"),
          entry("/￿y", "/%EF%BF%BFy"),
          entry("/‥/foo", "/%E2%80%A5/foo"),
          entry(
              "/ !\"$%&'()*+,-./:;<=>@[\\]^_`{|}~",
              "/%20!%22$%&'()*+,-./:;%3C=%3E@[\\]%5E_%60%7B|%7D~"),
          entry("/�", "/%EF%BF%BD"),
          entry("/foo/../../..", "/"),
          entry("/foo/%2e./%2e%2e/.%2e/%2e.bar", "/%2e.bar"),
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

  private static Entry<Path, Path> entry(String nonNormalised, String normalised) {
    return Map.entry(Path.parse(nonNormalised), Path.parse(normalised));
  }

  @ParameterizedTest
  @FieldSource("normaliseTestCases")
  void pathNormalises(Entry<Path, Path> testCase) {
    assertThat(testCase.getKey().normalise()).isEqualTo(testCase.getValue());
  }

  @Test
  void pathNormalisesSingle() {
    pathNormalises(entry("", ""));
  }

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
