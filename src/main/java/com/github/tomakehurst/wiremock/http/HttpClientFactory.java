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

import com.github.tomakehurst.wiremock.common.ProxySettings;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ProxySettings.NO_PROXY;
import static org.apache.http.client.params.ClientPNames.HANDLE_REDIRECTS;
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY;

public class HttpClientFactory {

    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    public static final int DEFAULT_MAX_CONNECTIONS = 50;

    public static HttpClient createClient(
            int maxConnections, int timeoutMilliseconds, ProxySettings proxySettings) {
        PoolingClientConnectionManager cm = createClientConnectionManagerWithSSLSettings(maxConnections);
		HttpClient client = new DefaultHttpClient(cm);
        HttpParams params = client.getParams();
        params.setParameter(HANDLE_REDIRECTS, false);
        HttpConnectionParams.setConnectionTimeout(params, timeoutMilliseconds);
        HttpConnectionParams.setSoTimeout(params, timeoutMilliseconds);

        if (proxySettings != NO_PROXY) {
            HttpHost proxyHost = new HttpHost(proxySettings.host(), proxySettings.port());
            params.setParameter(DEFAULT_PROXY, proxyHost);
        }

        return client;
	}

    public static HttpClient createClient(int maxConnections, int timeoutMilliseconds) {
        return createClient(maxConnections, timeoutMilliseconds, NO_PROXY);
    }
	
	public static HttpClient createClient(int timeoutMilliseconds) {
		return createClient(DEFAULT_MAX_CONNECTIONS, timeoutMilliseconds);
	}
	
	public static HttpClient createClient() {
		return createClient(30000);
	}

    public static PoolingClientConnectionManager createClientConnectionManagerWithSSLSettings() {
        return createClientConnectionManagerWithSSLSettings(DEFAULT_MAX_CONNECTIONS);
    }

    public static PoolingClientConnectionManager createClientConnectionManagerWithSSLSettings(int maxConnections) {
        try {
            X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
            SSLSocketFactory socketFactory = createSslSocketFactory(hostnameVerifier);
            SchemeRegistry schemeRegistry = createSchemeRegistry(socketFactory);
            PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
            cm.setDefaultMaxPerRoute(maxConnections);
            cm.setMaxTotal(maxConnections);
            return cm;
        } catch (Exception e) {
            return throwUnchecked(e, PoolingClientConnectionManager.class);
        }
    }


    private static SchemeRegistry createSchemeRegistry(SSLSocketFactory socketFactory) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme(HTTP_PROTOCOL, HTTP_PORT, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme(HTTPS_PROTOCOL, HTTPS_PORT, socketFactory));
        return schemeRegistry;
    }

    private static SSLSocketFactory createSslSocketFactory(X509HostnameVerifier hostnameVerifier) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustEverything(), new SecureRandom());
        return new SSLSocketFactory(sslContext, hostnameVerifier);
    }

    private static TrustManager[] trustEverything() {
        return new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
    }
	
}
