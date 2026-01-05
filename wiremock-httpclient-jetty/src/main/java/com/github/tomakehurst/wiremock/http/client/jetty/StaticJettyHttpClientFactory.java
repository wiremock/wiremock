/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.jetty;

import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.core.Version;
import java.time.Duration;
import java.util.List;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.Origin;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

class StaticJettyHttpClientFactory {

  static HttpClient createClient(
      int maxConnections,
      int timeoutMilliseconds,
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      boolean trustAllCertificates,
      final List<String> trustedHosts,
      boolean useSystemProperties,
      NetworkAddressRules networkAddressRules,
      boolean disableConnectionReuse,
      String userAgent) {

    // 1. Configure SSL
    SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
    sslContextFactory.setTrustAll(trustAllCertificates);

    if (trustStoreSettings != null && trustStoreSettings != NO_STORE) {
      sslContextFactory.setTrustStorePath(trustStoreSettings.path());
      sslContextFactory.setTrustStorePassword(trustStoreSettings.password());
      sslContextFactory.setTrustStoreType(trustStoreSettings.type());
    }

    // 2. Configure DNS resolver with network address rules
    NetworkAddressRulesAdheringSocketAddressResolver socketAddressResolver =
        new NetworkAddressRulesAdheringSocketAddressResolver(networkAddressRules);

    // 3. Configure the Connector (The closest thing to a Builder for the transport layer)
    ClientConnector clientConnector = new ClientConnector();
    clientConnector.setSslContextFactory(sslContextFactory);
    clientConnector.setConnectTimeout(Duration.ofMillis(timeoutMilliseconds));

    // 4. Assemble the Client
    HttpClient httpClient = new HttpClient(new HttpClientTransportDynamic(clientConnector));
    httpClient.setSocketAddressResolver(socketAddressResolver);

    // 5. Set remaining properties
    httpClient.setAddressResolutionTimeout(timeoutMilliseconds);
    httpClient.setMaxConnectionsPerDestination(maxConnections);
    httpClient.setFollowRedirects(false);

    String effectiveUserAgent =
        userAgent != null ? userAgent : "WireMock " + Version.getCurrentVersion();
    httpClient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT, effectiveUserAgent));

    httpClient.setIdleTimeout(timeoutMilliseconds);
    httpClient.setConnectTimeout(timeoutMilliseconds);
    // Disable cookie management
    httpClient.setHttpCookieStore(new HttpCookieStore.Empty());

    // 6. Proxy configuration
    if (proxySettings != null && proxySettings != NO_PROXY) {
      httpClient
          .getProxyConfiguration()
          .addProxy(
              new HttpProxy(new Origin.Address(proxySettings.host(), proxySettings.port()), false));
    }

    if (disableConnectionReuse) {
      httpClient.setMaxConnectionsPerDestination(1);
      httpClient.setIdleTimeout(1); // Close connections immediately
    } else {
      httpClient.setMaxConnectionsPerDestination(maxConnections);
    }

    try {
      httpClient.start();
    } catch (Exception e) {
      throw new RuntimeException("Failed to start Jetty HttpClient", e);
    }

    return httpClient;
  }
}
