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
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyStore;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.KeyStoreSettings.NO_STORE;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;

public class HttpClientFactory {

    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    public static HttpClient createClient(
            int maxConnections, int timeoutMilliseconds, ProxySettings proxySettings, KeyStoreSettings trustStoreSettings) {

        HttpClientBuilder builder = HttpClientBuilder.create()
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableCookieManagement()
                .disableRedirectHandling()
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
