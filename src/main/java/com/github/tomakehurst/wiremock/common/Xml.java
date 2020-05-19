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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static java.util.Collections.emptyMap;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;

public class Xml {

    private Xml() {
        // Hide constructor
    }
    public static void optimizeFactoriesLoading() {
        String transformerFactoryImpl = TransformerFactory.newInstance().getClass().getName();
        String xPathFactoryImpl = XPathFactory.newInstance().getClass().getName();

        setProperty(TransformerFactory.class.getName(), transformerFactoryImpl);
        setProperty(
                XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                xPathFactoryImpl
        );

        XMLUnit.setTransformerFactory(transformerFactoryImpl);
        XMLUnit.setXPathFactory(xPathFactoryImpl);
    }

    private static String setProperty(final String name, final String value) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            @Override
            public String run() {
                return System.setProperty(name, value);
            }
        });
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
        return new SkipResolvingEntitiesDocumentBuilderFactory();
    }

    public static NodeList findNodesByXPath(String xml, String xpathExpression, Map<String, String> namespaces) throws IOException, SAXException, XpathException, ParserConfigurationException {
        DocumentBuilder documentBuilder = Xml.newDocumentBuilderFactory().newDocumentBuilder();
        documentBuilder.setErrorHandler(new SilentErrorHandler());
        Document inDocument = XMLUnit.buildDocument(documentBuilder, new StringReader(xml));
        XpathEngine xpathEngine = XMLUnit.newXpathEngine();

        NamespaceContext namespaceContext = namespaces != null ?
                new SimpleNamespaceContext(namespaces) :
                new SimpleNamespaceContext(extractNamespaces(xpathExpression, inDocument));
        xpathEngine.setNamespaceContext(namespaceContext);

        return xpathEngine.getMatchingNodes(xpathExpression, inDocument);
    }

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("/([a-zA-Z0-9]+?):");

    public static Map<String, String> extractNamespaces(String xpathExpression, Document document) {
        final Matcher matcher = NAMESPACE_PATTERN.matcher(xpathExpression);
        Set<String> prefixes = new HashSet<>();
        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                prefixes.add(matcher.group(1));
            }
        }

        if (!document.hasChildNodes()) {
            return emptyMap();
        }

        return findNamespaces(prefixes, document.getFirstChild());
    }

    private static Map<String, String> findNamespaces(Set<String> prefixesToFind, Node parentNode) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        NamedNodeMap attributes = parentNode.getAttributes();
        Set<String> foundPrefixes = new HashSet<>();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String attributeName = attribute.getNodeName();
                if (attributeName.startsWith("xmlns")) {
                    String[] parts = attributeName.split(":");
                    if (parts.length == 2) {
                        String prefix = parts[1];
                        String uri = attribute.getNodeValue();

                        if (prefixesToFind.contains(prefix)) {
                            foundPrefixes.add(prefix);
                            builder.put(prefix, uri);
                        }
                    }
                }
            }
        }

        if (parentNode.hasChildNodes() && !prefixesToFind.isEmpty()) {
            Set<String> prefixesRemainingToFind = Sets.difference(prefixesToFind, foundPrefixes);
            for (int i = 0; i < parentNode.getChildNodes().getLength(); i++) {
                Node childNode = parentNode.getChildNodes().item(i);
                Map<String, String> childNamespaces = findNamespaces(prefixesRemainingToFind, childNode);
                prefixesRemainingToFind = Sets.difference(prefixesRemainingToFind, childNamespaces.keySet());
                builder.putAll(childNamespaces);
                if (prefixesRemainingToFind.isEmpty()) {
                    break;
                }
            }
        }

        return builder.build();
    }

    private static class SkipResolvingEntitiesDocumentBuilderFactory extends DocumentBuilderFactory {

        private static final ThreadLocal<DocumentBuilderFactory> DBF_CACHE = new ThreadLocal<DocumentBuilderFactory>() {
            @Override
            protected DocumentBuilderFactory initialValue() {
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setFeature("http://xml.org/sax/features/validation", false);
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
                    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    return dbf;
                } catch (ParserConfigurationException e) {
                    return throwUnchecked(e, DocumentBuilderFactory.class);
                }
            }
        };
        private static final ThreadLocal<DocumentBuilder> DB_CACHE = new ThreadLocal<DocumentBuilder>() {
            @Override
            protected DocumentBuilder initialValue() {
                try {
                    return DBF_CACHE.get().newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    return throwUnchecked(e, DocumentBuilder.class);
                }
            }

            @Override
            public DocumentBuilder get() {
                DocumentBuilder documentBuilder = super.get();
                documentBuilder.setEntityResolver(new ResolveToEmptyString());
                documentBuilder.setErrorHandler(null);
                return documentBuilder;
            }
        };

        @Override
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            return DB_CACHE.get();
        }

        private static class ResolveToEmptyString implements EntityResolver {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        }

        @Override
        public void setAttribute(String name, Object value) {
            DBF_CACHE.get().setAttribute(name, value);
        }

        @Override
        public Object getAttribute(String name) {
            return DBF_CACHE.get().getAttribute(name);
        }

        @Override
        public void setFeature(String name, boolean value) throws ParserConfigurationException {
            DBF_CACHE.get().setFeature(name, value);
        }

        @Override
        public boolean getFeature(String name) throws ParserConfigurationException {
            return DBF_CACHE.get().getFeature(name);
        }
    }
}
