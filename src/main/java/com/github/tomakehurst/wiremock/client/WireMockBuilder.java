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

import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.security.ClientBasicAuthenticator;
import com.github.tomakehurst.wiremock.security.NoClientAuthenticator;

/** The type Wire mock builder. */
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

  /**
   * Port wire mock builder.
   *
   * @param port the port
   * @return the wire mock builder
   */
  public WireMockBuilder port(int port) {
    this.port = port;
    return this;
  }

  /**
   * Host wire mock builder.
   *
   * @param host the host
   * @return the wire mock builder
   */
  public WireMockBuilder host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Url path prefix wire mock builder.
   *
   * @param urlPathPrefix the url path prefix
   * @return the wire mock builder
   */
  public WireMockBuilder urlPathPrefix(String urlPathPrefix) {
    this.urlPathPrefix = urlPathPrefix;
    return this;
  }

  /**
   * Scheme wire mock builder.
   *
   * @param scheme the scheme
   * @return the wire mock builder
   */
  public WireMockBuilder scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  /**
   * Http wire mock builder.
   *
   * @return the wire mock builder
   */
  public WireMockBuilder http() {
    return scheme("http");
  }

  /**
   * Https wire mock builder.
   *
   * @return the wire mock builder
   */
  public WireMockBuilder https() {
    return scheme("https");
  }

  /**
   * Host header wire mock builder.
   *
   * @param hostHeader the host header
   * @return the wire mock builder
   */
  public WireMockBuilder hostHeader(String hostHeader) {
    this.hostHeader = hostHeader;
    return this;
  }

  /**
   * Proxy host wire mock builder.
   *
   * @param proxyHost the proxy host
   * @return the wire mock builder
   */
  public WireMockBuilder proxyHost(String proxyHost) {
    this.proxyHost = proxyHost;
    return this;
  }

  /**
   * Proxy port wire mock builder.
   *
   * @param proxyPort the proxy port
   * @return the wire mock builder
   */
  public WireMockBuilder proxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
    return this;
  }

  /**
   * Authenticator wire mock builder.
   *
   * @param authenticator the authenticator
   * @return the wire mock builder
   */
  public WireMockBuilder authenticator(ClientAuthenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  /**
   * Basic authenticator wire mock builder.
   *
   * @param username the username
   * @param password the password
   * @return the wire mock builder
   */
  public WireMockBuilder basicAuthenticator(String username, String password) {
    return authenticator(new ClientBasicAuthenticator(username, password));
  }

  /**
   * Build wire mock.
   *
   * @return the wire mock
   */
  public WireMock build() {
    return new WireMock(
        scheme, host, port, urlPathPrefix, hostHeader, proxyHost, proxyPort, authenticator);
  }
}
