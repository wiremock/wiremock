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
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.wiremock.url.AbsoluteUrl.transform;
import static org.wiremock.url.Scheme.https;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

@SuppressWarnings("HttpUrlsUsage")
class AbsoluteUrlTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = AbsoluteUrl.parse("https://example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);

      assertThat(absoluteUrl.getScheme()).isEqualTo(https);

      assertThat(absoluteUrl.getAuthority()).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(absoluteUrl.getUserInfo()).isNull();
      assertThat(absoluteUrl.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(absoluteUrl.getPort()).isNull();

      assertThat(absoluteUrl.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(absoluteUrl.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(absoluteUrl.getFragment()).isEqualTo(Fragment.parse("fragment"));
    }

    @Test
    void parses_absolute_url_with_userinfo_correctly() {
      var absoluteUrl = AbsoluteUrl.parse("https://user@example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://user@example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);

      assertThat(absoluteUrl.getScheme()).isEqualTo(https);

      assertThat(absoluteUrl.getAuthority()).isEqualTo(Authority.parse("user@example.com"));
      assertThat(absoluteUrl.getUserInfo()).isEqualTo(UserInfo.parse("user"));
      assertThat(absoluteUrl.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(absoluteUrl.getPort()).isNull();

      assertThat(absoluteUrl.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(absoluteUrl.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(absoluteUrl.getFragment()).isEqualTo(Fragment.parse("fragment"));
    }

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = AbsoluteUrl.parse("https://example.com/path?query");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("https://example.com/path?query");
      assertThat(serversideAbsoluteUrl).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(serversideAbsoluteUrl).isNotInstanceOf(Origin.class);

      assertThat(serversideAbsoluteUrl.getScheme()).isEqualTo(https);

      assertThat(serversideAbsoluteUrl.getAuthority()).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(serversideAbsoluteUrl.getUserInfo()).isNull();
      assertThat(serversideAbsoluteUrl.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(serversideAbsoluteUrl.getPort()).isNull();

      assertThat(serversideAbsoluteUrl.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(serversideAbsoluteUrl.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(serversideAbsoluteUrl.getFragment()).isNull();
    }

    @Test
    void parses_origin_correctly() {
      var origin = AbsoluteUrl.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);

      assertThat(origin.getScheme()).isEqualTo(https);

      assertThat(origin.getAuthority()).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(origin.getUserInfo()).isNull();
      assertThat(origin.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(origin.getPort()).isNull();

      assertThat(origin.getPath()).isEqualTo(Path.EMPTY);
      assertThat(origin.getQuery()).isNull();

      assertThat(origin.getFragment()).isNull();
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = AbsoluteUrl.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);

      assertThat(fileUri.getScheme()).isEqualTo(Scheme.file);

      assertThat(fileUri.getAuthority()).isEqualTo(HostAndPort.EMPTY);
      assertThat(fileUri.getUserInfo()).isNull();
      assertThat(fileUri.getHost()).isEqualTo(Host.EMPTY);
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = AbsoluteUrl.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);

      assertThat(fileUri.getScheme()).isEqualTo(Scheme.file);

      assertThat(fileUri.getAuthority()).isEqualTo(Authority.parse("user@remote"));
      assertThat(fileUri.getUserInfo()).isEqualTo(UserInfo.parse("user"));
      assertThat(fileUri.getHost()).isEqualTo(Host.parse("remote"));
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();
    }

    @Test
    void rejects_invalid_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> AbsoluteUrl.parse("not a :uri"))
              .actual();
      assertThat(exception.getMessage()).isEqualTo("Illegal uri: `not a :uri`");
      assertThat(exception.getIllegalValue()).isEqualTo("not a :uri");

      IllegalScheme cause =
          assertThat(exception.getCause()).asInstanceOf(type(IllegalScheme.class)).actual();
      assertThat(cause.getMessage())
          .isEqualTo("Illegal scheme `not a `; Scheme must match [a-zA-Z][a-zA-Z0-9+\\-.]{0,255}");
      assertThat(cause.getIllegalValue()).isEqualTo("not a ");
      assertThat(cause.getCause()).isNull();
    }

    @Test
    void rejects_mailto() {
      IllegalAbsoluteUrl exception =
          assertThatExceptionOfType(IllegalAbsoluteUrl.class)
              .isThrownBy(() -> AbsoluteUrl.parse("mailto:joan@example.com"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal absolute url: `mailto:joan@example.com`");
      assertThat(exception.getIllegalValue()).isEqualTo("mailto:joan@example.com");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_arn() {
      IllegalAbsoluteUrl exception =
          assertThatExceptionOfType(IllegalAbsoluteUrl.class)
              .isThrownBy(
                  () ->
                      AbsoluteUrl.parse(
                          "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo(
              "Illegal absolute url: `arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS`");
      assertThat(exception.getIllegalValue())
          .isEqualTo(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_file_no_authority() {
      IllegalAbsoluteUrl exception =
          assertThatExceptionOfType(IllegalAbsoluteUrl.class)
              .isThrownBy(() -> AbsoluteUrl.parse("file:/home/me/some/dir"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal absolute url: `file:/home/me/some/dir`");
      assertThat(exception.getIllegalValue()).isEqualTo("file:/home/me/some/dir");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_relative_url() {
      IllegalAbsoluteUrl exception =
          assertThatExceptionOfType(IllegalAbsoluteUrl.class)
              .isThrownBy(() -> AbsoluteUrl.parse("//example.com/path?query#fragment"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal absolute url: `//example.com/path?query#fragment`");
      assertThat(exception.getIllegalValue()).isEqualTo("//example.com/path?query#fragment");
      assertThat(exception.getCause()).isNull();
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    void parses_valid_url(UrlReferenceParseTestCase urlTest) {
      AbsoluteUrl url = AbsoluteUrl.parse(urlTest.stringForm);
      assertThat(url.isAbsoluteUrl()).isTrue();
      assertThat(url.getScheme()).isEqualTo(urlTest.expectation.scheme);
      assertThat(url.getPath()).isEqualTo(urlTest.expectation.path);
      assertThat(url.getQuery()).isEqualTo(urlTest.expectation.query);
      assertThat(url.getFragment()).isEqualTo(urlTest.expectation.fragment);
    }

    @Test
    void normalise() {
      String urlString = "http://proxy.example.com/";
      AbsoluteUrl parsed = AbsoluteUrl.parse(urlString);
      AbsoluteUrl normalised = parsed.normalise();
      assertThat(normalised).isEqualTo(parsed);
      assertThat(normalised.toString()).isEqualTo(parsed.toString());
    }

    @Test
    void resolveRelative() {
      AbsoluteUrl base = AbsoluteUrl.parse("http://example.com");
      AbsoluteUrl resolved = base.resolve(Path.parse("foo"));
      assertThat(resolved.toString()).isEqualTo("http://example.com/foo");
      assertThat(resolved.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(resolved.getPath()).isEqualTo(Path.parse("/foo"));
    }

    @Test
    void settingPortToNullChangesNothing() {
      String urlString = "http://example.com";

      AbsoluteUrl noPortToStartWith = AbsoluteUrl.parse(urlString);
      assertThat(noPortToStartWith.toString()).isEqualTo(urlString);

      AbsoluteUrl stillNoPort = transform(noPortToStartWith, it -> it.setPort(null));
      assertThat(noPortToStartWith).isEqualTo(stillNoPort);
      assertThat(noPortToStartWith.toString()).isEqualTo(stillNoPort.toString());
    }

    @Test
    void normalisingOriginWithEmptyPathReturnsOrigin() {
      // Parse a URL that needs normalisation (uppercase scheme) with empty path
      String urlString = "HTTP://example.com:80";
      AbsoluteUrl parsed = AbsoluteUrl.parse(urlString);

      assertThat(parsed).isNotInstanceOf(Origin.class);
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

  @Nested
  class Normalise {

    static final List<NormalisationCase<Uri>> normalisationCases =
        Stream.<Pair<String, String>>of(
                // Scheme normalization - uppercase to lowercase
                Pair.of("HTTPS://EXAMPLE.COM:8080", "https://example.com:8080/"),
                Pair.of("HTTPS://EXAMPLE.COM:08080", "https://example.com:8080/"),
                Pair.of("HTTPS://example.com:08080", "https://example.com:8080/"),
                Pair.of("HTTPS://example.com:8080", "https://example.com:8080/"),
                Pair.of("HTTP://example.com", "http://example.com/"),
                Pair.of("FTP://example.com", "ftp://example.com/"),

                // Host normalization - uppercase to lowercase
                Pair.of("https://EXAMPLE.COM:8080", "https://example.com:8080/"),
                Pair.of("https://EXAMPLE.COM:08080", "https://example.com:8080/"),
                Pair.of("http://WWW.EXAMPLE.COM", "http://www.example.com/"),
                Pair.of("http://Example.Com", "http://example.com/"),

                // Port normalization - leading zeros
                Pair.of("https://example.com:08080", "https://example.com:8080/"),
                Pair.of("http://example.com:09090", "http://example.com:9090/"),
                Pair.of("http://example.com:00080", "http://example.com/"),

                // Port normalization - default port removal
                Pair.of("http://example.com:80", "http://example.com/"),
                Pair.of("http://example.com:80/", "http://example.com/"),
                Pair.of("http://example.com:80/path", "http://example.com/path"),
                Pair.of("http://example.com:080", "http://example.com/"),
                Pair.of("https://example.com:443", "https://example.com/"),
                Pair.of("https://example.com:443/", "https://example.com/"),
                Pair.of("https://example.com:443/path", "https://example.com/path"),
                Pair.of("https://example.com:0443", "https://example.com/"),

                // Percent encoding - uppercase hex digits in path
                Pair.of("http://example.com/%1f", "http://example.com/%1F"),
                Pair.of("http://example.com/%1f%3f", "http://example.com/%1F%3F"),
                Pair.of("http://example.com/path%1fto", "http://example.com/path%1Fto"),
                Pair.of("http://example.com/%3f%3F", "http://example.com/%3F%3F"),
                Pair.of("http://example.com/%ab%cd%ef", "http://example.com/%AB%CD%EF"),

                // Percent encoding - decode unreserved characters in path (A-Z a-z 0-9 - . _ ~)
                Pair.of("http://example.com/%41", "http://example.com/A"),
                Pair.of("http://example.com/%61", "http://example.com/a"),
                Pair.of("http://example.com/%30", "http://example.com/0"),
                Pair.of("http://example.com/%7E", "http://example.com/~"),
                Pair.of("http://example.com/%7e", "http://example.com/~"),
                Pair.of("http://example.com/%2D", "http://example.com/-"),
                Pair.of("http://example.com/%2E", "http://example.com/"),
                Pair.of("http://example.com/%5F", "http://example.com/_"),
                Pair.of("http://example.com/%41%42%43", "http://example.com/ABC"),
                Pair.of("http://example.com/~%75ser", "http://example.com/~user"),

                // Percent encoding - uppercase hex in query
                Pair.of("http://example.com?key=%1f", "http://example.com/?key=%1F"),
                Pair.of("http://example.com?a=%1f&b=%1a", "http://example.com/?a=%1F&b=%1A"),
                Pair.of("http://example.com?key=%ab", "http://example.com/?key=%AB"),

                // Percent encoding - decode unreserved in query
                Pair.of("http://example.com?key=%41", "http://example.com/?key=A"),
                Pair.of("http://example.com?%61=%62", "http://example.com/?a=b"),
                Pair.of("http://example.com?key=%7E", "http://example.com/?key=~"),

                // Percent encoding - uppercase hex in fragment
                Pair.of("http://example.com#%1f", "http://example.com/#%1F"),
                Pair.of("http://example.com#%ab", "http://example.com/#%AB"),

                // Percent encoding - decode unreserved in fragment
                Pair.of("http://example.com#%41", "http://example.com/#A"),
                Pair.of("http://example.com#%7E", "http://example.com/#~"),
                Pair.of("http://example.com#%61%62%63", "http://example.com/#abc"),

                // Combined normalizations - scheme + host + port
                Pair.of("HTTP://EXAMPLE.COM:80", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:443", "https://example.com/"),
                Pair.of("HTTP://EXAMPLE.COM:080", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:0443", "https://example.com/"),

                // Combined normalizations - multiple components
                Pair.of("HTTP://EXAMPLE.COM:80/%1f", "http://example.com/%1F"),
                Pair.of("HTTPS://EXAMPLE.COM:443/PATH", "https://example.com/PATH"),
                Pair.of("HTTP://EXAMPLE.COM/%41%42", "http://example.com/AB"),
                Pair.of("HTTPS://EXAMPLE.COM:0443/path?key=%41", "https://example.com/path?key=A"),
                Pair.of("HTTP://EXAMPLE.COM:080/%1f?a=%1f#%1f", "http://example.com/%1F?a=%1F#%1F"),
                Pair.of("HTTPS://EXAMPLE.COM:443/%61?%62=%63#%64", "https://example.com/a?b=c#d"),

                // Path with percent encoding variations
                Pair.of("http://example.com/%41/%42/%43", "http://example.com/A/B/C"),
                Pair.of(
                    "http://example.com/path/%1F/segment", "http://example.com/path/%1F/segment"),
                Pair.of("http://example.com/%7Euser/docs", "http://example.com/~user/docs"),

                // Query and fragment combinations
                Pair.of("http://example.com?%41=%42#%43", "http://example.com/?A=B#C"),
                Pair.of("http://example.com?key=%1f#%1f", "http://example.com/?key=%1F#%1F"),

                // Multiple ports in different contexts
                Pair.of("http://example.com:8080", "http://example.com:8080/"),
                Pair.of("https://example.com:8443", "https://example.com:8443/"),
                Pair.of("ftp://example.com:21", "ftp://example.com/"),

                // Mixed case hex digits
                Pair.of("http://example.com/%aB%Cd", "http://example.com/%AB%CD"),
                Pair.of("http://example.com?key=%aB", "http://example.com/?key=%AB"),
                Pair.of("http://example.com#%aB", "http://example.com/#%AB"))
            .map(
                it ->
                    new NormalisationCase<>(
                        AbsoluteUrl.parse(it.getLeft()), AbsoluteUrl.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<Uri> alreadyNormalisedUrlReferences =
        normalisationCases.stream().map(NormalisationCase::normalForm).distinct().toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedUrlReferences);
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
