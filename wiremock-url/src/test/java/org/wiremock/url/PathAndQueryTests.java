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
    void parses_relative_path_correctly() {
      var pathAndQuery = PathAndQuery.parse("relative");

      assertThat(pathAndQuery.toString()).isEqualTo("relative");
      assertThat(pathAndQuery).isInstanceOf(PathAndQuery.class);

      assertThat(pathAndQuery.getScheme()).isNull();

      assertThat(pathAndQuery.getAuthority()).isNull();
      assertThat(pathAndQuery.getUserInfo()).isNull();
      assertThat(pathAndQuery.getHost()).isNull();
      assertThat(pathAndQuery.getPort()).isNull();

      assertThat(pathAndQuery.getPath()).isEqualTo(Path.parse("relative"));
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
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> PathAndQuery.parse("not a :uri"))
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

    static final List<? extends String> illegalPathAndQueries =
        concat(
            illegalRelativeUrls,
            List.of(
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
    @ValueSource(strings = {
        "/path?query",
        "/path",
    })
    void cannot_set_path_with_colon_in_first_segment_when_no_authority(String relativeUrl) {
      RelativeUrl url = RelativeUrl.parse(relativeUrl);
      var query = url.transform(it -> it.setPath(Path.EMPTY)).toString();
      assertThatExceptionOfType(IllegalPathAndQuery.class)
          .isThrownBy(() -> url.transform(it -> it.setPath(Path.parse("foo:bar"))))
          .withMessage("Illegal path and query: `foo:bar"+query+"` - a relative url without authority's path may not contain a colon (`:`) in the first segment, as this is ambiguous")
          .extracting(IllegalRelativeUrl::getIllegalValue)
          .isEqualTo("foo:bar"+query);
    }
  }
}
