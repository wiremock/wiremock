package com.github.tomakehurst.wiremock.common;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SilentErrorHandler implements ErrorHandler {
    @Override
    public void warning(final SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void error(final SAXParseException exception) throws SAXException {
        throw exception;
    }

    @Override
    public void fatalError(final SAXParseException exception) throws SAXException {
        throw exception;
    }
}