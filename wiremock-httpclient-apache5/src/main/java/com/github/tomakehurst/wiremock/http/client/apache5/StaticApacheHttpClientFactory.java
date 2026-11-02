/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.apache5;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.Strings.isNotEmpty;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.core.Version;
import com.github.tomakehurst.wiremock.http.ssl.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

class StaticApacheHttpClientFactory {

  static CloseableHttpClient createClient(
      int maxConnections,
      int timeoutMilliseconds,
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      Map<String, String> clientKeyAliases,
      boolean trustAllCertificates,
      final List<String> trustedHosts,
      boolean useSystemProperties,
      NetworkAddressRules networkAddressRules,
      boolean disableConnectionReuse,
      String userAgent) {

    NetworkAddressRulesAdheringDnsResolver dnsResolver =
        new NetworkAddressRulesAdheringDnsResolver(networkAddressRules);

    HttpClientBuilder builder =
        HttpClientBuilder.create()
            .disableAuthCaching()
            .disableAutomaticRetries()
            .disableCookieManagement()
            .disableRedirectHandling()
            .disableContentCompression()
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofMilliseconds(timeoutMilliseconds))
                    .setProtocolUpgradeEnabled(false)
                    .build());

    String effectiveUserAgent =
        userAgent != null ? userAgent : "WireMock " + Version.getCurrentVersion();
    builder.setUserAgent(effectiveUserAgent);

    if (disableConnectionReuse) {
      builder
          .setConnectionReuseStrategy((request, response, context) -> false)
          .setKeepAliveStrategy((response, context) -> TimeValue.ZERO_MILLISECONDS);
    }

    if (useSystemProperties) {
      builder.useSystemProperties();
    }

    if (proxySettings != NO_PROXY) {
      HttpHost proxyHost = new HttpHost(proxySettings.host(), proxySettings.port());
      builder.setProxy(proxyHost);
      if (isNotEmpty(proxySettings.getUsername()) && isNotEmpty(proxySettings.getPassword())) {
        builder.setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy()); // TODO Verify
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            new AuthScope(proxySettings.host(), proxySettings.port()),
            new UsernamePasswordCredentials(
                proxySettings.getUsername(), proxySettings.getPassword().toCharArray()));
        builder.setDefaultCredentialsProvider(credentialsProvider);
      }
    }

    final SSLContext sslContext =
        buildSslContext(trustStoreSettings, clientKeyAliases, trustAllCertificates, trustedHosts);
    LayeredConnectionSocketFactory sslSocketFactory = buildSslConnectionSocketFactory(sslContext);
    builder.setConnectionManager(
        PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslSocketFactory)
            .setDnsResolver(dnsResolver)
            .setMaxConnPerRoute(maxConnections)
            .setMaxConnTotal(maxConnections)
            .setValidateAfterInactivity(TimeValue.ofSeconds(5)) // TODO Verify duration
            .setConnectionFactory(
                new ManagedHttpClientConnectionFactory(
                    null, CharCodingConfig.custom().setCharset(UTF_8).build(), null))
            .build());

    return builder.build();
  }

  private static LayeredConnectionSocketFactory buildSslConnectionSocketFactory(
      final SSLContext sslContext) {
    final String[] supportedProtocols = split(System.getProperty("https.protocols"));
    final String[] supportedCipherSuites = split(System.getProperty("https.cipherSuites"));

    return new SSLConnectionSocketFactory(
        new HostVerifyingSSLSocketFactory(sslContext.getSocketFactory()),
        supportedProtocols,
        supportedCipherSuites,
        new NoopHostnameVerifier() // using Java's hostname verification
        );
  }

  private static String[] split(final String s) {
    if (TextUtils.isBlank(s)) {
      return null;
    }
    return s.split(" *, *");
  }

  private static SSLContext buildSslContext(
      KeyStoreSettings trustStoreSettings,
      Map<String, String> clientKeyAliases,
      boolean trustAllCertificates,
      List<String> trustedHosts) {
    if (trustStoreSettings != NO_STORE) {
      return buildSSLContextWithTrustStore(
          trustStoreSettings, clientKeyAliases, trustAllCertificates, trustedHosts);
    } else if (!clientKeyAliases.isEmpty()) {
      throw new IllegalArgumentException(
          "Client certificate aliases " + clientKeyAliases + " require an HTTPS trust store");
    } else if (trustAllCertificates) {
      return buildAllowAnythingSSLContext();
    } else {
      try {
        return SSLContextBuilder.create()
            .loadTrustMaterial(new TrustSpecificHostsStrategy(trustedHosts))
            .build();
      } catch (NoSuchAlgorithmException | KeyManagementException e) {
        return throwUnchecked(e, null);
      }
    }
  }

  private static SSLContext buildSSLContextWithTrustStore(
      KeyStoreSettings trustStoreSettings,
      Map<String, String> clientKeyAliases,
      boolean trustSelfSignedCertificates,
      List<String> trustedHosts) {
    KeyStore trustStore = trustStoreSettings.loadStore();
    checkClientKeyAliasesExist(trustStore, trustStoreSettings.path(), clientKeyAliases);
    try {
      SSLContextBuilder sslContextBuilder =
          SSLContextBuilder.create()
              .loadKeyMaterial(
                  trustStore, trustStoreSettings.password().toCharArray(), clientKeyAliases);
      if (trustSelfSignedCertificates) {
        sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
      } else if (containsCertificate(trustStore)) {
        sslContextBuilder.loadTrustMaterial(
            trustStore, new TrustSpecificHostsStrategy(trustedHosts));
      } else {
        sslContextBuilder.loadTrustMaterial(new TrustSpecificHostsStrategy(trustedHosts));
      }
      return sslContextBuilder.build();
    } catch (Exception e) {
      return throwUnchecked(e, SSLContext.class);
    }
  }

  /** Fails fast on a mapping to an alias that is missing or is not a private key entry. */
  private static void checkClientKeyAliasesExist(
      KeyStore trustStore, String trustStorePath, Map<String, String> clientKeyAliases) {
    try {
      for (Map.Entry<String, String> mapping : clientKeyAliases.entrySet()) {
        if (!trustStore.isKeyEntry(mapping.getValue().trim())) {
          throw new IllegalArgumentException(
              "Client certificate alias '"
                  + mapping.getValue()
                  + "' for host '"
                  + mapping.getKey()
                  + "' is not a private key entry in trust store "
                  + trustStorePath
                  + "; private key entries: "
                  + keyEntryAliases(trustStore));
        }
      }
    } catch (KeyStoreException e) {
      throwUnchecked(e);
    }
  }

  private static List<String> keyEntryAliases(KeyStore keyStore) throws KeyStoreException {
    List<String> aliases = new ArrayList<>();
    for (String alias : Collections.list(keyStore.aliases())) {
      if (keyStore.isKeyEntry(alias)) {
        aliases.add(alias);
      }
    }
    return aliases;
  }

  private static boolean containsCertificate(KeyStore trustStore) throws KeyStoreException {
    Enumeration<String> aliases = trustStore.aliases();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      try {
        if (trustStore.getEntry(alias, null) instanceof KeyStore.TrustedCertificateEntry) {
          return true;
        }
      } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
        // ignore
      }
    }
    return false;
  }

  private static SSLContext buildAllowAnythingSSLContext() {
    try {
      return SSLContextBuilder.create().loadTrustMaterial(new TrustEverythingStrategy()).build();
    } catch (Exception e) {
      return throwUnchecked(e, null);
    }
  }
}
