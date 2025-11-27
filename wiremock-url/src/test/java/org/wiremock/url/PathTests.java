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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
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
}
