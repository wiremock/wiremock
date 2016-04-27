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

    @Test
    public void returnsExactMatchOnNamespacedXml() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "    <soap:Body>\n" +
            "        <stuff xmlns=\"https://example.com/mynamespace\">\n" +
            "            <things />\n" +
            "        </stuff>\n" +
            "    </soap:Body>\n" +
            "</soap:Envelope>\n"
        );

        MatchResult match = pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soap:Body>\n" +
                "        <stuff xmlns=\"https://example.com/mynamespace\">\n" +
                "            <things />\n" +
                "        </stuff>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>\n"
        );

        assertThat(match.getDistance(), is(0.0));
        assertTrue(match.isExactMatch());
    }

    @Test
    public void returnsExactMatchOnNamespacedXmlWhenNamespacePrefixesDiffer() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<shampoo:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:shampoo=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <shampoo:Body>\n" +
                "        <stuff xmlns=\"https://example.com/mynamespace\">\n" +
                "            <things />\n" +
                "        </stuff>\n" +
                "    </shampoo:Body>\n" +
                "</shampoo:Envelope>\n"
        );

        MatchResult match = pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soap:Body>\n" +
                "        <stuff xmlns=\"https://example.com/mynamespace\">\n" +
                "            <things />\n" +
                "        </stuff>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>\n"
        );

        assertThat(match.getDistance(), is(0.0));
        assertTrue(match.isExactMatch());
    }

    @Test
    public void doesNotReturnExactMatchWhenNamespaceUriDiffers() {
        EqualToXmlPattern pattern = new EqualToXmlPattern(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soap:Body>\n" +
                "        <stuff xmlns=\"https://example.com/mynamespace\">\n" +
                "            <things />\n" +
                "        </stuff>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>\n"
        );

        assertFalse(pattern.match(
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "    <soap:Body>\n" +
                "        <stuff xmlns=\"https://example.com/the-wrong-namespace\">\n" +
                "            <things />\n" +
                "        </stuff>\n" +
                "    </soap:Body>\n" +
                "</soap:Envelope>\n"
        ).isExactMatch());
    }
}
