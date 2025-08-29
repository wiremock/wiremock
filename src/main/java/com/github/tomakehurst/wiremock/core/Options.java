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

import com.github.tomakehurst.wiremock.common.AsynchronousResponseSettings;
import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.DataTruncationSettings;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.extension.ExtensionDeclarations;
import com.github.tomakehurst.wiremock.extension.Extensions;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.store.Stores;
import com.github.tomakehurst.wiremock.verification.notmatched.NotMatchedRenderer;
import com.github.tomakehurst.wiremock.verification.notmatched.PlainTextStubNotMatchedRenderer;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/** The interface Options. */
public interface Options {

  /** The enum Chunked encoding policy. */
  enum ChunkedEncodingPolicy {
    /** Always chunked encoding policy. */
    ALWAYS,
    /** Never chunked encoding policy. */
    NEVER,
    /** Body file chunked encoding policy. */
    BODY_FILE
  }

  /** The constant DEFAULT_PORT. */
  int DEFAULT_PORT = 8080;

  /** The constant DYNAMIC_PORT. */
  int DYNAMIC_PORT = 0;

  /** The constant DEFAULT_TIMEOUT. */
  int DEFAULT_TIMEOUT = 300_000;

  /** The constant DEFAULT_CONTAINER_THREADS. */
  int DEFAULT_CONTAINER_THREADS = 25;

  /** The constant DEFAULT_BIND_ADDRESS. */
  String DEFAULT_BIND_ADDRESS = "0.0.0.0";

  /** The constant DEFAULT_MAX_HTTP_CONNECTIONS. */
  int DEFAULT_MAX_HTTP_CONNECTIONS = 1000;

  /** The constant DEFAULT_WEBHOOK_THREADPOOL_SIZE. */
  int DEFAULT_WEBHOOK_THREADPOOL_SIZE = 10;

  /** The constant DEFAULT_DISABLE_CONNECTION_REUSE. */
  boolean DEFAULT_DISABLE_CONNECTION_REUSE = true;

  /** The constant DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES. */
  Long DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES = 1000L;

  /**
   * Port number int.
   *
   * @return the int
   */
  int portNumber();

  /**
   * Gets http disabled.
   *
   * @return the http disabled
   */
  boolean getHttpDisabled();

  /**
   * Gets http 2 plain disabled.
   *
   * @return the http 2 plain disabled
   */
  boolean getHttp2PlainDisabled();

  /**
   * Gets http 2 tls disabled.
   *
   * @return the http 2 tls disabled
   */
  boolean getHttp2TlsDisabled();

  /**
   * Https settings https settings.
   *
   * @return the https settings
   */
  HttpsSettings httpsSettings();

  /**
   * Jetty settings jetty settings.
   *
   * @return the jetty settings
   */
  JettySettings jettySettings();

  /**
   * Container threads int.
   *
   * @return the int
   */
  int containerThreads();

  /**
   * Browser proxying enabled boolean.
   *
   * @return the boolean
   * @deprecated use {@link BrowserProxySettings#enabled()}
   */
  @Deprecated
  boolean browserProxyingEnabled();

  /**
   * Browser proxy settings browser proxy settings.
   *
   * @return the browser proxy settings
   */
  BrowserProxySettings browserProxySettings();

  /**
   * Proxy via proxy settings.
   *
   * @return the proxy settings
   */
  ProxySettings proxyVia();

  /**
   * Gets stores.
   *
   * @return the stores
   */
  Stores getStores();

  /**
   * Files root file source.
   *
   * @return the file source
   */
  FileSource filesRoot();

  /**
   * Mappings loader mappings loader.
   *
   * @return the mappings loader
   */
  MappingsLoader mappingsLoader();

  /**
   * Mappings saver mappings saver.
   *
   * @return the mappings saver
   */
  MappingsSaver mappingsSaver();

  /**
   * Notifier notifier.
   *
   * @return the notifier
   */
  Notifier notifier();

  /**
   * Request journal disabled boolean.
   *
   * @return the boolean
   */
  boolean requestJournalDisabled();

  /**
   * Max request journal entries optional.
   *
   * @return the optional
   */
  Optional<Integer> maxRequestJournalEntries();

  /**
   * Bind address string.
   *
   * @return the string
   */
  String bindAddress();

  /**
   * Gets filename maker.
   *
   * @return the filename maker
   */
  FilenameMaker getFilenameMaker();

  /**
   * Matching headers list.
   *
   * @return the list
   */
  List<CaseInsensitiveKey> matchingHeaders();

  /**
   * Should preserve host header boolean.
   *
   * @return the boolean
   */
  boolean shouldPreserveHostHeader();

  /**
   * Should preserve user agent proxy header boolean.
   *
   * @return the boolean
   */
  boolean shouldPreserveUserAgentProxyHeader();

  /**
   * Proxy host header string.
   *
   * @return the string
   */
  String proxyHostHeader();

  /**
   * Http server factory http server factory.
   *
   * @return the http server factory
   */
  HttpServerFactory httpServerFactory();

  /**
   * Has default http server factory boolean.
   *
   * @return the boolean
   */
  boolean hasDefaultHttpServerFactory();

  /**
   * Http client factory http client factory.
   *
   * @return the http client factory
   */
  HttpClientFactory httpClientFactory();

  /**
   * Thread pool factory thread pool factory.
   *
   * @return the thread pool factory
   */
  ThreadPoolFactory threadPoolFactory();

  /**
   * Gets declared extensions.
   *
   * @return the declared extensions
   */
  ExtensionDeclarations getDeclaredExtensions();

  /**
   * Is extension scanning enabled boolean.
   *
   * @return the boolean
   */
  boolean isExtensionScanningEnabled();

  /**
   * Network traffic listener wiremock network traffic listener.
   *
   * @return the wiremock network traffic listener
   */
  WiremockNetworkTrafficListener networkTrafficListener();

  /**
   * Gets admin authenticator.
   *
   * @return the admin authenticator
   */
  Authenticator getAdminAuthenticator();

  /**
   * Gets https required for admin api.
   *
   * @return the https required for admin api
   */
  boolean getHttpsRequiredForAdminApi();

  /**
   * Gets not matched renderer factory.
   *
   * @return the not matched renderer factory
   */
  default Function<Extensions, NotMatchedRenderer> getNotMatchedRendererFactory() {
    return PlainTextStubNotMatchedRenderer::new;
  }

  /**
   * Gets asynchronous response settings.
   *
   * @return the asynchronous response settings
   */
  AsynchronousResponseSettings getAsynchronousResponseSettings();

  /**
   * Gets chunked encoding policy.
   *
   * @return the chunked encoding policy
   */
  ChunkedEncodingPolicy getChunkedEncodingPolicy();

  /**
   * Gets gzip disabled.
   *
   * @return the gzip disabled
   */
  boolean getGzipDisabled();

  /**
   * Gets stub request logging disabled.
   *
   * @return the stub request logging disabled
   */
  boolean getStubRequestLoggingDisabled();

  /**
   * Gets stub cors enabled.
   *
   * @return the stub cors enabled
   */
  boolean getStubCorsEnabled();

  /**
   * Timeout long.
   *
   * @return the long
   */
  long timeout();

  /**
   * Gets disable optimize xml factories loading.
   *
   * @return the disable optimize xml factories loading
   */
  boolean getDisableOptimizeXmlFactoriesLoading();

  /**
   * Gets disable strict http headers.
   *
   * @return the disable strict http headers
   */
  boolean getDisableStrictHttpHeaders();

  /**
   * Gets data truncation settings.
   *
   * @return the data truncation settings
   */
  DataTruncationSettings getDataTruncationSettings();

  /**
   * Gets proxy target rules.
   *
   * @return the proxy target rules
   */
  NetworkAddressRules getProxyTargetRules();

  /**
   * Proxy timeout int.
   *
   * @return the int
   */
  int proxyTimeout();

  /**
   * Gets max http client connections.
   *
   * @return the max http client connections
   */
  int getMaxHttpClientConnections();

  /**
   * Gets response templating enabled.
   *
   * @return the response templating enabled
   */
  boolean getResponseTemplatingEnabled();

  /**
   * Gets response templating global.
   *
   * @return the response templating global
   */
  boolean getResponseTemplatingGlobal();

  /**
   * Gets max template cache entries.
   *
   * @return the max template cache entries
   */
  Long getMaxTemplateCacheEntries();

  /**
   * Gets template permitted system keys.
   *
   * @return the template permitted system keys
   */
  Set<String> getTemplatePermittedSystemKeys();

  /**
   * Gets template escaping disabled.
   *
   * @return the template escaping disabled
   */
  boolean getTemplateEscapingDisabled();

  /**
   * Gets supported proxy encodings.
   *
   * @return the supported proxy encodings
   */
  Set<String> getSupportedProxyEncodings();

  /**
   * Gets disable connection reuse.
   *
   * @return the disable connection reuse
   */
  boolean getDisableConnectionReuse();

  /**
   * Gets webhook thread pool size.
   *
   * @return the webhook thread pool size
   */
  int getWebhookThreadPoolSize();
}
