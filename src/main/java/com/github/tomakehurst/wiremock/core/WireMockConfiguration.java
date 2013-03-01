package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class WireMockConfiguration implements Options {

    private int portNumber = DEFAULT_PORT;
    private Integer httpsPort = null;
    private boolean browserProxyingEnabled = false;
    private String proxyViaHost;
    private int proxyViaPort = 80;
    private FileSource filesRoot = new SingleRootFileSource("src/test/resources");
    private Notifier notifier = new Log4jNotifier();

    public static WireMockConfiguration wireMockConfig() {
        return new WireMockConfiguration();
    }

    public WireMockConfiguration onPortNumber(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public WireMockConfiguration enableHttpsOnPort(int httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }

    public WireMockConfiguration enableBrowserProxying() {
        this.browserProxyingEnabled = true;
        return this;
    }

    public WireMockConfiguration useProxyServer(String host, int port) {
        this.proxyViaHost = host;
        this.proxyViaPort = port;
        return this;
    }

    public WireMockConfiguration useProxyServer(String host) {
        this.proxyViaHost = host;
        return this;
    }

    public WireMockConfiguration withRootDirectory(String path) {
        this.filesRoot = new SingleRootFileSource(path);
        return this;
    }

    public WireMockConfiguration withFileSource(FileSource fileSource) {
        this.filesRoot = fileSource;
        return this;
    }

    @Override
    public int portNumber() {
        return portNumber;
    }

    @Override
    public boolean httpsEnabled() {
        return httpsPort != null;
    }

    @Override
    public int httpsPortNumber() {
        checkState(httpsEnabled(), "HTTPS not enabled");
        return httpsPort;
    }

    @Override
    public boolean browserProxyingEnabled() {
        return browserProxyingEnabled;
    }

    @Override
    public ProxySettings proxyVia() {
        checkNotNull(proxyViaHost, "Proxy via host must be specified");
        return new ProxySettings(proxyViaHost, proxyViaPort);
    }

    @Override
    public FileSource filesRoot() {
        return filesRoot;
    }

    @Override
    public Notifier notifier() {
        return notifier;
    }
}
