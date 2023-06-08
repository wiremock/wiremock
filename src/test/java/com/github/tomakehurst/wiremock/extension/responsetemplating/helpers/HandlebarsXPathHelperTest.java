/*
 * Copyright (C) 2017-2023 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HandlebarsXPathHelperTest extends HandlebarsHelperTestBase {

  private HandlebarsXPathHelper helper;

  @BeforeEach
  public void init() {
    helper = new HandlebarsXPathHelper();
  }

  @Test
  void rendersASimpleValue() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest().url("/xml").body("<a><test>success</test></a>"),
            aResponse().withBody("<test>{{xPath request.body '/a/test/text()'}}</test>").build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), is("<test>success</test>"));
  }

  @Test
  void rendersNothingWhenTheXPathExpressionResolvesNoContent() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest().url("/xml").body("<a><test>success</test></a>"),
            aResponse().withBody("<test>{{xPath request.body '/b/test'}}</test>").build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), startsWith("<test></test>"));
  }

  @Test
  void extractsASimpleValue() throws IOException {
    testHelper(helper, "<test>success</test>", "/test/text()", "success");
  }

  @Test
  void extractsAnAttribute() throws IOException {
    testHelper(helper, "<test outcome=\"success\"/>", "/test/@outcome", "success");
  }

  @Test
  void extractsASubElement() throws IOException {
    testHelper(
        helper,
        "<outer>\n" + "    <inner>stuff</inner>\n" + "</outer>",
        "/outer/inner",
        equalToXml("<inner>stuff</inner>"));
  }

  @Test
  void rendersAMeaningfulErrorWhenTheInputXmlIsInvalid() {
    testHelperError(
        helper,
        "<testsuccess</test>",
        "/test",
        is("[ERROR: <testsuccess</test> is not valid XML]"));
  }

  @Test
  void rendersAMeaningfulErrorWhenTheXPathExpressionIsInvalid() {
    testHelperError(
        helper,
        "<test>success</test>",
        "/\\test",
        is("[ERROR: /\\test is not a valid XPath expression]"));
  }

  @Test
  void rendersAMeaningfulErrorWhenTheXPathExpressionIsAbsent() {
    testHelperError(
        helper, "<test>success</test>", null, is("[ERROR: The XPath expression cannot be empty]"));
  }

  @Test
  void rendersABlankWhenTheInputXmlIsAbsent() {
    testHelperError(helper, null, "/test", is(""));
  }

  @Test
  void returnsCorrectResultWhenSameExpressionUsedTwiceOnIdenticalDocuments() throws Exception {
    String one = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();
    String two = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();

    assertThat(one, is("one"));
    assertThat(two, is("one"));
  }

  @Test
  void returnsCorrectResultWhenSameExpressionUsedTwiceOnDifferentDocuments() throws Exception {
    String one = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();
    String two = renderHelperValue(helper, "<test>two</test>", "/test/text()").toString();

    assertThat(one, is("one"));
    assertThat(two, is("two"));
  }

  @Test
  void returnsCorrectResultWhenDifferentExpressionsUsedOnSameDocument() throws Exception {
    String one =
        renderHelperValue(helper, "<test><one>1</one><two>2</two></test>", "/test/one/text()")
            .toString();
    String two =
        renderHelperValue(helper, "<test><one>1</one><two>2</two></test>", "/test/two/text()")
            .toString();

    assertThat(one, is("1"));
    assertThat(two, is("2"));
  }

  @Test
  void rendersXmlWhenElementIsSelected() throws Exception {
    String one =
        renderHelperValue(helper, "<test><one>1</one><two>2</two></test>", "/test/one").toString();
    assertThat(one.trim(), is("<one>1</one>"));
  }

  @Test
  void supportsIterationOverNodeListWithEachHelper() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff>\n"
                        + "    <thing>One</thing>\n"
                        + "    <thing>Two</thing>\n"
                        + "    <thing>Three</thing>\n"
                        + "</stuff>"),
            aResponse()
                .withBody(
                    "{{#each (xPath request.body '/stuff/thing/text()') as |thing|}}{{thing}} {{/each}}")
                .build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), is("One Two Three "));
  }

  @Test
  void supportsIterationOverElementsWithAttributes() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff>\n"
                        + "    <thing id=\"1\">One</thing>\n"
                        + "    <thing id=\"2\">Two</thing>\n"
                        + "    <thing id=\"3\">Three</thing>\n"
                        + "</stuff>"),
            aResponse()
                .withBody(
                    "{{#each (xPath request.body '/stuff/thing') as |thing|}}{{{thing.attributes.id}}} {{/each}}")
                .build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), is("1 2 3 "));
  }

  @Test
  void supportsIterationOverNamespacedElements() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff xmlns:th=\"https://thing.com\">\n"
                        + "    <th:thing>One</th:thing>\n"
                        + "    <th:thing>Two</th:thing>\n"
                        + "    <th:thing>Three</th:thing>\n"
                        + "</stuff>"),
            aResponse()
                .withBody(
                    "{{#each (xPath request.body '/stuff/thing') as |thing|}}{{{thing.text}}} {{/each}}")
                .build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), is("One Two Three "));
  }

  @Test
  void rendersNamespacedElement() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff xmlns:th=\"https://thing.com\">\n"
                        + "    <th:thing>One</th:thing>\n"
                        + "    <th:thing>Two</th:thing>\n"
                        + "    <th:thing>Three</th:thing>\n"
                        + "</stuff>"),
            aResponse().withBody("{{{xPath request.body '/stuff'}}}").build(),
            noFileSource(),
            Parameters.empty());

    assertThat(
        responseDefinition.getBody(),
        equalToCompressingWhiteSpace(
            "<stuff xmlns:th=\"https://thing.com\">\n"
                + "    <th:thing>One</th:thing>\n"
                + "    <th:thing>Two</th:thing>\n"
                + "    <th:thing>Three</th:thing>\n"
                + "</stuff>"));
  }

  @Test
  void rendersElementNames() {
    final ResponseDefinition responseDefinition =
        this.transformer.transform(
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff>\n"
                        + "    <one>1</one>\n"
                        + "    <two>2</two>\n"
                        + "    <three>3</three>\n"
                        + "</stuff>"),
            aResponse()
                .withBody(
                    "{{#each (xPath request.body '/stuff/*') as |thing|}}{{{thing.name}}} {{/each}}")
                .build(),
            noFileSource(),
            Parameters.empty());

    assertThat(responseDefinition.getBody(), is("one two three "));
  }
}
