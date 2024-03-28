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
import static com.github.tomakehurst.wiremock.stubbing.SubEvent.WARNING;
import static com.github.tomakehurst.wiremock.testsupport.ServeEventChecks.checkJsonError;
import static com.github.tomakehurst.wiremock.testsupport.ServeEventChecks.checkMessage;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Collections;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class MatchesXPathPatternTest {

  @Test
  public void returnsExactMatchWhenXPathMatches() {
    String mySolarSystemXML =
        "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

    StringValuePattern pattern = WireMock.matchingXPath("//planet[@name='Earth']");

    MatchResult match = pattern.match(mySolarSystemXML);
    assertTrue(match.isExactMatch(), "Expected XPath match");
    assertThat(match.getDistance(), is(0.0));
  }

  @Test
  public void returnsNoExactMatchWhenXPathDoesNotMatch() {
    String mySolarSystemXML =
        "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

    StringValuePattern pattern = WireMock.matchingXPath("//star[@name='alpha centauri']");

    MatchResult match = pattern.match(mySolarSystemXML);
    assertFalse(match.isExactMatch(), "Expected XPath non-match");
    assertThat(match.getDistance(), is(1.0));
  }

  @Test
  public void returnsNoExactMatchWhenXPathExpressionIsInvalid() {
    String mySolarSystemXML =
        "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

    StringValuePattern pattern = WireMock.matchingXPath("//\\\\&&&&&");

    MatchResult match = pattern.match(mySolarSystemXML);
    assertFalse(match.isExactMatch(), "Expected XPath non-match");
    assertThat(match.getDistance(), is(1.0));
  }

  @Test
  public void returnsNoExactMatchWhenXmlIsBadlyFormed() {
    String mySolarSystemXML =
        "solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

    StringValuePattern pattern = WireMock.matchingXPath("//star[@name='alpha centauri']");

    MatchResult match = pattern.match(mySolarSystemXML);
    assertFalse(match.isExactMatch(), "Expected XPath non-match");
    assertThat(match.getDistance(), is(1.0));
  }

  @Test
  public void matchesNamespacedXmlWhenNamespacesSpecified() {
    String xml =
        "<t:thing xmlns:t='http://things' xmlns:s='http://subthings'><s:subThing>The stuff</s:subThing></t:thing>";

    StringValuePattern pattern =
        WireMock.matchingXPath(
            "//sub:subThing[.='The stuff']",
            Map.of("sub", "http://subthings", "t", "http://things"));

    MatchResult match = pattern.match(xml);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesNamespacedXmlFromLocalNames() {
    String xml =
        "<t:thing xmlns:t='http://things' xmlns:s='http://subthings'><s:subThing>The stuff</s:subThing></t:thing>";

    StringValuePattern pattern = WireMock.matchingXPath("/thing/subThing[.='The stuff']");

    MatchResult match = pattern.match(xml);
    assertTrue(match.isExactMatch());
  }

  @Test
  public void matchesAgainstValuePatternWhenSingleElementReturnedFromXPath() {
    String xml = "<outer>\n" + "    <inner>stuff</inner>\n" + "</outer>";

    StringValuePattern pattern = WireMock.matchingXPath("//inner/text()", matching("[a-z]*"));

    assertThat(pattern.match(xml).isExactMatch(), is(true));
  }

  @Test
  public void matchesAgainstValuePatternWhenMultipleElementsReturnedFromXPath() {
    String xml =
        "<outer>\n"
            + "    <inner>stuffing</inner>\n"
            + "    <inner>stuffed</inner>\n"
            + "    <inner>stuff</inner>\n"
            + "    <inner>stuffable</inner>\n"
            + "</outer>";

    StringValuePattern pattern =
        WireMock.matchingXPath("//inner/text()", WireMock.equalTo("stuff"));

    assertThat(pattern.match(xml).isExactMatch(), is(true));
  }

  @Test
  public void returnsTheMatchFromTheClosestElementWhenNoneMatchExactly() {
    String xml =
        "<outer>\n"
            + "    <inner>stuffing</inner>\n"
            + "    <inner>stuffed</inner>\n"
            + "    <inner>stuffy</inner>\n"
            + "    <inner>stuffable</inner>\n"
            + "</outer>";

    StringValuePattern pattern =
        WireMock.matchingXPath("//inner/text()", WireMock.equalTo("stuff"));

    assertThat(pattern.match(xml).getDistance(), closeTo(0.16, 0.01));
  }

  @Test
  public void matchesAttributeAgainstValuePattern() {
    String xml = "<outer inner=\"stuff\"/>";

    StringValuePattern pattern =
        WireMock.matchingXPath("/outer/@inner", equalToIgnoreCase("Stuff"));

    assertThat(pattern.match(xml).isExactMatch(), is(true));
  }

  @Test
  public void returnsAMaxDistanceNoMatchWhenNoNodesReturnedAndValuePatternIsPresent() {
    String xml = "<outer inner=\"stuff\"/>";

    StringValuePattern pattern =
        WireMock.matchingXPath("/outer/@nothing", equalToIgnoreCase("Stuff"));

    assertThat(pattern.match(xml).isExactMatch(), is(false));
    assertThat(pattern.match(xml).getDistance(), is(1.0));
  }

  @Test
  public void matchesComplexElementAgainstValuePattern() {
    String xml = "<outer>\n" + "    <inner>stuff</inner>\n" + "</outer>";

    StringValuePattern pattern =
        WireMock.matchingXPath("/outer/inner", equalToXml("<inner>stuff</inner>"));

    assertThat(pattern.match(xml).isExactMatch(), is(true));
  }

  @Test
  public void matchesCorrectlyWhenSubMatcherIsDateEquality() {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<soapenv:Envelope>\n"
            + "    <soapenv:Body>\n"
            + "        <Retrieve>\n"
            + "            <Policy>\n"
            + "                <EffectiveDate Val=\"01/01/2021\" />\n"
            + "                <Policy Val=\"ABC123\" />\n"
            + "            </Policy>\n"
            + "        </Retrieve>\n"
            + "    </soapenv:Body>\n"
            + "</soapenv:Envelope>";

    StringValuePattern pattern =
        WireMock.matchesXPathWithSubMatcher(
            "//*[local-name() = 'EffectiveDate']/@Val",
            equalToDateTime("2021-01-01T00:00:00").actualFormat("dd/MM/yyyy"));

    assertThat(pattern.match(xml).isExactMatch(), is(true));
  }

  @Test
  public void deserialisesCorrectlyWithoutNamespaces() {
    String json = "{ \"matchesXPath\" : \"/stuff:outer/stuff:inner[.=111]\" }";

    MatchesXPathPattern pattern = Json.read(json, MatchesXPathPattern.class);

    assertThat(pattern.getMatchesXPath(), is("/stuff:outer/stuff:inner[.=111]"));
    assertThat(pattern.getXPathNamespaces(), nullValue());
  }

  @Test
  public void deserialisesCorrectlyWithNamespaces() {
    String json =
        "{ \"matchesXPath\" : \"/stuff:outer/stuff:inner[.=111]\" ,   \n"
            + "  \"xPathNamespaces\" : {                                    \n"
            + "      \"one\" : \"http://one.com/\",                         \n"
            + "      \"two\" : \"http://two.com/\"                          \n"
            + "  }                                                          \n"
            + "}";

    MatchesXPathPattern pattern = Json.read(json, MatchesXPathPattern.class);

    assertThat(pattern.getXPathNamespaces(), hasEntry("one", "http://one.com/"));
    assertThat(pattern.getXPathNamespaces(), hasEntry("two", "http://two.com/"));
  }

  @Test
  public void deserialisesCorrectlyWithValuePattern() {
    String json =
        "{                                      \n"
            + "    \"matchesXPath\": {                 \n"
            + "        \"expression\": \"/thing\",     \n"
            + "        \"matches\": \"[0-9]*\"         \n"
            + "    }                                   \n"
            + "}";

    MatchesXPathPattern pattern = Json.read(json, MatchesXPathPattern.class);

    assertThat(pattern.getValuePattern(), instanceOf(RegexPattern.class));
    assertThat(pattern.getExpected(), is("/thing"));
    assertThat(pattern.getValuePattern().getExpected(), is("[0-9]*"));
  }

  @Test
  public void serialisesCorrectlyWithNamspaces() throws JSONException {
    MatchesXPathPattern pattern =
        new MatchesXPathPattern("//*", Map.of("one", "http://one.com/", "two", "http://two.com/"));

    String json = Json.write(pattern);

    JSONAssert.assertEquals(
        "{ \"matchesXPath\" : \"//*\" ,   \n"
            + "  \"xPathNamespaces\" : {                                    \n"
            + "      \"one\" : \"http://one.com/\",                         \n"
            + "      \"two\" : \"http://two.com/\"                          \n"
            + "  }                                                          \n"
            + "}",
        json,
        false);
  }

  @Test
  public void serialisesCorrectlyWithoutNamspaces() throws JSONException {
    MatchesXPathPattern pattern = new MatchesXPathPattern("//*", Collections.emptyMap());

    String json = Json.write(pattern);

    JSONAssert.assertEquals("{ \"matchesXPath\" : \"//*\" }", json, false);
  }

  @Test
  public void serialisesCorrectlyWithValuePattern() {
    assertThat(
        Json.write(WireMock.matchingXPath("/thing", containing("123"))),
        equalToJson(
            "{                                      \n"
                + "    \"matchesXPath\": {                 \n"
                + "        \"expression\": \"/thing\",     \n"
                + "        \"contains\": \"123\"           \n"
                + "    }                                   \n"
                + "}"));
  }

  @Test
  public void noMatchOnNullValue() {
    assertThat(WireMock.matchingXPath("//*").match(null).isExactMatch(), is(false));
  }

  @Test
  void reportsErrorWhenActualXmlIsInvalid() {
    MatchResult matchResult = new MatchesXPathPattern("/thing").match("<xml");
    checkMessage(
        matchResult,
        WARNING,
        "Warning: failed to parse the XML document. Reason: {\n  \"errors\" : [ {\n    \"code\" : 50,\n    \"title\" : \"XML document structures must start and end within the same entity.\"\n  } ]\n}\nXML: <xml");
  }

  @Test
  void reportsErrorWhenActualIsNotXml() {
    MatchResult matchResult = new MatchesXPathPattern("/thing").match("{not-xml");
    checkMessage(matchResult, WARNING, "Warning: failed to parse the XML document\nXML: {not-xml");
  }

  @Test
  void reportsErrorFromSubMatcher() {
    MatchResult matchResult =
        new MatchesXPathPattern("/something/text()", WireMock.equalToJson("{}"))
            .match("<something>{ bad json</something>");
    checkJsonError(
        matchResult,
        "Unexpected character ('b' (code 98)): was expecting double-quote to start field name\n at [Source: (String)\"{ bad json\"; line: 1, column: 3]");
  }

  @Test
  void reportsErrorWhenXPathExpressionIsInvalid() {
    MatchResult matchResult = new MatchesXPathPattern("/\\!what?").match("<things/>");
    checkMessage(
        matchResult, WARNING, "Warning: failed to evaluate the XPath expression /\\!what?");
  }

  @Test
  public void objectsShouldBeEqualOnSameExpectedValue() {
    MatchesXPathPattern a = new MatchesXPathPattern("/thing");
    MatchesXPathPattern b = new MatchesXPathPattern("/thing");
    MatchesXPathPattern c = new MatchesXPathPattern("/other");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(b, a);
    assertEquals(b.hashCode(), a.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(b, c);
    assertNotEquals(b.hashCode(), c.hashCode());
  }
}
