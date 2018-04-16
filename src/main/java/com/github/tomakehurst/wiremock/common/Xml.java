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
package com.github.tomakehurst.wiremock.common;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

public class Xml {

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
        try {
            TransformerFactory transformerFactory = (TransformerFactory) Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl").newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            return transformerFactory;
        } catch (Exception e) {
            return TransformerFactory.newInstance();
        }
    }

    public static Document read(String xml) {
        try {
            DocumentBuilderFactory dbf = newDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return db.parse(is);
        } catch (SAXException e) {
            throw XmlException.fromSaxException(e);
        } catch (Exception e) {
            return throwUnchecked(e, Document.class);
        }
    }

    public static String toStringValue(Node node) {
        switch (node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.ATTRIBUTE_NODE:
                return node.getTextContent();
            case Node.ELEMENT_NODE:
                return render(node);
            default:
                return node.toString();
        }
    }

    public static String render(Node node) {
        try {
            StringWriter sw = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(INDENT, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            return throwUnchecked(e, String.class);
        }
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory dbf = new SkipResolvingEntitiesDocumentBuilderFactory();
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            return dbf;
        } catch (ParserConfigurationException e) {
            return throwUnchecked(e, DocumentBuilderFactory.class);
        }
    }

    public static class SkipResolvingEntitiesDocumentBuilderFactory extends DocumentBuilderFactoryImpl {
        @Override
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            DocumentBuilder documentBuilder = super.newDocumentBuilder();
            documentBuilder.setEntityResolver(new SkipResolvingEntitiesDocumentBuilderFactory.ResolveToEmptyString());
            return documentBuilder;
        }

        private static class ResolveToEmptyString implements EntityResolver {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        }
    }
}
