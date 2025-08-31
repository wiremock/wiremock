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
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.client.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.DoNothingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.jetty.JettyHttpServerFactory;
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

  private long _asyncResponseTimeout = DEFAULT_TIMEOUT;
  private boolean _disableOptimizeXmlFactoriesLoading = false;
  private int _portNumber = DEFAULT_PORT;
  private boolean _httpDisabled = false;
  private boolean _http2PlainDisabled = false;
  private boolean _http2TlsDisabled = false;
  private String _bindAddress = DEFAULT_BIND_ADDRESS;

  private int _containerThreads = DEFAULT_CONTAINER_THREADS;

  private int _httpsPort = -1;
  private String _keyStorePath = getResource(WireMockConfiguration.class, "keystore").toString();
  private String _keyStorePassword = "password";
  private String _keyManagerPassword = "password";
  private String _keyStoreType = "JKS";
  private String _trustStorePath;
  private String _trustStorePassword = "password";
  private String _trustStoreType = "JKS";
  private boolean _needClientAuth;

  private boolean _browserProxyingEnabled = false;
  private String _caKeystorePath = DEFAULT_CA_KEYSTORE_PATH;
  private String _caKeystorePassword = DEFAULT_CA_KESTORE_PASSWORD;
  private String _caKeystoreType = "JKS";
  private KeyStoreSettings _caKeyStoreSettings = null;
  private boolean _trustAllProxyTargets = false;
  private final List<String> _trustedProxyTargets = new ArrayList<>();

  private ProxySettings _proxySettings = ProxySettings.NO_PROXY;
  private FileSource _filesRoot = new SingleRootFileSource("src/test/resources");
  private Stores _stores;
  private MappingsSource _mappingsSource;
  private FilenameMaker _filenameMaker;

  private Notifier _notifier = new Slf4jNotifier(false);
  private boolean _requestJournalDisabled = false;
  private Optional<Integer> _maxRequestJournalEntries = Optional.empty();
  private List<CaseInsensitiveKey> _matchingHeaders = emptyList();

  private boolean _preserveHostHeader;
  private boolean _preserveUserAgentProxyHeader;
  private String _proxyHostHeader;
  private HttpServerFactory _httpServerFactory = new JettyHttpServerFactory();
  private HttpClientFactory _httpClientFactory = new ApacheHttpClientFactory();
  private ThreadPoolFactory _threadPoolFactory;
  private Integer _jettyAcceptors;
  private Integer _jettyAcceptQueueSize;
  private Integer _jettyHeaderBufferSize;
  private Integer _jettyHeaderRequestSize;
  private Integer _jettyHeaderResponseSize;
  private Long _jettyStopTimeout;
  private Long _jettyIdleTimeout;

  private ExtensionDeclarations _extensions = new ExtensionDeclarations();
  private boolean _extensionScanningEnabled = false;
  private WiremockNetworkTrafficListener _networkTrafficListener =
          new DoNothingWiremockNetworkTrafficListener();

  private Authenticator _adminAuthenticator = new NoAuthenticator();
  private boolean _requireHttpsForAdminApi = false;

  private Function<Extensions, NotMatchedRenderer> _notMatchedRendererFactory =
          PlainTextStubNotMatchedRenderer::new;
  private boolean _asynchronousResponseEnabled;
  private int _asynchronousResponseThreads;
  private ChunkedEncodingPolicy _chunkedEncodingPolicy;
  private boolean _gzipDisabled = false;
  private boolean _stubLoggingDisabled = false;

  private boolean _stubCorsEnabled = false;
  private boolean _disableStrictHttpHeaders;

  private boolean _proxyPassThrough = true;

  private Limit _responseBodySizeLimit = UNLIMITED;

  private NetworkAddressRules _proxyTargetRules = NetworkAddressRules.ALLOW_ALL;

  private int _proxyTimeout = DEFAULT_TIMEOUT;

  private int _maxHttpClientConnections = DEFAULT_MAX_HTTP_CONNECTIONS;
  private boolean _disableConnectionReuse = DEFAULT_DISABLE_CONNECTION_REUSE;

  private boolean _templatingEnabled = true;
  private boolean _globalTemplating = false;
  private Set<String> _permittedSystemKeys = null;
  private Long _maxTemplateCacheEntries = DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES;
  private boolean _templateEscapingDisabled = true;

  private Set<String> _supportedProxyEncodings = null;

  private int _webhookThreadPoolSize = DEFAULT_WEBHOOK_THREADPOOL_SIZE;

  private MappingsSource get_mappingsSource() {
    if (_mappingsSource == null) {
      _mappingsSource =
              new JsonFileMappingsSource(_filesRoot.child(MAPPINGS_ROOT), get_filenameMaker());
    }

    return _mappingsSource;
  }

  public static WireMockConfiguration wireMockConfig() {
    return new WireMockConfiguration();
  }

  public static WireMockConfiguration options() {
    return wireMockConfig();
  }

  public WireMockConfiguration proxyPassThrough(boolean proxyPassThrough) {
    this._proxyPassThrough = proxyPassThrough;
    GlobalSettings newSettings =
            get_stores().getSettingsStore().get().copy().proxyPassThrough(proxyPassThrough).build();
    get_stores().getSettingsStore().set(newSettings);
    return this;
  }

  public WireMockConfiguration timeout(int timeout) {
    this._asyncResponseTimeout = timeout;
    return this;
  }

  public WireMockConfiguration port(int portNumber) {
    this._portNumber = portNumber;
    return this;
  }

  public WireMockConfiguration filenameTemplate(String filenameTemplate) {
    this._filenameMaker = new FilenameMaker(filenameTemplate);
    return this;
  }

  public WireMockConfiguration dynamicPort() {
    this._portNumber = DYNAMIC_PORT;
    return this;
  }

  public WireMockConfiguration httpDisabled(boolean httpDisabled) {
    this._httpDisabled = httpDisabled;
    return this;
  }

  public WireMockConfiguration http2PlainDisabled(boolean enabled) {
    this._http2PlainDisabled = enabled;
    return this;
  }

  public WireMockConfiguration http2TlsDisabled(boolean enabled) {
    this._http2TlsDisabled = enabled;
    return this;
  }

  public WireMockConfiguration httpsPort(Integer httpsPort) {
    this._httpsPort = httpsPort;
    return this;
  }

  public WireMockConfiguration dynamicHttpsPort() {
    this._httpsPort = DYNAMIC_PORT;
    return this;
  }

  public WireMockConfiguration containerThreads(Integer containerThreads) {
    this._containerThreads = containerThreads;
    return this;
  }

  public WireMockConfiguration jettyAcceptors(Integer jettyAcceptors) {
    this._jettyAcceptors = jettyAcceptors;
    return this;
  }

  public WireMockConfiguration jettyAcceptQueueSize(Integer jettyAcceptQueueSize) {
    this._jettyAcceptQueueSize = jettyAcceptQueueSize;
    return this;
  }

  @Deprecated
  public WireMockConfiguration jettyHeaderBufferSize(Integer jettyHeaderBufferSize) {
    this._jettyHeaderBufferSize = jettyHeaderBufferSize;
    return this;
  }

  public WireMockConfiguration jettyHeaderRequestSize(Integer jettyHeaderRequestSize) {
    this._jettyHeaderRequestSize = jettyHeaderRequestSize;
    return this;
  }

  public WireMockConfiguration jettyHeaderResponseSize(Integer jettyHeaderResponseSize) {
    this._jettyHeaderResponseSize = jettyHeaderResponseSize;
    return this;
  }

  public WireMockConfiguration jettyStopTimeout(Long jettyStopTimeout) {
    this._jettyStopTimeout = jettyStopTimeout;
    return this;
  }

  public WireMockConfiguration jettyIdleTimeout(Long jettyIdleTimeout) {
    this._jettyIdleTimeout = jettyIdleTimeout;
    return this;
  }

  public WireMockConfiguration keystorePath(String path) {
    this._keyStorePath = path;
    return this;
  }

  public WireMockConfiguration keystorePassword(String keyStorePassword) {
    this._keyStorePassword = keyStorePassword;
    return this;
  }

  public WireMockConfiguration keyManagerPassword(String keyManagerPassword) {
    this._keyManagerPassword = keyManagerPassword;
    return this;
  }

  public WireMockConfiguration keystoreType(String keyStoreType) {
    this._keyStoreType = keyStoreType;
    return this;
  }

  public WireMockConfiguration caKeystoreSettings(KeyStoreSettings caKeyStoreSettings) {
    this._caKeyStoreSettings = caKeyStoreSettings;
    return this;
  }

  public WireMockConfiguration caKeystorePath(String path) {
    this._caKeystorePath = path;
    return this;
  }

  public WireMockConfiguration caKeystorePassword(String keyStorePassword) {
    this._caKeystorePassword = keyStorePassword;
    return this;
  }

  public WireMockConfiguration caKeystoreType(String caKeystoreType) {
    this._caKeystoreType = caKeystoreType;
    return this;
  }

  public WireMockConfiguration trustStorePath(String truststorePath) {
    this._trustStorePath = truststorePath;
    return this;
  }

  public WireMockConfiguration trustStorePassword(String trustStorePassword) {
    this._trustStorePassword = trustStorePassword;
    return this;
  }

  public WireMockConfiguration trustStoreType(String trustStoreType) {
    this._trustStoreType = trustStoreType;
    return this;
  }

  public WireMockConfiguration needClientAuth(boolean needClientAuth) {
    this._needClientAuth = needClientAuth;
    return this;
  }

  public WireMockConfiguration enableBrowserProxying(boolean enabled) {
    this._browserProxyingEnabled = enabled;
    return this;
  }

  public WireMockConfiguration proxyVia(String host, int port) {
    this._proxySettings = new ProxySettings(host, port);
    return this;
  }

  public WireMockConfiguration proxyVia(ProxySettings proxySettings) {
    this._proxySettings = proxySettings;
    return this;
  }

  public WireMockConfiguration withStores(Stores stores) {
    this._stores = stores;
    return this;
  }

  public WireMockConfiguration withRootDirectory(String path) {
    this._filesRoot = new SingleRootFileSource(path);
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
    this._filesRoot = fileSource;
    return this;
  }

  public WireMockConfiguration mappingSource(MappingsSource mappingsSource) {
    this._mappingsSource = mappingsSource;
    return this;
  }

  public WireMockConfiguration notifier(Notifier notifier) {
    this._notifier = notifier;
    return this;
  }

  public WireMockConfiguration bindAddress(String bindAddress) {
    this._bindAddress = bindAddress;
    return this;
  }

  public WireMockConfiguration disableRequestJournal() {
    _requestJournalDisabled = true;
    return this;
  }

  @Deprecated
  /**
   * @deprecated use {@link #maxRequestJournalEntries(int)} instead
   */
  public WireMockConfiguration maxRequestJournalEntries(
          Optional<Integer> maxRequestJournalEntries) {
    this._maxRequestJournalEntries = maxRequestJournalEntries;
    return this;
  }

  public WireMockConfiguration maxRequestJournalEntries(int maxRequestJournalEntries) {
    this._maxRequestJournalEntries = Optional.of(maxRequestJournalEntries);
    return this;
  }

  public WireMockConfiguration recordRequestHeadersForMatching(List<String> headers) {
    this._matchingHeaders =
            headers.stream().map(TO_CASE_INSENSITIVE_KEYS).collect(Collectors.toUnmodifiableList());
    return this;
  }

  public WireMockConfiguration preserveHostHeader(boolean preserveHostHeader) {
    this._preserveHostHeader = preserveHostHeader;
    return this;
  }

  public WireMockConfiguration preserveUserAgentProxyHeader(boolean preserveUserAgentProxyHeader) {
    this._preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
    return this;
  }

  public WireMockConfiguration proxyHostHeader(String hostHeaderValue) {
    this._proxyHostHeader = hostHeaderValue;
    return this;
  }

  public WireMockConfiguration extensions(String... classNames) {
    _extensions.add(classNames);
    return this;
  }

  public WireMockConfiguration extensions(Extension... extensionInstances) {
    _extensions.add(extensionInstances);
    return this;
  }

  public WireMockConfiguration extensionFactories(ExtensionFactory... extensionFactories) {
    return extensions(extensionFactories);
  }

  public WireMockConfiguration extensions(ExtensionFactory... extensionFactories) {
    _extensions.add(extensionFactories);
    return this;
  }

  public WireMockConfiguration extensions(Class<? extends Extension>... classes) {
    _extensions.add(classes);
    return this;
  }

  public WireMockConfiguration extensionFactories(
          Class<? extends ExtensionFactory>... factoryClasses) {
    _extensions.addFactories(factoryClasses);
    return this;
  }

  public WireMockConfiguration extensionScanningEnabled(boolean enabled) {
    this._extensionScanningEnabled = enabled;
    return this;
  }

  public WireMockConfiguration httpServerFactory(HttpServerFactory serverFactory) {
    this._httpServerFactory = serverFactory;
    return this;
  }

  public WireMockConfiguration httpClientFactory(HttpClientFactory httpClientFactory) {
    this._httpClientFactory = httpClientFactory;
    return this;
  }

  public WireMockConfiguration threadPoolFactory(ThreadPoolFactory threadPoolFactory) {
    this._threadPoolFactory = threadPoolFactory;
    return this;
  }

  public WireMockConfiguration networkTrafficListener(
          WiremockNetworkTrafficListener networkTrafficListener) {
    this._networkTrafficListener = networkTrafficListener;
    return this;
  }

  public WireMockConfiguration adminAuthenticator(Authenticator authenticator) {
    this._adminAuthenticator = authenticator;
    return this;
  }

  public WireMockConfiguration basicAdminAuthenticator(String username, String password) {
    return adminAuthenticator(new BasicAuthenticator(username, password));
  }

  public WireMockConfiguration requireHttpsForAdminApi() {
    this._requireHttpsForAdminApi = true;
    return this;
  }

  public WireMockConfiguration notMatchedRendererFactory(
          Function<Extensions, NotMatchedRenderer> notMatchedRendererFactory) {
    this._notMatchedRendererFactory = notMatchedRendererFactory;
    return this;
  }

  public WireMockConfiguration asynchronousResponseEnabled(boolean asynchronousResponseEnabled) {
    this._asynchronousResponseEnabled = asynchronousResponseEnabled;
    return this;
  }

  public WireMockConfiguration asynchronousResponseThreads(int asynchronousResponseThreads) {
    this._asynchronousResponseThreads = asynchronousResponseThreads;
    return this;
  }

  public WireMockConfiguration useChunkedTransferEncoding(ChunkedEncodingPolicy policy) {
    this._chunkedEncodingPolicy = policy;
    return this;
  }

  public WireMockConfiguration gzipDisabled(boolean gzipDisabled) {
    this._gzipDisabled = gzipDisabled;
    return this;
  }

  public WireMockConfiguration stubRequestLoggingDisabled(boolean disabled) {
    this._stubLoggingDisabled = disabled;
    return this;
  }

  public WireMockConfiguration stubCorsEnabled(boolean enabled) {
    this._stubCorsEnabled = enabled;
    return this;
  }

  public WireMockConfiguration trustAllProxyTargets(boolean enabled) {
    this._trustAllProxyTargets = enabled;
    return this;
  }

  public WireMockConfiguration trustedProxyTargets(String... trustedProxyTargets) {
    return trustedProxyTargets(asList(trustedProxyTargets));
  }

  public WireMockConfiguration trustedProxyTargets(List<String> trustedProxyTargets) {
    this._trustedProxyTargets.addAll(trustedProxyTargets);
    return this;
  }

  public WireMockConfiguration disableOptimizeXmlFactoriesLoading(
          boolean disableOptimizeXmlFactoriesLoading) {
    this._disableOptimizeXmlFactoriesLoading = disableOptimizeXmlFactoriesLoading;
    return this;
  }

  public WireMockConfiguration maxLoggedResponseSize(int maxSize) {
    this._responseBodySizeLimit = new Limit(maxSize);
    return this;
  }

  public WireMockConfiguration limitProxyTargets(NetworkAddressRules proxyTargetRules) {
    this._proxyTargetRules = proxyTargetRules;
    return this;
  }

  public WireMockConfiguration proxyTimeout(int proxyTimeout) {
    this._proxyTimeout = proxyTimeout;
    return this;
  }

  public WireMockConfiguration maxHttpClientConnections(int maxHttpClientConnections) {
    this._maxHttpClientConnections = maxHttpClientConnections;
    return this;
  }

  public WireMockConfiguration disableConnectionReuse(boolean disableConnectionReuse) {
    this._disableConnectionReuse = disableConnectionReuse;
    return this;
  }

  public WireMockConfiguration templatingEnabled(boolean templatingEnabled) {
    this._templatingEnabled = templatingEnabled;
    return this;
  }

  public WireMockConfiguration globalTemplating(boolean globalTemplating) {
    this._globalTemplating = globalTemplating;
    return this;
  }

  public WireMockConfiguration withPermittedSystemKeys(String... systemKeys) {
    this._permittedSystemKeys = Set.of(systemKeys);
    return this;
  }

  public WireMockConfiguration withTemplateEscapingDisabled(boolean templateEscapingDisabled) {
    this._templateEscapingDisabled = templateEscapingDisabled;
    return this;
  }

  public WireMockConfiguration withMaxTemplateCacheEntries(Long maxTemplateCacheEntries) {
    this._maxTemplateCacheEntries = maxTemplateCacheEntries;
    return this;
  }

  public WireMockConfiguration withSupportedProxyEncodings(Set<String> supportedProxyEncodings) {
    this._supportedProxyEncodings = supportedProxyEncodings;
    return this;
  }

  public WireMockConfiguration withSupportedProxyEncodings(String... supportedProxyEncodings) {
    return withSupportedProxyEncodings(Set.of(supportedProxyEncodings));
  }

  public WireMockConfiguration withWebhookThreadPoolSize(Integer webhookThreadPoolSize) {
    this._webhookThreadPoolSize = webhookThreadPoolSize;
    return this;
  }

  @Override
  public int portNumber() {
    return _portNumber;
  }

  @Override
  public boolean get_httpDisabled() {
    return _httpDisabled;
  }

  @Override
  public boolean get_http2PlainDisabled() {
    return _http2PlainDisabled;
  }

  @Override
  public boolean get_http2TlsDisabled() {
    return _http2TlsDisabled;
  }

  @Override
  public int containerThreads() {
    return _containerThreads;
  }

  @Override
  public HttpsSettings httpsSettings() {
    return new HttpsSettings.Builder()
            .port(_httpsPort)
            .keyStorePath(_keyStorePath)
            .keyStorePassword(_keyStorePassword)
            .keyManagerPassword(_keyManagerPassword)
            .keyStoreType(_keyStoreType)
            .trustStorePath(_trustStorePath)
            .trustStorePassword(_trustStorePassword)
            .trustStoreType(_trustStoreType)
            .needClientAuth(_needClientAuth)
            .build();
  }

  @Override
  public JettySettings jettySettings() {
    return JettySettings.Builder.aJettySettings()
            .withAcceptors(_jettyAcceptors)
            .withAcceptQueueSize(_jettyAcceptQueueSize)
            .withRequestHeaderSize(_jettyHeaderBufferSize)
            .withRequestHeaderSize(_jettyHeaderRequestSize)
            .withResponseHeaderSize(_jettyHeaderResponseSize)
            .withStopTimeout(_jettyStopTimeout)
            .withIdleTimeout(_jettyIdleTimeout)
            .build();
  }

  @Override
  public boolean browserProxyingEnabled() {
    return _browserProxyingEnabled;
  }

  @Override
  public ProxySettings proxyVia() {
    return _proxySettings;
  }

  @Override
  public Stores get_stores() {
    if (_stores == null) {
      _stores = new DefaultStores(_filesRoot);
    }

    return _stores;
  }

  @Override
  public FileSource filesRoot() {
    return _filesRoot;
  }

  @Override
  public MappingsLoader mappingsLoader() {
    return get_mappingsSource();
  }

  @Override
  public MappingsSaver mappingsSaver() {
    return get_mappingsSource();
  }

  @Override
  public Notifier notifier() {
    return _notifier;
  }

  @Override
  public boolean requestJournalDisabled() {
    return _requestJournalDisabled;
  }

  @Override
  public Optional<Integer> maxRequestJournalEntries() {
    return _maxRequestJournalEntries;
  }

  @Override
  public String bindAddress() {
    return _bindAddress;
  }

  @Override
  public FilenameMaker get_filenameMaker() {
    return _filenameMaker;
  }

  @Override
  public List<CaseInsensitiveKey> matchingHeaders() {
    return _matchingHeaders;
  }

  @Override
  public HttpServerFactory httpServerFactory() {
    return _httpServerFactory;
  }

  @Override
  public boolean hasDefaultHttpServerFactory() {
    return _httpServerFactory.getClass().equals(JettyHttpServerFactory.class);
  }

  @Override
  public HttpClientFactory httpClientFactory() {
    return _httpClientFactory;
  }

  @Override
  public ThreadPoolFactory threadPoolFactory() {
    return _threadPoolFactory;
  }

  @Override
  public boolean shouldPreserveHostHeader() {
    return _preserveHostHeader;
  }

  @Override
  public boolean shouldPreserveUserAgentProxyHeader() {
    return _preserveUserAgentProxyHeader;
  }

  @Override
  public String proxyHostHeader() {
    return _proxyHostHeader;
  }

  @Override
  public ExtensionDeclarations getDeclaredExtensions() {
    return _extensions;
  }

  @Override
  public boolean is_extensionScanningEnabled() {
    return _extensionScanningEnabled;
  }

  @Override
  public WiremockNetworkTrafficListener networkTrafficListener() {
    return _networkTrafficListener;
  }

  @Override
  public Authenticator get_adminAuthenticator() {
    return _adminAuthenticator;
  }

  @Override
  public boolean getHttpsRequiredForAdminApi() {
    return _requireHttpsForAdminApi;
  }

  @Override
  public Function<Extensions, NotMatchedRenderer> get_notMatchedRendererFactory() {
    return _notMatchedRendererFactory;
  }

  @Override
  public AsynchronousResponseSettings getAsynchronousResponseSettings() {
    return new AsynchronousResponseSettings(
            _asynchronousResponseEnabled, _asynchronousResponseThreads);
  }

  @Override
  public ChunkedEncodingPolicy get_chunkedEncodingPolicy() {
    return _chunkedEncodingPolicy;
  }

  @Override
  public boolean get_gzipDisabled() {
    return _gzipDisabled;
  }

  @Override
  public boolean getStubRequestLoggingDisabled() {
    return _stubLoggingDisabled;
  }

  @Override
  public boolean get_stubCorsEnabled() {
    return _stubCorsEnabled;
  }

  @Override
  public long timeout() {
    return _asyncResponseTimeout;
  }

  @Override
  public boolean get_disableOptimizeXmlFactoriesLoading() {
    return _disableOptimizeXmlFactoriesLoading;
  }

  @Override
  public boolean get_disableStrictHttpHeaders() {
    return _disableStrictHttpHeaders;
  }

  @Override
  public DataTruncationSettings getDataTruncationSettings() {
    return new DataTruncationSettings(_responseBodySizeLimit);
  }

  public WireMockConfiguration disableStrictHttpHeaders(boolean disableStrictHttpHeaders) {
    this._disableStrictHttpHeaders = disableStrictHttpHeaders;
    return this;
  }

  @Override
  public BrowserProxySettings browserProxySettings() {
    KeyStoreSettings keyStoreSettings =
            _caKeyStoreSettings != null
                    ? _caKeyStoreSettings
                    : new KeyStoreSettings(
                    KeyStoreSourceFactory.getAppropriateForJreVersion(
                            _caKeystorePath, _caKeystoreType, _caKeystorePassword.toCharArray()));

    return new BrowserProxySettings.Builder()
            .enabled(_browserProxyingEnabled)
            .trustAllProxyTargets(_trustAllProxyTargets)
            .trustedProxyTargets(_trustedProxyTargets)
            .caKeyStoreSettings(keyStoreSettings)
            .build();
  }

  @Override
  public NetworkAddressRules get_proxyTargetRules() {
    return _proxyTargetRules;
  }

  @Override
  public int proxyTimeout() {
    return _proxyTimeout;
  }

  @Override
  public int get_maxHttpClientConnections() {
    return _maxHttpClientConnections;
  }

  @Override
  public boolean get_disableConnectionReuse() {
    return _disableConnectionReuse;
  }

  @Override
  public boolean getResponseTemplatingEnabled() {
    return _templatingEnabled;
  }

  @Override
  public boolean getResponseTemplatingGlobal() {
    return _globalTemplating;
  }

  @Override
  public Long get_maxTemplateCacheEntries() {
    return _maxTemplateCacheEntries;
  }

  @Override
  public Set<String> getTemplatePermittedSystemKeys() {
    return _permittedSystemKeys;
  }

  @Override
  public boolean get_templateEscapingDisabled() {
    return _templateEscapingDisabled;
  }

  @Override
  public Set<String> get_supportedProxyEncodings() {
    return _supportedProxyEncodings;
  }

  @Override
  public int get_webhookThreadPoolSize() {
    return _webhookThreadPoolSize;
  }
}
