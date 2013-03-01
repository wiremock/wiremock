package com.github.tomakehurst.wiremock.common;

import com.google.common.io.Resources;

public class HttpsSettings {

    private final int port;
    private final String keyStorePath;

    public HttpsSettings(int port, String keyStorePath) {
        this.port = port;
        this.keyStorePath = keyStorePath;
    }

    public HttpsSettings(int port) {
        this(port, Resources.getResource("keystore").toString());
    }

    public static final HttpsSettings NO_HTTPS = new HttpsSettings(0, null);

    public int port() {
        return port;
    }

    public String keyStorePath() {
        return keyStorePath;
    }

    public boolean enabled() {
        return this != NO_HTTPS;
    }

    @Override
    public String toString() {
        return "HttpsSettings{" +
                "port=" + port +
                ", keyStorePath='" + keyStorePath + '\'' +
                '}';
    }
}
