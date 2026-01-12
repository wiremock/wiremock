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
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import org.wiremock.url.NormalisableInvariantTests.NormalisationCase;

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
  class Normalise {

    static final List<NormalisationCase<Uri>> normalisationCases =
        Stream.of(Pair.of("/?q=%ff", "/?q=%FF"))
            .map(
                it ->
                    new NormalisationCase<>(
                        PathAndQuery.parse(it.getLeft()), PathAndQuery.parse(it.getRight())))
            .toList();

    @TestFactory
    Stream<DynamicTest> normalises_uri_reference_correctly() {
      return NormalisableInvariantTests.generateNotNormalisedInvariantTests(
          normalisationCases.stream().filter(t -> !t.normalForm().equals(t.notNormal())).toList());
    }

    static final List<Uri> alreadyNormalisedUrlReferences =
        normalisationCases.stream().map(NormalisationCase::normalForm).distinct().toList();

    @TestFactory
    Stream<DynamicTest> already_normalised_invariants() {
      return NormalisableInvariantTests.generateNormalisedInvariantTests(
          alreadyNormalisedUrlReferences);
    }
  }
}
