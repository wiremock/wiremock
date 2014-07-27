/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.core;

import java.util.List;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;

import static com.google.common.collect.Lists.transform;

public class WireMockConfiguration implements Options {

    private int portNumber = DEFAULT_PORT;
    private String bindAddress = DEFAULT_BIND_ADDRESS;
    private Integer httpsPort = null;
    private String keyStorePath = null;
    private boolean browserProxyingEnabled = false;
    private ProxySettings proxySettings;
    private FileSource filesRoot = new SingleRootFileSource("src/test/resources");
    private Notifier notifier = new Log4jNotifier();
    private boolean requestJournalDisabled = false;
    private List<CaseInsensitiveKey> matchingHeaders;

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
    
    public WireMockConfiguration bindAddress(String bindAddress){
        this.bindAddress = bindAddress;
        return this;
    }

    public WireMockConfiguration disableRequestJournal() {
        requestJournalDisabled = true;
        return this;
    }

    public WireMockConfiguration recordRequestHeadersForMatching(List<String> headers) {
    	this.matchingHeaders = transform(headers, CaseInsensitiveKey.TO_CASE_INSENSITIVE_KEYS);
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

    @Override
    public String bindAddress() {
        return bindAddress;
    }
    
    @Override
    public List<CaseInsensitiveKey>matchingHeaders() {
    	return matchingHeaders;
    }
}
