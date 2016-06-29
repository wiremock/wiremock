package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MatchesXPathPatternTest {

    @Test
    public void returnsExactMatchWhenXPathMatches() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";


        StringValuePattern pattern = WireMock.matchingXPath("//planet[@name='Earth']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertTrue("Expected XPath match", match.isExactMatch());
        assertThat(match.getDistance(), is(0.0));
    }

    @Test
    public void returnsNoExactMatchWhenXPathDoesNotMatch() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = WireMock.matchingXPath("//star[@name='alpha centauri']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void returnsNoExactMatchWhenXPathExpressionIsInvalid() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = WireMock.matchingXPath("//\\\\&&&&&");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void returnsNoExactMatchWhenXmlIsBadlyFormed() {
        String mySolarSystemXML = "solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = WireMock.matchingXPath("//star[@name='alpha centauri']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void matchesNamespacedXmlExactly() {
        String xml = "<t:thing xmlns:t='http://things' xmlns:s='http://subthings'><s:subThing>The stuff</s:subThing></t:thing>";

        StringValuePattern pattern = WireMock.matchingXPath(
            "//s:subThing[.='The stuff']",
            ImmutableMap.of("s", "http://subthings", "t", "http://things"));

        MatchResult match = pattern.match(xml);
        assertTrue(match.isExactMatch());
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
            "{ \"matchesXPath\" : \"/stuff:outer/stuff:inner[.=111]\" ,   \n" +
            "  \"xPathNamespaces\" : {                                    \n" +
            "      \"one\" : \"http://one.com/\",                         \n" +
            "      \"two\" : \"http://two.com/\"                          \n" +
            "  }                                                          \n" +
            "}";

        MatchesXPathPattern pattern = Json.read(json, MatchesXPathPattern.class);

        assertThat(pattern.getXPathNamespaces(), hasEntry("one", "http://one.com/"));
        assertThat(pattern.getXPathNamespaces(), hasEntry("two", "http://two.com/"));
    }

    @Test
    public void serialisesCorrectlyWithNamspaces() throws JSONException {
        MatchesXPathPattern pattern = new MatchesXPathPattern("//*", ImmutableMap.of(
            "one", "http://one.com/",
            "two", "http://two.com/"
        ));

        String json = Json.write(pattern);

        JSONAssert.assertEquals(
            "{ \"matchesXPath\" : \"//*\" ,   \n" +
            "  \"xPathNamespaces\" : {                                    \n" +
            "      \"one\" : \"http://one.com/\",                         \n" +
            "      \"two\" : \"http://two.com/\"                          \n" +
            "  }                                                          \n" +
            "}",
            json, false);
    }

    @Test
    public void serialisesCorrectlyWithoutNamspaces() throws JSONException {
        MatchesXPathPattern pattern = new MatchesXPathPattern("//*", Collections.<String, String>emptyMap());

        String json = Json.write(pattern);

        JSONAssert.assertEquals(
            "{ \"matchesXPath\" : \"//*\" }",
            json, false);
    }
}
