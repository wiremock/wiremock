/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import java.util.Map;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathEvaluationResult;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

public abstract class XmlNode {

  protected static final ThreadLocal<XPath> XPATH_CACHE =
      ThreadLocal.withInitial(
          () -> {
            final XPathFactory xPathfactory = XPathFactory.newInstance();
            return xPathfactory.newXPath();
          });

  protected static final ThreadLocal<Transformer> TRANSFORMER_CACHE =
      ThreadLocal.withInitial(
          () -> {
            TransformerFactory transformerFactory;
            try {
              // Optimization to get likely transformerFactory directly, rather than going through
              // FactoryFinder#find
              transformerFactory =
                  (TransformerFactory)
                      Class.forName(
                              "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl")
                          .getDeclaredConstructor()
                          .newInstance();
            } catch (Exception e) {
              transformerFactory = TransformerFactory.newInstance();
            }
            transformerFactory.setAttribute("indent-number", 2);

            try {
              Transformer transformer = transformerFactory.newTransformer();
              transformer.setOutputProperty(INDENT, "yes");
              transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
              return transformer;
            } catch (TransformerConfigurationException e) {
              return throwUnchecked(e, Transformer.class);
            }
          });

  public abstract Map<String, String> getAttributes();

  @SuppressWarnings("unchecked")
  protected static ListOrSingle<XmlNode> toListOrSingle(XPathEvaluationResult<?> evaluationResult) {
    ListOrSingle<XmlNode> xmlNodes = new ListOrSingle<>();

    switch (evaluationResult.type()) {
      case NODESET:
        Iterable<Node> nodes = (Iterable<Node>) evaluationResult.value();
        nodes.forEach(node -> xmlNodes.add(new XmlDomNode(node)));
        break;
      case NODE:
        xmlNodes.add(new XmlDomNode((Node) evaluationResult.value()));
        break;
      default:
        xmlNodes.add(new XmlPrimitiveNode<>(evaluationResult.value()));
        break;
    }

    return xmlNodes;
  }
}
