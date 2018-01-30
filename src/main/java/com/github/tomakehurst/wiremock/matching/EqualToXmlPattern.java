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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Xml;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.xmlunit.diff.ComparisonType.ATTR_NAME_LOOKUP;
import static org.xmlunit.diff.ComparisonType.ATTR_VALUE;
import static org.xmlunit.diff.ComparisonType.CHILD_LOOKUP;
import static org.xmlunit.diff.ComparisonType.CHILD_NODELIST_LENGTH;
import static org.xmlunit.diff.ComparisonType.CHILD_NODELIST_SEQUENCE;
import static org.xmlunit.diff.ComparisonType.ELEMENT_NUM_ATTRIBUTES;
import static org.xmlunit.diff.ComparisonType.NAMESPACE_URI;
import static org.xmlunit.diff.ComparisonType.NODE_TYPE;
import static org.xmlunit.diff.ComparisonType.NO_NAMESPACE_SCHEMA_LOCATION;
import static org.xmlunit.diff.ComparisonType.PROCESSING_INSTRUCTION_DATA;
import static org.xmlunit.diff.ComparisonType.PROCESSING_INSTRUCTION_TARGET;
import static org.xmlunit.diff.ComparisonType.SCHEMA_LOCATION;
import static org.xmlunit.diff.ComparisonType.TEXT_VALUE;

public class EqualToXmlPattern extends StringValuePattern {

    private static List<ComparisonType> COUNTED_COMPARISONS = ImmutableList.of(
        SCHEMA_LOCATION,
        NO_NAMESPACE_SCHEMA_LOCATION,
        NODE_TYPE,
        NAMESPACE_URI,
        TEXT_VALUE,
        PROCESSING_INSTRUCTION_TARGET,
        PROCESSING_INSTRUCTION_DATA,
        ELEMENT_NUM_ATTRIBUTES,
        ATTR_VALUE,
        CHILD_NODELIST_LENGTH,
        CHILD_NODELIST_SEQUENCE,
        CHILD_LOOKUP,
        ATTR_NAME_LOOKUP
    );

    private final Document xmlDocument;

    public EqualToXmlPattern(@JsonProperty("equalToXml") String expectedValue) {
        super(expectedValue);
        xmlDocument = Xml.read(expectedValue);
    }

    public String getEqualToXml() {
        return expectedValue;
    }

    @Override
    public String getExpected() {
        return Xml.prettyPrint(getValue());
    }

    @Override
    public MatchResult match(final String value) {
        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                if (isNullOrEmpty(value)) {
                    return false;
                }

                try {
                    Diff diff = DiffBuilder.compare(Input.from(expectedValue))
                        .withTest(value)
                        .withComparisonController(ComparisonControllers.StopWhenDifferent)
                        .ignoreWhitespace()
                        .ignoreComments()
                        .withDifferenceEvaluator(IGNORE_UNCOUNTED_COMPARISONS)
                        .withDocumentBuilderFactory(new SkipResolvingEntitiesDocumentBuilderFactory())
                        .build();

                    return !diff.hasDifferences();
                } catch (XMLUnitException e) {
                    notifier().info("Failed to process XML. " + e.getMessage() +
                        "\nExpected:\n" + expectedValue +
                        "\n\nActual:\n" + value);
                    return false;
                }
            }

            @Override
            public double getDistance() {
                if (isNullOrEmpty(value)) {
                    return 1.0;
                }

                final AtomicInteger totalComparisons = new AtomicInteger(0);
                final AtomicInteger differences = new AtomicInteger(0);

                Diff diff = null;
                try {
                    diff = DiffBuilder.compare(Input.from(expectedValue))
                        .withTest(value)
                        .ignoreWhitespace()
                        .ignoreComments()
                        .withDifferenceEvaluator(IGNORE_UNCOUNTED_COMPARISONS)
                        .withComparisonListeners(new ComparisonListener() {
                            @Override
                            public void comparisonPerformed(Comparison comparison, ComparisonResult outcome) {
                                if (COUNTED_COMPARISONS.contains(comparison.getType()) && comparison.getControlDetails().getValue() != null) {
                                    totalComparisons.incrementAndGet();
                                    if (outcome == ComparisonResult.DIFFERENT) {
                                        differences.incrementAndGet();
                                    }
                                }
                            }
                        })
                        .withDocumentBuilderFactory(new SkipResolvingEntitiesDocumentBuilderFactory())
                        .build();
                } catch (XMLUnitException e) {
                    notifier().info("Failed to process XML. " + e.getMessage() +
                        "\nExpected:\n" + expectedValue +
                        "\n\nActual:\n" + value);
                    return 1.0;
                }

                notifier().info(
                    Joiner.on("\n").join(diff.getDifferences())
                );

                return differences.doubleValue() / totalComparisons.doubleValue();
            }
        };
    }

    private static final DifferenceEvaluator IGNORE_UNCOUNTED_COMPARISONS = new DifferenceEvaluator() {
        @Override
        public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
            if (COUNTED_COMPARISONS.contains(comparison.getType()) && comparison.getControlDetails().getValue() != null) {
                return outcome;
            }

            return ComparisonResult.EQUAL;
        }
    };

    public static class SkipResolvingEntitiesDocumentBuilderFactory extends DocumentBuilderFactoryImpl {
        @Override
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            DocumentBuilder documentBuilder = super.newDocumentBuilder();
            documentBuilder.setEntityResolver(new ResolveToEmptyString());
            return documentBuilder;
        }

        private class ResolveToEmptyString implements EntityResolver {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        }
    }
}
