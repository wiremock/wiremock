package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

/**
 * This class uses javax.xml.xpath.* for reading a xml via xPath so that the result can be used for response
 * templating.
 */
public class HandlebarsXmlHelper extends HandlebarsHelper<String> {

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

            return xpath.evaluate(getXPathPrefix() + xPathInput, doc);

        } catch (XPathExpressionException e) {
            return handleError(xPathInput + " is not a valid XPath expression", e);
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
