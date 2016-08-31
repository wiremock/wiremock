/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.google.common.base.MoreObjects.firstNonNull;

public class MatchesXPathPattern extends StringValuePattern {

    private final Map<String, String> xpathNamespaces;

    public MatchesXPathPattern(@JsonProperty("matchesXPath") String expectedValue,
                               @JsonProperty("namespaces") Map<String, String> namespaces) {
        super(expectedValue);
        xpathNamespaces = namespaces == null || namespaces.isEmpty() ? null : namespaces;
    }

    public MatchesXPathPattern withXPathNamespace(String name, String namespaceUri) {
        Map<String, String> namespaceMap = ImmutableMap.<String, String>builder()
            .putAll(firstNonNull(xpathNamespaces, Collections.<String, String>emptyMap()))
            .put(name, namespaceUri)
            .build();
        return new MatchesXPathPattern(expectedValue, namespaceMap);
    }

    public String getMatchesXPath() {
        return expectedValue;
    }

    @JsonGetter("xPathNamespaces")
    public Map<String, String> getXPathNamespaces() {
        return xpathNamespaces;
    }

    @Override
    public MatchResult match(String value) {
        return MatchResult.of(isXPathMatch(value));
    }

    private boolean isXPathMatch(String value) {
        if (value == null) {
            return false;
        }

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
