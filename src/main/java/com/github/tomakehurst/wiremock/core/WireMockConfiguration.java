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

import com.github.tomakehurst.wiremock.common.AsynchronousResponseSettings;
import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.ClasspathFileSource;
import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.common.Limit;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
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

/** The type Wire mock configuration. */
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
  private HttpServerFactory httpServerFactory = new JettyHttpServerFactory();
  private HttpClientFactory httpClientFactory = new ApacheHttpClientFactory();
  private ThreadPoolFactory threadPoolFactory;
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

  /**
   * Wire mock config wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public static WireMockConfiguration wireMockConfig() {
    return new WireMockConfiguration();
  }

  /**
   * Options wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public static WireMockConfiguration options() {
    return wireMockConfig();
  }

  /**
   * Proxy pass through wire mock configuration.
   *
   * @param proxyPassThrough the proxy pass through
   * @return the wire mock configuration
   */
  public WireMockConfiguration proxyPassThrough(boolean proxyPassThrough) {
    this.proxyPassThrough = proxyPassThrough;
    GlobalSettings newSettings =
        getStores().getSettingsStore().get().copy().proxyPassThrough(proxyPassThrough).build();
    getStores().getSettingsStore().set(newSettings);
    return this;
  }

  /**
   * Timeout wire mock configuration.
   *
   * @param timeout the timeout
   * @return the wire mock configuration
   */
  public WireMockConfiguration timeout(int timeout) {
    this.asyncResponseTimeout = timeout;
    return this;
  }

  /**
   * Port wire mock configuration.
   *
   * @param portNumber the port number
   * @return the wire mock configuration
   */
  public WireMockConfiguration port(int portNumber) {
    this.portNumber = portNumber;
    return this;
  }

  /**
   * Filename template wire mock configuration.
   *
   * @param filenameTemplate the filename template
   * @return the wire mock configuration
   */
  public WireMockConfiguration filenameTemplate(String filenameTemplate) {
    this.filenameMaker = new FilenameMaker(filenameTemplate);
    return this;
  }

  /**
   * Dynamic port wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public WireMockConfiguration dynamicPort() {
    this.portNumber = DYNAMIC_PORT;
    return this;
  }

  /**
   * Http disabled wire mock configuration.
   *
   * @param httpDisabled the http disabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration httpDisabled(boolean httpDisabled) {
    this.httpDisabled = httpDisabled;
    return this;
  }

  /**
   * Http 2 plain disabled wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration http2PlainDisabled(boolean enabled) {
    this.http2PlainDisabled = enabled;
    return this;
  }

  /**
   * Http 2 tls disabled wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration http2TlsDisabled(boolean enabled) {
    this.http2TlsDisabled = enabled;
    return this;
  }

  /**
   * Https port wire mock configuration.
   *
   * @param httpsPort the https port
   * @return the wire mock configuration
   */
  public WireMockConfiguration httpsPort(Integer httpsPort) {
    this.httpsPort = httpsPort;
    return this;
  }

  /**
   * Dynamic https port wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public WireMockConfiguration dynamicHttpsPort() {
    this.httpsPort = DYNAMIC_PORT;
    return this;
  }

  /**
   * Container threads wire mock configuration.
   *
   * @param containerThreads the container threads
   * @return the wire mock configuration
   */
  public WireMockConfiguration containerThreads(Integer containerThreads) {
    this.containerThreads = containerThreads;
    return this;
  }

  /**
   * Jetty acceptors wire mock configuration.
   *
   * @param jettyAcceptors the jetty acceptors
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyAcceptors(Integer jettyAcceptors) {
    this.jettyAcceptors = jettyAcceptors;
    return this;
  }

  /**
   * Jetty accept queue size wire mock configuration.
   *
   * @param jettyAcceptQueueSize the jetty accept queue size
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyAcceptQueueSize(Integer jettyAcceptQueueSize) {
    this.jettyAcceptQueueSize = jettyAcceptQueueSize;
    return this;
  }

  /**
   * Jetty header buffer size wire mock configuration.
   *
   * @param jettyHeaderBufferSize the jetty header buffer size
   * @return the wire mock configuration
   */
  @Deprecated
  public WireMockConfiguration jettyHeaderBufferSize(Integer jettyHeaderBufferSize) {
    this.jettyHeaderBufferSize = jettyHeaderBufferSize;
    return this;
  }

  /**
   * Jetty header request size wire mock configuration.
   *
   * @param jettyHeaderRequestSize the jetty header request size
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyHeaderRequestSize(Integer jettyHeaderRequestSize) {
    this.jettyHeaderRequestSize = jettyHeaderRequestSize;
    return this;
  }

  /**
   * Jetty header response size wire mock configuration.
   *
   * @param jettyHeaderResponseSize the jetty header response size
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyHeaderResponseSize(Integer jettyHeaderResponseSize) {
    this.jettyHeaderResponseSize = jettyHeaderResponseSize;
    return this;
  }

  /**
   * Jetty stop timeout wire mock configuration.
   *
   * @param jettyStopTimeout the jetty stop timeout
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyStopTimeout(Long jettyStopTimeout) {
    this.jettyStopTimeout = jettyStopTimeout;
    return this;
  }

  /**
   * Jetty idle timeout wire mock configuration.
   *
   * @param jettyIdleTimeout the jetty idle timeout
   * @return the wire mock configuration
   */
  public WireMockConfiguration jettyIdleTimeout(Long jettyIdleTimeout) {
    this.jettyIdleTimeout = jettyIdleTimeout;
    return this;
  }

  /**
   * Keystore path wire mock configuration.
   *
   * @param path the path
   * @return the wire mock configuration
   */
  public WireMockConfiguration keystorePath(String path) {
    this.keyStorePath = path;
    return this;
  }

  /**
   * Keystore password wire mock configuration.
   *
   * @param keyStorePassword the key store password
   * @return the wire mock configuration
   */
  public WireMockConfiguration keystorePassword(String keyStorePassword) {
    this.keyStorePassword = keyStorePassword;
    return this;
  }

  /**
   * Key manager password wire mock configuration.
   *
   * @param keyManagerPassword the key manager password
   * @return the wire mock configuration
   */
  public WireMockConfiguration keyManagerPassword(String keyManagerPassword) {
    this.keyManagerPassword = keyManagerPassword;
    return this;
  }

  /**
   * Ca keystore path wire mock configuration.
   *
   * @param path the path
   * @return the wire mock configuration
   */
  public WireMockConfiguration caKeystorePath(String path) {
    this.caKeystorePath = path;
    return this;
  }

  /**
   * Trust store path wire mock configuration.
   *
   * @param truststorePath the truststore path
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustStorePath(String truststorePath) {
    this.trustStorePath = truststorePath;
    return this;
  }

  /**
   * Trust store password wire mock configuration.
   *
   * @param trustStorePassword the trust store password
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustStorePassword(String trustStorePassword) {
    this.trustStorePassword = trustStorePassword;
    return this;
  }

  /**
   * Trust store type wire mock configuration.
   *
   * @param trustStoreType the trust store type
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustStoreType(String trustStoreType) {
    this.trustStoreType = trustStoreType;
    return this;
  }

  /**
   * Need client auth wire mock configuration.
   *
   * @param needClientAuth the need client auth
   * @return the wire mock configuration
   */
  public WireMockConfiguration needClientAuth(boolean needClientAuth) {
    this.needClientAuth = needClientAuth;
    return this;
  }

  /**
   * Enable browser proxying wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration enableBrowserProxying(boolean enabled) {
    this.browserProxyingEnabled = enabled;
    return this;
  }

  /**
   * Proxy via wire mock configuration.
   *
   * @param host the host
   * @param port the port
   * @return the wire mock configuration
   */
  public WireMockConfiguration proxyVia(String host, int port) {
    this.proxySettings = new ProxySettings(host, port);
    return this;
  }

  /**
   * Proxy via wire mock configuration.
   *
   * @param proxySettings the proxy settings
   * @return the wire mock configuration
   */
  public WireMockConfiguration proxyVia(ProxySettings proxySettings) {
    this.proxySettings = proxySettings;
    return this;
  }

  /**
   * With stores wire mock configuration.
   *
   * @param stores the stores
   * @return the wire mock configuration
   */
  public WireMockConfiguration withStores(Stores stores) {
    this.stores = stores;
    return this;
  }

  /**
   * With root directory wire mock configuration.
   *
   * @param path the path
   * @return the wire mock configuration
   */
  public WireMockConfiguration withRootDirectory(String path) {
    this.filesRoot = new SingleRootFileSource(path);
    return this;
  }

  /**
   * Using files under directory wire mock configuration.
   *
   * @param path the path
   * @return the wire mock configuration
   */
  public WireMockConfiguration usingFilesUnderDirectory(String path) {
    return withRootDirectory(path);
  }

  /**
   * Using files under classpath wire mock configuration.
   *
   * @param path the path
   * @return the wire mock configuration
   */
  public WireMockConfiguration usingFilesUnderClasspath(String path) {
    fileSource(new ClasspathFileSource(path));
    return this;
  }

  /**
   * File source wire mock configuration.
   *
   * @param fileSource the file source
   * @return the wire mock configuration
   */
  public WireMockConfiguration fileSource(FileSource fileSource) {
    this.filesRoot = fileSource;
    return this;
  }

  /**
   * Mapping source wire mock configuration.
   *
   * @param mappingsSource the mappings source
   * @return the wire mock configuration
   */
  public WireMockConfiguration mappingSource(MappingsSource mappingsSource) {
    this.mappingsSource = mappingsSource;
    return this;
  }

  /**
   * Notifier wire mock configuration.
   *
   * @param notifier the notifier
   * @return the wire mock configuration
   */
  public WireMockConfiguration notifier(Notifier notifier) {
    this.notifier = notifier;
    return this;
  }

  /**
   * Bind address wire mock configuration.
   *
   * @param bindAddress the bind address
   * @return the wire mock configuration
   */
  public WireMockConfiguration bindAddress(String bindAddress) {
    this.bindAddress = bindAddress;
    return this;
  }

  /**
   * Disable request journal wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public WireMockConfiguration disableRequestJournal() {
    requestJournalDisabled = true;
    return this;
  }

  /**
   * Max request journal entries wire mock configuration.
   *
   * @param maxRequestJournalEntries the max request journal entries
   * @return the wire mock configuration
   */
  @Deprecated
  /**
   * @deprecated use {@link #maxRequestJournalEntries(int)} instead
   */
  public WireMockConfiguration maxRequestJournalEntries(
      Optional<Integer> maxRequestJournalEntries) {
    this.maxRequestJournalEntries = maxRequestJournalEntries;
    return this;
  }

  /**
   * Max request journal entries wire mock configuration.
   *
   * @param maxRequestJournalEntries the max request journal entries
   * @return the wire mock configuration
   */
  public WireMockConfiguration maxRequestJournalEntries(int maxRequestJournalEntries) {
    this.maxRequestJournalEntries = Optional.of(maxRequestJournalEntries);
    return this;
  }

  /**
   * Record request headers for matching wire mock configuration.
   *
   * @param headers the headers
   * @return the wire mock configuration
   */
  public WireMockConfiguration recordRequestHeadersForMatching(List<String> headers) {
    this.matchingHeaders =
        headers.stream().map(TO_CASE_INSENSITIVE_KEYS).collect(Collectors.toUnmodifiableList());
    return this;
  }

  /**
   * Preserve host header wire mock configuration.
   *
   * @param preserveHostHeader the preserve host header
   * @return the wire mock configuration
   */
  public WireMockConfiguration preserveHostHeader(boolean preserveHostHeader) {
    this.preserveHostHeader = preserveHostHeader;
    return this;
  }

  /**
   * Preserve user agent proxy header wire mock configuration.
   *
   * @param preserveUserAgentProxyHeader the preserve user agent proxy header
   * @return the wire mock configuration
   */
  public WireMockConfiguration preserveUserAgentProxyHeader(boolean preserveUserAgentProxyHeader) {
    this.preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
    return this;
  }

  /**
   * Proxy host header wire mock configuration.
   *
   * @param hostHeaderValue the host header value
   * @return the wire mock configuration
   */
  public WireMockConfiguration proxyHostHeader(String hostHeaderValue) {
    this.proxyHostHeader = hostHeaderValue;
    return this;
  }

  /**
   * Extensions wire mock configuration.
   *
   * @param classNames the class names
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensions(String... classNames) {
    extensions.add(classNames);
    return this;
  }

  /**
   * Extensions wire mock configuration.
   *
   * @param extensionInstances the extension instances
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensions(Extension... extensionInstances) {
    extensions.add(extensionInstances);
    return this;
  }

  /**
   * Extension factories wire mock configuration.
   *
   * @param extensionFactories the extension factories
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensionFactories(ExtensionFactory... extensionFactories) {
    return extensions(extensionFactories);
  }

  /**
   * Extensions wire mock configuration.
   *
   * @param extensionFactories the extension factories
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensions(ExtensionFactory... extensionFactories) {
    extensions.add(extensionFactories);
    return this;
  }

  /**
   * Extensions wire mock configuration.
   *
   * @param classes the classes
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensions(Class<? extends Extension>... classes) {
    extensions.add(classes);
    return this;
  }

  /**
   * Extension factories wire mock configuration.
   *
   * @param factoryClasses the factory classes
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensionFactories(
      Class<? extends ExtensionFactory>... factoryClasses) {
    extensions.addFactories(factoryClasses);
    return this;
  }

  /**
   * Extension scanning enabled wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration extensionScanningEnabled(boolean enabled) {
    this.extensionScanningEnabled = enabled;
    return this;
  }

  /**
   * Http server factory wire mock configuration.
   *
   * @param serverFactory the server factory
   * @return the wire mock configuration
   */
  public WireMockConfiguration httpServerFactory(HttpServerFactory serverFactory) {
    this.httpServerFactory = serverFactory;
    return this;
  }

  /**
   * Http client factory wire mock configuration.
   *
   * @param httpClientFactory the http client factory
   * @return the wire mock configuration
   */
  public WireMockConfiguration httpClientFactory(HttpClientFactory httpClientFactory) {
    this.httpClientFactory = httpClientFactory;
    return this;
  }

  /**
   * Thread pool factory wire mock configuration.
   *
   * @param threadPoolFactory the thread pool factory
   * @return the wire mock configuration
   */
  public WireMockConfiguration threadPoolFactory(ThreadPoolFactory threadPoolFactory) {
    this.threadPoolFactory = threadPoolFactory;
    return this;
  }

  /**
   * Network traffic listener wire mock configuration.
   *
   * @param networkTrafficListener the network traffic listener
   * @return the wire mock configuration
   */
  public WireMockConfiguration networkTrafficListener(
      WiremockNetworkTrafficListener networkTrafficListener) {
    this.networkTrafficListener = networkTrafficListener;
    return this;
  }

  /**
   * Admin authenticator wire mock configuration.
   *
   * @param authenticator the authenticator
   * @return the wire mock configuration
   */
  public WireMockConfiguration adminAuthenticator(Authenticator authenticator) {
    this.adminAuthenticator = authenticator;
    return this;
  }

  /**
   * Basic admin authenticator wire mock configuration.
   *
   * @param username the username
   * @param password the password
   * @return the wire mock configuration
   */
  public WireMockConfiguration basicAdminAuthenticator(String username, String password) {
    return adminAuthenticator(new BasicAuthenticator(username, password));
  }

  /**
   * Require https for admin api wire mock configuration.
   *
   * @return the wire mock configuration
   */
  public WireMockConfiguration requireHttpsForAdminApi() {
    this.requireHttpsForAdminApi = true;
    return this;
  }

  /**
   * Not matched renderer factory wire mock configuration.
   *
   * @param notMatchedRendererFactory the not matched renderer factory
   * @return the wire mock configuration
   */
  public WireMockConfiguration notMatchedRendererFactory(
      Function<Extensions, NotMatchedRenderer> notMatchedRendererFactory) {
    this.notMatchedRendererFactory = notMatchedRendererFactory;
    return this;
  }

  /**
   * Asynchronous response enabled wire mock configuration.
   *
   * @param asynchronousResponseEnabled the asynchronous response enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration asynchronousResponseEnabled(boolean asynchronousResponseEnabled) {
    this.asynchronousResponseEnabled = asynchronousResponseEnabled;
    return this;
  }

  /**
   * Asynchronous response threads wire mock configuration.
   *
   * @param asynchronousResponseThreads the asynchronous response threads
   * @return the wire mock configuration
   */
  public WireMockConfiguration asynchronousResponseThreads(int asynchronousResponseThreads) {
    this.asynchronousResponseThreads = asynchronousResponseThreads;
    return this;
  }

  /**
   * Use chunked transfer encoding wire mock configuration.
   *
   * @param policy the policy
   * @return the wire mock configuration
   */
  public WireMockConfiguration useChunkedTransferEncoding(ChunkedEncodingPolicy policy) {
    this.chunkedEncodingPolicy = policy;
    return this;
  }

  /**
   * Gzip disabled wire mock configuration.
   *
   * @param gzipDisabled the gzip disabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration gzipDisabled(boolean gzipDisabled) {
    this.gzipDisabled = gzipDisabled;
    return this;
  }

  /**
   * Stub request logging disabled wire mock configuration.
   *
   * @param disabled the disabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration stubRequestLoggingDisabled(boolean disabled) {
    this.stubLoggingDisabled = disabled;
    return this;
  }

  /**
   * Stub cors enabled wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration stubCorsEnabled(boolean enabled) {
    this.stubCorsEnabled = enabled;
    return this;
  }

  /**
   * Trust all proxy targets wire mock configuration.
   *
   * @param enabled the enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustAllProxyTargets(boolean enabled) {
    this.trustAllProxyTargets = enabled;
    return this;
  }

  /**
   * Trusted proxy targets wire mock configuration.
   *
   * @param trustedProxyTargets the trusted proxy targets
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustedProxyTargets(String... trustedProxyTargets) {
    return trustedProxyTargets(asList(trustedProxyTargets));
  }

  /**
   * Trusted proxy targets wire mock configuration.
   *
   * @param trustedProxyTargets the trusted proxy targets
   * @return the wire mock configuration
   */
  public WireMockConfiguration trustedProxyTargets(List<String> trustedProxyTargets) {
    this.trustedProxyTargets.addAll(trustedProxyTargets);
    return this;
  }

  /**
   * Disable optimize xml factories loading wire mock configuration.
   *
   * @param disableOptimizeXmlFactoriesLoading the disable optimize xml factories loading
   * @return the wire mock configuration
   */
  public WireMockConfiguration disableOptimizeXmlFactoriesLoading(
      boolean disableOptimizeXmlFactoriesLoading) {
    this.disableOptimizeXmlFactoriesLoading = disableOptimizeXmlFactoriesLoading;
    return this;
  }

  /**
   * Max logged response size wire mock configuration.
   *
   * @param maxSize the max size
   * @return the wire mock configuration
   */
  public WireMockConfiguration maxLoggedResponseSize(int maxSize) {
    this.responseBodySizeLimit = new Limit(maxSize);
    return this;
  }

  /**
   * Limit proxy targets wire mock configuration.
   *
   * @param proxyTargetRules the proxy target rules
   * @return the wire mock configuration
   */
  public WireMockConfiguration limitProxyTargets(NetworkAddressRules proxyTargetRules) {
    this.proxyTargetRules = proxyTargetRules;
    return this;
  }

  /**
   * Proxy timeout wire mock configuration.
   *
   * @param proxyTimeout the proxy timeout
   * @return the wire mock configuration
   */
  public WireMockConfiguration proxyTimeout(int proxyTimeout) {
    this.proxyTimeout = proxyTimeout;
    return this;
  }

  /**
   * Max http client connections wire mock configuration.
   *
   * @param maxHttpClientConnections the max http client connections
   * @return the wire mock configuration
   */
  public WireMockConfiguration maxHttpClientConnections(int maxHttpClientConnections) {
    this.maxHttpClientConnections = maxHttpClientConnections;
    return this;
  }

  /**
   * Disable connection reuse wire mock configuration.
   *
   * @param disableConnectionReuse the disable connection reuse
   * @return the wire mock configuration
   */
  public WireMockConfiguration disableConnectionReuse(boolean disableConnectionReuse) {
    this.disableConnectionReuse = disableConnectionReuse;
    return this;
  }

  /**
   * Templating enabled wire mock configuration.
   *
   * @param templatingEnabled the templating enabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration templatingEnabled(boolean templatingEnabled) {
    this.templatingEnabled = templatingEnabled;
    return this;
  }

  /**
   * Global templating wire mock configuration.
   *
   * @param globalTemplating the global templating
   * @return the wire mock configuration
   */
  public WireMockConfiguration globalTemplating(boolean globalTemplating) {
    this.globalTemplating = globalTemplating;
    return this;
  }

  /**
   * With permitted system keys wire mock configuration.
   *
   * @param systemKeys the system keys
   * @return the wire mock configuration
   */
  public WireMockConfiguration withPermittedSystemKeys(String... systemKeys) {
    this.permittedSystemKeys = Set.of(systemKeys);
    return this;
  }

  /**
   * With template escaping disabled wire mock configuration.
   *
   * @param templateEscapingDisabled the template escaping disabled
   * @return the wire mock configuration
   */
  public WireMockConfiguration withTemplateEscapingDisabled(boolean templateEscapingDisabled) {
    this.templateEscapingDisabled = templateEscapingDisabled;
    return this;
  }

  /**
   * With max template cache entries wire mock configuration.
   *
   * @param maxTemplateCacheEntries the max template cache entries
   * @return the wire mock configuration
   */
  public WireMockConfiguration withMaxTemplateCacheEntries(Long maxTemplateCacheEntries) {
    this.maxTemplateCacheEntries = maxTemplateCacheEntries;
    return this;
  }

  /**
   * With supported proxy encodings wire mock configuration.
   *
   * @param supportedProxyEncodings the supported proxy encodings
   * @return the wire mock configuration
   */
  public WireMockConfiguration withSupportedProxyEncodings(Set<String> supportedProxyEncodings) {
    this.supportedProxyEncodings = supportedProxyEncodings;
    return this;
  }

  /**
   * With supported proxy encodings wire mock configuration.
   *
   * @param supportedProxyEncodings the supported proxy encodings
   * @return the wire mock configuration
   */
  public WireMockConfiguration withSupportedProxyEncodings(String... supportedProxyEncodings) {
    return withSupportedProxyEncodings(Set.of(supportedProxyEncodings));
  }

  /**
   * With webhook thread pool size wire mock configuration.
   *
   * @param webhookThreadPoolSize the webhook thread pool size
   * @return the wire mock configuration
   */
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
    return JettySettings.Builder.ajettysettings()
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
  public boolean hasDefaultHttpServerFactory() {
    return httpServerFactory.getClass().equals(JettyHttpServerFactory.class);
  }

  @Override
  public HttpClientFactory httpClientFactory() {
    return httpClientFactory;
  }

  @Override
  public ThreadPoolFactory threadPoolFactory() {
    return threadPoolFactory;
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

  /**
   * Disable strict http headers wire mock configuration.
   *
   * @param disableStrictHttpHeaders the disable strict http headers
   * @return the wire mock configuration
   */
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
