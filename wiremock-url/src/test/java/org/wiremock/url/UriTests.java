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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

public class UriTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = Uri.parse("https://example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_absolute_url_with_userinfo_correctly() {
      var absoluteUrl = Uri.parse("https://user@example.com/path?query#fragment");

      assertThat(absoluteUrl.toString()).isEqualTo("https://user@example.com/path?query#fragment");
      assertThat(absoluteUrl).isInstanceOf(AbsoluteUrl.class);
      assertThat(absoluteUrl).isNotInstanceOf(ServersideAbsoluteUrl.class);
    }

    @Test
    void parses_serverside_absolute_url_correctly() {
      var serversideAbsoluteUrl = Uri.parse("https://example.com/path?query");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("https://example.com/path?query");
      assertThat(serversideAbsoluteUrl).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(serversideAbsoluteUrl).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_serverside_absolute_url_empty_host_and_port_correctly() {
      var serversideAbsoluteUrl = Uri.parse("data://:443");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("data://:443");
      assertThat(serversideAbsoluteUrl).isInstanceOf(Origin.class);
    }

    @Test
    void parses_origin_correctly() {
      var origin = Uri.parse("https://example.com");

      assertThat(origin.toString()).isEqualTo("https://example.com");
      assertThat(origin).isInstanceOf(Origin.class);
    }

    @Test
    void parses_relative_url_with_authority_correctly() {
      var relativeUrl = Uri.parse("//example.com/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("//example.com/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_relative_url_without_authority_correctly() {
      var relativeUrl = Uri.parse("/path?query#fragment");

      assertThat(relativeUrl.toString()).isEqualTo("/path?query#fragment");
      assertThat(relativeUrl).isInstanceOf(RelativeUrl.class);
      assertThat(relativeUrl).isNotInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_path_and_query_correctly() {
      var pathAndQuery = Uri.parse("/path?query");

      assertThat(pathAndQuery.toString()).isEqualTo("/path?query");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_relative_path_correctly() {
      var pathAndQuery = Uri.parse("relative");

      assertThat(pathAndQuery.toString()).isEqualTo("relative");
      assertThat(pathAndQuery).isInstanceOf(RelativeUrl.class);
    }

    @Test
    void parses_empty_path_correctly() {
      var pathAndQuery = Uri.parse("");

      assertThat(pathAndQuery.toString()).isEqualTo("");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_mailto_correctly() {
      var mailtoUri = Uri.parse("mailto:joan@example.com");

      assertThat(mailtoUri.toString()).isEqualTo("mailto:joan@example.com");
      assertThat(mailtoUri).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void parses_arn_correctly() {
      var arn =
          Uri.parse(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");

      assertThat(arn.toString())
          .isEqualTo(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");
      assertThat(arn).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = Uri.parse("file:///home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:///home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_file_with_authority_correctly() {
      var fileUri = Uri.parse("file://user@remote/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file://user@remote/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(ServersideAbsoluteUrl.class);
      assertThat(fileUri).isNotInstanceOf(Origin.class);
    }

    @Test
    void parses_file_no_authority_correctly() {
      var fileUri = Uri.parse("file:/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(OpaqueUri.class);
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Uri.parse("not a :uri"))
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
    void rejects_illegal_relative_url() {
      assertThatExceptionOfType(IllegalRelativeUrl.class)
          .isThrownBy(() -> Uri.parse("#\n"))
          .withMessage("Illegal relative url: `#\n`")
          .withNoCause()
          .extracting(IllegalUri::getIllegalValue)
          .isEqualTo("#\n");
    }

    @Test
    void rejects_illegal_absolute_url() {
      assertThatExceptionOfType(IllegalAbsoluteUrl.class)
          .isThrownBy(() -> Uri.parse("#:\n"))
          .withMessage("Illegal absolute url: `#:\n`")
          .withNoCause()
          .extracting(IllegalUri::getIllegalValue)
          .isEqualTo("#:\n");
    }
  }

  @Nested
  class ResolvedPort {

    private static final List<ResolvedPortCase> resolvedPortCases =
        List.of(
            new ResolvedPortCase("//host:80", Port.of(80)),
            new ResolvedPortCase("https://host", Port.of(443)),
            new ResolvedPortCase("//host", null));

    @ParameterizedTest
    @FieldSource("resolvedPortCases")
    void get_resolved_port_returns_correct_port(ResolvedPortCase testCase) {
      var uri = Uri.parse(testCase.uri);
      assertThat(uri.getResolvedPort()).isEqualTo(testCase.expected);
    }

    record ResolvedPortCase(String uri, @Nullable Port expected) {}
  }

  @Nested
  class Builder {

    @Test
    void can_build_a_uri() {

      var uri =
          Uri.builder()
              .setAuthority(Authority.parse("example.com"))
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//example.com/path?query#fragment"));
    }

    @Test
    void can_build_a_uri_with_separate_authority_parts() {

      var uri =
          Uri.builder()
              .setHost(Host.parse("example.com"))
              .setUserInfo(UserInfo.parse("user:password"))
              .setPort(Port.of(8443))
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//user:password@example.com:8443/path?query#fragment"));
    }

    private static final List<Uri.Builder> authorityBuilders =
        List.of(
            Uri.builder()
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443)),
            Uri.builder()
                .setHost(Host.parse("example.com"))
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password")),
            Uri.builder()
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com"))
                .setUserInfo(UserInfo.parse("user:password")),
            Uri.builder()
                .setPort(Port.of(8443))
                .setUserInfo(UserInfo.parse("user:password"))
                .setHost(Host.parse("example.com")),
            Uri.builder()
                .setUserInfo(UserInfo.parse("user:password"))
                .setPort(Port.of(8443))
                .setHost(Host.parse("example.com")),
            Uri.builder()
                .setUserInfo(UserInfo.parse("user:password"))
                .setHost(Host.parse("example.com"))
                .setPort(Port.of(8443)));

    @ParameterizedTest
    @FieldSource("authorityBuilders")
    void can_set_authority_fields_in_any_order(UriBuilder builder) {
      var uri =
          builder
              .setPath(Path.parse("/path"))
              .setQuery(Query.parse("query"))
              .setFragment(Fragment.parse("fragment"))
              .build();
      assertThat(uri).isEqualTo(Uri.parse("//user:password@example.com:8443/path?query#fragment"));
    }

    @Test
    void setting_user_info_after_authority_works() {
      var uri =
          Uri.builder()
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setUserInfo(UserInfo.parse("me:passwd"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//me:passwd@example.com:8443/"));
    }

    @Test
    void setting_host_after_authority_works() {
      var uri =
          Uri.builder()
              .setAuthority(Authority.parse("user@www.example.com:8443"))
              .setHost(Host.parse("example.com"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//user@example.com:8443/"));
    }

    @Test
    void setting_port_after_authority_works() {
      var uri =
          Uri.builder()
              .setAuthority(Authority.parse("user@example.com:8443"))
              .setPort(Port.of(88443))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//user@example.com:88443/"));
    }

    @Test
    void authority_overrides_user_info_and_port() {
      var uri =
          Uri.builder()
              .setUserInfo(UserInfo.parse("me:passwd"))
              .setPort(Port.of(88443))
              .setAuthority(Authority.parse("user@example.com:8443"))
              .build();

      assertThat(uri).isEqualTo(Uri.parse("//user@example.com:8443/"));
    }

    @Test
    void build_fails_if_user_info_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(() -> Uri.builder().setUserInfo(UserInfo.parse("me:passwd")).build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(() -> Uri.builder().setPort(Port.of(88443)).build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_fails_if_user_info_and_port_set_and_host_or_authority_not() {
      assertThatIllegalStateException()
          .isThrownBy(
              () ->
                  Uri.builder()
                      .setUserInfo(UserInfo.parse("me:passwd"))
                      .setPort(Port.of(88443))
                      .build())
          .withMessage("Cannot construct a uri with a userinfo or port but no host")
          .withNoCause();
    }

    @Test
    void build_with_path_set_to_double_slash_and_no_authority_returns_path_and_query() {
      Uri uri = Uri.builder().setPath(Path.parse("//")).build();
      assertThat(uri).isInstanceOf(PathAndQuery.class);
      assertThat(uri.getPath()).hasToString("//");
    }

    @Test
    void build_fails_if_path_is_set_to_double_slash_and_no_authority_with_fragment() {
      assertThatExceptionOfType(IllegalPathAndQuery.class)
          .isThrownBy(
              () ->
                  Uri.builder()
                      .setPath(Path.parse("//"))
                      .setFragment(Fragment.parse("frag"))
                      .build())
          .withMessage(
              "Illegal relative url: `//#frag` - a relative url without authority's path may not start with //, as that would make the first segment an authority")
          .extracting(IllegalRelativeUrl::getIllegalValue)
          .isEqualTo("//#frag");
    }
  }
}
