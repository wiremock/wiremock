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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
      assertThat(pathAndQuery.getPath()).isEqualTo(Path.parse("/path"));
      assertThat(pathAndQuery.getQuery()).isEqualTo(Query.parse("query"));

      assertThat(pathAndQuery.getFragment()).isNull();
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
    void rejects_invalid_uri() {
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

    @Test
    void rejects_mailto() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Url.parse("mailto:joan@example.com"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal url: `mailto:joan@example.com`; a url has an authority");
      assertThat(exception.getIllegalValue()).isEqualTo("mailto:joan@example.com");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_arn() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(
                  () ->
                      Url.parse(
                          "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS"))
              .actual();
      exception.printStackTrace();
      assertThat(exception.getMessage())
          .isEqualTo(
              "Illegal url: `arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS`; a url has an authority");
      assertThat(exception.getIllegalValue())
          .isEqualTo(
              "arn:aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_file_no_authority() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Url.parse("file:/home/me/some/dir"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal url: `file:/home/me/some/dir`; a url has an authority");
      assertThat(exception.getIllegalValue()).isEqualTo("file:/home/me/some/dir");
      assertThat(exception.getCause()).isNull();
    }
  }
}
