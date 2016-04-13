package com.github.tomakehurst.wiremock.matching;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EqualToXmlPatternTest {

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
        assertThat(matchResult.getDistance(), is(1.0));
    }

    @Ignore
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

        assertThat(matchResult.getDistance(), is(0.3));
    }
}
