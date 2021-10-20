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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import static com.github.tomakehurst.wiremock.HttpsAcceptanceTest.readKeyStore;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PASSWORD;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.TRUST_STORE_PATH;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HttpsBrowserProxyClientAuthAcceptanceTest {

    private static final String NO_PREEXISTING_KEYSTORE_PATH = tempNonExistingPath("wiremock-keystores", "ca-keystore.jks");

    @RegisterExtension
    public static WireMockExtension target = WireMockExtension.newInstance().options(options()
            .httpDisabled(true)
            .dynamicHttpsPort()
            .needClientAuth(true)
            .trustStorePath(TRUST_STORE_PATH)
            .trustStorePassword(TRUST_STORE_PASSWORD))
    .build();

    @RegisterExtension
    public WireMockExtension proxy = WireMockExtension.newInstance().options(options()
            .dynamicPort()
            .enableBrowserProxying(true)
            .caKeystorePath(NO_PREEXISTING_KEYSTORE_PATH)
            .trustedProxyTargets("localhost")
            .needClientAuth(true) // fine to set this to false, but more "realistic" for it to be true
            .trustStorePath(TRUST_STORE_PATH)
            .trustStorePassword(TRUST_STORE_PASSWORD))
    .build();

    @Test
    public void canDoClientAuthEndToEndWhenProxying() throws Exception {
        target.stubFor(get("/whatever").willReturn(aResponse().withBody("Success")));

        CloseableHttpClient testClient = buildHttpClient();
        CloseableHttpResponse response = testClient.execute(new HttpGet(target.url("/whatever")));

        assertThat(response.getStatusLine().getStatusCode(), is(HTTP_OK));
        assertThat(EntityUtils.toString(response.getEntity()), is("Success"));
    }

    private static String tempNonExistingPath(String prefix, String filename) {
        try {
            Path tempDirectory = Files.createTempDirectory(prefix);
            return tempDirectory.resolve(filename).toFile().getAbsolutePath();
        } catch (IOException e) {
            return throwUnchecked(e, null);
        }
    }

    private CloseableHttpClient buildHttpClient() throws Exception {
        KeyStore trustStore = readKeyStore(TRUST_STORE_PATH, TRUST_STORE_PASSWORD);

        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .loadKeyMaterial(trustStore, TRUST_STORE_PASSWORD.toCharArray())
                .build();

        HttpHost proxyInfo = new HttpHost("localhost", proxy.getRuntimeInfo().getHttpPort());
        return HttpClientBuilder.create()
                .disableAuthCaching()
                .disableAutomaticRetries()
                .disableCookieManagement()
                .disableRedirectHandling()
                .setSSLContext(sslcontext)
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .setProxy(proxyInfo)
                .build();
    }
}
