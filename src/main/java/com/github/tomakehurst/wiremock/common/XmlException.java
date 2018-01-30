package com.github.tomakehurst.wiremock.common;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlException extends InvalidInputException {

    protected XmlException(Errors errors) {
        super(errors);
    }

    public static XmlException fromSaxException(SAXException e) {
        if (e instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException) e;
            String detail = String.format("%s; line %d; column %d", spe.getMessage(), spe.getLineNumber(), spe.getColumnNumber());
            return new XmlException(Errors.singleWithDetail(50, e.getMessage(), detail));
        }

        return new XmlException(Errors.single(50, e.getMessage()));
    }
}
