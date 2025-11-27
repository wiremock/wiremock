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
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UrlTests {

  static Stream<String> onlyRfc3986ValidUrls() {
    return Stream.of(
        "foo://example.com:99999/",
        "http://example.com/%C3%28",
        "http://[::1",
        "http://[::1]]",
        "mailto:user@[::1]",
        "urn:example:foo bar",
        "http://example..com",
        "ftp://user:pass@host:99999/",
        "x://%61",
        "http://example.com/#",
        "http://example.com/%%20",
        "http://example.com/%G1",
        "http://example.com/%2",
        "http://example.com/abc^def",
        "http://example.com/\\abc",
        "http://example.com/<>",
        "http://example.com/|",
        "http://example.com/`",
        "http://example.com/{",
        "http://example.com/}",
        "http://example.com/~",
        "http://example.com/[",
        "http://example.com/]",
        "http://example.com/space here",
        "http://example.com/?q=100%",
        "http://example.com/?q=%ZZ",
        "http://example.com/#frag%",
        "http://example.com/#%",
        "ftp://example.com:70000/",
        "http://user:pass@:80/",
        "http://example.com:0/",
        "http://example.com:65536/",
        "http://example.com:999999/",
        "http://example.com:1000000/",
        "http://example.com:080",
        "file://",
        "file:///",
        "file:///C:/path\\file.txt",
        "http://example.com/\\path",
        "http://example.com/\\%20",
        "http://example.com/\\%ZZ",
        "http://example.com/\\%G1",
        "http://example.com/\\^",
        "http://example.com/\\|",
        "http://example.com/\\`");
  }

  static Stream<String> onlyWHATWGValidUrls() {
    return Stream.of(
        "http://example.com/a b",
        "http://example.com/foo\\bar",
        "example.com/foo",
        "http://exa mple.com/",
        "http://example.com/안녕",
        "http://example.com/你好",
        "http://example.com/☺",
        "http://example.com/foo|bar",
        "http://example.com/foo<TAB>bar",
        "http://example.com/leading space",
        "http://example.com/trailing space",
        "http://example.com/\\backslash",
        "http://example.com/\\^",
        "http://example.com/\\`",
        "http://example.com/\\|",
        "http://example.com/#frag with space",
        "http://example.com/?q=hello world",
        "http://example.com/%C3%A9  // Chrome accepts unencoded é",
        "http://example.com/%E2%98%BA // Chrome accepts ☺ unencoded",
        "http://example.com/%E4%BD%A0%E5%A5%BD // Chrome accepts 你好 unencoded",
        "http://example.com/%EC%95%88%EB%85%95 // Chrome accepts 안녕 unencoded",
        "http://example.com/😀",
        "http://example.com/😎",
        "http://example.com/🧠",
        "http://example.com/💡",
        "http://example.com/foo|bar|baz",
        "http://example.com/foo^bar",
        "http://example.com/foo`bar",
        "http://example.com/`~!@#$%^&*()_+={}[]|;:'\",.<>?/",
        "http://example.com/foo\\tbar",
        "http://example.com/foo\\nbar",
        "http://example.com/foo\\rbar",
        "http://example.com/space here and there",
        "http://example.com/mixed/안녕|world^",
        "http://example.com/💡/path",
        "http://example.com/😀/emoji",
        "http://example.com/unicode/नमस्ते",
        "http://example.com/unicode/مرحب,ا",
        "http://example.com/unicode/こんにちは",
        "http://example.com/unicode/Здравствуйте",
        "http://example.com/unicode/שָׁלוֹ,ם",
        "http://example.com/unicode/👩‍💻",
        "http://example.com/unicode/🏳️‍🌈",
        "http://example.com/unicode/🀄",
        "http://example.com/unicode/♞",
        "http://example.com/./../path",
        "http://example.com/..//",
        "http://example.com/.//",
        "http://example.com/%20space%20encoded",
        "http://example.com/%09tabencoded");
  }

  static Stream<String> allUrls() {
    return Stream.concat(onlyWHATWGValidUrls(), onlyRfc3986ValidUrls());
  }

  public static Stream<String> actuallyIllegalUrls() {
    return Stream.of(
        "http://example.com:99999", // port out of range (>65535)
        "http://example.com:0", // port zero (invalid)
        "://example.com" // missing scheme
        );
  }

  public static Stream<String> maybeShouldBeIllegalUrls() {
    return Stream.of(
        "http://[::1", // missing closing bracket for IPv6
        "http://[::1]]", // extra closing bracket
        "http://example.com:abc", // non-numeric port
        "http://exa mple.com", // unencoded space in host
        "http://example.com/abc^def", // illegal ^ in path
        "http://example.com/<>", // illegal characters
        "http://example.com/|", // pipe not allowed
        "http://example.com/`", // backtick illegal
        "http://example.com/{", // brace illegal
        "http://example.com/}", // brace illegal
        "http://example.com/[", // bracket illegal outside IPv6 literal
        "http://example.com/]", // bracket illegal outside IPv6 literal
        "http://example.com/%", // incomplete percent-encoding
        "http://example.com/%G1", // invalid percent-encoding
        "http://example.com/%2", // incomplete percent-encoding
        "file://", // file scheme without path or authority
        "http://example.com/#%", // invalid fragment
        "http://example.com/%%20", // double percent
        "http://example.com/\\abc", // raw backslash in path
        "http://example.com/\\%G1", // invalid backslash percent encoding
        "http://example.com/\\^", // backslash + illegal char
        "http://example.com/\\|", // backslash + illegal char
        "http://example.com/\\`" // backslash + illegal char
        );
  }

  public static Stream<String> illegalUrls() {
    return Stream.concat(maybeShouldBeIllegalUrls(), actuallyIllegalUrls());
  }

  @ParameterizedTest
  @MethodSource("allUrls")
  void we_accept_all_things(String url) {
    Url.parse(url);
  }

  @ParameterizedTest
  @MethodSource("actuallyIllegalUrls")
  void illegal_urls(String url) {
    assertThatExceptionOfType(IllegalUrl.class).isThrownBy(() -> Url.parse(url));
  }

  @ParameterizedTest
  @MethodSource("maybeShouldBeIllegalUrls")
  void maybe_should_be_illegal_urls(String urlString) {
    Url url = Url.parse(urlString);
    System.out.println(
        "testCase(\""
            + urlString
            + "\", expectation(\""
            + url.scheme()
            + "\", \""
            + url.authority()
            + "\", \""
            + url.path()
            + "\", "
            + url.query()
            + ", "
            + url.fragment()
            + ")),");
  }

  @ParameterizedTest
  @MethodSource("illegalUrls")
  void java_rejects_illegal_urls(String illegalUrl) {
    assertThatExceptionOfType(URISyntaxException.class).isThrownBy(() -> new URI(illegalUrl));
  }

  @ParameterizedTest
  @MethodSource("onlyWHATWGValidUrls")
  void java_rejects_whatwg_urls(String onlyWHATWGValidUrl) {
    assertThatExceptionOfType(URISyntaxException.class)
        .isThrownBy(() -> new URI(onlyWHATWGValidUrl));
  }

  @ParameterizedTest
  @MethodSource("onlyRfc3986ValidUrls")
  void java_accepts_rfc3986_urls(String onlyRfc3986ValidUrl) throws URISyntaxException {
    new URI(onlyRfc3986ValidUrl);
  }

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
}
