package com.github.tomakehurst.wiremock.common;

import static com.google.common.base.Charsets.UTF_8;

public class Strings {

    public static String stringFromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        return new String(bytes, UTF_8);
    }

    public static byte[] bytesFromString(String str) {
        if (str == null) {
            return null;
        }

        return str.getBytes();
    }
}
