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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.wiremock.url.SchemeRegistry.https;
import static org.wiremock.url.SchemeRegistry.wss;

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

public class AbsoluteUriTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = AbsoluteUri.parse("https://example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
    }

    @Test
    void parses_absolute_url_with_userinfo_correctly() {
      var absoluteUrl = AbsoluteUri.parse("https://user@example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://user@example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
    }

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = AbsoluteUri.parse("https://example.com/path?query");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("https://example.com/path?query");
      assertThat(serversideAbsoluteUrl).isInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_serverside_absolute_url_empty_host_and_port_correctly() {
      var serversideAbsoluteUrl = AbsoluteUri.parse("data://:443");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("data://:443");
      assertThat(serversideAbsoluteUrl).isInstanceOf(Origin.class);
    }

    @Test
    void parses_origin_correctly() {
      var origin = AbsoluteUri.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);
    }

    @Test
    void parses_mailto_correctly() {
      var mailtoUri = AbsoluteUri.parse("mailto:joan@example.com");

      assertThat(mailtoUri.toString()).isEqualTo("mailto:joan@example.com");
      assertThat(mailtoUri).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void parses_arn_correctly() {
      var arn =
          AbsoluteUri.parse(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");

      assertThat(arn.toString())
          .isEqualTo(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");
      assertThat(arn).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = AbsoluteUri.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = AbsoluteUri.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_file_no_authority_correctly() {
      var fileUri = AbsoluteUri.parse("file:/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> AbsoluteUri.parse("not a :uri"))
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

    static final List<String> illegalAbsoluteUris =
        List.of(
            "//example.com/path?query#fragment",
            "/path?query#fragment",
            "/path?query",
            "",
            "relative",
            "?",
            "#");

    @ParameterizedTest
    @FieldSource("illegalAbsoluteUris")
    void rejects_illegal_absolute_uri(String illegalAbsoluteUri) {
      assertThatExceptionOfType(IllegalAbsoluteUri.class)
          .isThrownBy(() -> AbsoluteUri.parse(illegalAbsoluteUri))
          .withMessage("Illegal absolute uri: `" + illegalAbsoluteUri + "`")
          .extracting(IllegalAbsoluteUri::getIllegalValue)
          .isEqualTo(illegalAbsoluteUri);
    }
  }

  @SuppressWarnings("HttpUrlsUsage")
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

                // Non-spec driven
                /*
                `whatever:/..//` is a URI without an Authority.
                Acording to the spec https://datatracker.ietf.org/doc/html/rfc3986#section-5.2.4
                `/..//` should normalise to `//`, so `whatever:/..//` should normalise to `whatever://`.
                However, this changes the semantics to now have an (empty) authority and an empty path.

                We have made an executive decision that if a URI without an Authority has a path that
                normalises to more than one `/` at the start, they will be treatd as a single `/`.
                */
                Pair.of("whatever:/..//", "whatever:/"),

                // Mixed case hex digits
                Pair.of("http://example.com/%aB%Cd", "http://example.com/%AB%CD"),
                Pair.of("http://example.com?key=%aB", "http://example.com/?key=%AB"),
                Pair.of("http://example.com#%aB", "http://example.com/#%AB"))
            .map(
                it ->
                    new NormalisationCase<>(
                        AbsoluteUri.parse(it.getLeft()), AbsoluteUri.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<AbsoluteUri> alreadyNormalisedUriReferences =
        normalisationCases.stream().map(NormalisationCase::normalForm).distinct().toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedUriReferences);
    }
  }

  @Nested
  class Resolve {

    @Test
    void resolve_replaces_query_if_other_has_authority_and_query() {
      var base = AbsoluteUri.parse("https://example.com?query");
      assertThat(base.getQuery()).isEqualTo(Query.parse("query"));
      var other = RelativeUrl.parse("//other.com?newquery");
      assertThat(other.getQuery()).isEqualTo(Query.parse("newquery"));

      var resolved = base.resolve(other);
      assertThat(resolved.toString()).isEqualTo("https://other.com/?newquery");
      assertThat(resolved.getQuery()).isEqualTo(Query.parse("newquery"));
    }

    @Test
    void resolve_replaces_query_if_other_has_authority_and_no_query() {
      var base = AbsoluteUri.parse("https://example.com?query");
      assertThat(base.getQuery()).isEqualTo(Query.parse("query"));
      var other = RelativeUrl.parse("//other.com");
      assertThat(other.getQuery()).isNull();

      var resolved = base.resolve(other);
      assertThat(resolved.toString()).isEqualTo("https://other.com/");
      assertThat(resolved.getQuery()).isNull();
    }
  }

  @Nested
  class Builder {

    @Test
    void can_build_an_absolute_uri() {

      AbsoluteUri uri =
          AbsoluteUri.builder(https)
              .setAuthority(Authority.parse("example.com"))
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("https://example.com/path?query#fragment"));
    }

    @Test
    void can_build_an_absolute_uri_with_separate_authority_parts() {

      var uri =
          AbsoluteUri.builder(https)
              .setHost(Host.parse("example.com"))
              .setUserInfo(UserInfo.parse("user:password"))
              .setPort(Port.of(8443))
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();

      assertThat(uri)
          .isEqualTo(
              AbsoluteUri.parse("https://user:password@example.com:8443/path?query#fragment"));
    }

    private static final List<AbsoluteUri.Builder<?>> authorityBuilders =
        List.of(
            AbsoluteUri.builder(https)
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443)),
            AbsoluteUri.builder(https)
                .setHost(Host.parse("example.com"))
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password")),
            AbsoluteUri.builder(https)
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password")),
            AbsoluteUri.builder(https)
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password"))
                .setHost(Host.parse("example.com")),
            AbsoluteUri.builder(https)
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com")),
            AbsoluteUri.builder(https)
                .setUserInfo(UserInfo.parse("user:password"))
                .setHost(Host.parse("example.com"))
                .setPort(Port.of(8443)));

    @ParameterizedTest
    @FieldSource("authorityBuilders")
    void can_set_authority_fields_in_any_order(AbsoluteUri.Builder<?> builder) {
      var uri =
          builder
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();
      assertThat(uri)
          .isEqualTo(
              AbsoluteUri.parse("https://user:password@example.com:8443/path?query#fragment"));
    }

    @Test
    void setting_user_info_after_authority_works() {
      var uri =
          AbsoluteUri.builder(https)
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setUserInfo(UserInfo.parse("me:passwd"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://me:passwd@example.com:8443/"));
    }

    @Test
    void setting_host_after_authority_works() {
      var uri =
          AbsoluteUri.builder(https)
              .setAuthority(Authority.parse("user@www.example.com:8443"))
              .setHost(Host.parse("example.com"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://user@example.com:8443/"));
    }

    @Test
    void setting_port_after_authority_works() {
      var uri =
          AbsoluteUri.builder(https)
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setPort(Port.of(88443))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://user@example.com:88443/"));
    }

    @Test
    void authority_overrides_user_info_and_port() {
      var uri =
          AbsoluteUri.builder(https)
              .setUserInfo(UserInfo.parse("me:passwd"))
              .setPort(Port.of(88443))
              .setAuthority(Authority.parse("user@example.com:8443"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://user@example.com:8443/"));
    }

    @Test
    void build_fails_if_user_info_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(
              () -> AbsoluteUri.builder(https).setUserInfo(UserInfo.parse("me:passwd")).build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(() -> AbsoluteUri.builder(https).setPort(Port.of(88443)).build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_user_info_and_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(
              () ->
                  AbsoluteUri.builder(https)
                      .setUserInfo(UserInfo.parse("me:passwd"))
                      .setPort(Port.of(88443))
                      .build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }
  }

  @Nested
  class Transform {

    @Test
    void can_change_an_absolute_uris_scheme() {

      AbsoluteUri absoluteUri =
          AbsoluteUri.parse("https://user@example.com:8443/path?query#fragment");
      AbsoluteUri transformed = absoluteUri.transform(it -> it.setScheme(wss));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(AbsoluteUri.parse("wss://user@example.com:8443/path?query#fragment"));
    }

    @Test
    void can_change_an_absolute_uris_authority() {

      AbsoluteUri absoluteUri =
          AbsoluteUri.parse("https://user@example.com:8443/path?query#fragment");
      AbsoluteUri transformed =
          absoluteUri.transform(it -> it.setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(AbsoluteUri.parse("https://www.example.com/path?query#fragment"));
    }

    @Test
    void can_change_an_opaque_uris_scheme() {

      AbsoluteUri absoluteUri = AbsoluteUri.parse("file:/path?query#fragment");
      AbsoluteUri transformed = absoluteUri.transform(it -> it.setScheme(wss));

      assertThat(transformed)
          .isInstanceOf(OpaqueUri.class)
          .isEqualTo(AbsoluteUri.parse("wss:/path?query#fragment"));
    }

    @Test
    void can_change_an_opaque_uris_authority() {

      AbsoluteUri absoluteUri = AbsoluteUri.parse("file:/path?query#fragment");
      AbsoluteUri transformed =
          absoluteUri.transform(it -> it.setAuthority(Authority.parse("www.example.com")));

      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .isEqualTo(AbsoluteUri.parse("file://www.example.com/path?query#fragment"));
    }
  }
}
