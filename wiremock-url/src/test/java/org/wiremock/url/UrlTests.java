/*
 * Copyright (C) 2026 Thomas Akehurst
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
import static org.wiremock.url.Scheme.https;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

class UrlTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = Url.parse("https://example.com/path?query#fragment");

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
      var absoluteUrl = Url.parse("https://user@example.com/path?query#fragment");

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
      var serversideAbsoluteUrl = Url.parse("https://example.com/path?query");

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
      var origin = Url.parse("https://example.com");

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
    void parses_relative_url_with_authority_correctly() {
      var relativeUrl = Url.parse("//example.com/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("//example.com/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);

      assertThat(relativeUrl.getScheme()).isNull();

      assertThat(relativeUrl.getAuthority()).isEqualTo(Authority.parse("example.com"));
      assertThat(relativeUrl.getUserInfo()).isNull();
      assertThat(relativeUrl.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(relativeUrl.getPort()).isNull();

      assertThat(relativeUrl.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(relativeUrl.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(relativeUrl.getFragment()).isEqualTo(Fragment.parse("fragment"));
    }

    @Test
    void parses_relative_url_without_authority_correctly() {
      var relativeUrl = Url.parse("/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);

      assertThat(relativeUrl.getScheme()).isNull();

      assertThat(relativeUrl.getAuthority()).isNull();
      assertThat(relativeUrl.getUserInfo()).isNull();
      assertThat(relativeUrl.getHost()).isNull();
      assertThat(relativeUrl.getPort()).isNull();

      assertThat(relativeUrl.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(relativeUrl.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(relativeUrl.getFragment()).isEqualTo(Fragment.parse("fragment"));
    }

    @Test
    void parses_path_and_query_correctly() {
      var pathAndQuery = Url.parse("/path?query");

      assertThat(pathAndQuery.toString()).isEqualTo("/path?query");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(pathAndQuery.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(pathAndQuery.getFragment()).isNull();
    }

    @Test
    void parses_relative_path_correctly() {
      var pathAndQuery = Url.parse("relative");

      assertThat(pathAndQuery.toString()).isEqualTo("relative");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.parse("relative"));
      assertThat(pathAndQuery.getQuery()).isNull();

      assertThat(pathAndQuery.getFragment()).isNull();
    }

    @Test
    void parses_empty_path_correctly() {
      var pathAndQuery = Url.parse("");

      assertThat(pathAndQuery.toString()).isEqualTo("");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.EMPTY);
      assertThat(pathAndQuery.getQuery()).isNull();

      assertThat(pathAndQuery.getFragment()).isNull();
    }

    @Test
    void parses_query_only_correctly() {
      var pathAndQuery = Url.parse("?");

      assertThat(pathAndQuery.toString()).isEqualTo("?");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.EMPTY);
      assertThat(pathAndQuery.getQuery()).isEqualTo(Query.parse(""));

      assertThat(pathAndQuery.getFragment()).isNull();
    }

    @Test
    void parses_fragment_only_correctly() {
      var relativeUrl = Url.parse("#");

      assertThat(relativeUrl.toString()).isEqualTo("#");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);

      assertThat(relativeUrl.getScheme()).isNull();

      assertThat(relativeUrl.getAuthority()).isNull();
      assertThat(relativeUrl.getUserInfo()).isNull();
      assertThat(relativeUrl.getHost()).isNull();
      assertThat(relativeUrl.getPort()).isNull();

      assertThat(relativeUrl.getPath()).isEqualTo(Path.EMPTY);
      assertThat(relativeUrl.getQuery()).isNull();

      assertThat(relativeUrl.getFragment()).isEqualTo(Fragment.parse(""));
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = Url.parse("file:///home/me/some/dir");

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
      var fileUri = Url.parse("file://user@remote/home/me/some/dir");

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
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Url.parse("not a :uri"))
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

    static final List<String> illegalUrls =
        Stream.of(
                "mailto:joan@example.com",
                "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS",
                "file:/home/me/some/dir")
            .toList();

    @ParameterizedTest
    @FieldSource("illegalUrls")
    void rejects_illegal_url(String illegalUrl) {
      assertThatExceptionOfType(IllegalUrl.class)
          .isThrownBy(() -> Url.parse(illegalUrl))
          .withMessage("Illegal url: `" + illegalUrl + "`; a url has an authority")
          .extracting(IllegalUrl::getIllegalValue)
          .isEqualTo(illegalUrl);
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

                // Protocol-relative URLs - host normalization
                Pair.of("//EXAMPLE.COM:8080", "//example.com:8080/"),
                Pair.of("//EXAMPLE.COM:08080", "//example.com:8080/"),
                Pair.of("//example.com:08080", "//example.com:8080/"),

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
            .map(it -> new NormalisationCase<>(Url.parse(it.getLeft()), Url.parse(it.getRight())))
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
}
