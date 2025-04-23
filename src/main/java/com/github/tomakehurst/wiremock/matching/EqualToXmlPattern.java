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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.Strings.isNullOrEmpty;
import static org.xmlunit.diff.ComparisonType.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.*;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

public class EqualToXmlPattern extends StringValuePattern {

  private static final Set<ComparisonType> COUNTED_COMPARISONS =
      Set.of(
          ELEMENT_TAG_NAME,
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
          CHILD_LOOKUP,
          ATTR_NAME_LOOKUP);

  private final DocumentBuilderFactory documentBuilderFactory;

  private final Boolean enablePlaceholders;
  private final String placeholderOpeningDelimiterRegex;
  private final String placeholderClosingDelimiterRegex;
  private final DifferenceEvaluator diffEvaluator;
  private final Set<ComparisonType> exemptedComparisons;
  private final Boolean ignoreOrderOfSameNode;
  private final NamespaceAwareness namespaceAwareness;
  private final Set<ComparisonType> countedComparisons;
  private final Document expectedXmlDoc;

  public EqualToXmlPattern(@JsonProperty("equalToXml") String expectedValue) {
    this(expectedValue, null, null, null, null, null, null);
  }

  public EqualToXmlPattern(
      @JsonProperty("equalToXml") String expectedValue,
      @JsonProperty("enablePlaceholders") Boolean enablePlaceholders,
      @JsonProperty("ignoreOrderOfSameNode") boolean ignoreOrderOfSameNode) {
    this(expectedValue, enablePlaceholders, null, null, null, ignoreOrderOfSameNode, null);
  }

  @JsonCreator
  public EqualToXmlPattern(
      @JsonProperty("equalToXml") String expectedValue,
      @JsonProperty("enablePlaceholders") Boolean enablePlaceholders,
      @JsonProperty("placeholderOpeningDelimiterRegex") String placeholderOpeningDelimiterRegex,
      @JsonProperty("placeholderClosingDelimiterRegex") String placeholderClosingDelimiterRegex,
      @JsonProperty("exemptedComparisons") Set<ComparisonType> exemptedComparisons,
      @JsonProperty("ignoreOrderOfSameNode") Boolean ignoreOrderOfSameNode,
      @JsonProperty("namespaceAwareness") NamespaceAwareness namespaceAwareness) {

    super(expectedValue);
    documentBuilderFactory = getDocumentBuilderFactory(namespaceAwareness);
    // Throw an exception if we can't parse the document
    expectedXmlDoc = Xml.read(expectedValue, documentBuilderFactory);
    this.enablePlaceholders = enablePlaceholders;
    this.placeholderOpeningDelimiterRegex = placeholderOpeningDelimiterRegex;
    this.placeholderClosingDelimiterRegex = placeholderClosingDelimiterRegex;
    this.exemptedComparisons = exemptedComparisons;
    this.ignoreOrderOfSameNode = ignoreOrderOfSameNode;
    this.namespaceAwareness = namespaceAwareness;
    Set<ComparisonType> comparisonsToExempt = new HashSet<>();
    if (exemptedComparisons != null) {
      comparisonsToExempt.addAll(exemptedComparisons);
    }
    this.countedComparisons =
        COUNTED_COMPARISONS.stream()
            .filter(e -> !comparisonsToExempt.contains(e))
            .collect(Collectors.toSet());

    IgnoreUncountedDifferenceEvaluator baseDifferenceEvaluator =
        new IgnoreUncountedDifferenceEvaluator(comparisonsToExempt);
    if (enablePlaceholders != null && enablePlaceholders) {
      diffEvaluator =
          DifferenceEvaluators.chain(
              baseDifferenceEvaluator,
              new PlaceholderDifferenceEvaluator(
                  placeholderOpeningDelimiterRegex, placeholderClosingDelimiterRegex));
    } else {
      diffEvaluator = baseDifferenceEvaluator;
    }
  }

  public String getEqualToXml() {
    return expectedValue;
  }

  @Override
  public String getExpected() {
    try {
      // as of writing, Xml.prettyPrint will throw an exception if the provided XML has unbound
      // namespace prefixes.
      return Xml.prettyPrint(getValue());
    } catch (Exception e) {
      return getValue();
    }
  }

  public Boolean isEnablePlaceholders() {
    return enablePlaceholders;
  }

  public Boolean isIgnoreOrderOfSameNode() {
    return ignoreOrderOfSameNode;
  }

  public String getPlaceholderOpeningDelimiterRegex() {
    return placeholderOpeningDelimiterRegex;
  }

  public String getPlaceholderClosingDelimiterRegex() {
    return placeholderClosingDelimiterRegex;
  }

  public Set<ComparisonType> getExemptedComparisons() {
    return exemptedComparisons;
  }

  public NamespaceAwareness getNamespaceAwareness() {
    return namespaceAwareness;
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
          DiffBuilder diffBuilder =
              DiffBuilder.compare(Input.from(expectedXmlDoc))
                  .withTest(value)
                  .withComparisonController(ComparisonControllers.StopWhenDifferent)
                  .ignoreWhitespace()
                  .withDifferenceEvaluator(diffEvaluator)
                  .withNodeMatcher(new OrderInvariantNodeMatcher(ignoreOrderOfSameNode))
                  .withDocumentBuilderFactory(documentBuilderFactory);
          if (namespaceAwareness == NamespaceAwareness.LEGACY) {
            // See NamespaceAwareness javadoc for details of why this is set here.
            diffBuilder.ignoreComments();
          }
          Diff diff = diffBuilder.build();

          return !diff.hasDifferences();
        } catch (XMLUnitException e) {
          appendSubEvent(SubEvent.warning(e.getMessage()));

          notifier()
              .info(
                  "Failed to process XML. "
                      + e.getMessage()
                      + "\nExpected:\n"
                      + expectedValue
                      + "\n\nActual:\n"
                      + value);
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

        Diff diff;
        try {
          DiffBuilder diffBuilder =
              DiffBuilder.compare(Input.from(expectedValue))
                  .withTest(value)
                  .ignoreWhitespace()
                  .withDifferenceEvaluator(diffEvaluator)
                  .withComparisonListeners(
                      (comparison, outcome) -> {
                        if (countedComparisons.contains(comparison.getType())
                            && comparison.getControlDetails().getValue() != null) {
                          totalComparisons.incrementAndGet();
                          if (outcome == ComparisonResult.DIFFERENT) {
                            differences.incrementAndGet();
                          }
                        }
                      })
                  .withDocumentBuilderFactory(documentBuilderFactory);
          if (namespaceAwareness == NamespaceAwareness.LEGACY) {
            diffBuilder.ignoreComments();
          }
          diff = diffBuilder.build();
        } catch (XMLUnitException e) {
          notifier()
              .info(
                  "Failed to process XML. "
                      + e.getMessage()
                      + "\nExpected:\n"
                      + expectedValue
                      + "\n\nActual:\n"
                      + value);
          return 1.0;
        }

        notifier()
            .info(
                StreamSupport.stream(diff.getDifferences().spliterator(), false)
                    .map(Object::toString)
                    .collect(Collectors.joining("\n")));

        return differences.doubleValue() / totalComparisons.doubleValue();
      }
    };
  }

  private static final DocumentBuilderFactory namespaceAware = newDocumentBuilderFactory(true);
  private static final DocumentBuilderFactory namespaceUnaware = newDocumentBuilderFactory(false);

  private static DocumentBuilderFactory getDocumentBuilderFactory(
      NamespaceAwareness namespaceAwareness) {
    if (namespaceAwareness == null || namespaceAwareness == NamespaceAwareness.STRICT) {
      return namespaceAware;
    } else {
      return namespaceUnaware;
    }
  }

  private static DocumentBuilderFactory newDocumentBuilderFactory(boolean namespaceAware) {
    DocumentBuilderFactory factory = Xml.newDocumentBuilderFactory();
    try {
      factory.setFeature("http://apache.org/xml/features/include-comments", false);
      factory.setFeature("http://xml.org/sax/features/namespaces", namespaceAware);
    } catch (ParserConfigurationException e) {
      throwUnchecked(e);
    }
    return factory;
  }

  private static class IgnoreUncountedDifferenceEvaluator implements DifferenceEvaluator {

    private final Set<ComparisonType> finalCountedComparisons;

    public IgnoreUncountedDifferenceEvaluator(Set<ComparisonType> exemptedComparisons) {
      finalCountedComparisons =
          exemptedComparisons != null
              ? COUNTED_COMPARISONS.stream()
                  .filter(e -> !exemptedComparisons.contains(e))
                  .collect(Collectors.toSet())
              : COUNTED_COMPARISONS;
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
      if (finalCountedComparisons.contains(comparison.getType())
          && comparison.getControlDetails().getValue() != null) {
        return outcome;
      }

      return ComparisonResult.EQUAL;
    }
  }

  public EqualToXmlPattern exemptingComparisons(ComparisonType... comparisons) {
    return new EqualToXmlPattern(
        expectedValue,
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        new HashSet<>(Arrays.asList(comparisons)),
        ignoreOrderOfSameNode,
        namespaceAwareness);
  }

  public EqualToXmlPattern withNamespaceAwareness(NamespaceAwareness namespaceAwareness) {
    return new EqualToXmlPattern(
        expectedValue,
        enablePlaceholders,
        placeholderOpeningDelimiterRegex,
        placeholderClosingDelimiterRegex,
        exemptedComparisons,
        ignoreOrderOfSameNode,
        namespaceAwareness);
  }

  private static final class OrderInvariantNodeMatcher extends DefaultNodeMatcher {
    private static Boolean secondaryOrderByTextContent;

    public OrderInvariantNodeMatcher(Boolean secondaryOrderByTextContent) {
      OrderInvariantNodeMatcher.secondaryOrderByTextContent = secondaryOrderByTextContent;
    }

    @Override
    public Iterable<Map.Entry<Node, Node>> match(
        Iterable<Node> controlNodes, Iterable<Node> testNodes) {

      return super.match(sort(controlNodes), sort(testNodes));
    }

    private static Iterable<Node> sort(Iterable<Node> nodes) {
      return StreamSupport.stream(nodes.spliterator(), false)
          .sorted(getComparator())
          .collect(Collectors.toList());
    }

    private static Comparator<Node> getComparator() {
      if (Objects.nonNull(secondaryOrderByTextContent) && secondaryOrderByTextContent) {
        return Comparator.comparing(Node::getLocalName).thenComparing(Node::getTextContent);
      } else {
        return Comparator.comparing(Node::getLocalName);
      }
    }
  }

  /**
   * This enum represents how the pattern will treat XML namespaces when matching.
   *
   * <p>{@link NamespaceAwareness#LEGACY} represents the old way that namespaces were treated. This
   * had a lot of unpredictability and some behaviours were more of a side effect of other
   * implementation details, rather than intentional. A key detail is that the original {@link
   * DocumentBuilderFactory} was not namespace aware, but the XSLT transform performed by {@link
   * DiffBuilder#ignoreComments()} seems to return a document that is semi-namespace aware, so some
   * namespace aware functionality was available. Now {@link DiffBuilder#ignoreComments()} has been
   * replaced by setting the {@link DocumentBuilderFactory} to ignore comment on read (much more
   * performant and predictable), so is only used to produce the legacy namespace aware behaviour.
   *
   * <p>{@link NamespaceAwareness#STRICT} and {@link NamespaceAwareness#NONE} represent firmer, more
   * intentional behaviour around how namespaces are handled. The details of how each option behaves
   * are documented below:
   *
   * <p>{@link NamespaceAwareness#LEGACY} behaviour:
   *
   * <ul>
   *   <li>Namespace prefixes do not need to be bound to a namespace URI.
   *   <li>Element namespace prefixes (and their corresponding namespace URIs) are ignored (e.g.
   *       `&lt;th:thing>Match this&lt;/th:thing>` == `&lt;st:thing>Match this&lt;/st:thing>`)
   *       <ul>
   *         <li>Element prefixes seem to effectively be totally removed from the document by the
   *             XSLT transform performed by {@link DiffBuilder#ignoreComments()} (and no namespace
   *             URI is assigned to the element).
   *       </ul>
   *   <li>Attributes are compared by their full name (i.e. namespace prefixes are NOT ignored)
   *       (e.g. `&lt;thing th:attr="abc">Match this&lt;/thing>` != `&lt;thing st:attr="abc">Match
   *       this&lt;/thing>`)
   *       <ul>
   *         <li>The XSLT transform performed by {@link DiffBuilder#ignoreComments()} does not
   *             assign a namespace URI to attributes, so XMLUnit uses the attribute's full name.
   *       </ul>
   *   <li>xmlns namespaced attributes are ignored (e.g. `&lt;thing
   *       xmlns:th="https://thing.com">Match this&lt;/thing>` == `&lt;thing
   *       xmlns:st="https://stuff.com">Match this&lt;/thing>`)
   *       <ul>
   *         <li>XMLUnit ignores all attributes namespaced to http://www.w3.org/2000/xmlns/, which
   *             all xmlns prefixed attributes are assigned to by the XSLT transform performed by
   *             {@link DiffBuilder#ignoreComments()}.
   *       </ul>
   *   <li>Element default namespace attributes (i.e. `xmlns` attributes) are NOT ignored unless
   *       NAMESPACE_URI comparison type is explicitly excluded (e.g. `&lt;thing
   *       xmlns="https://thing.com">Match this&lt;/thing>` != `&lt;thing
   *       xmlns="https://stuff.com">Match this&lt;/thing>`)
   *       <ul>
   *         <li>Like xmlns namespaced attributes, XMLUnit ignores all attributes namespaced to
   *             http://www.w3.org/2000/xmlns/, which all xmlns attributes are assigned to by the
   *             XSLT transform performed by {@link DiffBuilder#ignoreComments()}.
   *         <li>The difference between default xmlns attributes and xmlns <i>prefixed</i>
   *             attributes is that the XSLT transform performed by {@link
   *             DiffBuilder#ignoreComments()} assigns the namespace URI of default xmlns attributes
   *             to the attributed element, which is why matching will fail (unless NAMESPACE_URI
   *             comparison type is explicitly excluded).
   *       </ul>
   * </ul>
   *
   * <p>{@link NamespaceAwareness#STRICT} behaviour:
   *
   * <ul>
   *   <li>Namespace prefixes need to be bound to a namespace URI.
   *   <li>Element and attribute namespace URIs are compared, but their prefixes are ignored.
   *       <ul>
   *         <li>Namespace URIs can be explicitly excluded. Although, due to how XMLUnit's engine is
   *             implemented, excluding NAMESPACE_URI does not work with attributes (<a
   *             href="https://github.com/xmlunit/xmlunit/issues/282">see XMLUnit issue</a>).
   *       </ul>
   *   <li>The namespaces defined by xmlns namespaced attributes are compared, but the attributes
   *       themselves are ignored (e.g. `&lt;thing xmlns:th="https://thing.com">Match
   *       this&lt;/thing>` == `&lt;thing xmlns:st="https://stuff.com">Match this&lt;/thing>`)
   *       <ul>
   *         <li>XMLUnit ignores all attributes namespaced to http://www.w3.org/2000/xmlns/, which
   *             all default and prefixed xmlns attributes are assigned to by when the document
   *             builder factory is namespace aware.
   *       </ul>
   * </ul>
   *
   * <p>{@link NamespaceAwareness#NONE} behaviour:
   *
   * <ul>
   *   <li>Namespace prefixes do not need to be bound to a namespace URI.
   *   <li>Element and attribute are compared by their full name and all namespace URIs are ignored.
   *   <li>xmlns attributes are not ignored and are treated like any other attribute.
   * </ul>
   */
  public enum NamespaceAwareness {
    STRICT,
    LEGACY,
    NONE,
  }
}
