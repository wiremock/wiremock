/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonValue;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import java.util.Objects;

/** The type Url pattern. */
public class UrlPattern implements NamedValueMatcher<String> {

  /** The constant ANY. */
  public static final UrlPattern ANY = new UrlPattern(new AnythingPattern(), false);

  /** The Pattern. */
  protected final StringValuePattern pattern;

  private final boolean regex;

  /**
   * Instantiates a new Url pattern.
   *
   * @param pattern the pattern
   * @param regex the regex
   */
  public UrlPattern(StringValuePattern pattern, boolean regex) {
    this.pattern = pattern;
    this.regex = regex;
  }

  /**
   * From one of url pattern.
   *
   * @param url the url
   * @param urlPattern the url pattern
   * @param urlPath the url path
   * @param urlPathPattern the url path pattern
   * @param urlPathTemplate the url path template
   * @return the url pattern
   */
  public static UrlPattern fromOneOf(
      String url,
      String urlPattern,
      String urlPath,
      String urlPathPattern,
      String urlPathTemplate) {
    if (url != null) {
      return WireMock.urlEqualTo(url);
    } else if (urlPattern != null) {
      return WireMock.urlMatching(urlPattern);
    } else if (urlPath != null) {
      return WireMock.urlPathEqualTo(urlPath);
    } else if (urlPathPattern != null) {
      return WireMock.urlPathMatching(urlPathPattern);
    } else if (urlPathTemplate != null) {
      return WireMock.urlPathTemplate(urlPathTemplate);
    } else {
      return WireMock.anyUrl();
    }
  }

  @Override
  public MatchResult match(String url) {
    return pattern.match(url);
  }

  @Override
  public String getName() {
    return pattern.getName();
  }

  /**
   * Is regex boolean.
   *
   * @return the boolean
   */
  public boolean isRegex() {
    return regex;
  }

  /**
   * Gets pattern.
   *
   * @return the pattern
   */
  @JsonValue
  public StringValuePattern getPattern() {
    return pattern;
  }

  /**
   * Gets path template.
   *
   * @return the path template
   */
  public PathTemplate getPathTemplate() {
    return null;
  }

  @Override
  public String getExpected() {
    return pattern.expectedValue;
  }

  @Override
  public String toString() {
    return "path and query " + pattern.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlPattern that = (UrlPattern) o;
    return regex == that.regex && Objects.equals(pattern, that.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern, regex);
  }

  /**
   * Is specified boolean.
   *
   * @return the boolean
   */
  public boolean isSpecified() {
    return pattern.getClass() != AnythingPattern.class;
  }
}
