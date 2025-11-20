/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.client;

import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.Strings.isNotBlank;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.client.HttpClientFactory.DEFAULT_MAX_CONNECTIONS;
import static com.github.tomakehurst.wiremock.http.client.HttpClientFactory.DEFAULT_TIMEOUT;

import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.http.client.HttpClientFactory;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.security.ClientBasicAuthenticator;
import com.github.tomakehurst.wiremock.security.NoClientAuthenticator;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class WireMockBuilder {

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 8080;

  private int port = DEFAULT_PORT;
  private String host = DEFAULT_HOST;
  private String urlPathPrefix = "";
  private String scheme = "http";
  private String hostHeader = null;
  private String proxyHost = null;
  private int proxyPort = 0;
  private ClientAuthenticator authenticator = new NoClientAuthenticator();

  public WireMockBuilder port(int port) {
    this.port = port;
    return this;
  }

  public WireMockBuilder host(String host) {
    this.host = host;
    return this;
  }

  public WireMockBuilder urlPathPrefix(String urlPathPrefix) {
    this.urlPathPrefix = urlPathPrefix;
    return this;
  }

  public WireMockBuilder scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  public WireMockBuilder http() {
    return scheme("http");
  }

  public WireMockBuilder https() {
    return scheme("https");
  }

  public WireMockBuilder hostHeader(String hostHeader) {
    this.hostHeader = hostHeader;
    return this;
  }

  public WireMockBuilder proxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
    return this;
  }

  public WireMockBuilder proxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
    return this;
  }

  public WireMockBuilder authenticator(ClientAuthenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  public WireMockBuilder basicAuthenticator(String username, String password) {
    return authenticator(new ClientBasicAuthenticator(username, password));
  }

  public WireMock build() {
    ProxySettings proxySettings =
        isNotBlank(proxyHost) ? new ProxySettings(proxyHost, proxyPort) : NO_PROXY;
    return new WireMock(buildAdminClient(proxySettings));
  }

  HttpAdminClient buildAdminClient() {
    return buildAdminClient(NO_PROXY);
  }

  HttpAdminClient buildAdminClient(ProxySettings proxySettings) {

    Options options =
        wireMockConfig()
            .maxHttpClientConnections(DEFAULT_MAX_CONNECTIONS)
            .timeout(DEFAULT_TIMEOUT)
            .proxyVia(proxySettings)
            .disableConnectionReuse(false);

    HttpClient httpClient =
        httpClientFactory.buildHttpClient(options, true, Collections.emptyList(), true);
    return new HttpAdminClient(
        scheme, host, port, urlPathPrefix, hostHeader, authenticator, httpClient);
  }

  private static final HttpClientFactory httpClientFactory = loadHttpClientFactory();

  private static HttpClientFactory loadHttpClientFactory() {
    final ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class);
    Stream<HttpClientFactory> clientFactories =
        loader.stream()
            .filter(provider -> HttpClientFactory.class.isAssignableFrom(provider.type()))
            .map(provider -> (HttpClientFactory) provider.get())
        // TODO sort them so that client provided ones win
        //                .sorted((o1, o2) -> );
        ;
    // TODO throw an exception that explains how to fix the problem
    return clientFactories
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No HttpClientFactory found"));
  }
}
