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
package com.github.tomakehurst.wiremock.common.xml;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.SilentErrorHandler;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xml {

  private Xml() {
    // Hide constructor
  }

  public static void optimizeFactoriesLoading() {
    try {
      String transformerFactoryImpl = TransformerFactory.newInstance().getClass().getName();
      String xPathFactoryImpl = XPathFactory.newInstance().getClass().getName();

      System.setProperty(TransformerFactory.class.getName(), transformerFactoryImpl);
      System.setProperty(
          XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI,
          xPathFactoryImpl);

      XMLUnit.setTransformerFactory(transformerFactoryImpl);
      XMLUnit.setXPathFactory(xPathFactoryImpl);
    } catch (Exception ignored) {
      // Since this is just an optimisation, if an exception is thrown we do nothing and carry on
    }
  }

  public static String prettyPrint(String xml) {
    try {
      return prettyPrint(read(xml));
    } catch (Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  public static String prettyPrint(Document doc) {
    try {
      TransformerFactory transformerFactory = createTransformerFactory();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(INDENT, "yes");
      transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, result);
      return result.getWriter().toString();
    } catch (Exception e) {
      return throwUnchecked(e, String.class);
    }
  }

  private static TransformerFactory createTransformerFactory() {
    TransformerFactory transformerFactory;
    try {
      // Optimization to get likely transformerFactory directly, rather than going through
      // FactoryFinder#find
      transformerFactory =
          (TransformerFactory)
              Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl")
                  .getDeclaredConstructor()
                  .newInstance();
    } catch (Exception e) {
      transformerFactory = TransformerFactory.newInstance();
    }
    transformerFactory.setAttribute("indent-number", 2);
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    return transformerFactory;
  }

  public static Document read(String xml) {
    return read(xml, newDocumentBuilderFactory());
  }

  public static Document read(String xml, DocumentBuilderFactory dbf) {
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      InputSource is = new InputSource(new StringReader(xml));
      return db.parse(is);
    } catch (SAXException e) {
      throw XmlException.fromSaxException(e);
    } catch (Exception e) {
      return throwUnchecked(e, Document.class);
    }
  }

  public static XmlDocument parse(String xml) {
    try {
      InputSource source = new InputSource(new StringReader(xml));
      return new XmlDocument(getDocumentBuilder().parse(source));
    } catch (SAXException | IOException e) {
      throw new XmlException(Errors.single(50, e.getMessage()));
    }
  }

  private static DocumentBuilder getDocumentBuilder() {
    try {
      return newDocumentBuilderFactory().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      return throwUnchecked(e, DocumentBuilder.class);
    }
  }

  public static DocumentBuilderFactory newDocumentBuilderFactory() {
    return new SkipResolvingEntitiesDocumentBuilderFactory();
  }

  public static DocumentBuilderFactory newDocumentBuilderFactory(boolean isNamespaceAware) {
    return isNamespaceAware
        ? new SkipResolvingEntitiesDocumentBuilderFactoryNS()
        : new SkipResolvingEntitiesDocumentBuilderFactory();
  }

  private static class SkipResolvingEntitiesDocumentBuilderFactory extends DocumentBuilderFactory {

    private static final ThreadLocal<DocumentBuilderFactory> DBF_CACHE =
        ThreadLocal.withInitial(() -> createInternalDocumentBuilderFactory(false));

    private static final ThreadLocal<DocumentBuilder> DB_CACHE =
        ThreadLocal.withInitial(() -> createInternalDocumentBuilder(DBF_CACHE.get()));

    @Override
    public DocumentBuilder newDocumentBuilder() {
      return DB_CACHE.get();
    }

    private static class ResolveToEmptyString implements EntityResolver {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
      }
    }

    @Override
    public void setAttribute(String name, Object value) {
      getCachedDocumentBuilderFactory().setAttribute(name, value);
    }

    @Override
    public Object getAttribute(String name) {
      return getCachedDocumentBuilderFactory().getAttribute(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
      getCachedDocumentBuilderFactory().setFeature(name, value);
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
      return getCachedDocumentBuilderFactory().getFeature(name);
    }

    protected DocumentBuilderFactory getCachedDocumentBuilderFactory() {
      return DBF_CACHE.get();
    }

    protected static DocumentBuilderFactory createInternalDocumentBuilderFactory(
        boolean isNamespaceAware) {
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(isNamespaceAware);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return dbf;
      } catch (ParserConfigurationException e) {
        return throwUnchecked(e, DocumentBuilderFactory.class);
      }
    }

    protected static DocumentBuilder createInternalDocumentBuilder(DocumentBuilderFactory factory) {
      try {
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(new ResolveToEmptyString());
        documentBuilder.setErrorHandler(new SilentErrorHandler());
        return documentBuilder;
      } catch (ParserConfigurationException e) {
        return throwUnchecked(e, DocumentBuilder.class);
      }
    }
  }

  private static class SkipResolvingEntitiesDocumentBuilderFactoryNS
      extends SkipResolvingEntitiesDocumentBuilderFactory {

    private static final ThreadLocal<DocumentBuilderFactory> DBF_NS_CACHE =
        ThreadLocal.withInitial(() -> createInternalDocumentBuilderFactory(true));

    private static final ThreadLocal<DocumentBuilder> DB_NS_CACHE =
        ThreadLocal.withInitial(() -> createInternalDocumentBuilder(DBF_NS_CACHE.get()));

    @Override
    public DocumentBuilder newDocumentBuilder() {
      return DB_NS_CACHE.get();
    }

    @Override
    protected DocumentBuilderFactory getCachedDocumentBuilderFactory() {
      return DBF_NS_CACHE.get();
    }
  }
}
