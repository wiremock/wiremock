/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.servlet;

import static java.util.Collections.emptyList;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.common.BrowserProxySettings;
import com.github.tomakehurst.wiremock.common.filemaker.FilenameMaker;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.ExtensionDeclarations;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.client.ApacheHttpClientFactory;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.DoNothingWiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.security.NoAuthenticator;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsSource;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.store.DefaultStores;
import com.github.tomakehurst.wiremock.store.Stores;
import jakarta.servlet.ServletContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class WarConfiguration implements Options {

  private static final String FILE_SOURCE_ROOT_KEY = "WireMockFileSourceRoot";

  private final ServletContext servletContext;

  public WarConfiguration(ServletContext servletContext) {
    this.servletContext = servletContext;
  }

  @Override
  public int portNumber() {
    return 0;
  }

  @Override
  public boolean getHttpDisabled() {
    return false;
  }

  @Override
  public boolean getHttp2PlainDisabled() {
    return false;
  }

  @Override
  public boolean getHttp2TlsDisabled() {
    return false;
  }

  @Override
  public HttpsSettings httpsSettings() {
    return new HttpsSettings.Builder().build();
  }

  @Override
  public JettySettings jettySettings() {
    return null;
  }

  @Override
  public int containerThreads() {
    return 0;
  }

  @Override
  public boolean browserProxyingEnabled() {
    return false;
  }

  @Override
  public ProxySettings proxyVia() {
    return ProxySettings.NO_PROXY;
  }

  @Override
  public Stores getStores() {
    return new DefaultStores(filesRoot());
  }

  @Override
  public FileSource filesRoot() {
    String fileSourceRoot = servletContext.getInitParameter(FILE_SOURCE_ROOT_KEY);
    return new ServletContextFileSource(servletContext, fileSourceRoot);
  }

  @Override
  public MappingsLoader mappingsLoader() {
    return new JsonFileMappingsSource(filesRoot().child("mappings"), new FilenameMaker());
  }

  @Override
  public MappingsSaver mappingsSaver() {
    return new NotImplementedMappingsSaver();
  }

  @Override
  public Notifier notifier() {
    return null;
  }

  @Override
  public boolean requestJournalDisabled() {
    return false;
  }

  @Override
  public Optional<Integer> maxRequestJournalEntries() {
    String str = servletContext.getInitParameter("maxRequestJournalEntries");
    if (str == null) {
      return Optional.empty();
    }
    return Optional.of(Integer.parseInt(str));
  }

  @Override
  public String bindAddress() {
    return null;
  }

  @Override
  public FilenameMaker getFilenameMaker() {
    return null;
  }

  @Override
  public List<CaseInsensitiveKey> matchingHeaders() {
    return emptyList();
  }

  @Override
  public boolean shouldPreserveHostHeader() {
    return false;
  }

  @Override
  public String proxyHostHeader() {
    return null;
  }

  @Override
  public HttpServerFactory httpServerFactory() {
    return null;
  }

  @Override
  public HttpClientFactory httpClientFactory() {
    return new ApacheHttpClientFactory();
  }

  @Override
  public ThreadPoolFactory threadPoolFactory() {
    return null;
  }

  @Override
  public ExtensionDeclarations getDeclaredExtensions() {
    return new ExtensionDeclarations();
  }

  @Override
  public boolean isExtensionScanningEnabled() {
    return true;
  }

  @Override
  public WiremockNetworkTrafficListener networkTrafficListener() {
    return new DoNothingWiremockNetworkTrafficListener();
  }

  @Override
  public Authenticator getAdminAuthenticator() {
    return new NoAuthenticator();
  }

  @Override
  public boolean getHttpsRequiredForAdminApi() {
    return false;
  }

  @Override
  public AsynchronousResponseSettings getAsynchronousResponseSettings() {
    return new AsynchronousResponseSettings(false, 0);
  }

  @Override
  public ChunkedEncodingPolicy getChunkedEncodingPolicy() {
    return ChunkedEncodingPolicy.ALWAYS;
  }

  @Override
  public boolean getGzipDisabled() {
    return false;
  }

  @Override
  public boolean getStubRequestLoggingDisabled() {
    return false;
  }

  @Override
  public boolean getStubCorsEnabled() {
    return false;
  }

  @Override
  public long timeout() {
    return 0;
  }

  @Override
  public boolean getDisableOptimizeXmlFactoriesLoading() {
    return false;
  }

  @Override
  public boolean getDisableStrictHttpHeaders() {
    return false;
  }

  @Override
  public DataTruncationSettings getDataTruncationSettings() {
    return DataTruncationSettings.DEFAULTS;
  }

  @Override
  public NetworkAddressRules getProxyTargetRules() {
    return NetworkAddressRules.ALLOW_ALL;
  }

  @Override
  public BrowserProxySettings browserProxySettings() {
    return BrowserProxySettings.DISABLED;
  }

  @Override
  public int proxyTimeout() {
    return DEFAULT_TIMEOUT;
  }

  @Override
  public boolean getResponseTemplatingEnabled() {
    return true;
  }

  @Override
  public boolean getResponseTemplatingGlobal() {
    return false;
  }

  @Override
  public Long getMaxTemplateCacheEntries() {
    return null;
  }

  @Override
  public Set<String> getTemplatePermittedSystemKeys() {
    return null;
  }

  @Override
  public boolean getTemplateEscapingDisabled() {
    return false;
  }
}
