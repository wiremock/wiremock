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

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionLoader;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class WireMockConfiguration implements Options {

    private int portNumber = DEFAULT_PORT;
    private String bindAddress = DEFAULT_BIND_ADDRESS;

    private int containerThreads = DEFAULT_CONTAINER_THREADS;

    private int httpsPort = -1;
    private String keyStorePath = Resources.getResource("keystore").toString();
    private String keyStorePassword = "password";
    private String trustStorePath;
    private String trustStorePassword = "password";
    private boolean needClientAuth;

    private boolean browserProxyingEnabled = false;
    private ProxySettings proxySettings = ProxySettings.NO_PROXY;
    private FileSource filesRoot = new SingleRootFileSource("src/test/resources");
    private Notifier notifier = new Slf4jNotifier(false);
    private boolean requestJournalDisabled = false;
    private List<CaseInsensitiveKey> matchingHeaders = emptyList();

    private String proxyUrl;
    private boolean preserveHostHeader;
    private String proxyHostHeader;

    private Map<String, Extension> extensions = newLinkedHashMap();

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

    public WireMockConfiguration containerThreads(Integer containerThreads) {
        this.containerThreads = containerThreads;
        return this;
    }

    public WireMockConfiguration keystorePath(String path) {
        this.keyStorePath = path;
        return this;
    }

    public WireMockConfiguration keystorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    public WireMockConfiguration trustStorePath(String truststorePath) {
        this.trustStorePath = truststorePath;
        return this;
    }

    public WireMockConfiguration trustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public WireMockConfiguration needClientAuth(boolean gottaHaveIt) {
        this.needClientAuth=gottaHaveIt;
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

    public WireMockConfiguration usingFilesUnderDirectory(String path) {
        return withRootDirectory(path);
    }

    public WireMockConfiguration usingFilesUnderClasspath(String path) {
        this.filesRoot = new ClasspathFileSource(path);
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

    public WireMockConfiguration withProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
        return this;
    }

    public WireMockConfiguration preserveHostHeader(boolean preserveHostHeader) {
        this.preserveHostHeader = preserveHostHeader;
        return this;
    }

    public WireMockConfiguration proxyHostHeader(String hostHeaderValue) {
        this.proxyHostHeader = hostHeaderValue;
        return this;
    }

    public WireMockConfiguration extensions(String... classNames) {
        extensions.putAll(ExtensionLoader.load(classNames));
        return this;
    }

    public WireMockConfiguration extensions(Extension... extensionInstances) {
        extensions.putAll(ExtensionLoader.asMap(asList(extensionInstances)));
        return this;
    }

    public WireMockConfiguration extensions(Class<? extends Extension>... classes) {
        extensions.putAll(ExtensionLoader.load(classes));
        return this;
    }

    @Override
    public int portNumber() {
        return portNumber;
    }

    @Override
    public int containerThreads() {
        return containerThreads;
    }

    @Override
    public HttpsSettings httpsSettings() {
        return new HttpsSettings.Builder()
                .port(httpsPort)
                .keyStorePath(keyStorePath)
                .keyStorePassword(keyStorePassword)
                .trustStorePath(trustStorePath)
                .trustStorePassword(trustStorePassword)
                .needClientAuth(needClientAuth)
                .build();
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
    public List<CaseInsensitiveKey> matchingHeaders() {
    	return matchingHeaders;
    }

    @Override
    public String proxyUrl() {
        return proxyUrl;
    }

    @Override
    public boolean shouldPreserveHostHeader() {
        return preserveHostHeader;
    }

    public String proxyHostHeader() {
        return proxyHostHeader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension> Map<String, T> extensionsOfType(final Class<T> extensionType) {
        return (Map<String, T>) Maps.filterEntries(extensions, new Predicate<Map.Entry<String, Extension>>() {
            public boolean apply(Map.Entry<String, Extension> input) {
                return extensionType.isAssignableFrom(input.getValue().getClass());
            }
        });
    }
}
