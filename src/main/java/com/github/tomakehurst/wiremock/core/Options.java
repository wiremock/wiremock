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

import com.github.tomakehurst.wiremock.common.*;
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

public interface Options {

  enum ChunkedEncodingPolicy {
    ALWAYS,
    NEVER,
    BODY_FILE
  }

  int DEFAULT_PORT = 8080;
  int DYNAMIC_PORT = 0;
  int DEFAULT_TIMEOUT = 300_000;
  int DEFAULT_CONTAINER_THREADS = 25;
  String DEFAULT_BIND_ADDRESS = "0.0.0.0";
  int DEFAULT_MAX_HTTP_CONNECTIONS = 1000;
  int DEFAULT_WEBHOOK_THREADPOOL_SIZE = 10;
  boolean DEFAULT_DISABLE_CONNECTION_REUSE = true;
  Long DEFAULT_MAX_TEMPLATE_CACHE_ENTRIES = 1000L;

  int portNumber();

  boolean getHttpDisabled();

  boolean getHttp2PlainDisabled();

  boolean getHttp2TlsDisabled();

  HttpsSettings httpsSettings();

  JettySettings jettySettings();

  int containerThreads();

  /**
   * @deprecated use {@link BrowserProxySettings#enabled()}
   */
  @Deprecated
  boolean browserProxyingEnabled();

  BrowserProxySettings browserProxySettings();

  ProxySettings proxyVia();

  Stores getStores();

  FileSource filesRoot();

  MappingsLoader mappingsLoader();

  MappingsSaver mappingsSaver();

  Notifier notifier();

  boolean requestJournalDisabled();

  Optional<Integer> maxRequestJournalEntries();

  String bindAddress();

  FilenameMaker getFilenameMaker();

  List<CaseInsensitiveKey> matchingHeaders();

  boolean shouldPreserveHostHeader();

  boolean shouldPreserveUserAgentProxyHeader();

  String proxyHostHeader();

  HttpServerFactory httpServerFactory();

  boolean hasDefaultHttpServerFactory();

  HttpClientFactory httpClientFactory();

  ThreadPoolFactory threadPoolFactory();

  ExtensionDeclarations getDeclaredExtensions();

  boolean isExtensionScanningEnabled();

  WiremockNetworkTrafficListener networkTrafficListener();

  Authenticator getAdminAuthenticator();

  boolean getHttpsRequiredForAdminApi();

  default Function<Extensions, NotMatchedRenderer> getNotMatchedRendererFactory() {
    return PlainTextStubNotMatchedRenderer::new;
  }

  AsynchronousResponseSettings getAsynchronousResponseSettings();

  ChunkedEncodingPolicy getChunkedEncodingPolicy();

  boolean getGzipDisabled();

  boolean getStubRequestLoggingDisabled();

  boolean getStubCorsEnabled();

  long timeout();

  boolean getDisableOptimizeXmlFactoriesLoading();

  boolean getDisableStrictHttpHeaders();

  DataTruncationSettings getDataTruncationSettings();

  NetworkAddressRules getProxyTargetRules();

  int proxyTimeout();

  int getMaxHttpClientConnections();

  boolean getResponseTemplatingEnabled();

  boolean getResponseTemplatingGlobal();

  Long getMaxTemplateCacheEntries();

  Set<String> getTemplatePermittedSystemKeys();

  boolean getTemplateEscapingDisabled();

  Set<String> getSupportedProxyEncodings();

  boolean getDisableConnectionReuse();

  int getWebhookThreadPoolSize();
}
