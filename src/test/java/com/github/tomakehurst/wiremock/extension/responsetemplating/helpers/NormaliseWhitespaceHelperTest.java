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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class NormaliseWhitespaceHelperTest extends HandlebarsHelperTestBase {

  @ParameterizedTest
  @ValueSource(strings = {"normaliseWhitespace", "normalizeWhitespace"})
  void trimsAndCollapsesWhitespace(String helper) {
    var response =
        transform(
            transformer,
            mockRequest().body(" \t one\r\n two\f\t three  "),
            aResponse().withBody("{{" + helper + " request.body}}"));

    assertThat(response.getBody(), is("one two three"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"normaliseWhitespace", "normalizeWhitespace"})
  void normalisesUnicodeWhitespaceWithoutChangingOtherCharacters(String helper) {
    var response =
        transform(
            transformer,
            mockRequest().body("\u00a0one\u2003\u00a0two\u200bthree\u2003"),
            aResponse().withBody("{{" + helper + " request.body}}"));

    assertThat(response.getBody(), is("one two\u200bthree"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"normaliseWhitespace", "normalizeWhitespace"})
  void rendersNullEmptyAndBlankValuesAsEmpty(String helper) {
    var response =
        transform(
            transformer,
            mockRequest().body(" \t\n "),
            aResponse()
                .withBody(
                    "{{"
                        + helper
                        + " null}}|{{"
                        + helper
                        + " ''}}|{{"
                        + helper
                        + " request.body}}"));

    assertThat(response.getBody(), is("||"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"normaliseWhitespace", "normalizeWhitespace"})
  void rendersBlockContentBeforeNormalising(String helper) {
    var response =
        transform(
            transformer,
            mockRequest().url("/things"),
            aResponse()
                .withBody(
                    "{{#" + helper + "}}\n  Path: \t {{request.path}}  \n{{/" + helper + "}}"));

    assertThat(response.getBody(), is("Path: /things"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"normaliseWhitespace", "normalizeWhitespace"})
  void retainsHandlebarsEscapingForInlineValues(String helper) {
    var template =
        TemplateEngine.defaultTemplateEngine().getUncachedTemplate("{{" + helper + " value}}");

    assertThat(
        template.apply(Map.of("value", "  <p>one   & two</p>  ")),
        is("&lt;p&gt;one &amp; two&lt;/p&gt;"));
  }
}
