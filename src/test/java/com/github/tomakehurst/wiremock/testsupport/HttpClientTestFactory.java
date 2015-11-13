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
package com.github.tomakehurst.wiremock.testsupport;

import com.github.tomakehurst.wiremock.common.KeyStoreSettings;
import com.github.tomakehurst.wiremock.common.ProxySettings;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;

import java.security.KeyStore;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.KeyStoreSettings.NO_STORE;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;

/* --- FOR ConcurrentDelayResponsesFixTest TEST SUPPORT ONLY ---
 * --- NOT REAL CODE ---
 * 
 * Just duplicates <b>all</b> of HttpClientFactory but adds support for an HttpConnectionManager
 * to allow more than two concurrent client connections for identical requests. 
 * 
 * NOTE: As per the comment for TestWireMockServer, this is a totally horrible way to extend 
 *       HttpClientFactory. It is *just* a quick and dirty test support implementation for
 *       ConcurrentDelayResponsesFixTest - didn't seem advisable to add it to HttpClientFactory 
 *       just for unit testing.
 *       
 * @see com.github.tomakehurst.wiremock.ConcurrentDelayedResponsesFixTest,
 *      com.github.tomakehurst.wiremock.http.TestWireMockServer,
 *      com.github.tomakehurst.wiremock.http.TestProxyResponseRenderer
 *      
 */
public class HttpClientTestFactory {

    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    public static HttpClient createClient(int maxConnections, int timeoutMilliseconds, ProxySettings proxySettings, KeyStoreSettings trustStoreSettings) {
        return createClient(maxConnections,timeoutMilliseconds,proxySettings,trustStoreSettings,null);
    }

    public static HttpClient createClient(int maxConnections, 
                                          int timeoutMilliseconds, 
                                          ProxySettings proxySettings, 
                                          KeyStoreSettings trustStoreSettings, 
                                          HttpClientConnectionManager connectionManager) {

        HttpClientBuilder builder = HttpClientBuilder.create()
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableCookieManagement()
                .disableRedirectHandling()
                .disableContentCompression()
                .setMaxConnTotal(maxConnections)
                .setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(timeoutMilliseconds).build())
                .setHostnameVerifier(new AllowAllHostnameVerifier());

        if (proxySettings != NO_PROXY) {
            HttpHost proxyHost = new HttpHost(proxySettings.host(), proxySettings.port());
            builder.setProxy(proxyHost);
        }

        if (trustStoreSettings != NO_STORE) {
            builder.setSslcontext(buildSSLContextWithTrustStore(trustStoreSettings));
        } else {
            builder.setSslcontext(buildAllowAnythingSSLContext());
        }

        if (connectionManager != null) {
            builder.setConnectionManager(connectionManager);
        }

        return builder.build();
	}

    private static SSLContext buildSSLContextWithTrustStore(KeyStoreSettings trustStoreSettings) {
        try {
            KeyStore trustStore = trustStoreSettings.loadStore();
            return SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .loadKeyMaterial(trustStore, trustStoreSettings.password().toCharArray())
                    .useTLS()
                    .build();
        } catch (Exception e) {
            return throwUnchecked(e, SSLContext.class);
        }
    }

    private static SSLContext buildAllowAnythingSSLContext() {
        try {
            return SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        } catch (Exception e) {
            return throwUnchecked(e, SSLContext.class);
        }
    }

    public static HttpClient createClient(int maxConnections, int timeoutMilliseconds) {
        return createClient(maxConnections, timeoutMilliseconds, NO_PROXY, NO_STORE);
    }
	
	public static HttpClient createClient(int timeoutMilliseconds) {
		return createClient(DEFAULT_MAX_CONNECTIONS, timeoutMilliseconds);
	}
	
	public static HttpClient createClient() {
		return createClient(30000);
	}
}
