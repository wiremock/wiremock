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

/**
 * This class uses javax.xml.xpath.* for reading a xml via xPath so that the result can be used for response
 * templating.
 *
 * @author Christopher Holomek
 */
public class HandlebarsXmlHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(final String context, final Options options) throws IOException {
        if (context == null || options == null || options.param(0, null) == null) {
            return this.handleError(this.getClass().getSimpleName() + ": No parameters defined. Helper not applied");
        }

        final String xPathInput = options.param(0);

        try (final StringReader reader = new StringReader(context)) {
            final InputSource source = new InputSource(reader);

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(source);

            final XPathFactory xPathfactory = XPathFactory.newInstance();
            final XPath xpath = xPathfactory.newXPath();

            return xpath.evaluate(getXPathPrefix() + xPathInput, doc);

        } catch (SAXException | XPathExpressionException | ParserConfigurationException e) {
            return this.handleError(this.getClass().getSimpleName() + ": An error occurred. Helper not applied.", e);
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
