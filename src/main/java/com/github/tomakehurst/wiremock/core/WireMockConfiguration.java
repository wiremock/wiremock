package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.*;

public class WireMockConfiguration implements Options {

    private int portNumber = DEFAULT_PORT;
    private Integer httpsPort = null;
    private String keyStorePath = null;
    private boolean browserProxyingEnabled = false;
    private ProxySettings proxySettings;
    private FileSource filesRoot = new SingleRootFileSource("src/test/resources");
    private Notifier notifier = new Log4jNotifier();
    private boolean requestJournalDisabled = false;

    public static WireMockConfiguration wireMockConfig() {
        return new WireMockConfiguration();
    }

    public WireMockConfiguration port(int portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public WireMockConfiguration httpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
        return this;
    }

    public WireMockConfiguration keystorePath(String path) {
        this.keyStorePath = path;
        return this;
    }

    public WireMockConfiguration enableBrowserProxying(boolean enabled) {
        this.browserProxyingEnabled = enabled;
        return this;
    }

    public WireMockConfiguration proxyVia(String host, int port) {
        this.proxySettings = new ProxySettings(host, port);
        return this;
    }

    public WireMockConfiguration proxyVia(ProxySettings proxySettings) {
        this.proxySettings = proxySettings;
        return this;
    }

    public WireMockConfiguration withRootDirectory(String path) {
        this.filesRoot = new SingleRootFileSource(path);
        return this;
    }

    public WireMockConfiguration fileSource(FileSource fileSource) {
        this.filesRoot = fileSource;
        return this;
    }

    public WireMockConfiguration notifier(Notifier notifier) {
        this.notifier = notifier;
        return this;
    }

    public WireMockConfiguration disableRequestJournal() {
        requestJournalDisabled = true;
        return this;
    }

    @Override
    public int portNumber() {
        return portNumber;
    }

    @Override
    public HttpsSettings httpsSettings() {
        if (httpsPort == null) {
            return HttpsSettings.NO_HTTPS;
        }

        if (keyStorePath == null) {
            return new HttpsSettings(httpsPort);
        }

        return new HttpsSettings(httpsPort, keyStorePath);
    }

    @Override
    public boolean browserProxyingEnabled() {
        return browserProxyingEnabled;
    }

    @Override
    public ProxySettings proxyVia() {
        return proxySettings;
    }

    @Override
    public FileSource filesRoot() {
        return filesRoot;
    }

    @Override
    public Notifier notifier() {
        return notifier;
    }

    public boolean requestJournalDisabled() {
        return requestJournalDisabled;
    }
}
