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
package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

import com.github.tomakehurst.wiremock.common.xml.Xml;
import com.github.tomakehurst.wiremock.common.xml.XmlException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handlebars helper to allow xml to be formatted :
 *
 * <p>```handlebars {{#formatXml format='pretty'}} // Badly formatted XML {{/formatXml}} ```
 * `format` can be `pretty` or `compact` and defaults to `pretty`
 */
public class FormatXmlHelper extends AbstractFormattingHelper {

  @Override
  String getName() {
    return "formatXml";
  }

  @Override
  String getDataFormat() {
    return "XML";
  }

  private final DocumentBuilderFactory documentBuilderFactory =
      Xml.DEFAULT_DOCUMENT_BUILDER_FACTORY;

  private final TransformerFactory transformerFactory;

  public FormatXmlHelper() {
    TransformerFactory factory = TransformerFactory.newDefaultInstance();
    factory.setAttribute("indent-number", 2);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    transformerFactory = factory;
  }

  @Override
  protected String apply(String bodyText, Format format) {
    try {
      var xml = read(bodyText);
      switch (format) {
        case pretty:
          return print(xml, "yes");
        case compact:
          return print(xml, "no");
        default:
          throw new IllegalStateException();
      }
    } catch (XmlException
        | ParserConfigurationException
        | IOException
        | SAXException
        | TransformerException e) {
      return handleError("Input is not valid XML", e);
    }
  }

  private Document read(String xml) throws ParserConfigurationException, IOException, SAXException {
    try {
      DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
      Document doc = db.parse(new InputSource(new StringReader(xml)));
      return removeBlankTextNodes(doc);
    } catch (SAXException e) {
      throw XmlException.fromSaxException(e);
    }
  }

  private <T extends Node> T removeBlankTextNodes(T node) {
    List<Node> toRemove = new ArrayList<>();
    if (!(node instanceof Attr)) {
      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        Node child = node.getChildNodes().item(i);
        if (isBlankTextNode(child)) {
          toRemove.add(child);
        } else {
          removeBlankTextNodes(child);
        }
      }
      for (Node child : toRemove) {
        node.removeChild(child);
      }
    }
    return node;
  }

  private boolean isBlankTextNode(Node node) {
    return (node instanceof Text) && node.getNodeValue().isBlank();
  }

  private String print(Document doc, String indent) throws TransformerException {
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(INDENT, indent);
    transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
    return writeToString(doc, transformer);
  }

  private String writeToString(Document doc, Transformer transformer) throws TransformerException {
    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(doc), new StreamResult(writer));
    return writer.toString();
  }
}
