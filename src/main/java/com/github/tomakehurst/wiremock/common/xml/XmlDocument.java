package com.github.tomakehurst.wiremock.common.xml;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static javax.xml.xpath.XPathConstants.NODESET;

public class XmlDocument extends XmlNode {

    private final Document document;

    public XmlDocument(Document document) {
        super(document);
        this.document = document;
    }

    public ListOrSingle<XmlNode> findNodes(String xpathExpression) {
        try {
            NodeList nodeSet = (NodeList) XPATH_CACHE.get().evaluate(xpathExpression, document, NODESET);
            return toListOrSingle(nodeSet);
        } catch (XPathExpressionException e) {
            throw XmlException.fromXPathException(e);
        }
    }

}
