package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;

public class MatchesXPathPattern extends StringValuePattern {

    private final Map<String, String> xpathNamespaces = null;

    public MatchesXPathPattern(@JsonProperty("matchesXPath") String expectedValue) {
        super(expectedValue);
    }

    public String getMatchesXPath() {
        return expectedValue;
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.of(isXPathMatch(value));
    }

    private boolean isXPathMatch(String value) {
        try {
            Document inDocument = XMLUnit.buildControlDocument(value);
            XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
            if (xpathNamespaces != null) {
                NamespaceContext namespaceContext = new SimpleNamespaceContext(xpathNamespaces);
                simpleXpathEngine.setNamespaceContext(namespaceContext);
            }
            NodeList nodeList = simpleXpathEngine.getMatchingNodes(expectedValue, inDocument);
            return nodeList.getLength() > 0;
        } catch (SAXException e) {
            notifier().info(String.format(
                "Warning: failed to parse the XML document. Reason: %s\nXML: %s", e.getMessage(), value));
            return false;
        } catch (IOException e) {
            notifier().info(e.getMessage());
            return false;
        } catch (XpathException e) {
            notifier().info("Warning: failed to evaluate the XPath expression " + expectedValue);
            return false;
        }
    }
}
