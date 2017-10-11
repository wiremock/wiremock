package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.security.ClientBasicAuthenticator;
import com.github.tomakehurst.wiremock.security.NoClientAuthenticator;

public class WireMockBuilder {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;

    private int port = DEFAULT_PORT;
    private String host = DEFAULT_HOST;
    private String urlPathPrefix = "";
    private String scheme = "http";
    private String hostHeader = null;
    private String proxyHost = null;
    private int proxyPort = 0;
    private ClientAuthenticator authenticator = new NoClientAuthenticator();

    public WireMockBuilder port(int port) {
        this.port = port;
        return this;
    }

    public WireMockBuilder host(String host) {
        this.host = host;
        return this;
    }

    public WireMockBuilder urlPathPrefix(String urlPathPrefix) {
        this.urlPathPrefix = urlPathPrefix;
        return this;
    }

    public WireMockBuilder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public WireMockBuilder http() {
        return scheme("http");
    }

    public WireMockBuilder https() {
        return scheme("https");
    }

    public WireMockBuilder hostHeader(String hostHeader) {
        this.hostHeader = hostHeader;
        return this;
    }

    public WireMockBuilder proxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        return this;
    }

    public WireMockBuilder proxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        return this;
    }

    public WireMockBuilder authenticator(ClientAuthenticator authenticator) {
        this.authenticator = authenticator;
        return this;
    }

    public WireMockBuilder basicAuthenticator(String username, String password) {
        return authenticator(new ClientBasicAuthenticator(username, password));
    }

    public WireMock build() {
        return new WireMock(scheme, host, port, urlPathPrefix, hostHeader, proxyHost, proxyPort, authenticator);
    }
}