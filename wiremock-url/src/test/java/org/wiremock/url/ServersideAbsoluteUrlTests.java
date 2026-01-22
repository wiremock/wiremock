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
import static org.wiremock.url.AbsoluteUriTests.Parse.illegalAbsoluteUris;
import static org.wiremock.url.Lists.concat;
import static org.wiremock.url.SchemeRegistry.file;
import static org.wiremock.url.SchemeRegistry.https;

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

class ServersideAbsoluteUrlTests {

  @Nested
  class Parse {

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = ServersideAbsoluteUrl.parse("https://example.com/path?query");

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

      assertThat(serversideAbsoluteUrl.isAbsolute()).isTrue();
      assertThat(serversideAbsoluteUrl.isRelative()).isFalse();
      assertThat(serversideAbsoluteUrl.isAbsoluteUrl()).isTrue();
      assertThat(serversideAbsoluteUrl.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_origin_correctly() {
      var origin = ServersideAbsoluteUrl.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = ServersideAbsoluteUrl.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);

      assertThat(fileUri.getScheme()).isEqualTo(file);

      assertThat(fileUri.getAuthority()).isEqualTo(HostAndPort.EMPTY);
      assertThat(fileUri.getUserInfo()).isNull();
      assertThat(fileUri.getHost()).isEqualTo(Host.EMPTY);
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();

      assertThat(fileUri.isAbsolute()).isTrue();
      assertThat(fileUri.isRelative()).isFalse();
      assertThat(fileUri.isAbsoluteUrl()).isTrue();
      assertThat(fileUri.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = ServersideAbsoluteUrl.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);

      assertThat(fileUri.getScheme()).isEqualTo(file);

      assertThat(fileUri.getAuthority()).isEqualTo(Authority.parse("user@remote"));
      assertThat(fileUri.getUserInfo()).isEqualTo(UserInfo.parse("user"));
      assertThat(fileUri.getHost()).isEqualTo(Host.parse("remote"));
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();

      assertThat(fileUri.isAbsolute()).isTrue();
      assertThat(fileUri.isRelative()).isFalse();
      assertThat(fileUri.isAbsoluteUrl()).isTrue();
      assertThat(fileUri.isOpaqueUri()).isFalse();
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> ServersideAbsoluteUrl.parse("not a :uri"))
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

    static final List<? extends String> illegalServersideAbsoluteUrls =
        concat(
            illegalAbsoluteUris,
            List.of(
                "mailto:joan@example.com",
                "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS",
                "file:/home/me/some/dir",
                "//example.com/path?query#fragment"));

    @ParameterizedTest
    @FieldSource("illegalServersideAbsoluteUrls")
    void rejects_illegal_serverside_absolute_url(String illegalAbsoluteUrl) {
      assertThatExceptionOfType(IllegalServersideAbsoluteUrl.class)
          .isThrownBy(() -> ServersideAbsoluteUrl.parse(illegalAbsoluteUrl))
          .withMessage("Illegal serverside absolute url: `" + illegalAbsoluteUrl + "`")
          .extracting(IllegalServersideAbsoluteUrl::getIllegalValue)
          .isEqualTo(illegalAbsoluteUrl);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<AbsoluteUri>> normalisationCases =
        Stream.<Pair<String, String>>of(
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

                // Combined normalizations - scheme + host + port
                Pair.of("HTTP://EXAMPLE.COM:80", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:443", "https://example.com/"),
                Pair.of("HTTP://EXAMPLE.COM:080", "http://example.com/"),
                Pair.of("HTTPS://EXAMPLE.COM:0443", "https://example.com/"),

                // Combined normalizations - multiple components
                Pair.of("HTTP://EXAMPLE.COM:80/%1f", "http://example.com/%1F"),
                Pair.of("HTTPS://EXAMPLE.COM:443/PATH", "https://example.com/PATH"),
                Pair.of("HTTP://EXAMPLE.COM/%41%42", "http://example.com/AB"),
                Pair.of(
                    "HTTPS://EXAMPLE.COM:0443/path?key=%41", "https://example.com/path?key=%41"),

                // Path with percent encoding variations
                Pair.of("http://example.com/%41/%42/%43", "http://example.com/A/B/C"),
                Pair.of(
                    "http://example.com/path/%1F/segment", "http://example.com/path/%1F/segment"),
                Pair.of("http://example.com/%7Euser/docs", "http://example.com/~user/docs"),

                // Multiple ports in different contexts
                Pair.of("http://example.com:8080", "http://example.com:8080/"),
                Pair.of("https://example.com:8443", "https://example.com:8443/"),
                Pair.of("ftp://example.com:21", "ftp://example.com/"),

                // Mixed case hex digits
                Pair.of("http://example.com/%aB%Cd", "http://example.com/%AB%CD"),
                Pair.of("http://example.com?key=%aB", "http://example.com/?key=%AB"))
            .map(
                it ->
                    new NormalisationCase<>(
                        ServersideAbsoluteUrl.parse(it.getLeft()),
                        ServersideAbsoluteUrl.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<String> alreadyNormalised = List.of();

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
}
