package com.github.tomakehurst.wiremock.common;

import com.google.common.io.BaseEncoding;

public class Encoding {

    public static byte[] decodeBase64(String base64) {
        return base64 != null ?
            BaseEncoding.base64().decode(base64) :
            null;
    }

    public static String encodeBase64(byte[] content) {
        return content != null ?
            BaseEncoding.base64().encode(content) :
            null;
    }
}
