package com.github.tomakehurst.wiremock.common;

import javax.xml.bind.DatatypeConverter;

class DatatypeConverterBase64Encoder implements Base64Encoder {
    @Override
    public String encode(byte[] content) {
        return DatatypeConverter.printBase64Binary(content);
    }

    @Override
    public byte[] decode(String base64) {
        return DatatypeConverter.parseBase64Binary(base64);
    }
}
