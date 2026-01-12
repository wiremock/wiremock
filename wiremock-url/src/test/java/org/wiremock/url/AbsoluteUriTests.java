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

public class AbsoluteUriTests {

  @Nested
  class Parse {

    @Test
    void parses_absolute_url_correctly() {
      var absoluteUrl = AbsoluteUri.parse("https://example.com/path?query#fragment");

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
      var absoluteUrl = AbsoluteUri.parse("https://user@example.com/path?query#fragment");

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
      var serversideAbsoluteUrl = AbsoluteUri.parse("https://example.com/path?query");

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
    void parses_serverside_absolute_url_empty_host_and_port_correctly() {
      var serversideAbsoluteUrl = AbsoluteUri.parse("data://:443");

      assertThat(serversideAbsoluteUrl.toString()).isEqualTo("data://:443");
      assertThat(serversideAbsoluteUrl).isInstanceOf(Origin.class);

      assertThat(serversideAbsoluteUrl.getScheme()).isEqualTo(Scheme.parse("data"));

      assertThat(serversideAbsoluteUrl.getAuthority())
          .isEqualTo(HostAndPort.of(Host.EMPTY, Port.of(443)));
      assertThat(serversideAbsoluteUrl.getUserInfo()).isNull();
      assertThat(serversideAbsoluteUrl.getHost()).isEqualTo(Host.EMPTY);
      assertThat(serversideAbsoluteUrl.getPort()).isEqualTo(Port.of(443));

      assertThat(serversideAbsoluteUrl.getPath()).isEqualTo(Path.EMPTY);
      assertThat(serversideAbsoluteUrl.getQuery()).isNull();

      assertThat(serversideAbsoluteUrl.getFragment()).isNull();
    }

    @Test
    void parses_origin_correctly() {
      var origin = AbsoluteUri.parse("https://example.com");

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
    void parses_mailto_correctly() {
      var mailtoUri = AbsoluteUri.parse("mailto:joan@example.com");

      assertThat(mailtoUri.toString()).isEqualTo("mailto:joan@example.com");
      assertThat(mailtoUri).isInstanceOf(OpaqueUri.class);

      assertThat(mailtoUri.getScheme()).isEqualTo(Scheme.mailto);

      assertThat(mailtoUri.getAuthority()).isNull();
      assertThat(mailtoUri.getUserInfo()).isNull();
      assertThat(mailtoUri.getHost()).isNull();
      assertThat(mailtoUri.getPort()).isNull();

      assertThat(mailtoUri.getPath()).isEqualTo(Path.parse("joan@example.com"));
      assertThat(mailtoUri.getQuery()).isNull();

      assertThat(mailtoUri.getFragment()).isNull();
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

      assertThat(arn.getScheme()).isEqualTo(Scheme.parse("arn"));

      assertThat(arn.getAuthority()).isNull();
      assertThat(arn.getUserInfo()).isNull();
      assertThat(arn.getHost()).isNull();
      assertThat(arn.getPort()).isNull();

      assertThat(arn.getPath())
          .isEqualTo(
              Path.parse(
                  "aws:servicecatalog:us-east-1:912624918755:stack/some-stack/pp-a3B9zXp1mQ7rS"));
      assertThat(arn.getQuery()).isNull();

      assertThat(arn.getFragment()).isNull();
    }

    @Test
    void parses_file_empty_authority_correctly() {
      var fileUri = AbsoluteUri.parse("file:///home/me/some/dir");

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
      var fileUri = AbsoluteUri.parse("file://user@remote/home/me/some/dir");

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
    void parses_file_no_authority_correctly() {
      var fileUri = AbsoluteUri.parse("file:/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(OpaqueUri.class);

      assertThat(fileUri.getScheme()).isEqualTo(Scheme.file);

      assertThat(fileUri.getAuthority()).isNull();
      assertThat(fileUri.getUserInfo()).isNull();
      assertThat(fileUri.getHost()).isNull();
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();
    }

    @Test
    void rejects_invalid_uri() {
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

    @Test
    void rejects_path_and_query() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalAbsoluteUri.class)
              .isThrownBy(() -> AbsoluteUri.parse("/path?query"))
              .actual();
      assertThat(exception.getMessage()).isEqualTo("Illegal absolute uri: `/path?query`");
      assertThat(exception.getIllegalValue()).isEqualTo("/path?query");
      assertThat(exception.getCause()).isNull();
    }

    @Test
    void rejects_relative_url() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalAbsoluteUri.class)
              .isThrownBy(() -> AbsoluteUri.parse("//example.com/path?query#fragment"))
              .actual();
      assertThat(exception.getMessage())
          .isEqualTo("Illegal absolute uri: `//example.com/path?query#fragment`");
      assertThat(exception.getIllegalValue()).isEqualTo("//example.com/path?query#fragment");
      assertThat(exception.getCause()).isNull();
    }
  }
}
