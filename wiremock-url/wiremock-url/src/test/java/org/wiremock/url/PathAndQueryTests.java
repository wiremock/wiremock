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
import static org.wiremock.url.RelativeUrlTests.Parse.illegalRelativeUrls;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.junit.jupiter.params.provider.ValueSource;

class PathAndQueryTests {

  @Nested
  class Parse {

    @Test
    void parses_path_and_query_correctly() {
      var pathAndQuery = PathAndQuery.parse("/path?query");

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

      assertThat(pathAndQuery.isAbsolute()).isFalse();
      assertThat(pathAndQuery.isRelative()).isTrue();
      assertThat(pathAndQuery.isAbsoluteUrl()).isFalse();
      assertThat(pathAndQuery.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_path_with_double_slash_correctly() {
      var pathAndQuery = PathAndQuery.parse("//relative");

      assertThat(pathAndQuery.toString()).isEqualTo("//relative");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.parse("//relative"));
      assertThat(pathAndQuery.getQuery()).isNull();

      assertThat(pathAndQuery.getFragment()).isNull();

      assertThat(pathAndQuery.isAbsolute()).isFalse();
      assertThat(pathAndQuery.isRelative()).isTrue();
      assertThat(pathAndQuery.isAbsoluteUrl()).isFalse();
      assertThat(pathAndQuery.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_empty_path_correctly() {
      var pathAndQuery = PathAndQuery.parse("");

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

      assertThat(pathAndQuery.isAbsolute()).isFalse();
      assertThat(pathAndQuery.isRelative()).isTrue();
      assertThat(pathAndQuery.isAbsoluteUrl()).isFalse();
      assertThat(pathAndQuery.isOpaqueUri()).isFalse();
    }

    @Test
    void parses_query_only_correctly() {
      var pathAndQuery = PathAndQuery.parse("?");

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

      assertThat(pathAndQuery.isAbsolute()).isFalse();
      assertThat(pathAndQuery.isRelative()).isTrue();
      assertThat(pathAndQuery.isAbsoluteUrl()).isFalse();
      assertThat(pathAndQuery.isOpaqueUri()).isFalse();
    }

    @Test
    void rejects_illegal_path_and_query() {
      assertThatExceptionOfType(IllegalUri.class)
          .isThrownBy(() -> PathAndQuery.parse("not a :uri"))
          .withMessage("Illegal path and query: `not a :uri`")
          .withNoCause();
    }

    static final List<? extends String> illegalPathAndQueries =
        concat(
            illegalRelativeUrls,
            List.of(
                "relative",
                "//example.com/path?query#fragment",
                "/path?query#fragment",
                "/path#fragment",
                "?query#fragment",
                "?#fragment",
                "?#",
                "#fragment",
                "#"));

    @ParameterizedTest
    @FieldSource("illegalPathAndQueries")
    void rejects_illegal_path_and_query(String illegalPathAndQuery) {
      assertThatExceptionOfType(IllegalPathAndQuery.class)
          .isThrownBy(() -> PathAndQuery.parse(illegalPathAndQuery))
          .withMessage("Illegal path and query: `" + illegalPathAndQuery + "`")
          .extracting(IllegalPathAndQuery::getIllegalValue)
          .isEqualTo(illegalPathAndQuery);
    }
  }

  @Nested
  class Transform {

    @ParameterizedTest
    @ValueSource(
        strings = {
          "/path?query",
          "/path",
        })
    void cannot_set_path_with_colon_in_first_segment_when_no_authority(String relativeUrl) {
      PathAndQuery pathAndQuery = PathAndQuery.parse(relativeUrl);
      var query = pathAndQuery.transform(it -> it.setPath(Path.EMPTY)).toString();
      assertThatExceptionOfType(IllegalRelativeUrl.class)
          .isThrownBy(() -> pathAndQuery.transform(it -> it.setPath(Path.parse("foo:bar"))))
          .withMessage(
              "Illegal relative url: `foo:bar"
                  + query
                  + "` - a relative url without authority's path may not contain a colon (`:`) in the first segment, as that implies a scheme")
          .extracting(IllegalRelativeUrl::getIllegalValue)
          .isEqualTo("foo:bar" + query);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "//", "///", "//foo", "//foo/", "///foo/",
        })
    void can_set_path_with_double_slash_when_no_authority(String legalPath) {
      PathAndQuery original = PathAndQuery.parse("/path?query");
      Url transformed = original.transform(it -> it.setPath(Path.parse(legalPath)));
      PathAndQuery transfo =
          assertThat(transformed).asInstanceOf(type(PathAndQuery.class)).actual();
      assertThat(transfo.toString()).isEqualTo(legalPath + "?query");
      assertThat(transfo.getPath().toString()).isEqualTo(legalPath);
      assertThat(transfo.getQuery()).hasToString("query");
    }
  }

  @Nested
  class Of {
    @ParameterizedTest
    @ValueSource(
        strings = {
          "//", "///", "//foo", "//foo/", "///foo/", "", "/", "/foo",
        })
    void can_set_path_with_double_slash_when_no_authority(String legalPath) {
      PathAndQuery pathAndQuery = PathAndQuery.of(Path.parse(legalPath));
      assertThat(pathAndQuery.toString()).isEqualTo(legalPath);
      assertThat(pathAndQuery.getPath().toString()).isEqualTo(legalPath);
      assertThat(pathAndQuery.getQuery()).isNull();
    }
  }

  @Test
  void path_and_query_may_not_be_equal_to_relative_url_with_same_string_representation() {

    var stringForm = "//example.com/path";

    PathAndQuery pathAndQuery = PathAndQuery.parse(stringForm);
    RelativeUrl relativeUrl = RelativeUrl.parse(stringForm);

    assertThat(pathAndQuery).isNotEqualTo(relativeUrl);
    assertThat(relativeUrl).isNotEqualTo(pathAndQuery);

    var baseUrl = AbsoluteUrl.parse("https://www.example.com/other/");

    assertThat(baseUrl.resolve(pathAndQuery))
        .isEqualTo(AbsoluteUrl.parse("https://www.example.com//example.com/path"));
    assertThat(baseUrl.resolve(relativeUrl))
        .isEqualTo(AbsoluteUrl.parse("https://example.com/path"));
  }
}
