/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.http.ssl.*;
import java.net.URI;
import java.security.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.util.TextUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

public class HttpClientFactory {

  public static final int DEFAULT_MAX_CONNECTIONS = 50;
  public static final int DEFAULT_TIMEOUT = 30000;

  public static CloseableHttpClient createClient(
      int maxConnections,
      int timeoutMilliseconds,
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      boolean trustAllCertificates,
      final List<String> trustedHosts,
      boolean useSystemProperties,
      NetworkAddressRules networkAddressRules,
      boolean disableConnectionReuse) {

    NetworkAddressRulesAdheringDnsResolver dnsResolver =
        new NetworkAddressRulesAdheringDnsResolver(networkAddressRules);

    HttpClientBuilder builder =
        HttpClientBuilder.create()
            .disableAuthCaching()
            .disableAutomaticRetries()
            .disableCookieManagement()
            .disableRedirectHandling()
            .disableContentCompression()
            .setConnectionManager(
                PoolingHttpClientConnectionManagerBuilder.create()
                    .setDnsResolver(dnsResolver)
                    .setMaxConnPerRoute(maxConnections)
                    .setMaxConnTotal(maxConnections)
                    .setValidateAfterInactivity(TimeValue.ofSeconds(5)) // TODO Verify duration
                    .setConnectionFactory(
                        new ManagedHttpClientConnectionFactory(
                            null, CharCodingConfig.custom().setCharset(UTF_8).build(), null))
                    .build())
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofMilliseconds(timeoutMilliseconds))
                    .build());

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
      if (!isEmpty(proxySettings.getUsername()) && !isEmpty(proxySettings.getPassword())) {
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
        buildSslContext(trustStoreSettings, trustAllCertificates, trustedHosts);
    LayeredConnectionSocketFactory sslSocketFactory = buildSslConnectionSocketFactory(sslContext);
    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(sslSocketFactory)
            .setDnsResolver(dnsResolver)
            .build();
    builder.setConnectionManager(connectionManager);

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

  /**
   * Copied from {@link HttpClientBuilder#split(String)} which is not the same as {@link
   * org.apache.commons.lang3.StringUtils#split(String)}
   */
  private static String[] split(final String s) {
    if (TextUtils.isBlank(s)) {
      return null;
    }
    return s.split(" *, *");
  }

  private static SSLContext buildSslContext(
      KeyStoreSettings trustStoreSettings,
      boolean trustAllCertificates,
      List<String> trustedHosts) {
    if (trustStoreSettings != NO_STORE) {
      return buildSSLContextWithTrustStore(trustStoreSettings, trustAllCertificates, trustedHosts);
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

  public static CloseableHttpClient createClient(
      int maxConnections,
      int timeoutMilliseconds,
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      boolean useSystemProperties,
      NetworkAddressRules networkAddressRules,
      boolean disableConnectionReuse) {
    return createClient(
        maxConnections,
        timeoutMilliseconds,
        proxySettings,
        trustStoreSettings,
        true,
        Collections.emptyList(),
        useSystemProperties,
        networkAddressRules,
        disableConnectionReuse);
  }

  private static SSLContext buildSSLContextWithTrustStore(
      KeyStoreSettings trustStoreSettings,
      boolean trustSelfSignedCertificates,
      List<String> trustedHosts) {
    try {
      KeyStore trustStore = trustStoreSettings.loadStore();
      SSLContextBuilder sslContextBuilder =
          SSLContextBuilder.create()
              .loadKeyMaterial(trustStore, trustStoreSettings.password().toCharArray());
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

  public static CloseableHttpClient createClient(int maxConnections, int timeoutMilliseconds) {
    return createClient(
        maxConnections,
        timeoutMilliseconds,
        NO_PROXY,
        NO_STORE,
        true,
        NetworkAddressRules.ALLOW_ALL,
        false);
  }

  public static CloseableHttpClient createClient(int timeoutMilliseconds) {
    return createClient(DEFAULT_MAX_CONNECTIONS, timeoutMilliseconds);
  }

  public static CloseableHttpClient createClient(ProxySettings proxySettings) {
    return createClient(
        DEFAULT_MAX_CONNECTIONS,
        DEFAULT_TIMEOUT,
        proxySettings,
        NO_STORE,
        true,
        NetworkAddressRules.ALLOW_ALL,
        false);
  }

  public static CloseableHttpClient createClient() {
    return createClient(DEFAULT_TIMEOUT);
  }

  public static HttpUriRequest getHttpRequestFor(RequestMethod method, String url) {
    notifier().info("Proxying: " + method + " " + url);

    if (method.equals(GET)) return new HttpGet(url);
    else if (method.equals(POST)) return new HttpPost(url);
    else if (method.equals(PUT)) return new HttpPut(url);
    else if (method.equals(DELETE)) return new HttpDelete(url);
    else if (method.equals(HEAD)) return new HttpHead(url);
    else if (method.equals(OPTIONS)) return new HttpOptions(url);
    else if (method.equals(TRACE)) return new HttpTrace(url);
    else if (method.equals(PATCH)) return new HttpPatch(url);
    else return new HttpUriRequestBase(method.toString(), URI.create(url));
  }
}
