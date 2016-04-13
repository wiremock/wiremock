package com.github.tomakehurst.wiremock.matching;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import java.io.IOException;

public class EqualToXmlPattern extends StringValuePattern {

    static {
        XMLUnit.setIgnoreWhitespace(true);
    }

    public EqualToXmlPattern(String expectedValue) {
        super(expectedValue);
    }

    public String getEqualToXml() {
        return expectedValue;
    }

    @Override
    public MatchResult match(String value) {
        try {
            Diff diff = XMLUnit.compareXML(expectedValue, value);
            return MatchResult.of(diff.similar());
        } catch (SAXException e) {
            return MatchResult.noMatch();
        } catch (IOException e) {
            return MatchResult.noMatch();
        }
    }
}
