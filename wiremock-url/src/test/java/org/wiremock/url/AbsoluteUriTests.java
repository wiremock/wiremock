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
import static org.wiremock.url.Scheme.wss;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

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
          AbsoluteUri.builder(Scheme.https)
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
          AbsoluteUri.builder(Scheme.https)
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
            AbsoluteUri.builder(Scheme.https)
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443)),
            AbsoluteUri.builder(Scheme.https)
                .setHost(Host.parse("example.com"))
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password")),
            AbsoluteUri.builder(Scheme.https)
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password")),
            AbsoluteUri.builder(Scheme.https)
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password"))
                .setHost(Host.parse("example.com")),
            AbsoluteUri.builder(Scheme.https)
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com")),
            AbsoluteUri.builder(Scheme.https)
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
          AbsoluteUri.builder(Scheme.https)
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setUserInfo(UserInfo.parse("me:passwd"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://me:passwd@example.com:8443/"));
    }

    @Test
    void setting_host_after_authority_works() {
      var uri =
          AbsoluteUri.builder(Scheme.https)
              .setAuthority(Authority.parse("user@www.example.com:8443"))
              .setHost(Host.parse("example.com"))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://user@example.com:8443/"));
    }

    @Test
    void setting_port_after_authority_works() {
      var uri =
          AbsoluteUri.builder(Scheme.https)
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setPort(Port.of(88443))
              .build();

      assertThat(uri).isEqualTo(AbsoluteUri.parse("https://user@example.com:88443/"));
    }

    @Test
    void authority_overrides_user_info_and_port() {
      var uri =
          AbsoluteUri.builder(Scheme.https)
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
              () ->
                  AbsoluteUri.builder(Scheme.https)
                      .setUserInfo(UserInfo.parse("me:passwd"))
                      .build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(() -> AbsoluteUri.builder(Scheme.https).setPort(Port.of(88443)).build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_user_info_and_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(
              () ->
                  AbsoluteUri.builder(Scheme.https)
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
