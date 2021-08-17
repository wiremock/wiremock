package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class WireMockRuntimeInfo {

    private final WireMockServer wireMockServer;
    private final WireMock wireMock;

    public WireMockRuntimeInfo(WireMockServer wireMockServer) {
        this.wireMockServer = wireMockServer;
        this.wireMock = new WireMock(wireMockServer);
    }

    public int getHttpPort() {
        return wireMockServer.port();
    }

    public int getHttpsPort() {
        return wireMockServer.httpsPort();
    }

    public boolean isHttpEnabled() {
        return wireMockServer.isHttpEnabled();
    }

    public boolean isHttpsEnabled() {
        return wireMockServer.isHttpsEnabled();
    }

    public String getHttpBaseUrl() {
        return "http://localhost:" + getHttpPort();
    }

    public String getHttpsBaseUrl() {
        return "https://localhost:" + getHttpsPort();
    }

    public WireMock getWireMock() {
        return wireMock;
    }
}
