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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.wiremock.url.whatwg.SuccessWhatWGUrlTestCase;
import org.wiremock.url.whatwg.WhatWGUrlTestManagement;

class UrlTests {

  @Nested
  class ParseMethod {

    @ParameterizedTest
    @MethodSource("validUrls")
    void parses_valid_url(UrlReferenceParseTestCase urlTest) {
      UrlReference url = UrlReference.parse(urlTest.stringForm);
      assertThat(url.isUrl()).isTrue();
      assertThat(url.scheme()).isEqualTo(urlTest.expectation.scheme);
      assertThat(url.path()).isEqualTo(urlTest.expectation.path);
      assertThat(url.query()).isEqualTo(urlTest.expectation.query);
      assertThat(url.fragment()).isEqualTo(urlTest.expectation.fragment);
    }

    @Test
    void normalise() {
      String urlString = "http://proxy.example.com/";
      Url parsed = Url.parse(urlString);
      Url normalised = parsed.normalise();
      assertThat(normalised).isEqualTo(parsed);
      assertThat(normalised.toString()).isEqualTo(parsed.toString());
    }

    @Test
    void settingPortToNullChangesNothing() {
      String urlString = "http://example.com";

      Url noPortToStartWith = Url.parse(urlString);
      assertThat(noPortToStartWith.toString()).isEqualTo(urlString);

      Url stillNoPort = noPortToStartWith.transform(it -> it.setPort(null));
      assertThat(noPortToStartWith).isEqualTo(stillNoPort);
      assertThat(noPortToStartWith.toString()).isEqualTo(stillNoPort.toString());
    }

    private static final Set<? extends SuccessWhatWGUrlTestCase> whatWgUrlOrigins =
        WhatWGUrlTestManagement.whatwg_valid.stream()
            .filter(
                t ->
                    t.origin() != null
                        && !t.origin().isEmpty()
                        && !t.origin().equals("null")
                        && !t.origin().equals("http://!\"$&'()*+,-.;=_`{}~")
                        && !t.origin().equals("wss://!\"$&'()*+,-.;=_`{}~"))
            .collect(Collectors.toSet());

    @ParameterizedTest
    @FieldSource("whatWgUrlOrigins")
    void whatWgUrlOriginsAreAllBaseUrls(SuccessWhatWGUrlTestCase testCase) {
      try {
        String origin = testCase.origin();
        assert origin != null;

        URI javaUri = URI.create(origin);

        var baseUrl = BaseUrl.parse(origin);

        assertThat(baseUrl.toString()).isEqualTo(origin);
        assertThat(baseUrl).isEqualTo(UrlReference.parse(origin));
        assertThat(baseUrl.scheme().toString()).isEqualTo(javaUri.getScheme());
        assertThat(baseUrl.authority().toString()).isEqualTo(javaUri.getRawAuthority());
        assertThat(baseUrl.normalise())
            .isEqualTo(
                Url.builder(baseUrl.scheme(), baseUrl.authority()).setPath(Path.ROOT).build());
      } catch (Throwable e) {
        System.out.println(testCase);
        throw e;
      }
    }

    private static final Set<? extends SuccessWhatWGUrlTestCase> whatWgUrlBase =
        WhatWGUrlTestManagement.whatwg_valid.stream()
            .filter(
                t ->
                    t.base() != null
                        && !t.base().isEmpty()
                        && !t.hostname().isEmpty()
                        && !t.base().equals("sc://ñ"))
            .collect(Collectors.toSet());

    @ParameterizedTest
    @FieldSource("whatWgUrlBase")
    void whatWgUrlBasesAreAllUrls(SuccessWhatWGUrlTestCase testCase) {
      assert testCase.base() != null;
      testWhatWgUrl(testCase, testCase.base());
    }

    private static final Set<? extends SuccessWhatWGUrlTestCase> whatWgUrlHref =
        WhatWGUrlTestManagement.whatwg_valid.stream()
            .filter(
                t ->
                    t.href() != null
                        && !t.href().isEmpty()
                        && !t.hostname().isEmpty()
                        && !t.href().equals("sc://%/")
                        && !t.href()
                            .equals(
                                "wss://%20!%22$%&'()*+,-.%3B%3C%3D%3E%40%5B%5D%5E_%60%7B%7C%7D~@host/")
                        && !t.href()
                            .equals("http://!\"$&'()*+,-.;=_`{}~/") // host contains `{` and `}`
                        && !t.href()
                            .equals("foo://!\"$%&'()*+,-.;=_`{}~/") // host contains `{` and `}`
                        && !t.href()
                            .equals("wss://!\"$&'()*+,-.;=_`{}~/") // host contains `{` and `}`
                        && !t.href()
                            .equals(
                                "foo://joe:%20!%22$%&'()*+,-.%3A%3B%3C%3D%3E%40%5B%5C%5D%5E_%60%7B%7C%7D~@host/") // user-info contains a `%` not followed by two hex digits
                        && !t.href()
                            .equals(
                                "foo://%20!%22$%&'()*+,-.%3B%3C%3D%3E%40%5B%5C%5D%5E_%60%7B%7C%7D~@host/") // user-info contains a `%` not followed by two hex digits
                        && !t.href()
                            .equals(
                                "wss://joe:%20!%22$%&'()*+,-.%3A%3B%3C%3D%3E%40%5B%5D%5E_%60%7B%7C%7D~@host/") // user-info contains a `%` not followed by two hex digits
                        && !t.href()
                            .equals(
                                "sc://%01%02%03%04%05%06%07%08%0B%0C%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%7F!\"$%&'()*+,-.;=_`{}~/"))
            .collect(Collectors.toSet());

    @ParameterizedTest
    @FieldSource("whatWgUrlHref")
    void whatWgUrlHrefsAreAllUrls(SuccessWhatWGUrlTestCase testCase) {
      assert testCase.href() != null;
      testWhatWgUrl(testCase, testCase.href());
    }

    private static void testWhatWgUrl(SuccessWhatWGUrlTestCase testCase, String base) {
      try {
        var url = Url.parse(base);

        assertThat(url.toString()).isEqualTo(base);
        assertThat(url).isEqualTo(UrlReference.parse(base));
        if (url.path().isEmpty()) {
          assertThat(url.normalise()).isEqualTo(url.transform(b -> b.setPath(Path.ROOT)));
        }

        URI javaUri = uriOrNull(base);

        if (javaUri != null) {
          assertThat(url.scheme().toString()).isEqualTo(javaUri.getScheme());
          Authority authority = url.authority();
          String javaUriRawAuthority = javaUri.getRawAuthority();
          if (javaUriRawAuthority == null) {
            assertThat(authority.toString()).isEmpty();
            assertThat(authority.hostAndPort()).isEqualTo(HostAndPort.of(Host.parse(""), null));
          } else {
            assertThat(authority.toString()).isEqualTo(javaUriRawAuthority);
          }
        }
      } catch (Throwable e) {
        System.out.println(testCase);
        throw e;
      }
    }

    static Stream<UrlReferenceParseTestCase> validUrls() {
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

  private static @Nullable URI uriOrNull(String origin) {
    try {
      return new URI(origin);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  static UrlReferenceParseTestCase testCase(
      String stringForm, UrlReferenceExpectation expectation) {
    return new UrlReferenceParseTestCase(stringForm, expectation);
  }

  static UrlReferenceExpectation expectation(
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
    return new UrlReferenceExpectation(scheme, authority, path, query, fragment);
  }

  record UrlReferenceParseTestCase(String stringForm, UrlReferenceExpectation expectation) {}

  record UrlReferenceExpectation(
      @Nullable Scheme scheme,
      @Nullable Authority authority,
      @Nullable Path path,
      @Nullable Query query,
      @Nullable Fragment fragment) {}
}
