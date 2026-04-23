/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.XMLReader;

public class XmlDomNode extends XmlNode {

  private final Node domNode;
  private final Map<String, String> attributes;

  public XmlDomNode(Node domNode) {
    this.domNode = domNode;
    attributes =
        domNode.hasAttributes()
            ? convertAttributeMap(domNode.getAttributes())
            : Collections.emptyMap();
  }

  private static Map<String, String> convertAttributeMap(NamedNodeMap namedNodeMap) {
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < namedNodeMap.getLength(); i++) {
      Node node = namedNodeMap.item(i);
      map.put(node.getNodeName(), node.getNodeValue());
    }

    return Collections.unmodifiableMap(map);
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public String getName() {
    return domNode.getNodeName();
  }

  public String getText() {
    return domNode.getTextContent();
  }

  @Override
  public String toString() {
    switch (domNode.getNodeType()) {
      case Node.TEXT_NODE:
      case Node.ATTRIBUTE_NODE:
        return domNode.getTextContent();
      case Node.DOCUMENT_NODE:
      case Node.ELEMENT_NODE:
        return render();
      default:
        return domNode.toString();
    }
  }

  private String render() {
    try {
      Transformer transformer = TRANSFORMER_CACHE.get();
      StreamResult result = new StreamResult(new StringWriter());
      Source source = getSourceForTransform(domNode);
      transformer.transform(source, result);
      return result.getWriter().toString();
    } catch (Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  private static final Class<XMLReader> DOM2SAX_XMLREADER_CLASS = getDom2SaxAvailability();

  @SuppressWarnings("unchecked")
  private static Class<XMLReader> getDom2SaxAvailability() {
    try {
      return (Class<XMLReader>)
          Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  // This nasty little hack attempts to ensure no exception is thrown when attempting to print an
  // XML node with
  // unbound namespace prefixes (which can happen when you've selected an element via XPath whose
  // namespaces are declared in a parent element).
  // For some reason Transformer is happy to do this with a SAX source, but not a DOM source.
  private static Source getSourceForTransform(Node node) {
    if (DOM2SAX_XMLREADER_CLASS != null) {
      try {
        Constructor<XMLReader> constructor = DOM2SAX_XMLREADER_CLASS.getConstructor(Node.class);
        XMLReader dom2SAX = constructor.newInstance(node);
        SAXSource saxSource = new SAXSource();
        saxSource.setXMLReader(dom2SAX);
        return saxSource;
      } catch (NoSuchMethodException
          | InstantiationException
          | IllegalAccessException
          | InvocationTargetException e) {
        return new DOMSource(node);
      }
    }

    return new DOMSource(node);
  }
}
