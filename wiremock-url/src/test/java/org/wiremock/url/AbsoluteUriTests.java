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
}
