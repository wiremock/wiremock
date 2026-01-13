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
import static org.wiremock.url.Scheme.http;
import static org.wiremock.url.ServersideAbsoluteUrlTests.Parse.illegalServersideAbsoluteUrls;

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

public class OriginTests {

  @Nested
  class Parse {

    @Test
    void parses_origin_correctly() {
      var origin = Origin.parse("http://example.com");

      assertThat(origin.getScheme()).isEqualTo(http);

      assertThat(origin.getAuthority()).isEqualTo(HostAndPort.parse("example.com"));
      assertThat(origin.getUserInfo()).isNull();
      assertThat(origin.getHost()).isEqualTo(Host.parse("example.com"));
      assertThat(origin.getPort()).isNull();

      assertThat(origin.getPathAndQuery()).isEqualTo(PathAndQuery.EMPTY);
      assertThat(origin.getPath()).isEqualTo(Path.EMPTY);
      assertThat(origin.getQuery()).isNull();

      assertThat(origin.getFragment()).isNull();

      assertThat(origin.toString()).isEqualTo("http://example.com");

      assertThat(origin.isAbsolute()).isTrue();
      assertThat(origin.isRelative()).isFalse();
      assertThat(origin.isAbsoluteUrl()).isTrue();
      assertThat(origin.isOpaqueUri()).isFalse();
    }

    @Test
    void rejects_illegal_uri() {
      IllegalUri exception =
          assertThatExceptionOfType(IllegalUri.class)
              .isThrownBy(() -> Origin.parse("not a :uri"))
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

    static final List<? extends String> illegalOrigins =
        concat(
            illegalServersideAbsoluteUrls,
            List.of(
                "http://example.com/",
                "http://example.com/?",
                "http://example.com?",
                "http://example.com/",
                "http://example.com/?"));

    @ParameterizedTest
    @FieldSource("illegalOrigins")
    void rejects_illegal_origin(String illegalOrigin) {
      assertThatExceptionOfType(IllegalOrigin.class)
          .isThrownBy(() -> Origin.parse(illegalOrigin))
          .withMessage("Illegal origin: `" + illegalOrigin + "`")
          .extracting(IllegalOrigin::getIllegalValue)
          .isEqualTo(illegalOrigin);
    }
  }

  @Nested
  class Normalise {

    static final List<NormalisationCase<Uri>> normalisationCases =
        Stream.of(
                Pair.of("http://example.com", "http://example.com/"),
                Pair.of("http://example.com:8080", "http://example.com:8080/"))
            .map(
                it ->
                    new NormalisationCase<>(
                        Origin.parse(it.getLeft()), AbsoluteUrl.parse(it.getRight())))
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

  @Nested
  class Of {

    @Test
    void of_builds_origin_from_valid_parameters_no_port() {
      assertThat(Origin.of(http, HostAndPort.parse("example.com")))
          .isEqualTo(Origin.parse("http://example.com"));
    }

    @Test
    void of_builds_origin_from_valid_parameters_with_port() {
      assertThat(Origin.of(http, HostAndPort.parse("example.com:8080")))
          .isEqualTo(Origin.parse("http://example.com:8080"));
    }

    @Test
    void of_rejects_non_normal_scheme() {
      assertThatExceptionOfType(IllegalOrigin.class)
          .isThrownBy(() -> Origin.of(Scheme.parse("HTTP"), HostAndPort.parse("example.com")))
          .withMessage("Illegal origin: `HTTP://example.com`")
          .withNoCause();
    }

    @Test
    void of_rejects_non_normal_host() {
      assertThatExceptionOfType(IllegalOrigin.class)
          .isThrownBy(() -> Origin.of(http, HostAndPort.parse("EXAMPLE.com")))
          .withMessage("Illegal origin: `http://EXAMPLE.com`")
          .withNoCause();
    }

    @Test
    void of_rejects_non_normal_port() {
      assertThatExceptionOfType(IllegalOrigin.class)
          .isThrownBy(() -> Origin.of(http, HostAndPort.parse("example.com:80")))
          .withMessage("Illegal origin: `http://example.com:80`")
          .withNoCause();
    }
  }
}
