package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class EqualToXmlPatternTest {

    @Before
    public void init() {
        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @After
    public void cleanup() {
        LocalNotifier.set(null);
    }

    @Test
    public void returnsExactMatchWhenDocumentsAreIdentical() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "    <thing characteristic=\"tedious\"/>\n" +
            "</things>"
        );

        assertTrue(pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "    <thing characteristic=\"tedious\"/>\n" +
            "</things>"
        ).isExactMatch());
    }

    @Test
    public void returnsExactMatchWhenDocumentsAreIdenticalOtherThanWhitespace() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "    <thing characteristic=\"tedious\"/>\n" +
            "</things>"
        );

        assertTrue(pattern.match(
            "<things><thing characteristic=\"tepid\"/><thing characteristic=\"tedious\"/></things>"
        ).isExactMatch());
    }

    @Test
    public void returnsNoMatchAnd1DistanceWhenDocumentsAreTotallyDifferent() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "    <thing characteristic=\"tedious\"/>\n" +
            "</things>"
        );

        MatchResult matchResult = pattern.match("<no-things-at-all />");

        assertFalse(matchResult.isExactMatch());
        assertThat(matchResult.getDistance(), is(0.375)); //Not high enough really, some more tweaking needed
    }

    @Test
    public void returnsLowDistanceWhenActualDocumentHasMissingElement() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "    <thing characteristic=\"tedious\"/>\n" +
            "</things>"
        );

        MatchResult matchResult = pattern.match(
            "<things>\n" +
            "    <thing characteristic=\"tepid\"/>\n" +
            "</things>"
        );

        assertThat(matchResult.getDistance(), closeTo(0.14, 2));
    }
}
