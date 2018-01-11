package com.github.tomakehurst.wiremock.common;

import com.google.common.io.BaseEncoding;

public class GuavaBase64Encoder implements Base64Encoder {
    @Override
    public String encode(byte[] content) {
        return BaseEncoding.base64().encode(content);
    }

    @Override
    public byte[] decode(String base64) {
        return BaseEncoding.base64().decode(base64);
    }
}
