/*
 * Copyright (C) 2020-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.common.xml;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.xmlunit.util.Convert;

public class XmlDocument extends XmlDomNode {

  private final Document document;

  public XmlDocument(Document document) {
    super(document);
    this.document = document;
  }

  public ListOrSingle<XmlNode> findNodes(String xPathExpression) {
    return findNodes(xPathExpression, null);
  }

  public ListOrSingle<XmlNode> findNodes(String xPathExpression, Map<String, String> namespaces) {
    try {
      final XPath xPath = XPATH_CACHE.get();
      xPath.reset();

      XPathEvaluationResult<?> xPathEvaluationResult;
      if (namespaces != null) {
        Map<String, String> fullNamespaces = addStandardNamespaces(namespaces);
        NamespaceContext namespaceContext = Convert.toNamespaceContext(fullNamespaces);
        xPath.setNamespaceContext(namespaceContext);
        xPathEvaluationResult =
            xPath.evaluateExpression(
                xPathExpression, Convert.toInputSource(new DOMSource(document)));
      } else {
        xPathEvaluationResult = xPath.evaluateExpression(xPathExpression, document);
      }

      return toListOrSingle(xPathEvaluationResult);
    } catch (XPathExpressionException e) {
      throw XPathException.fromXPathException(e);
    }
  }

  private static Map<String, String> addStandardNamespaces(Map<String, String> namespaces) {
    Map<String, String> result = new HashMap<String, String>();
    for (String prefix : namespaces.keySet()) {
      String uri = namespaces.get(prefix);
      // according to the Javadocs only the constants defined in
      // XMLConstants are allowed as prefixes for the following
      // two URIs
      if (!XMLConstants.XML_NS_URI.equals(uri)
          && !XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri)) {
        result.put(prefix, uri);
      }
    }
    result.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    result.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);

    return result;
  }
}
