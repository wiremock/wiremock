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

import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UrlTests {

  @Nested
  class ParseMethod {

    @ParameterizedTest
    @MethodSource("validUrls")
    void parses_valid_url(UriReferenceParseTestCase urlTest) {
      Url url = Url.parse(urlTest.stringForm);
      assertThat(url.isUrl()).isTrue();
      assertThat(url.scheme()).isEqualTo(urlTest.expectation.scheme);
      assertThat(url.path()).isEqualTo(urlTest.expectation.path);
      assertThat(url.query()).isEqualTo(urlTest.expectation.query);
      assertThat(url.fragment()).isEqualTo(urlTest.expectation.fragment);
    }

    static Stream<UriReferenceParseTestCase> validUrls() {
      return Stream.of(
          testCase(
              "https://user:password@www.example.com:8080/foo/bar?a=b#somefragment",
              expectation(
                  "https",
                  "user:password@www.example.com:8080",
                  "/foo/bar",
                  "a=b",
                  "somefragment")),
          testCase("s://h/p2", expectation("s", "h", "/p2", null, null)),
          testCase("s://h/p2?", expectation("s", "h", "/p2", "", null)),
          testCase("s://h/p2?q", expectation("s", "h", "/p2", "q", null)),
          testCase("s://h/p2#", expectation("s", "h", "/p2", null, "")),
          testCase("s://h/p2#f", expectation("s", "h", "/p2", null, "f")),
          testCase("s://h/p2?#", expectation("s", "h", "/p2", "", "")),
          testCase("s://h/p2?q#", expectation("s", "h", "/p2", "q", "")),
          testCase("s://h/p2?#f", expectation("s", "h", "/p2", "", "f")),
          testCase("s://h/p2?q#f", expectation("s", "h", "/p2", "q", "f")),
          testCase(
              "ftp://user:pass@example.com:21/",
              expectation("ftp", "user:pass@example.com:21", "/", null, null)),
          testCase(
              "https://example.com:00080/",
              expectation("https", "example.com:00080", "/", null, null)),
          testCase(
              "https://example.com?foo=bar",
              expectation("https", "example.com", "", "foo=bar", null)),
          testCase(
              "https://example.com#frag", expectation("https", "example.com", "", null, "frag")),
          testCase(
              "https://example.com/?q=100%25",
              expectation("https", "example.com", "/", "q=100%25", null)),
          testCase(
              "https://example.com/path%2Fwith%2Fslashes",
              expectation("https", "example.com", "/path%2Fwith%2Fslashes", null, null)),
          testCase("https://[::1]/", expectation("https", "[::1]", "/", null, null)),
          testCase(
              "https://[2001:db8::1]/", expectation("https", "[2001:db8::1]", "/", null, null)),
          testCase(
              "https://[v7.fe80::1234]/", expectation("https", "[v7.fe80::1234]", "/", null, null)),
          testCase(
              "scheme+ext.-123://host/", expectation("scheme+ext.-123", "host", "/", null, null)),
          testCase("a://%61", expectation("a", "%61", "", null, null)),
          testCase("x://host/path;param", expectation("x", "host", "/path;param", null, null)),
          testCase(
              "x://host/path?query=foo&bar=baz",
              expectation("x", "host", "/path", "query=foo&bar=baz", null)),
          testCase(
              "x://host/path%00segment", expectation("x", "host", "/path%00segment", null, null)));
    }
  }

  static UriReferenceParseTestCase testCase(
      String stringForm, UriReferenceExpectation expectation) {
    return new UriReferenceParseTestCase(stringForm, expectation);
  }

  static UriReferenceExpectation expectation(
      @Nullable String schemeStr,
      @Nullable String authorityStr,
      String pathStr,
      @Nullable String queryStr,
      @Nullable String fragmentStr) {
    Scheme scheme = schemeStr == null ? null : Scheme.parse(schemeStr);
    Authority authority = authorityStr == null ? null : Authority.parse(authorityStr);
    Path path = Path.parse(pathStr);
    Query query = queryStr == null ? null : Query.parse(queryStr);
    Fragment fragment = fragmentStr == null ? null : Fragment.parse(fragmentStr);
    return new UriReferenceExpectation(scheme, authority, path, query, fragment);
  }

  record UriReferenceParseTestCase(String stringForm, UriReferenceExpectation expectation) {}

  record UriReferenceExpectation(
      @Nullable Scheme scheme,
      @Nullable Authority authority,
      @Nullable Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {}
}
