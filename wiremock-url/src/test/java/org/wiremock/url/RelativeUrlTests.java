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
import static org.wiremock.url.UrlTests.Parse.illegalUrls;

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
  class Normalise {

    static final List<NormalisationCase<Uri>> normalisationCases =
        Stream.of(
                // Protocol-relative URLs - host normalization
                Pair.of("//EXAMPLE.COM:8080", "//example.com:8080/"),
                Pair.of("//EXAMPLE.COM:08080", "//example.com:8080/"),
                Pair.of("//example.com:08080", "//example.com:8080/"))
            .map(
                it ->
                    new NormalisationCase<>(
                        RelativeUrl.parse(it.getLeft()), RelativeUrl.parse(it.getRight())))
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
