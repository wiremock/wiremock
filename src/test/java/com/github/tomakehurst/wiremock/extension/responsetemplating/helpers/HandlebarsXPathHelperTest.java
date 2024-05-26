/*
 * Copyright (C) 2017-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToXml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HandlebarsXPathHelperTest extends HandlebarsHelperTestBase {

  private HandlebarsXPathHelper helper;

  @BeforeEach
  public void init() {
    helper = new HandlebarsXPathHelper();
  }

  @Test
  public void rendersASimpleValue() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().url("/xml").body("<a><test>success</test></a>"),
            aResponse().withBody("<test>{{xPath request.body '/a/test/text()'}}</test>"));

    assertThat(responseDefinition.getBody(), is("<test>success</test>"));
  }

  @Test
  public void rendersNothingWhenTheXPathExpressionResolvesNoContent() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().url("/xml").body("<a><test>success</test></a>"),
            aResponse().withBody("<test>{{xPath request.body '/b/test'}}</test>"));

    assertThat(responseDefinition.getBody(), startsWith("<test></test>"));
  }

  @Test
  public void extractsASimpleValue() throws IOException {
    testHelper(helper, "<test>success</test>", "/test/text()", "success");
  }

  @Test
  public void extractsAnAttribute() throws IOException {
    testHelper(helper, "<test outcome=\"success\"/>", "/test/@outcome", "success");
  }

  @Test
  public void extractsASubElement() throws IOException {
    testHelper(
        helper,
        "<outer>\n" + "    <inner>stuff</inner>\n" + "</outer>",
        "/outer/inner",
        equalToXml("<inner>stuff</inner>"));
  }

  @Test
  public void rendersAMeaningfulErrorWhenTheInputXmlIsInvalid() {
    testHelperError(
        helper,
        "<testsuccess</test>",
        "/test",
        is("[ERROR: <testsuccess</test> is not valid XML]"));
  }

  @Test
  public void rendersAMeaningfulErrorWhenTheXPathExpressionIsInvalid() {
    testHelperError(
        helper,
        "<test>success</test>",
        "/\\test",
        is("[ERROR: /\\test is not a valid XPath expression]"));
  }

  @Test
  public void rendersAMeaningfulErrorWhenTheXPathExpressionIsAbsent() {
    testHelperError(
        helper, "<test>success</test>", null, is("[ERROR: The XPath expression cannot be empty]"));
  }

  @Test
  public void rendersABlankWhenTheInputXmlIsAbsent() {
    testHelperError(helper, null, "/test", is(""));
  }

  @Test
  public void returnsCorrectResultWhenSameExpressionUsedTwiceOnIdenticalDocuments()
      throws Exception {
    String one = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();
    String two = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();

    assertThat(one, is("one"));
    assertThat(two, is("one"));
  }

  @Test
  public void returnsCorrectResultWhenSameExpressionUsedTwiceOnDifferentDocuments()
      throws Exception {
    String one = renderHelperValue(helper, "<test>one</test>", "/test/text()").toString();
    String two = renderHelperValue(helper, "<test>two</test>", "/test/text()").toString();

    assertThat(one, is("one"));
    assertThat(two, is("two"));
  }

  @Test
  public void returnsCorrectResultWhenDifferentExpressionsUsedOnSameDocument() throws Exception {
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
  public void rendersXmlWhenElementIsSelected() throws Exception {
    String one =
        renderHelperValue(helper, "<test><one>1</one><two>2</two></test>", "/test/one").toString();
    assertThat(one.trim(), is("<one>1</one>"));
  }

  @Test
  public void supportsIterationOverNodeListWithEachHelper() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
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
                    "{{#each (xPath request.body '/stuff/thing/text()') as |thing|}}{{thing}} {{/each}}"));

    assertThat(responseDefinition.getBody(), is("One Two Three "));
  }

  @Test
  public void supportsIterationOverElementsWithAttributes() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
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
                    "{{#each (xPath request.body '/stuff/thing') as |thing|}}{{{thing.attributes.id}}} {{/each}}"));

    assertThat(responseDefinition.getBody(), is("1 2 3 "));
  }

  @Test
  public void supportsIterationOverNamespacedElements() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
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
                    "{{#each (xPath request.body '/stuff/thing') as |thing|}}{{{thing.text}}} {{/each}}"));

    assertThat(responseDefinition.getBody(), is("One Two Three "));
  }

  @Test
  public void rendersNamespacedElement() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest()
                .body(
                    "<?xml version=\"1.0\"?>\n"
                        + "<stuff xmlns:th=\"https://thing.com\">\n"
                        + "    <th:thing>One</th:thing>\n"
                        + "    <th:thing>Two</th:thing>\n"
                        + "    <th:thing>Three</th:thing>\n"
                        + "</stuff>"),
            aResponse().withBody("{{{xPath request.body '/stuff'}}}"));

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
  public void rendersElementNames() {
    final ResponseDefinition responseDefinition =
        transform(
            transformer,
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
                    "{{#each (xPath request.body '/stuff/*') as |thing|}}{{{thing.name}}} {{/each}}"));

    assertThat(responseDefinition.getBody(), is("one two three "));
  }

  @Test
  void rendersElementWhenXPathSelectorReturnsPrimitiveStringResult() {
    ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest().body("<one>\n" + "    <two>value</two>\n" + "</one>"),
            aResponse().withBody("{{xPath request.body 'local-name(/*/*[1])'}}"));

    assertThat(responseDefinition.getBody(), is("two"));
  }

  @Test
  void rendersElementWhenXPathSelectorReturnsPrimitiveNumberResult() {
    ResponseDefinition responseDefinition =
        transform(
            transformer,
            mockRequest()
                .body(
                    "<wrap>\n"
                        + "    <one>value</one>\n"
                        + "    <two>value</two>\n"
                        + "    <three>value</three>\n"
                        + "    <four>value</four>\n"
                        + "</wrap>"),
            aResponse().withBody("{{xPath request.body 'count(/wrap/*)'}}"));

    assertThat(responseDefinition.getBody(), is("4"));
  }
}
