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

import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class FragmentTests {

  static Stream<String> validFragment() {
    return Stream.of(
        // Empty and simple fragments
        "",
        "section",
        "top",
        "introduction",
        "chapter-1",
        "heading123",

        // Unreserved characters (alphanumeric, hyphen, period, underscore, tilde)
        "section-name",
        "section.name",
        "section_name",
        "section~name",
        "Section123",
        "test-section_123.name~test",

        // Sub-delimiters (!$&'()*+,;=)
        "section!name",
        "section$name",
        "section&name",
        "section'name",
        "section(name)",
        "section*name",
        "section+name",
        "section,name",
        "section;name",
        "section=name",

        // Colon and at-sign
        "time:12:30",
        "user@example",
        "ref:v1.2.3",
        "id:123",

        // Forward slash and question mark
        "path/to/section",
        "section?detail",
        "part/1?view=full",
        "nested/section/subsection",

        // Percent-encoded characters
        "%20", // space
        "section%20name", // section name
        "caf%C3%A9", // café
        "%C3%A9ric", // éric
        "100%25", // 100%
        "path%2Fsection", // path/section

        // Characters that extend beyond RFC 3986
        "section{name}",
        "data[123]",
        "tag<value>",
        "section|name",
        "back\\slash",
        "caret^name",
        "grave`name",

        // Spaces and special characters (permissive)
        "section name", // unencoded space
        "section#nested", // hash
        "section[1]", // brackets
        "section<name>", // angle brackets

        // Complex combinations
        "api/v1/users/123",
        "section:subsection:detail",
        "user@domain.com/profile",
        "heading-1.2.3?expanded=true",
        "doc%20section/page%202",

        // IDs and references
        "L123",
        "line-456",
        "ref123",
        "footnote1",

        // Edge cases
        "//double//slashes",
        "trailing/slash/",
        "multiple?question?marks",
        "dots...",
        "dashes---",

        // JSON-like fragments (percent-encoded and unencoded)
        "%7B%22key%22:%22value%22%7D", // {"key":"value"}
        "data=%7B%7D", // data={}
        "data={}",

        // No separators
        "justonefragment",
        "noseparators123",

        // Invalid percent encoding (still accepted - permissive parser)
        "%", // incomplete
        "%2", // incomplete
        "%GG", // invalid hex
        "section%ZZname"); // invalid hex
  }

  @TestFactory
  Stream<DynamicTest> invariants() {
    return CharSequenceParserInvariantTests.generateInvariantTests(
        FragmentParser.INSTANCE, validFragment().toList());
  }
}
