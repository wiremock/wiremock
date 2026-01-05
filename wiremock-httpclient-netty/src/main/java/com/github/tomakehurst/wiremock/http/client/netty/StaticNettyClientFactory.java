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
package com.github.tomakehurst.wiremock.http.client.netty;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings.NO_STORE;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProhibitedNetworkAddressException;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.core.Version;
import com.github.tomakehurst.wiremock.http.ssl.SSLContextBuilder;
import com.github.tomakehurst.wiremock.http.ssl.TrustEverythingStrategy;
import com.github.tomakehurst.wiremock.http.ssl.TrustSpecificHostsStrategy;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.SSLContext;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;

class StaticNettyClientFactory {

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

    // Create connection provider
    ConnectionProvider connectionProvider;
    if (disableConnectionReuse) {
      connectionProvider = ConnectionProvider.newConnection();
    } else {
      connectionProvider =
          ConnectionProvider.builder("wiremock-netty")
              .maxConnections(maxConnections)
              .pendingAcquireTimeout(Duration.ofMillis(timeoutMilliseconds))
              .maxIdleTime(Duration.ofMinutes(5))
              .build();
    }

    HttpClient client =
        HttpClient.create(connectionProvider)
            .followRedirect(false)
            .responseTimeout(Duration.ofMillis(timeoutMilliseconds))
            .resolver(new NetworkAddressRulesAdheringResolverGroup(networkAddressRules));

    // Set User-Agent header
    String effectiveUserAgent =
        userAgent != null ? userAgent : "WireMock " + Version.getCurrentVersion();
    client =
        client.headers(
            headers ->
                headers.set(
                    io.netty.handler.codec.http.HttpHeaderNames.USER_AGENT, effectiveUserAgent));

    // Configure proxy
    if (proxySettings != ProxySettings.NO_PROXY) {
      final ProxySettings finalProxySettings = proxySettings;
      client =
          client.proxy(
              proxy ->
                  proxy
                      .type(ProxyProvider.Proxy.HTTP)
                      .host(finalProxySettings.host())
                      .port(finalProxySettings.port())
                      .username(finalProxySettings.getUsername())
                      .password(u -> finalProxySettings.getPassword()));
    }

    // Configure SSL
    final SSLContext sslContext =
        buildSslContext(trustStoreSettings, trustAllCertificates, trustedHosts);
    SslContext nettySslContext =
        new JdkSslContext(
            sslContext,
            true,
            null,
            IdentityCipherSuiteFilter.INSTANCE,
            ApplicationProtocolConfig.DISABLED,
            ClientAuth.OPTIONAL,
            null,
            false);
    client = client.secure(sslSpec -> sslSpec.sslContext(nettySslContext));

    return client;
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

  private static class NetworkAddressRulesAdheringResolverGroup
      extends AddressResolverGroup<InetSocketAddress> {
    private final NetworkAddressRules networkAddressRules;

    NetworkAddressRulesAdheringResolverGroup(NetworkAddressRules networkAddressRules) {
      this.networkAddressRules = networkAddressRules;
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
      return new AddressResolver<InetSocketAddress>() {
        @Override
        public boolean isSupported(SocketAddress address) {
          return address instanceof InetSocketAddress;
        }

        @Override
        public boolean isResolved(SocketAddress address) {
          return address instanceof InetSocketAddress
              && !((InetSocketAddress) address).isUnresolved();
        }

        @Override
        public Promise<InetSocketAddress> resolve(SocketAddress address) {
          Promise<InetSocketAddress> promise = executor.newPromise();
          if (!(address instanceof InetSocketAddress)) {
            promise.setFailure(new IllegalArgumentException("Unsupported address type"));
            return promise;
          }

          InetSocketAddress inetAddress = (InetSocketAddress) address;
          try {
            InetAddress[] addresses = InetAddress.getAllByName(inetAddress.getHostName());
            for (InetAddress addr : addresses) {
              String hostname = inetAddress.getHostName();
              if (!networkAddressRules.isAllowed(hostname)) {
                promise.setFailure(new ProhibitedNetworkAddressException());
                return promise;
              }
              if (!networkAddressRules.isAllowed(addr.getHostAddress())) {
                promise.setFailure(new ProhibitedNetworkAddressException());
                return promise;
              }
            }
            promise.setSuccess(new InetSocketAddress(addresses[0], inetAddress.getPort()));
          } catch (UnknownHostException e) {
            promise.setFailure(e);
          }
          return promise;
        }

        @Override
        public Promise<InetSocketAddress> resolve(
            SocketAddress address, Promise<InetSocketAddress> promise) {
          return resolve(address);
        }

        @Override
        public Promise<List<InetSocketAddress>> resolveAll(SocketAddress address) {
          Promise<List<InetSocketAddress>> promise = executor.newPromise();
          return resolveAll(address, promise);
        }

        @Override
        public Promise<List<InetSocketAddress>> resolveAll(
            SocketAddress address, Promise<List<InetSocketAddress>> promise) {
          if (!(address instanceof InetSocketAddress)) {
            promise.setFailure(new IllegalArgumentException("Unsupported address type"));
            return promise;
          }

          InetSocketAddress inetAddress = (InetSocketAddress) address;
          try {
            InetAddress[] addresses = InetAddress.getAllByName(inetAddress.getHostName());
            List<InetSocketAddress> resolvedAddresses = new ArrayList<>();

            for (InetAddress addr : addresses) {
              String hostname = inetAddress.getHostName();
              if (!networkAddressRules.isAllowed(hostname)) {
                promise.setFailure(new ProhibitedNetworkAddressException());
                return promise;
              }
              if (!networkAddressRules.isAllowed(addr.getHostAddress())) {
                promise.setFailure(new ProhibitedNetworkAddressException());
                return promise;
              }
              resolvedAddresses.add(new InetSocketAddress(addr, inetAddress.getPort()));
            }

            promise.setSuccess(resolvedAddresses);
          } catch (UnknownHostException e) {
            promise.setFailure(e);
          }
          return promise;
        }

        @Override
        public void close() {
          // Nothing to close
        }
      };
    }
  }
}
