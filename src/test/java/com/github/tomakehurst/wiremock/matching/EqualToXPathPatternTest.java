package com.github.tomakehurst.wiremock.matching;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EqualToXPathPatternTest {

    @Test
    public void returnsExactMatchWhenXPathMatches() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";


        StringValuePattern pattern = StringValuePattern.equalToXPath("//planet[@name='Earth']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertTrue("Expected XPath match", match.isExactMatch());
        assertThat(match.getDistance(), is(0.0));
    }

    @Test
    public void returnsNoExactMatchWhenXPathDoesNotMatch() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = StringValuePattern.equalToXPath("//star[@name='alpha centauri']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void returnsNoExactMatchWhenXPathExpressionIsInvalid() {
        String mySolarSystemXML = "<solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = StringValuePattern.equalToXPath("//\\\\&&&&&");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }

    @Test
    public void returnsNoExactMatchWhenXmlIsBadlyFormed() {
        String mySolarSystemXML = "solar-system>"
            + "<planet name='Earth' position='3' supportsLife='yes'/>"
            + "<planet name='Venus' position='4'/></solar-system>";

        StringValuePattern pattern = StringValuePattern.equalToXPath("//star[@name='alpha centauri']");

        MatchResult match = pattern.match(mySolarSystemXML);
        assertFalse("Expected XPath non-match", match.isExactMatch());
        assertThat(match.getDistance(), is(1.0));
    }
}
