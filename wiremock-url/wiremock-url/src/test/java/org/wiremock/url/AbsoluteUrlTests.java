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
import static org.wiremock.url.AbsoluteUriTests.Parse.illegalAbsoluteUris;
import static org.wiremock.url.Lists.concat;
import static org.wiremock.url.SchemeRegistry.https;
import static org.wiremock.url.UriExpectation.expectation;
import static org.wiremock.url.UriParseTestCase.testCase;
import static org.wiremock.url.UrlTests.Parse.illegalUrls;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

      assertThat(absoluteUrl.isAbsolute()).isTrue();
      assertThat(absoluteUrl.isRelative()).isFalse();
      assertThat(absoluteUrl.isAbsoluteUrl()).isTrue();
      assertThat(absoluteUrl.isOpaqueUri()).isFalse();
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

      assertThat(absoluteUrl.isAbsolute()).isTrue();
      assertThat(absoluteUrl.isRelative()).isFalse();
      assertThat(absoluteUrl.isAbsoluteUrl()).isTrue();
      assertThat(absoluteUrl.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = AbsoluteUrl.parse("https://example.com/path?query");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("https://example.com/path?query");
      assertThat(serversideAbsoluteUrl).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(serversideAbsoluteUrl).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_origin_correctly() {
      var origin = AbsoluteUrl.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = AbsoluteUrl.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = AbsoluteUrl.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void rejects_illegal_uri() {
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

    static final List<? extends String> illegalAbsoluteUrls =
        concat(illegalUrls, illegalAbsoluteUris);

    @ParameterizedTest
    @FieldSource("illegalAbsoluteUrls")
    void rejects_illegal_absolute_url(String illegalAbsoluteUrl) {
      assertThatExceptionOfType(IllegalAbsoluteUrl.class)
          .isThrownBy(() -> AbsoluteUrl.parse(illegalAbsoluteUrl))
          .withMessage("Illegal absolute url: `" + illegalAbsoluteUrl + "`")
          .extracting(IllegalAbsoluteUrl::getIllegalValue)
          .isEqualTo(illegalAbsoluteUrl);
    }

    @ParameterizedTest
    @MethodSource("validUrls")
    void parses_valid_url(UriParseTestCase urlTest) {
      AbsoluteUrl url = AbsoluteUrl.parse(urlTest.stringForm());
      assertThat(url.isAbsoluteUrl()).isTrue();
      assertThat(url.getScheme()).isEqualTo(urlTest.expectation().scheme());
      assertThat(url.getPath()).isEqualTo(urlTest.expectation().path());
      assertThat(url.getQuery()).isEqualTo(urlTest.expectation().query());
      assertThat(url.getFragment()).isEqualTo(urlTest.expectation().fragment());
    }

    static Stream<UriParseTestCase> validUrls() {
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
              expectation("https", "example.com", "/a|b", "a|b", "a|b")),
          testCase("http://example.com/😀", expectation("http", "example.com", "/😀", null, null)),
          testCase(
              "http://example.com/{}?{}#{}", expectation("http", "example.com", "/{}", "{}", "{}")),
          testCase("http://example.com/안녕", expectation("http", "example.com", "/안녕", null, null)),
          testCase(
              "http://example.com/नमस्ते",
              expectation("http", "example.com", "/नमस्ते", null, null)),
          testCase(
              "http://example.com/こんにちは", expectation("http", "example.com", "/こんにちは", null, null)),
          testCase(
              "http://example.com/Здравствуйте",
              expectation("http", "example.com", "/Здравствуйте", null, null)),
          testCase(
              "http://example.com/مرحب,ا",
              expectation("http", "example.com", "/مرحب,ا", null, null)),
          testCase(
              "http://example.com/שָׁלוֹ,ם",
              expectation("http", "example.com", "/שָׁלוֹ,ם", null, null)));
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<AbsoluteUri>> normalisationCases =
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

                // Percent encoding - uppercase hex in fragment
                Pair.of("http://example.com#%1f", "http://example.com/#%1F"),
                Pair.of("http://example.com#%ab", "http://example.com/#%AB"),

                // Combined normalizations - scheme + host + port
                Pair.of("HTTP://EXAMPLE.COM:80", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:443", "https://example.com/"),
                Pair.of("HTTP://EXAMPLE.COM:080", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:0443", "https://example.com/"),

                // Combined normalizations - multiple components
                Pair.of("HTTP://EXAMPLE.COM:80/%1f", "http://example.com/%1F"),
                Pair.of("HTTPS://EXAMPLE.COM:443/PATH", "https://example.com/PATH"),
                Pair.of("HTTP://EXAMPLE.COM/%41%42", "http://example.com/AB"),
                Pair.of("HTTP://EXAMPLE.COM:080/%1f?a=%1f#%1f", "http://example.com/%1F?a=%1F#%1F"),
                Pair.of(
                    "HTTPS://EXAMPLE.COM:443/%61?%62=%63#%64", "https://example.com/a?%62=%63#%64"),

                // Path with percent encoding variations
                Pair.of("http://example.com/%41/%42/%43", "http://example.com/A/B/C"),
                Pair.of(
                    "http://example.com/path/%1F/segment", "http://example.com/path/%1F/segment"),
                Pair.of("http://example.com/%7Euser/docs", "http://example.com/~user/docs"),

                // Query and fragment combinations
                Pair.of("http://example.com?%41=%42#%43", "http://example.com/?%41=%42#%43"),
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
    Stream<DynamicTest> normalises_absolute_urls_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<String> alreadyNormalised =
        List.of("http://example.com/?%41=%42#%43", "http://example.com/?%61=%62");

    static final List<? extends AbsoluteUri> alreadyNormalisedUrlReferences =
        Lists.concat(
            normalisationCases.stream().map(NormalisationCase::normalForm).distinct().toList(),
            alreadyNormalised.stream().map(AbsoluteUri::parse).toList());

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedUrlReferences);
    }
  }

  @Nested
  class Resolve {

    @Test
    void resolves_relative_path() {
      AbsoluteUrl base = AbsoluteUrl.parse("http://example.com");
      AbsoluteUrl resolved = base.resolve(Path.parse("foo"));
      assertThat(resolved.toString()).isEqualTo("http://example.com/foo");
      assertThat(resolved.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(resolved.getPath()).isEqualTo(Path.parse("/foo"));
    }

    static final List<ResolutionTestCase> resolutionCases =
        List.of(
            testCase("http://example.com", "https://www.example.com", "https://www.example.com/"),
            testCase("http://example.com", "https://example.com", "https://example.com/"),
            testCase("http://example.com", "https://example.com:8443", "https://example.com:8443/"),
            testCase(
                "http://example.com",
                "https://user@example.com:8443/path",
                "https://user@example.com:8443/path"),
            testCase(
                "http://example.com",
                "https://user@example.com:8443?query",
                "https://user@example.com:8443/?query"),
            testCase(
                "http://example.com",
                "https://user@example.com:8443#fragment",
                "https://user@example.com:8443/#fragment"),
            testCase(
                "http://example.com",
                "https://user@example.com:8443/path?query#fragment",
                "https://user@example.com:8443/path?query#fragment"),
            testCase("http://example.com", "//www.example.com", "http://www.example.com/"),
            testCase("http://example.com", "//example.com", "http://example.com/"),
            testCase("http://example.com", "//example.com:8443", "http://example.com:8443/"),
            testCase(
                "http://example.com",
                "//user@example.com:8443/path",
                "http://user@example.com:8443/path"),
            testCase(
                "http://example.com",
                "//user@example.com:8443?query",
                "http://user@example.com:8443/?query"),
            testCase(
                "http://example.com",
                "//user@example.com:8443#fragment",
                "http://user@example.com:8443/#fragment"),
            testCase(
                "http://example.com",
                "//user@example.com:8443/path?query#fragment",
                "http://user@example.com:8443/path?query#fragment"),
            testCase("http://example.com", "?query", "http://example.com/?query"),
            testCase("http://example.com", "#fragment", "http://example.com/#fragment"),
            testCase(
                "http://example.com",
                "/path?query#fragment",
                "http://example.com/path?query#fragment"),
            testCase("http://example.com", "path?query", "http://example.com/path?query"),
            testCase("http://example.com", "path#fragment", "http://example.com/path#fragment"),
            testCase(
                "http://example.com",
                "path?query#fragment",
                "http://example.com/path?query#fragment"),
            testCase("http://example.com/basepath", "path?query", "http://example.com/path?query"),
            testCase(
                "http://example.com/basepath/",
                "path?query",
                "http://example.com/basepath/path?query"));

    @ParameterizedTest
    @FieldSource("resolutionCases")
    void resolves_strings_when_they_are_valid_urls(ResolutionTestCase resolutionTestCase) {
      var base = AbsoluteUrl.parse(resolutionTestCase.base());
      String input = resolutionTestCase.input();
      var resolved = base.resolve(input);
      assertThat(resolved).isEqualTo(AbsoluteUrl.parse(resolutionTestCase.expectedResult()));
    }

    @ParameterizedTest
    @FieldSource("resolutionCases")
    void resolves_urls(ResolutionTestCase resolutionTestCase) {
      var base = AbsoluteUrl.parse(resolutionTestCase.base());
      Url other = Url.parse(resolutionTestCase.input());
      var resolved = base.resolve(other);
      assertThat(resolved).isEqualTo(AbsoluteUrl.parse(resolutionTestCase.expectedResult()));
    }

    record ResolutionTestCase(String base, String input, String expectedResult) {}

    static ResolutionTestCase testCase(String base, String input, String expectedResult) {
      return new ResolutionTestCase(base, input, expectedResult);
    }
  }

  @Nested
  class Builder {

    @Test
    void can_build_an_absolute_uri() {

      AbsoluteUrl uri =
          AbsoluteUrl.builder(https, Authority.parse("example.com"))
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("https://example.com/path?query#fragment"));
    }

    @Test
    void setting_user_info_after_authority_works() {
      var uri =
          AbsoluteUrl.builder(https, Authority.parse("user@example.com:8443"))
              .setUserInfo(UserInfo.parse("me:passwd"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUrl.parse("https://me:passwd@example.com:8443/"));
    }

    @Test
    void setting_host_after_authority_works() {
      var uri =
          AbsoluteUrl.builder(https, Authority.parse("user@www.example.com:8443"))
              .setHost(Host.parse("example.com"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUrl.parse("https://user@example.com:8443/"));
    }

    @Test
    void setting_port_after_authority_works() {
      var uri =
          AbsoluteUrl.builder(https, Authority.parse("user@example.com:8443"))
              .setPort(Port.of(88443))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUrl.parse("https://user@example.com:88443/"));
    }

    @Test
    void rejects_relative_path() {
      assertThatExceptionOfType(IllegalAbsoluteUrl.class)
          .isThrownBy(
              () ->
                  AbsoluteUrl.builder(https, Authority.parse("example.com"))
                      .setPath(Path.parse("relative"))
                      .build())
          .withMessage(
              "Illegal absolute url: `https://example.comrelative` - an absolute url's path must be absolute or empty, was `relative`")
          .extracting(IllegalUrl::getIllegalValue)
          .isEqualTo("https://example.comrelative");
    }
  }

  @Nested
  class Transform {

    @Test
    void can_change_a_urls_scheme() {

      AbsoluteUrl uri = AbsoluteUrl.parse("https://user@example.com:8443/path?query#fragment");
      AbsoluteUrl transformed = uri.thaw().setScheme(SchemeRegistry.wss).build();

      assertThat(transformed)
          .isEqualTo(AbsoluteUrl.parse("wss://user@example.com:8443/path?query#fragment"));
    }

    @Test
    void can_change_authority() {
      var uri = AbsoluteUrl.parse("https://example.com/path#fragment");

      var uriWithNewAuthority =
          uri.transform(
              builder -> builder.setAuthority(Authority.parse("user@www.example.com:8443")));

      assertThat(uriWithNewAuthority)
          .isEqualTo(AbsoluteUrl.parse("https://user@www.example.com:8443/path#fragment"));
    }

    @Test
    void setting_port_to_null_changes_nothing() {
      String urlString = "http://example.com";

      AbsoluteUrl noPortToStartWith = AbsoluteUrl.parse(urlString);
      assertThat(noPortToStartWith.toString()).isEqualTo(urlString);

      AbsoluteUrl stillNoPort = noPortToStartWith.transform(it -> it.setPort(null));
      assertThat(noPortToStartWith).isEqualTo(stillNoPort);
      assertThat(noPortToStartWith.toString()).isEqualTo(stillNoPort.toString());
    }

    @Test
    void cannot_set_scheme_to_null() {
      var url = AbsoluteUrl.parse("https://example.com/path#fragment");

      //noinspection DataFlowIssue
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> url.transform(it -> it.setScheme(null)))
          .withMessage(null)
          .withNoCause();
    }

    @Test
    void cannot_set_authority_to_null() {
      var url = AbsoluteUrl.parse("https://example.com/path#fragment");

      //noinspection DataFlowIssue
      assertThatExceptionOfType(NullPointerException.class)
          .isThrownBy(() -> url.transform(it -> it.setAuthority(null)))
          .withMessage(null)
          .withNoCause();
    }

    @Test
    void can_update_query() {
      var url = AbsoluteUrl.parse("https://example.com/?a=b");
      Url updated = url.transform(builder -> builder.getQueryBuilder().append("b", "2"));
      assertThat(updated).hasToString("https://example.com/?a=b&b=2");
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://example.com/", "https://example.com/#fragment"})
  void get_serverside_absolute_url_returns_without_fragment(String urlString) {
    var url = AbsoluteUrl.parse(urlString);
    assertThat(url.getServersideAbsoluteUrl())
        .isEqualTo(ServersideAbsoluteUrl.parse("https://example.com/"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://example.com/",
        "https://example.com/#fragment",
        "https://example.com:443/#fragment"
      })
  void get_origin_always_returns(String urlString) {
    var url = AbsoluteUrl.parse(urlString);
    assertThat(url.getOrigin()).isEqualTo(Origin.parse("https://example.com"));
  }
}
