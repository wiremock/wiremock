package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODE;

/**
 * This class uses javax.xml.xpath.* for reading a xml via xPath so that the result can be used for response
 * templating.
 */
public class HandlebarsXPathHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(final String inputXml, final Options options) throws IOException {
        if (inputXml == null ) {
            return "";
        }

        if (options.param(0, null) == null) {
            return handleError("The XPath expression cannot be empty");
        }

        final String xPathInput = options.param(0);

        Document doc;
        try (final StringReader reader = new StringReader(inputXml)) {
            InputSource source = new InputSource(reader);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(source);
        } catch (SAXException se) {
            return handleError(inputXml + " is not valid XML");
        } catch (ParserConfigurationException e) {
            return throwUnchecked(e, Object.class);
        }

        try {
            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();

            Node node = (Node) xpath.evaluate(getXPathPrefix() + xPathInput, doc, NODE);

            if (node == null) {
                return "";
            }

            switch(node.getNodeType()) {
                case Node.TEXT_NODE:
                case Node.ATTRIBUTE_NODE:
                    return node.getTextContent();
                case Node.ELEMENT_NODE:
                    return render(node);
                default:
                    return node.toString();
            }
        } catch (XPathExpressionException e) {
            return handleError(xPathInput + " is not a valid XPath expression", e);
        }
    }

    private String render(Node node) {
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


    /**
     * No prefix by default. It allows to extend this class with a specified prefix. Just overwrite this method to do
     * so.
     *
     * @return a prefix which will be applied before the specified xpath.
     */
    protected String getXPathPrefix() {
        return "";
    }
}
