package com.github.tomakehurst.wiremock.common;

interface Base64Encoder {
    String encode(byte[] content);
    byte[] decode(String base64);
}
