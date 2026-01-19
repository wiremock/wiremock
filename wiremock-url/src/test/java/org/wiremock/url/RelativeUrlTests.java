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
import static org.wiremock.url.Lists.concat;
import static org.wiremock.url.Scheme.file;
import static org.wiremock.url.Scheme.https;
import static org.wiremock.url.UrlTests.Parse.illegalUrls;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class RelativeUrlTests {

  @Nested
  class Parse {

    @Test
    void parses_relative_url_with_authority_correctly() {
      var relativeUrl = RelativeUrl.parse("//example.com/path?query#fragment");

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

      assertThat(relativeUrl.isAbsolute()).isFalse();
      assertThat(relativeUrl.isRelative()).isTrue();
      assertThat(relativeUrl.isAbsoluteUrl()).isFalse();
      assertThat(relativeUrl.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_relative_url_without_authority_correctly() {
      var relativeUrl = RelativeUrl.parse("/path?query#fragment");

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

      assertThat(relativeUrl.isAbsolute()).isFalse();
      assertThat(relativeUrl.isRelative()).isTrue();
      assertThat(relativeUrl.isAbsoluteUrl()).isFalse();
      assertThat(relativeUrl.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_path_and_query_correctly() {
      var pathAndQuery = RelativeUrl.parse("/path?query");

      assertThat(pathAndQuery.toString()).isEqualTo("/path?query");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_relative_path_correctly() {
      var pathAndQuery = RelativeUrl.parse("relative");

      assertThat(pathAndQuery.toString()).isEqualTo("relative");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_empty_path_correctly() {
      var pathAndQuery = RelativeUrl.parse("");

      assertThat(pathAndQuery.toString()).isEqualTo("");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_query_only_correctly() {
      var pathAndQuery = RelativeUrl.parse("?");

      assertThat(pathAndQuery.toString()).isEqualTo("?");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);
    }

    @Test
    void parses_fragment_only_correctly() {
      var pathAndQuery = RelativeUrl.parse("#");

      assertThat(pathAndQuery.toString()).isEqualTo("#");
      assertThat(pathAndQuery).isInstanceOf(RelativeUrl.class);
      assertThat(pathAndQuery).isNotInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.EMPTY);
      assertThat(pathAndQuery.getQuery()).isNull();

      assertThat(pathAndQuery.getFragment()).isEqualTo(Fragment.parse(""));

      assertThat(pathAndQuery.isAbsolute()).isFalse();
      assertThat(pathAndQuery.isRelative()).isTrue();
      assertThat(pathAndQuery.isAbsoluteUrl()).isFalse();
      assertThat(pathAndQuery.isOpaqueUri()).isFalse();
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> RelativeUrl.parse("not a :uri"))
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

    static final List<? extends String> illegalRelativeUrls =
        concat(
            illegalUrls,
            List.of(
                "https://example.com/path?query#fragment",
                "https://user@example.com/path?query#fragment",
                "https://example.com/path?query",
                "https://example.com"));

    @ParameterizedTest
    @FieldSource("illegalRelativeUrls")
    void rejects_illegal_relative_url(String illegalRelativeUrl) {
      assertThatExceptionOfType(IllegalRelativeUrl.class)
          .isThrownBy(() -> RelativeUrl.parse(illegalRelativeUrl))
          .withMessage("Illegal relative url: `" + illegalRelativeUrl + "`")
          .extracting(IllegalRelativeUrl::getIllegalValue)
          .isEqualTo(illegalRelativeUrl);
    }
  }

  @Nested
  class Transform {

    @Test
    void can_transform_a_relative_url() {
      RelativeUrl base = RelativeUrl.parse("//example.com/path");
      Url transformed = base.transform(b -> b.setScheme(https).setQuery(Query.parse("a=b")));
      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .hasToString("https://example.com/path?a=b");
    }

    @Test
    void can_transform_a_path_and_query() {
      RelativeUrl base = RelativeUrl.parse("/path?a=b");
      Url transformed =
          base.transform(b -> b.setScheme(https).setAuthority(Authority.parse("example.com")));
      assertThat(transformed)
          .isInstanceOf(AbsoluteUrl.class)
          .hasToString("https://example.com/path?a=b");
    }

    @Test
    void can_pointlessly_set_scheme_to_null() {
      var url = RelativeUrl.parse("//example.com/path#fragment");
      Url transformed = url.thaw().setScheme(null).build();
      assertThat(transformed).isInstanceOf(SchemeRelativeUrl.class).isEqualTo(url);
    }

    @Test
    void can_set_authority_to_null() {
      var url = RelativeUrl.parse("//example.com/path#fragment");
      Url transformed = url.thaw().setAuthority(null).build();
      assertThat(transformed)
          .isInstanceOf(RelativeUrl.class)
          .isEqualTo(RelativeUrl.parse("/path#fragment"));
    }

    @Test
    void cannot_set_scheme_without_authority() {

      RelativeUrl url = RelativeUrl.parse("/path?query#fragment");
      assertThatExceptionOfType(IllegalUrl.class)
          .isThrownBy(() -> url.transform(it -> it.setScheme(file)))
          .withMessage("Illegal url: `file:/path?query#fragment`; a url has an authority")
          .withNoCause()
          .extracting(IllegalUrl::getIllegalValue)
          .isEqualTo("file:/path?query#fragment");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "/path?query#fragment",
        "/path?query",
        "/path",
    })
    void cannot_set_path_with_colon_in_first_segment_when_no_authority() {
      RelativeUrl url = RelativeUrl.parse("/path?query#fragment");
      assertThatExceptionOfType(IllegalRelativeUrl.class)
          .isThrownBy(() -> url.transform(it -> it.setPath(Path.parse("foo:bar"))))
          .withMessage("Illegal relative url: `foo:bar?query#fragment` - a relative url without authority's path may not contain a colon (`:`) in the first segment, as this is ambiguous")
          .extracting(IllegalRelativeUrl::getIllegalValue)
          .isEqualTo("foo:bar?query#fragment");
    }
  }
}
