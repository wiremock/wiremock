/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.okhttp;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.common.Strings.isNotEmpty;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.core.Version;
import com.github.tomakehurst.wiremock.http.ssl.HostVerifyingSSLSocketFactory;
import com.github.tomakehurst.wiremock.http.ssl.SSLContextBuilder;
import com.github.tomakehurst.wiremock.http.ssl.TrustEverythingStrategy;
import com.github.tomakehurst.wiremock.http.ssl.TrustSpecificHostsStrategy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

class StaticOkHttpClientFactory {

  static OkHttpClient createClient(
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

    NetworkAddressRulesAdheringDns dns = new NetworkAddressRulesAdheringDns(networkAddressRules);

    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .retryOnConnectionFailure(false)
            .dns(dns)
            .connectTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS)
            .callTimeout(timeoutMilliseconds, TimeUnit.MILLISECONDS);

    String effectiveUserAgent =
        userAgent != null ? userAgent : "WireMock " + Version.getCurrentVersion();
    builder.addInterceptor(
        chain -> {
          Request original = chain.request();
          Request request = original.newBuilder().header("User-Agent", effectiveUserAgent).build();
          return chain.proceed(request);
        });

    if (disableConnectionReuse) {
      builder.connectionPool(new ConnectionPool(0, 1, TimeUnit.MILLISECONDS));
    } else {
      builder.connectionPool(new ConnectionPool(maxConnections, 5, TimeUnit.MINUTES));
    }

    if (proxySettings != NO_PROXY) {
      Proxy proxy =
          new Proxy(
              Proxy.Type.HTTP, new InetSocketAddress(proxySettings.host(), proxySettings.port()));
      builder.proxy(proxy);

      if (isNotEmpty(proxySettings.getUsername()) && isNotEmpty(proxySettings.getPassword())) {
        Authenticator proxyAuthenticator =
            (Route route, Response response) -> {
              String credential =
                  Credentials.basic(proxySettings.getUsername(), proxySettings.getPassword());
              return response
                  .request()
                  .newBuilder()
                  .header("Proxy-Authorization", credential)
                  .build();
            };
        builder.proxyAuthenticator(proxyAuthenticator);
      }
    }

    final SSLContext sslContext =
        buildSslContext(trustStoreSettings, trustAllCertificates, trustedHosts);
    SSLSocketFactory sslSocketFactory =
        new HostVerifyingSSLSocketFactory(sslContext.getSocketFactory());

    // Get the trust manager for OkHttp
    X509TrustManager trustManager = getTrustManager(sslContext);
    builder.sslSocketFactory(sslSocketFactory, trustManager);

    // Disable hostname verification as we're using Java's hostname verification in
    // HostVerifyingSSLSocketFactory
    builder.hostnameVerifier((hostname, session) -> true);

    return builder.build();
  }

  private static X509TrustManager getTrustManager(SSLContext sslContext) {
    try {
      // Get the trust managers that were configured in the SSLContext
      // We need to get them from the SSLContext parameters or create a default one
      javax.net.ssl.TrustManagerFactory trustManagerFactory =
          javax.net.ssl.TrustManagerFactory.getInstance(
              javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      javax.net.ssl.TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers != null && trustManagers.length > 0) {
        for (javax.net.ssl.TrustManager tm : trustManagers) {
          if (tm instanceof X509TrustManager) {
            return (X509TrustManager) tm;
          }
        }
      }
      throw new IllegalStateException("No X509TrustManager found");
    } catch (Exception e) {
      return throwUnchecked(e, null);
    }
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

  private static class NetworkAddressRulesAdheringDns implements Dns {
    private final NetworkAddressRules networkAddressRules;

    NetworkAddressRulesAdheringDns(NetworkAddressRules networkAddressRules) {
      this.networkAddressRules = networkAddressRules;
    }

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
      List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
      for (InetAddress address : addresses) {
        if (!networkAddressRules.isAllowed(hostname)) {
          throw new UnknownHostException(
              "Host " + hostname + " is denied by network address rules");
        }
        if (!networkAddressRules.isAllowed(address.getHostAddress())) {
          throw new UnknownHostException(
              "IP address " + address.getHostAddress() + " is denied by network address rules");
        }
      }
      return addresses;
    }
  }
}
