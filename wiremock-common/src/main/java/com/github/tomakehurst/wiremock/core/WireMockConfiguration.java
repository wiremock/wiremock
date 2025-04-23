/*
 * Copyright (C) 2013-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.BrowserProxySettings.DEFAULT_CA_KESTORE_PASSWORD;
import static com.github.tomakehurst.wiremock.common.BrowserProxySettings.DEFAULT_CA_KEYSTORE_PATH;
import static com.github.tomakehurst.wiremock.common.Limit.UNLIMITED;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResource;
import static com.github.tomakehurst.wiremock.core.WireMockApp.MAPPINGS_ROOT;
import static com.github.tomakehurst.wiremock.http.CaseInsensitiveKey.TO_CASE_INSENSITIVE_KEYS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSourceFactory;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionDeclarations;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.client.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.DoNothingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.security.BasicAuthenticator;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsSource;
import com.github.tomakehurst.wiremock.store.DefaultStores;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import com.github.tomakehurst.wiremock.verification.notmatched.PlainTextStubNotMatchedRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WireMockConfiguration implements Options {

  private long asyncResponseTimeout = DEFAULT_TIMEOUT;
  private boolean disableOptimizeXmlFactoriesLoading = false;
  private int portNumber = DEFAULT_PORT;
  private boolean httpDisabled = false;
  private boolean http2PlainDisabled = false;
  private boolean http2TlsDisabled = false;
  private String bindAddress = DEFAULT_BIND_ADDRESS;

  private int containerThreads = DEFAULT_CONTAINER_THREADS;

  private int httpsPort = -1;
  private String keyStorePath = getResource(WireMockConfiguration.class, "keystore").toString();
  private String keyStorePassword = "password";
  private String keyManagerPassword = "password";
  private String keyStoreType = "JKS";
  private String trustStorePath;
  private String trustStorePassword = "password";
  private String trustStoreType = "JKS";
  private boolean needClientAuth;

  private boolean browserProxyingEnabled = false;
  private String caKeystorePath = DEFAULT_CA_KEYSTORE_PATH;
  private String caKeystorePassword = DEFAULT_CA_KESTORE_PASSWORD;
  private String caKeystoreType = "JKS";
  private KeyStoreSettings caKeyStoreSettings = null;
  private boolean trustAllProxyTargets = false;
  private final List<String> trustedProxyTargets = new ArrayList<>();

  private ProxySettings proxySettings = ProxySettings.NO_PROXY;
  private FileSource filesRoot = new SingleRootFileSource("src/test/resources");
  private Stores stores;
  private MappingsSource mappingsSource;
  private FilenameMaker filenameMaker;

  private Notifier notifier = new Slf4jNotifier(false);
  private boolean requestJournalDisabled = false;
  private Optional<Integer> maxRequestJournalEntries = Optional.empty();
  private List<CaseInsensitiveKey> matchingHeaders = emptyList();

  private boolean preserveHostHeader;
  private boolean preserveUserAgentProxyHeader;
  private String proxyHostHeader;
  private HttpServerFactory httpServerFactory = null;
  private HttpClientFactory httpClientFactory = new ApacheHttpClientFactory();
  private Integer jettyAcceptors;
  private Integer jettyAcceptQueueSize;
  private Integer jettyHeaderBufferSize;
  private Integer jettyHeaderRequestSize;
  private Integer jettyHeaderResponseSize;
  private Long jettyStopTimeout;
  private Long jettyIdleTimeout;

  private ExtensionDeclarations extensions = new ExtensionDeclarations();
  private boolean extensionScanningEnabled = false;
  private WiremockNetworkTrafficListener networkTrafficListener =
      new DoNothingWiremockNetworkTrafficListener();

  private Authenticator adminAuthenticator = new NoAuthenticator();
  private boolean requireHttpsForAdminApi = false;

  private Function<Extensions, NotMatchedRenderer> notMatchedRendererFactory =
      PlainTextStubNotMatchedRenderer::new;
  private boolean asynchronousResponseEnabled;
  private int asynchronousResponseThreads;
  private ChunkedEncodingPolicy chunkedEncodingPolicy;
  private boolean gzipDisabled = false;
  private boolean stubLoggingDisabled = false;

  private boolean stubCorsEnabled = false;
  private boolean disableStrictHttpHeaders;

  private boolean proxyPassThrough = true;

  private Limit responseBodySizeLimit = UNLIMITED;

  private NetworkAddressRules proxyTargetRules = NetworkAddressRules.ALLOW_ALL;

  private int proxyTimeout = DEFAULT_TIMEOUT;

  private int maxHttpClientConnections = DEFAULT_MAX_HTTP_CONNECTIONS;
  private boolean disableConnectionReuse = DEFAULT_DISABLE_CONNECTION_REUSE;

  private boolean templatingEnabled = true;
  private boolean globalTemplating = false;
  private Set<String> permittedSystemKeys = null;
  private Long maxTemplateCacheEntries = DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES;
  private boolean templateEscapingDisabled = true;

  private Set<String> supportedProxyEncodings = null;

  private int webhookThreadPoolSize = DEFAULT_WEBHOOK_THREADPOOL_SIZE;

  private MappingsSource getMappingsSource() {
    if (mappingsSource == null) {
      mappingsSource =
          new JsonFileMappingsSource(filesRoot.child(MAPPINGS_ROOT), getFilenameMaker());
    }

    return mappingsSource;
  }

  public static WireMockConfiguration wireMockConfig() {
    return new WireMockConfiguration();
  }

  public static WireMockConfiguration options() {
    return wireMockConfig();
  }

  public WireMockConfiguration proxyPassThrough(boolean proxyPassThrough) {
    this.proxyPassThrough = proxyPassThrough;
    GlobalSettings newSettings =
        getStores().getSettingsStore().get().copy().proxyPassThrough(proxyPassThrough).build();
    getStores().getSettingsStore().set(newSettings);
    return this;
  }

  public WireMockConfiguration timeout(int timeout) {
    this.asyncResponseTimeout = timeout;
    return this;
  }

  public WireMockConfiguration port(int portNumber) {
    this.portNumber = portNumber;
    return this;
  }

  public WireMockConfiguration filenameTemplate(String filenameTemplate) {
    this.filenameMaker = new FilenameMaker(filenameTemplate);
    return this;
  }

  public WireMockConfiguration dynamicPort() {
    this.portNumber = DYNAMIC_PORT;
    return this;
  }

  public WireMockConfiguration httpDisabled(boolean httpDisabled) {
    this.httpDisabled = httpDisabled;
    return this;
  }

  public WireMockConfiguration http2PlainDisabled(boolean enabled) {
    this.http2PlainDisabled = enabled;
    return this;
  }

  public WireMockConfiguration http2TlsDisabled(boolean enabled) {
    this.http2TlsDisabled = enabled;
    return this;
  }

  public WireMockConfiguration httpsPort(Integer httpsPort) {
    this.httpsPort = httpsPort;
    return this;
  }

  public WireMockConfiguration dynamicHttpsPort() {
    this.httpsPort = DYNAMIC_PORT;
    return this;
  }

  public WireMockConfiguration containerThreads(Integer containerThreads) {
    this.containerThreads = containerThreads;
    return this;
  }

  public WireMockConfiguration jettyAcceptors(Integer jettyAcceptors) {
    this.jettyAcceptors = jettyAcceptors;
    return this;
  }

  public WireMockConfiguration jettyAcceptQueueSize(Integer jettyAcceptQueueSize) {
    this.jettyAcceptQueueSize = jettyAcceptQueueSize;
    return this;
  }

  @Deprecated
  public WireMockConfiguration jettyHeaderBufferSize(Integer jettyHeaderBufferSize) {
    this.jettyHeaderBufferSize = jettyHeaderBufferSize;
    return this;
  }

  public WireMockConfiguration jettyHeaderRequestSize(Integer jettyHeaderRequestSize) {
    this.jettyHeaderRequestSize = jettyHeaderRequestSize;
    return this;
  }

  public WireMockConfiguration jettyHeaderResponseSize(Integer jettyHeaderResponseSize) {
    this.jettyHeaderResponseSize = jettyHeaderResponseSize;
    return this;
  }

  public WireMockConfiguration jettyStopTimeout(Long jettyStopTimeout) {
    this.jettyStopTimeout = jettyStopTimeout;
    return this;
  }

  public WireMockConfiguration jettyIdleTimeout(Long jettyIdleTimeout) {
    this.jettyIdleTimeout = jettyIdleTimeout;
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

  public WireMockConfiguration keyManagerPassword(String keyManagerPassword) {
    this.keyManagerPassword = keyManagerPassword;
    return this;
  }

  public WireMockConfiguration keystoreType(String keyStoreType) {
    this.keyStoreType = keyStoreType;
    return this;
  }

  public WireMockConfiguration caKeystoreSettings(KeyStoreSettings caKeyStoreSettings) {
    this.caKeyStoreSettings = caKeyStoreSettings;
    return this;
  }

  public WireMockConfiguration caKeystorePath(String path) {
    this.caKeystorePath = path;
    return this;
  }

  public WireMockConfiguration caKeystorePassword(String keyStorePassword) {
    this.caKeystorePassword = keyStorePassword;
    return this;
  }

  public WireMockConfiguration caKeystoreType(String caKeystoreType) {
    this.caKeystoreType = caKeystoreType;
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

  public WireMockConfiguration trustStoreType(String trustStoreType) {
    this.trustStoreType = trustStoreType;
    return this;
  }

  public WireMockConfiguration needClientAuth(boolean needClientAuth) {
    this.needClientAuth = needClientAuth;
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

  public WireMockConfiguration withStores(Stores stores) {
    this.stores = stores;
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
    fileSource(new ClasspathFileSource(path));
    return this;
  }

  public WireMockConfiguration fileSource(FileSource fileSource) {
    this.filesRoot = fileSource;
    return this;
  }

  public WireMockConfiguration mappingSource(MappingsSource mappingsSource) {
    this.mappingsSource = mappingsSource;
    return this;
  }

  public WireMockConfiguration notifier(Notifier notifier) {
    this.notifier = notifier;
    return this;
  }

  public WireMockConfiguration bindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
    return this;
  }

  public WireMockConfiguration disableRequestJournal() {
    requestJournalDisabled = true;
    return this;
  }

  @Deprecated
  /**
   * @deprecated use {@link #maxRequestJournalEntries(int)} instead
   */
  public WireMockConfiguration maxRequestJournalEntries(
      Optional<Integer> maxRequestJournalEntries) {
    this.maxRequestJournalEntries = maxRequestJournalEntries;
    return this;
  }

  public WireMockConfiguration maxRequestJournalEntries(int maxRequestJournalEntries) {
    this.maxRequestJournalEntries = Optional.of(maxRequestJournalEntries);
    return this;
  }

  public WireMockConfiguration recordRequestHeadersForMatching(List<String> headers) {
    this.matchingHeaders =
        headers.stream().map(TO_CASE_INSENSITIVE_KEYS).collect(Collectors.toUnmodifiableList());
    return this;
  }

  public WireMockConfiguration preserveHostHeader(boolean preserveHostHeader) {
    this.preserveHostHeader = preserveHostHeader;
    return this;
  }

  public WireMockConfiguration preserveUserAgentProxyHeader(boolean preserveUserAgentProxyHeader) {
    this.preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
    return this;
  }

  public WireMockConfiguration proxyHostHeader(String hostHeaderValue) {
    this.proxyHostHeader = hostHeaderValue;
    return this;
  }

  public WireMockConfiguration extensions(String... classNames) {
    extensions.add(classNames);
    return this;
  }

  public WireMockConfiguration extensions(Extension... extensionInstances) {
    extensions.add(extensionInstances);
    return this;
  }

  public WireMockConfiguration extensionFactories(ExtensionFactory... extensionFactories) {
    return extensions(extensionFactories);
  }

  public WireMockConfiguration extensions(ExtensionFactory... extensionFactories) {
    extensions.add(extensionFactories);
    return this;
  }

  public WireMockConfiguration extensions(Class<? extends Extension>... classes) {
    extensions.add(classes);
    return this;
  }

  public WireMockConfiguration extensionFactories(
      Class<? extends ExtensionFactory>... factoryClasses) {
    extensions.addFactories(factoryClasses);
    return this;
  }

  public WireMockConfiguration extensionScanningEnabled(boolean enabled) {
    this.extensionScanningEnabled = enabled;
    return this;
  }

  public WireMockConfiguration httpServerFactory(HttpServerFactory serverFactory) {
    this.httpServerFactory = serverFactory;
    return this;
  }

  public WireMockConfiguration httpClientFactory(HttpClientFactory httpClientFactory) {
    this.httpClientFactory = httpClientFactory;
    return this;
  }

  public WireMockConfiguration networkTrafficListener(
      WiremockNetworkTrafficListener networkTrafficListener) {
    this.networkTrafficListener = networkTrafficListener;
    return this;
  }

  public WireMockConfiguration adminAuthenticator(Authenticator authenticator) {
    this.adminAuthenticator = authenticator;
    return this;
  }

  public WireMockConfiguration basicAdminAuthenticator(String username, String password) {
    return adminAuthenticator(new BasicAuthenticator(username, password));
  }

  public WireMockConfiguration requireHttpsForAdminApi() {
    this.requireHttpsForAdminApi = true;
    return this;
  }

  public WireMockConfiguration notMatchedRendererFactory(
      Function<Extensions, NotMatchedRenderer> notMatchedRendererFactory) {
    this.notMatchedRendererFactory = notMatchedRendererFactory;
    return this;
  }

  public WireMockConfiguration asynchronousResponseEnabled(boolean asynchronousResponseEnabled) {
    this.asynchronousResponseEnabled = asynchronousResponseEnabled;
    return this;
  }

  public WireMockConfiguration asynchronousResponseThreads(int asynchronousResponseThreads) {
    this.asynchronousResponseThreads = asynchronousResponseThreads;
    return this;
  }

  public WireMockConfiguration useChunkedTransferEncoding(ChunkedEncodingPolicy policy) {
    this.chunkedEncodingPolicy = policy;
    return this;
  }

  public WireMockConfiguration gzipDisabled(boolean gzipDisabled) {
    this.gzipDisabled = gzipDisabled;
    return this;
  }

  public WireMockConfiguration stubRequestLoggingDisabled(boolean disabled) {
    this.stubLoggingDisabled = disabled;
    return this;
  }

  public WireMockConfiguration stubCorsEnabled(boolean enabled) {
    this.stubCorsEnabled = enabled;
    return this;
  }

  public WireMockConfiguration trustAllProxyTargets(boolean enabled) {
    this.trustAllProxyTargets = enabled;
    return this;
  }

  public WireMockConfiguration trustedProxyTargets(String... trustedProxyTargets) {
    return trustedProxyTargets(asList(trustedProxyTargets));
  }

  public WireMockConfiguration trustedProxyTargets(List<String> trustedProxyTargets) {
    this.trustedProxyTargets.addAll(trustedProxyTargets);
    return this;
  }

  public WireMockConfiguration disableOptimizeXmlFactoriesLoading(
      boolean disableOptimizeXmlFactoriesLoading) {
    this.disableOptimizeXmlFactoriesLoading = disableOptimizeXmlFactoriesLoading;
    return this;
  }

  public WireMockConfiguration maxLoggedResponseSize(int maxSize) {
    this.responseBodySizeLimit = new Limit(maxSize);
    return this;
  }

  public WireMockConfiguration limitProxyTargets(NetworkAddressRules proxyTargetRules) {
    this.proxyTargetRules = proxyTargetRules;
    return this;
  }

  public WireMockConfiguration proxyTimeout(int proxyTimeout) {
    this.proxyTimeout = proxyTimeout;
    return this;
  }

  public WireMockConfiguration maxHttpClientConnections(int maxHttpClientConnections) {
    this.maxHttpClientConnections = maxHttpClientConnections;
    return this;
  }

  public WireMockConfiguration disableConnectionReuse(boolean disableConnectionReuse) {
    this.disableConnectionReuse = disableConnectionReuse;
    return this;
  }

  public WireMockConfiguration templatingEnabled(boolean templatingEnabled) {
    this.templatingEnabled = templatingEnabled;
    return this;
  }

  public WireMockConfiguration globalTemplating(boolean globalTemplating) {
    this.globalTemplating = globalTemplating;
    return this;
  }

  public WireMockConfiguration withPermittedSystemKeys(String... systemKeys) {
    this.permittedSystemKeys = Set.of(systemKeys);
    return this;
  }

  public WireMockConfiguration withTemplateEscapingDisabled(boolean templateEscapingDisabled) {
    this.templateEscapingDisabled = templateEscapingDisabled;
    return this;
  }

  public WireMockConfiguration withMaxTemplateCacheEntries(Long maxTemplateCacheEntries) {
    this.maxTemplateCacheEntries = maxTemplateCacheEntries;
    return this;
  }

  public WireMockConfiguration withSupportedProxyEncodings(Set<String> supportedProxyEncodings) {
    this.supportedProxyEncodings = supportedProxyEncodings;
    return this;
  }

  public WireMockConfiguration withSupportedProxyEncodings(String... supportedProxyEncodings) {
    return withSupportedProxyEncodings(Set.of(supportedProxyEncodings));
  }

  public WireMockConfiguration withWebhookThreadPoolSize(Integer webhookThreadPoolSize) {
    this.webhookThreadPoolSize = webhookThreadPoolSize;
    return this;
  }

  @Override
  public int portNumber() {
    return portNumber;
  }

  @Override
  public boolean getHttpDisabled() {
    return httpDisabled;
  }

  @Override
  public boolean getHttp2PlainDisabled() {
    return http2PlainDisabled;
  }

  @Override
  public boolean getHttp2TlsDisabled() {
    return http2TlsDisabled;
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
        .keyManagerPassword(keyManagerPassword)
        .keyStoreType(keyStoreType)
        .trustStorePath(trustStorePath)
        .trustStorePassword(trustStorePassword)
        .trustStoreType(trustStoreType)
        .needClientAuth(needClientAuth)
        .build();
  }

  @Override
  public JettySettings jettySettings() {
    return JettySettings.Builder.aJettySettings()
        .withAcceptors(jettyAcceptors)
        .withAcceptQueueSize(jettyAcceptQueueSize)
        .withRequestHeaderSize(jettyHeaderBufferSize)
        .withRequestHeaderSize(jettyHeaderRequestSize)
        .withResponseHeaderSize(jettyHeaderResponseSize)
        .withStopTimeout(jettyStopTimeout)
        .withIdleTimeout(jettyIdleTimeout)
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
  public Stores getStores() {
    if (stores == null) {
      stores = new DefaultStores(filesRoot);
    }

    return stores;
  }

  @Override
  public FileSource filesRoot() {
    return filesRoot;
  }

  @Override
  public MappingsLoader mappingsLoader() {
    return getMappingsSource();
  }

  @Override
  public MappingsSaver mappingsSaver() {
    return getMappingsSource();
  }

  @Override
  public Notifier notifier() {
    return notifier;
  }

  @Override
  public boolean requestJournalDisabled() {
    return requestJournalDisabled;
  }

  @Override
  public Optional<Integer> maxRequestJournalEntries() {
    return maxRequestJournalEntries;
  }

  @Override
  public String bindAddress() {
    return bindAddress;
  }

  @Override
  public FilenameMaker getFilenameMaker() {
    return filenameMaker;
  }

  @Override
  public List<CaseInsensitiveKey> matchingHeaders() {
    return matchingHeaders;
  }

  @Override
  public HttpServerFactory httpServerFactory() {
    return httpServerFactory;
  }

  @Override
  public HttpClientFactory httpClientFactory() {
    return httpClientFactory;
  }

  @Override
  public boolean shouldPreserveHostHeader() {
    return preserveHostHeader;
  }

  @Override
  public boolean shouldPreserveUserAgentProxyHeader() {
    return preserveUserAgentProxyHeader;
  }

  @Override
  public String proxyHostHeader() {
    return proxyHostHeader;
  }

  @Override
  public ExtensionDeclarations getDeclaredExtensions() {
    return extensions;
  }

  @Override
  public boolean isExtensionScanningEnabled() {
    return extensionScanningEnabled;
  }

  @Override
  public WiremockNetworkTrafficListener networkTrafficListener() {
    return networkTrafficListener;
  }

  @Override
  public Authenticator getAdminAuthenticator() {
    return adminAuthenticator;
  }

  @Override
  public boolean getHttpsRequiredForAdminApi() {
    return requireHttpsForAdminApi;
  }

  @Override
  public Function<Extensions, NotMatchedRenderer> getNotMatchedRendererFactory() {
    return notMatchedRendererFactory;
  }

  @Override
  public AsynchronousResponseSettings getAsynchronousResponseSettings() {
    return new AsynchronousResponseSettings(
        asynchronousResponseEnabled, asynchronousResponseThreads);
  }

  @Override
  public ChunkedEncodingPolicy getChunkedEncodingPolicy() {
    return chunkedEncodingPolicy;
  }

  @Override
  public boolean getGzipDisabled() {
    return gzipDisabled;
  }

  @Override
  public boolean getStubRequestLoggingDisabled() {
    return stubLoggingDisabled;
  }

  @Override
  public boolean getStubCorsEnabled() {
    return stubCorsEnabled;
  }

  @Override
  public long timeout() {
    return asyncResponseTimeout;
  }

  @Override
  public boolean getDisableOptimizeXmlFactoriesLoading() {
    return disableOptimizeXmlFactoriesLoading;
  }

  @Override
  public boolean getDisableStrictHttpHeaders() {
    return disableStrictHttpHeaders;
  }

  @Override
  public DataTruncationSettings getDataTruncationSettings() {
    return new DataTruncationSettings(responseBodySizeLimit);
  }

  public WireMockConfiguration disableStrictHttpHeaders(boolean disableStrictHttpHeaders) {
    this.disableStrictHttpHeaders = disableStrictHttpHeaders;
    return this;
  }

  @Override
  public BrowserProxySettings browserProxySettings() {
    KeyStoreSettings keyStoreSettings =
        caKeyStoreSettings != null
            ? caKeyStoreSettings
            : new KeyStoreSettings(
                KeyStoreSourceFactory.getAppropriateForJreVersion(
                    caKeystorePath, caKeystoreType, caKeystorePassword.toCharArray()));

    return new BrowserProxySettings.Builder()
        .enabled(browserProxyingEnabled)
        .trustAllProxyTargets(trustAllProxyTargets)
        .trustedProxyTargets(trustedProxyTargets)
        .caKeyStoreSettings(keyStoreSettings)
        .build();
  }

  @Override
  public NetworkAddressRules getProxyTargetRules() {
    return proxyTargetRules;
  }

  @Override
  public int proxyTimeout() {
    return proxyTimeout;
  }

  @Override
  public int getMaxHttpClientConnections() {
    return maxHttpClientConnections;
  }

  @Override
  public boolean getDisableConnectionReuse() {
    return disableConnectionReuse;
  }

  @Override
  public boolean getResponseTemplatingEnabled() {
    return templatingEnabled;
  }

  @Override
  public boolean getResponseTemplatingGlobal() {
    return globalTemplating;
  }

  @Override
  public Long getMaxTemplateCacheEntries() {
    return maxTemplateCacheEntries;
  }

  @Override
  public Set<String> getTemplatePermittedSystemKeys() {
    return permittedSystemKeys;
  }

  @Override
  public boolean getTemplateEscapingDisabled() {
    return templateEscapingDisabled;
  }

  @Override
  public Set<String> getSupportedProxyEncodings() {
    return supportedProxyEncodings;
  }

  @Override
  public int getWebhookThreadPoolSize() {
    return webhookThreadPoolSize;
  }
}
