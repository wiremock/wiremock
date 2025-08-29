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

import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.xml.*;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import java.util.*;
import java.util.stream.Collectors;

/** The type Matches x path pattern. */
@JsonSerialize(using = XPathPatternJsonSerializer.class)
public class MatchesXPathPattern extends PathPattern {

  private final Map<String, String> xpathNamespaces;

  /**
   * Instantiates a new Matches x path pattern.
   *
   * @param xpath the xpath
   */
  public MatchesXPathPattern(String xpath) {
    this(xpath, null, null);
  }

  /**
   * Instantiates a new Matches x path pattern.
   *
   * @param xpath the xpath
   * @param valuePattern the value pattern
   */
  public MatchesXPathPattern(String xpath, StringValuePattern valuePattern) {
    this(xpath, null, valuePattern);
  }

  /**
   * Instantiates a new Matches x path pattern.
   *
   * @param xpath the xpath
   * @param namespaces the namespaces
   */
  public MatchesXPathPattern(String xpath, Map<String, String> namespaces) {
    this(xpath, namespaces, null);
  }

  /**
   * Instantiates a new Matches x path pattern.
   *
   * @param xpath the xpath
   * @param namespaces the namespaces
   * @param valuePattern the value pattern
   */
  public MatchesXPathPattern(
      @JsonProperty("matchesXPath") String xpath,
      @JsonProperty("namespaces") Map<String, String> namespaces,
      @JsonProperty("valuePattern") StringValuePattern valuePattern) {
    super(xpath, valuePattern);
    xpathNamespaces = namespaces == null || namespaces.isEmpty() ? null : namespaces;
  }

  /**
   * With x path namespace matches x path pattern.
   *
   * @param name the name
   * @param namespaceUri the namespace uri
   * @return the matches x path pattern
   */
  public MatchesXPathPattern withXPathNamespace(String name, String namespaceUri) {
    Map<String, String> namespaceMap =
        new HashMap<>(getFirstNonNull(xpathNamespaces, new HashMap<>()));
    namespaceMap.put(name, namespaceUri);
    return new MatchesXPathPattern(expectedValue, Collections.unmodifiableMap(namespaceMap));
  }

  /**
   * Gets matches x path.
   *
   * @return the matches x path
   */
  public String getMatchesXPath() {
    return expectedValue;
  }

  /**
   * Gets x path namespaces.
   *
   * @return the x path namespaces
   */
  @JsonGetter("xPathNamespaces")
  public Map<String, String> getXPathNamespaces() {
    return xpathNamespaces;
  }

  @Override
  protected MatchResult isSimpleMatch(String value) {
    final XmlNodeFindResult xmlNodeFindResult = findXmlNodes(value);
    ListOrSingle<XmlNode> nodeList = xmlNodeFindResult.nodes;
    return MatchResult.of(nodeList != null && !nodeList.isEmpty(), xmlNodeFindResult.subEvents);
  }

  @Override
  protected MatchResult isAdvancedMatch(String value) {
    final XmlNodeFindResult xmlNodeFindResult = findXmlNodes(value);
    ListOrSingle<XmlNode> nodeList = xmlNodeFindResult.nodes;
    if (nodeList == null || nodeList.isEmpty()) {
      return MatchResult.noMatch(xmlNodeFindResult.subEvents);
    }

    SortedSet<MatchResult> results = new TreeSet<>();
    for (XmlNode node : nodeList) {
      results.add(valuePattern.match(node.toString()));
    }

    return results.last();
  }

  @Override
  public ListOrSingle<String> getExpressionResult(String value) {
    ListOrSingle<XmlNode> nodeList = findXmlNodes(value).nodes;
    if (nodeList == null || nodeList.isEmpty()) {
      return ListOrSingle.of();
    }

    return ListOrSingle.of(nodeList.stream().map(XmlNode::toString).collect(Collectors.toList()));
  }

  private XmlNodeFindResult findXmlNodes(String value) {
    // For performance reason, don't try to parse non XML value
    if (value == null || !value.trim().startsWith("<")) {
      final String message =
          String.format("Warning: failed to parse the XML document\nXML: %s", value);
      notifier().info(message);
      return new XmlNodeFindResult(null, SubEvent.warning(message));
    }

    try {
      XmlDocument xmlDocument = Xml.parse(value);
      return new XmlNodeFindResult(xmlDocument.findNodes(expectedValue, xpathNamespaces));
    } catch (XmlException e) {
      final String message =
          String.format(
              "Warning: failed to parse the XML document. Reason: %s\nXML: %s",
              e.getMessage(), value);
      notifier().info(message);
      return new XmlNodeFindResult(null, SubEvent.warning(message));
    } catch (XPathException e) {
      final String message = "Warning: failed to evaluate the XPath expression " + expectedValue;
      notifier().info(message);
      return new XmlNodeFindResult(null, SubEvent.warning(message));
    }
  }

  private static class XmlNodeFindResult {
    /** The Nodes. */
    final ListOrSingle<XmlNode> nodes;

    /** The Sub events. */
    final List<SubEvent> subEvents;

    /**
     * Instantiates a new Xml node find result.
     *
     * @param nodes the nodes
     * @param subEvents the sub events
     */
    public XmlNodeFindResult(ListOrSingle<XmlNode> nodes, SubEvent... subEvents) {
      this.nodes = nodes;
      this.subEvents = List.of(subEvents);
    }
  }
}
