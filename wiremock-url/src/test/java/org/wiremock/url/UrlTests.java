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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;

class UrlTests {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static final List<String> whatwg_valid_rfc3986_valid_wiremock_valid =
      readResource("whatwg_valid_rfc3986_valid_wiremock_valid");
  static List<String> whatwg_valid_rfc3986_valid_wiremock_invalid =
      readResource("whatwg_valid_rfc3986_valid_wiremock_invalid");
  static List<String> whatwg_valid_rfc3986_invalid_wiremock_valid =
      readResource("whatwg_valid_rfc3986_invalid_wiremock_valid");
  static List<String> whatwg_valid_rfc3986_invalid_wiremock_invalid =
      readResource("whatwg_valid_rfc3986_invalid_wiremock_invalid");
  static List<String> whatwg_invalid_rfc3986_valid_wiremock_valid =
      readResource("whatwg_invalid_rfc3986_valid_wiremock_valid");
  static List<String> whatwg_invalid_rfc3986_valid_wiremock_invalid =
      readResource("whatwg_invalid_rfc3986_valid_wiremock_invalid");
  static List<String> whatwg_invalid_rfc3986_invalid_wiremock_valid =
      readResource("whatwg_invalid_rfc3986_invalid_wiremock_valid");
  static List<String> whatwg_invalid_rfc3986_invalid_wiremock_invalid =
      readResource("whatwg_invalid_rfc3986_invalid_wiremock_invalid");

  static List<String> whatwg_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid);

  static List<String> whatwg_invalid =
      concat(
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  static List<String> rfc3986_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid);

  static List<String> rfc3986_invalid =
      concat(
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  static List<String> wiremock_valid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid);

  static List<String> wiremock_invalid =
      concat(
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  @SafeVarargs
  static <T> Set<T> concat(Set<T>... sets) {
    return Stream.of(sets).flatMap(Collection::stream).collect(Collectors.toSet());
  }

  static <T> List<T> concat(List<List<T>> lists) {
    return lists.stream().flatMap(Collection::stream).toList();
  }

  @SafeVarargs
  static <T> List<T> concat(List<T>... lists) {
    return Stream.of(lists).flatMap(Collection::stream).toList();
  }

  static List<List<String>> parameter_groups =
      List.of(
          whatwg_valid_rfc3986_valid_wiremock_valid,
          whatwg_valid_rfc3986_valid_wiremock_invalid,
          whatwg_valid_rfc3986_invalid_wiremock_valid,
          whatwg_valid_rfc3986_invalid_wiremock_invalid,
          whatwg_invalid_rfc3986_valid_wiremock_valid,
          whatwg_invalid_rfc3986_valid_wiremock_invalid,
          whatwg_invalid_rfc3986_invalid_wiremock_valid,
          whatwg_invalid_rfc3986_invalid_wiremock_invalid);

  @ParameterizedTest
  @FieldSource("parameter_groups")
  void groups_contain_no_duplicates(List<String> group) {
    assertThat(group).doesNotHaveDuplicates();
  }

  @Test
  void test_parameter_invariants() {

    List<String> all = concat(parameter_groups);

    assertThat(all).doesNotHaveDuplicates();

    assertThat(all).containsExactlyInAnyOrderElementsOf(concat(whatwg_valid, whatwg_invalid));
    assertThat(all).containsExactlyInAnyOrderElementsOf(concat(rfc3986_valid, rfc3986_invalid));
    assertThat(all).containsExactlyInAnyOrderElementsOf(concat(wiremock_valid, wiremock_invalid));
  }

  // Taken from https://datatracker.ietf.org/doc/html/rfc3986#page-50
  //  private static final Pattern rfc3986Pattern =
  // Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
  private static final Pattern rfc3986Pattern =
      Pattern.compile(
          "^[A-Za-z][A-Za-z0-9+.-]*:(//(([A-Za-z0-9._~!$&'()*+,;=:-]|%[0-9A-Fa-f]{2})*@)?(\\[([0-9A-Fa-f:.]+|v[0-9A-Fa-f]+\\.[A-Za-z0-9._~!$&'()*+,;=:-]+)]|([0-9]{1,3}\\.){3}[0-9]{1,3}|([A-Za-z0-9._~!$&'()*+,;=-]|%[0-9A-Fa-f]{2})*)(:[0-9]*)?(/([A-Za-z0-9._~!$&'()*+,;=:@/-]|%[0-9A-Fa-f]{2})*)?|(/?([A-Za-z0-9._~!$&'()*+,;=:@/-]|%[0-9A-Fa-f]{2})*))(\\?([A-Za-z0-9._~!$&'()*+,;=:@/?-]|%[0-9A-Fa-f]{2})*)?(#([A-Za-z0-9._~!$&'()*+,;=:@/?-]|%[0-9A-Fa-f]{2})*)?$");

  //  private static final Pattern rfc3986Pattern =
  // Pattern.compile("(?:[A-Za-z][A-Za-z0-9+.-]*:/{2})?(?:(?:[A-Za-z0-9-._~]|%[A-Fa-f0-9]{2})+(?::([A-Za-z0-9-._~]?|[%][A-Fa-f0-9]{2})+)?@)?(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\\\.){1,126}[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?::[0-9]+)?(?:/(?:[A-Za-z0-9-._~]|%[A-Fa-f0-9]{2})*)*(?:\\\\?[A-Za-z0-9-._~]+(?:=(?:[A-Za-z0-9-._~+]|%[A-Fa-f0-9]{2})+)?(?:&|;[A-Za-z0-9-._~]+(?:=(?:[A-Za-z0-9-._~+]|%[A-Fa-f0-9]{2})+)?)*)?\n");

  @ParameterizedTest
  @FieldSource("rfc3986_valid")
  void rfc3986_valid(String url) {
    assertThat(url).matches(Rfc3986Validator::isValidUriReference);
  }

  @ParameterizedTest
  @FieldSource("rfc3986_invalid")
  void rfc3986_invalid(String url) {
    assertThat(url).doesNotMatch(Rfc3986Validator::isValidUriReference);
  }

  @ParameterizedTest
  @FieldSource("wiremock_valid")
  void wiremock_valid(String url) {
    UrlReference.parse(url);
  }

  @ParameterizedTest
  @FieldSource("wiremock_invalid")
  void wiremock_invalid(String url) {
    assertThatExceptionOfType(IllegalUrlReference.class).isThrownBy(() -> UrlReference.parse(url));
  }

  @ParameterizedTest
  @FieldSource("rfc3986_invalid")
  @Disabled
  void java_rejects_rfc3986_invalid_urls(String illegalUrl) {
    assertThatExceptionOfType(URISyntaxException.class).isThrownBy(() -> new URI(illegalUrl));
  }

  @ParameterizedTest
  @FieldSource("rfc3986_valid")
  @Disabled
  void java_accepts_rfc3986_valid_urls(String onlyRfc3986ValidUrl) throws URISyntaxException {
    new URI(onlyRfc3986ValidUrl);
  }

  @Nested
  class ParseMethod {

    @ParameterizedTest
    @MethodSource("validUrls")
    void parses_valid_url(UriReferenceParseTestCase urlTest) {
      UrlReference url = UrlReference.parse(urlTest.stringForm);
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
              "x://host/path%00segment", expectation("x", "host", "/path%00segment", null, null)),
          testCase(
              "https://example.com/{}?{}#{}",
              expectation("https", "example.com", "/{}", "{}", "{}")),
          testCase(
              "https://example.com/a b?a b#a b",
              expectation("https", "example.com", "/a b", "a b", "a b")),
          testCase(
              "https://example.com/a\tb?a\tb#a\tb",
              expectation("https", "example.com", "/a\tb", "a\tb", "a\tb")),
          testCase(
              "https://example.com/a|b?a|b#a|b",
              expectation("https", "example.com", "/a|b", "a|b", "a|b")));
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

  private static List<String> readResource(String resourceName) {
    try (var resource = UrlTests.class.getResourceAsStream(resourceName + ".json")) {
      assert resource != null;
      return objectMapper.readValue(resource, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
