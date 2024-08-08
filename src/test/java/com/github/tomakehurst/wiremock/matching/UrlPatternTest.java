/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UrlPatternTest {

  @Test
  public void matchesExactUrlWithQuery() {
    UrlPattern urlPattern = urlEqualTo("/my/exact/url?one=1&two=2&three=3333333");
    assertTrue(urlPattern.match("/my/exact/url?one=1&two=2&three=3333333").isExactMatch());
    assertFalse(urlPattern.match("/my/wrong/url?one=1&three=3333333").isExactMatch());
  }

  @Test
  public void matchesOnRegexWithQuery() {
    UrlPattern urlPattern = urlMatching("/my/([a-z]*)/url\\?one=1&two=([0-9]*)&three=3333333");
    assertTrue(urlPattern.match("/my/regex/url?one=1&two=123456&three=3333333").isExactMatch());
    assertFalse(urlPattern.match("/my/BAD/url?one=1&two=123456&three=3333333").isExactMatch());
  }

  @Test
  public void matchesExactlyOnPathOnly() {
    UrlPathPattern urlPathPattern = urlPathEqualTo("/the/exact/path");
    assertTrue(urlPathPattern.match("/the/exact/path").isExactMatch());
    assertFalse(urlPathPattern.match("/totally/incorrect/path").isExactMatch());
  }

  @Test
  public void matchesOnPathWithRegex() {
    UrlPathPattern urlPathPattern = urlPathMatching("/my/([a-z]*)/path");
    assertTrue(urlPathPattern.match("/my/regex/path?one=not_looked_at").isExactMatch());
    assertFalse(urlPathPattern.match("/my/12345/path").isExactMatch());
  }

  @Test
  public void noMatchOnNullValueForUrlEquality() {
    assertThat(urlEqualTo("/things").match(null).isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnNullValueForUrlPathEquality() {
    assertThat(urlPathEqualTo("/things").match(null).isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnNullValueForUrlRegex() {
    assertThat(urlMatching("/things/.*").match(null).isExactMatch(), is(false));
  }

  @Test
  public void noMatchOnNullValueForUrlPathRegex() {
    assertThat(urlPathMatching("/things/.*").match(null).isExactMatch(), is(false));
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    UrlPathPattern a = urlPathMatching("/things/.*");
    UrlPathPattern b = urlPathMatching("/things/.*");
    UrlPathPattern c = urlPathMatching("/test/.*");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(b, a);
    assertEquals(b.hashCode(), a.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(b, c);
    assertNotEquals(b.hashCode(), c.hashCode());
  }

  @SuppressWarnings("EqualsWithItself")
  @ParameterizedTest
  @MethodSource("matchersThatShouldBeEqual")
  void matchersShouldBeEqual(UrlPattern matcher1, UrlPattern matcher2) {
    assertEquals(matcher1, matcher2);
    assertEquals(matcher2, matcher1);
    assertEquals(matcher1, matcher1);
    assertEquals(matcher2, matcher2);
  }

  private static Stream<Arguments> matchersThatShouldBeEqual() {
    return Stream.of(
        Arguments.of(urlEqualTo("/things"), urlEqualTo("/things")),
        Arguments.of(urlPathEqualTo("/things"), urlPathEqualTo("/things")),
        Arguments.of(urlPathTemplate("/things"), urlPathTemplate("/things")),
        Arguments.of(urlEqualTo("/things"), urlPathEqualTo("/things")),
        Arguments.of(urlEqualTo("/things"), urlPathTemplate("/things")),
        Arguments.of(urlPathEqualTo("/things"), urlPathTemplate("/things")));
  }

  @ParameterizedTest
  @MethodSource("matchersThatShouldNotBeEqual")
  void matchersShouldNotBeEqual(UrlPattern matcher1, UrlPattern matcher2) {
    assertNotEquals(matcher1, matcher2);
    assertNotEquals(matcher2, matcher1);
  }

  private static Stream<Arguments> matchersThatShouldNotBeEqual() {
    return Stream.of(
        Arguments.of(urlEqualTo("/things"), urlEqualTo("/other-things")),
        Arguments.of(urlPathEqualTo("/things"), urlPathEqualTo("/other-things")),
        Arguments.of(urlPathTemplate("/things"), urlPathTemplate("/other-things")),
        Arguments.of(urlEqualTo("/things"), urlPathEqualTo("/other-things")),
        Arguments.of(urlEqualTo("/things"), urlPathTemplate("/other-things")),
        Arguments.of(urlPathEqualTo("/things"), urlPathTemplate("/other-things")),
        Arguments.of(urlEqualTo("/things"), urlPathTemplate("/{things}")),
        Arguments.of(urlEqualTo("/things"), urlPathTemplate("/things/{thing}")),
        Arguments.of(urlEqualTo("/things/{thing}"), urlPathTemplate("/things/{thing}")),
        Arguments.of(urlEqualTo("/things?foo"), urlPathTemplate("/things")),
        Arguments.of(urlPathEqualTo("/things"), urlPathTemplate("/{things}")),
        Arguments.of(urlPathEqualTo("/things"), urlPathTemplate("/things/{thing}")),
        Arguments.of(urlPathEqualTo("/things/{thing}"), urlPathTemplate("/things/{thing}")));
  }
}
