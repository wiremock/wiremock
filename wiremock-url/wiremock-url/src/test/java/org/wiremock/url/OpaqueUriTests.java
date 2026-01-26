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
import static org.wiremock.url.SchemeRegistry.mailto;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

class OpaqueUriTests {

  @Nested
  class Parse {

    @Test
    void parses_mailto_correctly() {
      var mailtoUri = OpaqueUri.parse("mailto:joan@example.com");

      assertThat(mailtoUri.toString()).isEqualTo("mailto:joan@example.com");
      assertThat(mailtoUri).isInstanceOf(OpaqueUri.class);

      assertThat(mailtoUri.getScheme()).isEqualTo(mailto);

      assertThat(mailtoUri.getAuthority()).isNull();
      assertThat(mailtoUri.getUserInfo()).isNull();
      assertThat(mailtoUri.getHost()).isNull();
      assertThat(mailtoUri.getPort()).isNull();

      assertThat(mailtoUri.getPath()).isEqualTo(Path.parse("joan@example.com"));
      assertThat(mailtoUri.getQuery()).isNull();

      assertThat(mailtoUri.getFragment()).isNull();

      assertThat(mailtoUri.isAbsolute()).isTrue();
      assertThat(mailtoUri.isRelative()).isFalse();
      assertThat(mailtoUri.isAbsoluteUrl()).isFalse();
      assertThat(mailtoUri.isOpaqueUri()).isTrue();
    }

    @Test
    void parses_arn_correctly() {
      var arn =
          OpaqueUri.parse(
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

      assertThat(arn.isAbsolute()).isTrue();
      assertThat(arn.isRelative()).isFalse();
      assertThat(arn.isAbsoluteUrl()).isFalse();
      assertThat(arn.isOpaqueUri()).isTrue();
    }

    @Test
    void parses_file_no_authority_correctly() {
      var fileUri = OpaqueUri.parse("file:/home/me/some/dir");

      assertThat(fileUri.toString()).isEqualTo("file:/home/me/some/dir");
      assertThat(fileUri).isInstanceOf(OpaqueUri.class);

      assertThat(fileUri.getScheme()).isEqualTo(file);

      assertThat(fileUri.getAuthority()).isNull();
      assertThat(fileUri.getUserInfo()).isNull();
      assertThat(fileUri.getHost()).isNull();
      assertThat(fileUri.getPort()).isNull();

      assertThat(fileUri.getPath()).isEqualTo(Path.parse("/home/me/some/dir"));
      assertThat(fileUri.getQuery()).isNull();

      assertThat(fileUri.getFragment()).isNull();

      assertThat(fileUri.isAbsolute()).isTrue();
      assertThat(fileUri.isRelative()).isFalse();
      assertThat(fileUri.isAbsoluteUrl()).isFalse();
      assertThat(fileUri.isOpaqueUri()).isTrue();
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> OpaqueUri.parse("not a :uri"))
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

    private static final List<? extends String> nonOpaqueUris =
        concat(
            illegalAbsoluteUris,
            List.of(
                "https://example.com/path?query#fragment",
                "https://user@example.com/path?query#fragment",
                "https://example.com/path?query",
                "data://:443",
                "https://example.com",
                "file:///home/me/some/dir",
                "file://user@remote/home/me/some/dir"));

    @ParameterizedTest
    @FieldSource("nonOpaqueUris")
    void rejects_illegal_opaque_uris(String nonOpaqueUri) {
      assertThatExceptionOfType(IllegalOpaqueUri.class)
          .isThrownBy(() -> OpaqueUri.parse(nonOpaqueUri))
          .withMessage("Illegal opaque uri: `" + nonOpaqueUri + "`")
          .withNoCause()
          .extracting(IllegalOpaqueUri::getIllegalValue)
          .isEqualTo(nonOpaqueUri);
    }
  }

  @Nested
  class Resolve {

    @Test
    void resolvesOpaqueUriAgainstInput() {
      var opaqueUri = OpaqueUri.parse("non-spec:/..//p");
      assertThat(opaqueUri.resolve(Uri.parse(""))).isEqualTo(OpaqueUri.parse("non-spec:/p"));
    }
  }

  @Nested
  class Normalise {

    @Test
    void normalisesOpaqueUri() {
      var opaqueUri = OpaqueUri.parse("non-spec:/..//p");
      assertThat(opaqueUri.normalise()).isEqualTo(OpaqueUri.parse("non-spec:/p"));
    }
  }
}
