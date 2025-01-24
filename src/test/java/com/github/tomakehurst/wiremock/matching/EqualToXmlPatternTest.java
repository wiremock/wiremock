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

import static com.github.tomakehurst.wiremock.client.WireMock.equalToXml;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.xmlunit.diff.ComparisonType.ATTR_VALUE;
import static org.xmlunit.diff.ComparisonType.NAMESPACE_URI;
import static org.xmlunit.diff.ComparisonType.SCHEMA_LOCATION;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import java.util.Locale;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class EqualToXmlPatternTest {

  @RegisterExtension public WireMockExtension wm = WireMockExtension.newInstance().build();

  @BeforeEach
  public void init() {
    LocalNotifier.set(new ConsoleNotifier(true));

    // We assert English XML parser error messages in this test. So we set our default locale to
    // English to make
    // this test succeed even for users with non-English default locales.
    Locale.setDefault(Locale.ENGLISH);
  }

  @AfterEach
  public void cleanup() {
    LocalNotifier.set(null);
  }

  @Test
  public void returnsNoMatchAnd1DistanceWhenActualIsNull() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    MatchResult matchResult = pattern.match(null);

    assertFalse(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(1.0));
  }

  @Test
  public void returnsNoMatchAnd1DistanceWhenActualIsEmpty() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    MatchResult matchResult = pattern.match("");

    assertFalse(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(1.0));
  }

  @Test
  public void returnsNoMatchAnd1DistanceWhenActualIsNotXml() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    MatchResult matchResult = pattern.match("{ \"markup\": \"wrong\" }");

    assertFalse(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(1.0));
  }

  @Test
  public void returnsExactMatchWhenDocumentsAreIdentical() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    assertTrue(
        pattern
            .match(
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
                    + "<things>\n"
                    + "    <thing characteristic=\"tepid\"/>\n"
                    + "    <thing characteristic=\"tedious\"/>\n"
                    + "</things>")
            .isExactMatch());
  }

  @Test
  public void returnsExactMatchWhenDocumentsAreIdenticalOtherThanWhitespace() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    assertTrue(
        pattern
            .match(
                "<things><thing characteristic=\"tepid\"/><thing characteristic=\"tedious\"/></things>")
            .isExactMatch());
  }

  @Test
  public void returnsNoMatchWhenDocumentsAreTotallyDifferent() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    MatchResult matchResult = pattern.match("<no-things-at-all />");

    assertFalse(matchResult.isExactMatch());
    assertThat(
        matchResult.getDistance(), is(0.5)); // Not high enough really, some more tweaking needed
  }

  @Test
  public void returnsLowDistanceWhenActualDocumentHasMissingElement() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<things>\n"
                + "    <thing characteristic=\"tepid\"/>\n"
                + "    <thing characteristic=\"tedious\"/>\n"
                + "</things>");

    MatchResult matchResult =
        pattern.match("<things>\n" + "    <thing characteristic=\"tepid\"/>\n" + "</things>");

    assertThat(matchResult.getDistance(), closeTo(0.14, 2));
  }

  @Test
  public void returnsExactMatchOnNamespacedXml() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "    <soap:Body>\n"
                + "        <stuff xmlns=\"https://example.com/mynamespace\">\n"
                + "            <things />\n"
                + "        </stuff>\n"
                + "    </soap:Body>\n"
                + "</soap:Envelope>\n");

    MatchResult match =
        pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "    <soap:Body>\n"
                + "        <stuff xmlns=\"https://example.com/mynamespace\">\n"
                + "            <things />\n"
                + "        </stuff>\n"
                + "    </soap:Body>\n"
                + "</soap:Envelope>\n");

    assertThat(match.getDistance(), is(0.0));
    assertTrue(match.isExactMatch());
  }

  @Test
  public void returnsExactMatchOnNamespacedXmlWhenNamespacePrefixesDiffer() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<shampoo:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:shampoo=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "    <shampoo:Body>\n"
                + "        <stuff xmlns=\"https://example.com/mynamespace\">\n"
                + "            <things />\n"
                + "        </stuff>\n"
                + "    </shampoo:Body>\n"
                + "</shampoo:Envelope>\n");

    MatchResult match =
        pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "    <soap:Body>\n"
                + "        <stuff xmlns=\"https://example.com/mynamespace\">\n"
                + "            <things />\n"
                + "        </stuff>\n"
                + "    </soap:Body>\n"
                + "</soap:Envelope>\n");

    assertThat(match.getDistance(), is(0.0));
    assertTrue(match.isExactMatch());
  }

  @Test
  public void doesNotReturnExactMatchWhenNamespaceUriDiffers() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + "    <soap:Body>\n"
                + "        <stuff xmlns=\"https://example.com/mynamespace\">\n"
                + "            <things />\n"
                + "        </stuff>\n"
                + "    </soap:Body>\n"
                + "</soap:Envelope>\n");

    assertFalse(
        pattern
            .match(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                    + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                    + "    <soap:Body>\n"
                    + "        <stuff xmlns=\"https://example.com/the-wrong-namespace\">\n"
                    + "            <things />\n"
                    + "        </stuff>\n"
                    + "    </soap:Body>\n"
                    + "</soap:Envelope>\n")
            .isExactMatch());
  }

  @Test
  public void returnsExactMatchWhenAttributesAreInDifferentOrder() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern("<my-attribs one=\"1\" two=\"2\" three=\"3\"/>");
    assertTrue(pattern.match("<my-attribs two=\"2\" one=\"1\" three=\"3\"/>").isExactMatch());
  }

  @Test
  public void returnsExactMatchWhenElementsAreInDifferentOrder() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<my-elements>\n"
                + "    <one />\n"
                + "    <two />\n"
                + "    <three />\n"
                + "</my-elements>");

    assertTrue(
        pattern
            .match(
                "<my-elements>\n"
                    + "    <two />\n"
                    + "    <three />\n"
                    + "    <one />\n"
                    + "</my-elements>")
            .isExactMatch());
  }

  @Test
  public void returnsNoMatchWhenTagNamesDifferAndContentIsSame() {
    final EqualToXmlPattern pattern = new EqualToXmlPattern("<one>Hello</one>");
    final MatchResult matchResult = pattern.match("<two>Hello</two>");

    assertThat(matchResult.isExactMatch(), equalTo(false));
    assertThat(matchResult.getDistance(), not(equalTo(0.0)));
  }

  @Test
  public void logsASensibleErrorMessageWhenActualXmlIsBadlyFormed() {
    Notifier notifier = Mockito.mock(Notifier.class);
    LocalNotifier.set(notifier);
    equalToXml("<well-formed />").match("badly-formed >").isExactMatch();
    verify(notifier).info(contains("Failed to process XML. Content is not allowed in prolog."));
  }

  @Test
  public void doesNotFetchDtdBecauseItCouldResultInAFailedMatch() {
    String xmlWithDtdThatCannotBeFetched =
        "<!DOCTYPE my_request SYSTEM \"https://thishostname.doesnotexist.com/one.dtd\"><do_request/>";
    EqualToXmlPattern pattern = new EqualToXmlPattern(xmlWithDtdThatCannotBeFetched);
    assertTrue(pattern.match(xmlWithDtdThatCannotBeFetched).isExactMatch());
  }

  @Test
  public void createEqualToXmlPatternWithPlaceholderFromWireMockClass() {
    String placeholderOpeningDelimiterRegex = "theOpeningDelimiterRegex";
    String placeholderClosingDelimiterRegex = "theClosingDelimiterRegex";
    EqualToXmlPattern equalToXmlPattern =
        equalToXml(
            "<a/>", true, placeholderOpeningDelimiterRegex, placeholderClosingDelimiterRegex);
    assertThat(equalToXmlPattern.isEnablePlaceholders(), is(true));
    assertThat(
        equalToXmlPattern.getPlaceholderOpeningDelimiterRegex(),
        is(placeholderOpeningDelimiterRegex));
    assertThat(
        equalToXmlPattern.getPlaceholderClosingDelimiterRegex(),
        is(placeholderClosingDelimiterRegex));
  }

  @Test
  public void createEqualToXmlPatternWithPlaceholderFromWireMockClass_DefaultDelimiters() {
    EqualToXmlPattern equalToXmlPattern = equalToXml("<a/>", true);
    assertThat(equalToXmlPattern.isEnablePlaceholders(), is(true));
    assertNull(equalToXmlPattern.getPlaceholderOpeningDelimiterRegex());
    assertNull(equalToXmlPattern.getPlaceholderClosingDelimiterRegex());
  }

  @Test
  public void returnsMatchWhenTextNodeIsIgnored() {
    String expectedXml = "<a>#{xmlunit.ignore}</a>";
    String actualXml = "<a>123</a>";
    EqualToXmlPattern pattern = new EqualToXmlPattern(expectedXml, true, "#\\{", "}", null, false);
    MatchResult matchResult = pattern.match(actualXml);

    assertTrue(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(0.0));
  }

  @Test
  public void returnsMatchWhenTextNodeIsIgnored_DefaultDelimiters() {
    String expectedXml = "<a>${xmlunit.ignore}</a>";
    String actualXml = "<a>123</a>";
    EqualToXmlPattern pattern = new EqualToXmlPattern(expectedXml, true, null, null, null, false);
    MatchResult matchResult = pattern.match(actualXml);

    assertTrue(matchResult.isExactMatch());
    assertThat(matchResult.getDistance(), is(0.0));
  }

  @Test
  public void deserializesEqualToXmlWithMinimalParameters() {
    String patternJson = "{" + "\"equalToXml\" : \"<a/>\"" + "}";
    StringValuePattern stringValuePattern = Json.read(patternJson, StringValuePattern.class);

    assertTrue(stringValuePattern instanceof EqualToXmlPattern);
    EqualToXmlPattern equalToXmlPattern = (EqualToXmlPattern) stringValuePattern;
    assertThat(equalToXmlPattern.isEnablePlaceholders(), nullValue());
    assertThat(equalToXmlPattern.getPlaceholderOpeningDelimiterRegex(), nullValue());
    assertThat(equalToXmlPattern.getPlaceholderClosingDelimiterRegex(), nullValue());
    assertThat(equalToXmlPattern.getExemptedComparisons(), nullValue());
  }

  @Test
  public void deserializesEqualToXmlWithAllParameters() {
    Boolean enablePlaceholders = Boolean.TRUE;
    Boolean ignoreOrderOfSameNode = Boolean.TRUE;
    String placeholderOpeningDelimiterRegex = "theOpeningDelimiterRegex";
    String placeholderClosingDelimiterRegex = "theClosingDelimiterRegex";
    String patternJson =
        "{"
            + "\"equalToXml\" : \"<a/>\", "
            + "\"enablePlaceholders\" : "
            + enablePlaceholders
            + ", "
            + "\"ignoreOrderOfSameNode\" : "
            + ignoreOrderOfSameNode
            + ", "
            + "\"placeholderOpeningDelimiterRegex\" : \""
            + placeholderOpeningDelimiterRegex
            + "\", "
            + "\"placeholderClosingDelimiterRegex\" : \""
            + placeholderClosingDelimiterRegex
            + "\", "
            + "\"exemptedComparisons\": [\"SCHEMA_LOCATION\", \"NAMESPACE_URI\", \"ATTR_VALUE\"] }";
    StringValuePattern stringValuePattern = Json.read(patternJson, StringValuePattern.class);

    assertTrue(stringValuePattern instanceof EqualToXmlPattern);
    EqualToXmlPattern equalToXmlPattern = (EqualToXmlPattern) stringValuePattern;
    assertEquals(enablePlaceholders, equalToXmlPattern.isEnablePlaceholders());
    assertEquals(ignoreOrderOfSameNode, equalToXmlPattern.isIgnoreOrderOfSameNode());
    assertEquals(
        placeholderOpeningDelimiterRegex, equalToXmlPattern.getPlaceholderOpeningDelimiterRegex());
    assertEquals(
        placeholderClosingDelimiterRegex, equalToXmlPattern.getPlaceholderClosingDelimiterRegex());
    assertThat(
        equalToXmlPattern.getExemptedComparisons(),
        Matchers.is(Set.of(SCHEMA_LOCATION, NAMESPACE_URI, ATTR_VALUE)));
  }

  @Test
  public void serializesEqualToXmlWithAllParameters() {
    String xml = "<stuff />";
    Boolean enablePlaceholders = Boolean.TRUE;
    Boolean ignoreOrderOfSameNode = Boolean.TRUE;
    String placeholderOpeningDelimiterRegex = "[";
    String placeholderClosingDelimiterRegex = "]";

    StringValuePattern pattern =
        new EqualToXmlPattern(
            xml,
            enablePlaceholders,
            placeholderOpeningDelimiterRegex,
            placeholderClosingDelimiterRegex,
            Set.of(SCHEMA_LOCATION, NAMESPACE_URI, ATTR_VALUE),
            ignoreOrderOfSameNode);

    String json = Json.write(pattern);

    assertThat(
        json,
        WireMatchers.equalToJson(
            "{\n"
                + "  \"equalToXml\": \"<stuff />\",\n"
                + "  \"enablePlaceholders\": true,\n"
                + "  \"ignoreOrderOfSameNode\": true,\n"
                + "  \"placeholderOpeningDelimiterRegex\": \"[\",\n"
                + "  \"placeholderClosingDelimiterRegex\": \"]\",\n"
                + "  \"exemptedComparisons\": [\"SCHEMA_LOCATION\", \"ATTR_VALUE\", \"NAMESPACE_URI\"]\n"
                + "}",
            JSONCompareMode.NON_EXTENSIBLE));
  }

  @Test
  public void namespaceComparisonCanBeExcluded() {
    String expected =
        "<?xml version=\"1.0\"?>\n"
            + "<stuff xmlns:th=\"https://thing.com\">\n"
            + "    <th:thing>Match this</th:thing>\n"
            + "</stuff>";

    String actual =
        "<?xml version=\"1.0\"?>\n"
            + "<stuff xmlns:st=\"https://stuff.com\">\n"
            + "    <st:thing>Match this</st:thing>\n"
            + "</stuff>";

    MatchResult matchResult =
        equalToXml(expected).exemptingComparisons(NAMESPACE_URI).match(actual);

    assertTrue(matchResult.isExactMatch());
  }

  @Test
  public void namespaceComparisonCanBeExcluded2() {
    String expected =
        "<ns2:GetValue\n"
            + "        xmlns=\"http://CIS/BIR/PUBL/2014/07/DataContract\"\n"
            + "        xmlns:ns2=\"http://CIS/BIR/2014/07\"                         \n"
            + "        xmlns:ns3=\"http://CIS/BIR/PUBL/2014/07\"                    \n"
            + "        xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serializa  \n"
            + "        tion/\"/>";

    String actual =
        "<ns3:GetValue\n"
            + "        xmlns=\"http://CIS/BIR/PUBL/2014/07\"\n"
            + "        xmlns:ns2=\"http://CIS/BIR/PUBL/2014/07/DataContract\"\n"
            + "        xmlns:ns3=\"http://CIS/BIR/2014/07\"\n"
            + "        xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serializa\n"
            + "        tion/\"/>";

    StringValuePattern pattern = equalToXml(expected).exemptingComparisons(NAMESPACE_URI);

    assertTrue(pattern.match(actual).isExactMatch());
  }

  @Test
  public void testEquals() {
    EqualToXmlPattern a =
        new EqualToXmlPattern(
            "<ns2:GetValue\n"
                + "        xmlns=\"http://CIS/BIR/PUBL/2014/07/DataContract\"\n"
                + "        xmlns:ns2=\"http://CIS/BIR/2014/07\"                         \n"
                + "        xmlns:ns3=\"http://CIS/BIR/PUBL/2014/07\"                    \n"
                + "        xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serializa  \n"
                + "        tion/\"/>");
    EqualToXmlPattern b =
        new EqualToXmlPattern(
            "<ns2:GetValue\n"
                + "        xmlns=\"http://CIS/BIR/PUBL/2014/07/DataContract\"\n"
                + "        xmlns:ns2=\"http://CIS/BIR/2014/07\"                         \n"
                + "        xmlns:ns3=\"http://CIS/BIR/PUBL/2014/07\"                    \n"
                + "        xmlns:ns4=\"http://schemas.microsoft.com/2003/10/Serializa  \n"
                + "        tion/\"/>");
    EqualToXmlPattern c =
        new EqualToXmlPattern(
            "<ns2:GetValue\n"
                + "        xmlns=\"http://CIS/BIR/PUBL/2015/07/DataContract\"\n"
                + "        xmlns:ns2=\"http://CIS/BIR/2015/07\"                         \n"
                + "        xmlns:ns3=\"http://CIS/BIR/PUBL/2015/07\"                    \n"
                + "        xmlns:ns4=\"http://schemas.microsoft.com/2004/10/Serializa  \n"
                + "        tion/\"/>");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(b, a);
    assertEquals(b.hashCode(), a.hashCode());
    assertNotEquals(a, c);
    assertNotEquals(a.hashCode(), c.hashCode());
    assertNotEquals(b, c);
    assertNotEquals(b.hashCode(), c.hashCode());
  }

  @Test
  void subEventIsReturnedOnXmlParsingError() {
    MatchResult match = new EqualToXmlPattern("<things />").match("<wrong");

    assertThat(match.isExactMatch(), is(false));
    assertThat(match.getSubEvents().size(), is(1));
    String message =
        match.getSubEvents().stream().findFirst().get().getData().get("message").toString();
    assertThat(
        message, startsWith("XML document structures must start and end within the same entity"));
  }

  @Test
  void ignoreOrderOfSameNodeOnSameLevel() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern("<body><entry>1</entry><entry>2</entry></body>", false, true);
    MatchResult result = pattern.match("<body><entry>2</entry><entry>1</entry></body>");
    assertTrue(result.isExactMatch());
  }

  @Test
  void dontIgnoreOrderOfSameNodeOnSameLevel() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern("<body><entry>1</entry><entry>2</entry></body>", false, false);
    MatchResult result = pattern.match("<body><entry>2</entry><entry>1</entry></body>");
    assertFalse(result.isExactMatch());
  }

  @Test
  void doesNotMatchWhenSameNodeOnSameLevelHasDifferentValues() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern("<body><entry>1</entry><entry>3</entry></body>", false, true);
    MatchResult result = pattern.match("<body><entry>2</entry><entry>1</entry></body>");
    assertFalse(result.isExactMatch());
  }

  @Test
  void matchesIfMultipleSameNodesOnSameLevelWithDifferentNodes() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern(
            "<body><entry>1</entry><entry>2</entry><other>2</other></body>", false, true);
    MatchResult result =
        pattern.match("<body><entry>2</entry><entry>1</entry><other>2</other></body>");
    assertTrue(result.isExactMatch());
  }

  @Test
  void matchesIfTwoIdenticalChildNodesAreEmpty() {
    EqualToXmlPattern pattern = new EqualToXmlPattern("<body><entry/><entry/></body>", false, true);
    MatchResult result = pattern.match("<body><entry/><entry/></body>");
    assertTrue(result.isExactMatch());
  }

  @Test
  void matchesIfCommentsDiffer() {
    EqualToXmlPattern pattern =
        new EqualToXmlPattern("<body><!-- Comment --><entry/><entry/></body>", false, true);
    MatchResult result = pattern.match("<body><entry/><entry/><!-- A different comment --></body>");
    assertTrue(result.isExactMatch());
  }
}
