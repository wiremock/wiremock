/*
 * Copyright (C) 2011 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.http.ssl.SSLContextBuilder;
import com.github.tomakehurst.wiremock.http.ssl.TrustEverythingStrategy;
import com.github.tomakehurst.wiremock.http.ssl.TrustSelfSignedStrategy;
import com.github.tomakehurst.wiremock.http.ssl.TrustSpecificHostsStrategy;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.KeyStoreSettings.NO_STORE;
import static com.github.tomakehurst.wiremock.common.LocalNotifier.notifier;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static com.github.tomakehurst.wiremock.http.RequestMethod.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HttpClientFactory {

    public static final int DEFAULT_MAX_CONNECTIONS = 50;
    public static final int DEFAULT_TIMEOUT = 30000;

    public static CloseableHttpClient createClient(
            int maxConnections,
            int timeoutMilliseconds,
            ProxySettings proxySettings,
            KeyStoreSettings trustStoreSettings,
            boolean trustSelfSignedCertificates,
            final List<String> trustedHosts) {

        HttpClientBuilder builder = HttpClientBuilder.create()
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableCookieManagement()
                .disableRedirectHandling()
                .disableContentCompression()
                .setMaxConnTotal(maxConnections)
                .setDefaultRequestConfig(RequestConfig.custom().setStaleConnectionCheckEnabled(true).build())
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeoutMilliseconds).build())
                .useSystemProperties();

        HostnameVerifier hostnameVerifier = buildHostnameVerifier(trustSelfSignedCertificates, trustedHosts);
        builder.setSSLHostnameVerifier(hostnameVerifier);

        if (proxySettings != NO_PROXY) {
            HttpHost proxyHost = new HttpHost(proxySettings.host(), proxySettings.port());
            builder.setProxy(proxyHost);
            if(!isEmpty(proxySettings.getUsername()) && !isEmpty(proxySettings.getPassword())) {
                builder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxySettings.host(), proxySettings.port()),
                        new UsernamePasswordCredentials(proxySettings.getUsername(), proxySettings.getPassword()));
                builder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }

        final SSLContext sslContext = buildSslContext(trustStoreSettings, trustSelfSignedCertificates, trustedHosts);
        builder.setSSLContext(sslContext);

        return builder.build();
	}

    private static SSLContext buildSslContext(
        KeyStoreSettings trustStoreSettings,
        boolean trustSelfSignedCertificates,
        List<String> trustedHosts
    ) {
        if (trustStoreSettings != NO_STORE) {
            return buildSSLContextWithTrustStore(trustStoreSettings, trustSelfSignedCertificates, trustedHosts);
        } else if (trustSelfSignedCertificates) {
            return buildAllowAnythingSSLContext();
        } else {
            try {
                return SSLContextBuilder.create().loadTrustMaterial(new TrustSpecificHostsStrategy(trustedHosts)).build();
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                return throwUnchecked(e, null);
            }
        }
    }

    private static HostnameVerifier buildHostnameVerifier(boolean trustSelfSignedCertificates, List<String> trustedHosts) {
        if (trustSelfSignedCertificates) {
            return new NoopHostnameVerifier();
        } else {
            return new ExtraHostsHostnameVerifier(trustedHosts);
        }
    }

    public static CloseableHttpClient createClient(
            int maxConnections,
            int timeoutMilliseconds,
            ProxySettings proxySettings,
            KeyStoreSettings trustStoreSettings) {
        return createClient(maxConnections, timeoutMilliseconds, proxySettings, trustStoreSettings, true, Collections.<String>emptyList());
    }

    private static SSLContext buildSSLContextWithTrustStore(KeyStoreSettings trustStoreSettings, boolean trustSelfSignedCertificates, List<String> trustedHosts) {
        try {
            KeyStore trustStore = trustStoreSettings.loadStore();
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
                    .loadKeyMaterial(trustStore, trustStoreSettings.password().toCharArray())
                    .setProtocol("TLS");
            if (trustSelfSignedCertificates) {
                sslContextBuilder.loadTrustMaterial(new TrustSelfSignedStrategy());
            } else if (containsCertificate(trustStore)) {
                sslContextBuilder.loadTrustMaterial(trustStore, new TrustSpecificHostsStrategy(trustedHosts));
            } else {
                sslContextBuilder.loadTrustMaterial(new TrustSpecificHostsStrategy(trustedHosts));
            }
            return sslContextBuilder
                    .build();
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
        return createClient(maxConnections, timeoutMilliseconds, NO_PROXY, NO_STORE);
    }

	public static CloseableHttpClient createClient(int timeoutMilliseconds) {
		return createClient(DEFAULT_MAX_CONNECTIONS, timeoutMilliseconds);
	}

    public static CloseableHttpClient createClient(ProxySettings proxySettings) {
        return createClient(DEFAULT_MAX_CONNECTIONS, DEFAULT_TIMEOUT, proxySettings, NO_STORE);
    }

    public static CloseableHttpClient createClient() {
      return createClient(DEFAULT_TIMEOUT);
    }

    public static HttpUriRequest getHttpRequestFor(RequestMethod method, String url) {
        notifier().info("Proxying: " + method + " " + url);

        if (method.equals(GET))
            return new HttpGet(url);
        else if (method.equals(POST))
            return new HttpPost(url);
        else if (method.equals(PUT))
            return new HttpPut(url);
        else if (method.equals(DELETE))
            return new HttpDelete(url);
        else if (method.equals(HEAD))
            return new HttpHead(url);
        else if (method.equals(OPTIONS))
            return new HttpOptions(url);
        else if (method.equals(TRACE))
            return new HttpTrace(url);
        else if (method.equals(PATCH))
            return new HttpPatch(url);
        else
            return new GenericHttpUriRequest(method.toString(), url);
    }

    private static class ExtraHostsHostnameVerifier implements HostnameVerifier {

        private final List<String> trustedHosts;
        private final DefaultHostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();

        public ExtraHostsHostnameVerifier(List<String> trustedHosts) {
            this.trustedHosts = trustedHosts;
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            if (trustedHosts.contains(hostname)) {
                return true;
            } else {
                return defaultHostnameVerifier.verify(hostname, session);
            }
        }
    }
}
