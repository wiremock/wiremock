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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.google.common.io.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;
import org.mortbay.log.Log;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HttpsAcceptanceTest {

    private static final int HTTPS_PORT = 8443;

    private WireMockServer wireMockServer;
    private HttpClient httpClient;

    private void startServerEnforcingClientCert(String keystorePath, String truststorePath) {
        WireMockConfiguration config = wireMockConfig().httpsPort(HTTPS_PORT);
        if (keystorePath != null) {
            config.keystorePath(keystorePath);
        }
        if (truststorePath != null) {
            config.truststorePath(truststorePath);
            config.needClientAuth(true);
        }
        config.bindAddress("localhost");

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        WireMock.configure();

        httpClient = HttpClientFactory.createClient();
    }

    private void startServerWithKeystore(String keystorePath) {
        WireMockConfiguration config = wireMockConfig().httpsPort(HTTPS_PORT);
        if (keystorePath != null) {
            config.keystorePath(keystorePath);
        }

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        WireMock.configure();

        httpClient = HttpClientFactory.createClient();
    }

    private void startServerWithDefaultKeystore() {
        startServerWithKeystore(null);
    }

    @After
    public void serverShutdown() {
        wireMockServer.stop();
    }

    @Test
    public void shouldReturnStubOnSpecifiedPort() throws Exception {
        startServerWithDefaultKeystore();
        stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

        assertThat(contentFor(url("/https-test")), is("HTTPS content"));
    }

    @Test
    public void emptyResponseFault() {
        startServerWithDefaultKeystore();
        stubFor(get(urlEqualTo("/empty/response")).willReturn(
                aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)));


        getAndAssertUnderlyingExceptionInstanceClass(url("/empty/response"), NoHttpResponseException.class);
    }

    @Test
    public void malformedResponseChunkFault() {
        startServerWithDefaultKeystore();
        stubFor(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                        .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        getAndAssertUnderlyingExceptionInstanceClass(url("/malformed/response"), MalformedChunkCodingException.class);
    }

    @Test
    public void randomDataOnSocketFault() {
        startServerWithDefaultKeystore();
        stubFor(get(urlEqualTo("/random/data")).willReturn(
                aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        getAndAssertUnderlyingExceptionInstanceClass(url("/random/data"), ProtocolException.class);
    }

    @Test(expected = Exception.class)
    public void throwsExceptionWhenBadAlternativeKeystore() {
        String testKeystorePath = Resources.getResource("bad-keystore").toString();
        startServerWithKeystore(testKeystorePath);
    }

    @Test
    public void acceptsAlternativeKeystore() throws Exception {
        String testKeystorePath = Resources.getResource("test-keystore").toString();
        startServerWithKeystore(testKeystorePath);
        stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

        assertThat(contentFor(url("/https-test")), is("HTTPS content"));
    }

    @Test(expected=SSLHandshakeException.class)
    public void rejectsWithoutClientCertificate() throws Exception {
        String testTrustStorePath = Resources.getResource("test-clientstore").toString();
        String testKeystorePath = Resources.getResource("test-keystore").toString();
        startServerEnforcingClientCert(testKeystorePath, testTrustStorePath);
        stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));

        contentFor(url("/https-test")); // this lacks the required client certificate
    }

    @Test
    public void acceptWithClientCertificate() throws Exception {
        String testTrustStorePath = Resources.getResource("test-clientstore").toString();
        String testKeystorePath = Resources.getResource("test-keystore").toString();
        String testClientCertPath = Resources.getResource("test-clientstore").toString();

        startServerEnforcingClientCert(testKeystorePath, testTrustStorePath);
        stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));


        assertThat(secureContentFor(url("/https-test"), testKeystorePath, testClientCertPath), is("HTTPS content"));
    }


    @Test
    public void testClientCertificate() throws Exception {
        String testTrustStorePath = Resources.getResource("test-clientstore").toString();
        String testKeystorePath = Resources.getResource("test-keystore").toString();
        String testClientCertPath = Resources.getResource("test-clientstore").toString();

        startServerEnforcingClientCert(testKeystorePath, testTrustStorePath);
        //stubFor(get(urlEqualTo("/https-test")).willReturn(aResponse().withStatus(200).withBody("HTTPS content")));


        assertThat(secureContentFor("https://dmtest2.intranet.dev.int.devlab.redhat.com:8443/", testKeystorePath, testClientCertPath), is("protected\n"));
    }

    private String url(String path) {
        return String.format("https://localhost:%d%s", HTTPS_PORT, path);
    }

    private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
        boolean thrown = false;
        try {
            contentFor(url);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                assertThat(e.getCause(), instanceOf(expectedClass));
            } else {
                assertThat(e, instanceOf(expectedClass));
            }

            thrown = true;
        }

        assertTrue("No exception was thrown", thrown);
    }

    private String contentFor(String url) throws Exception {
        HttpGet get = new HttpGet(url);
        HttpResponse response = httpClient.execute(get);
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }

    private String secureContentFor(String url, String clientKeyStore, String clientTrustStore) throws Exception {
        KeyStore trustStore = readKeyStore(clientTrustStore);
        KeyStore keyStore = readKeyStore(clientKeyStore);

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, "password".toCharArray())
                .loadKeyMaterial(trustStore, "password".toCharArray())
                .useTLS()
                .build();

        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        /*
                        new String[] { "TLSv1" }, // supported protocols
                null, //new String[] { "TLS_DHE_RSA_WITH_AES_128_CBC_SHA" }, // supported cipher suites
         */
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();

        HttpGet get = new HttpGet(url);
        Log.warn("test A");
        HttpResponse response = httpClient.execute(get);
        Log.warn("test B");
        String content = EntityUtils.toString(response.getEntity());
        return content;
    }

    private KeyStore readKeyStore(String resourceURL) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(new URL(resourceURL).getFile());
        try {
            trustStore.load(instream, "password".toCharArray());
        } finally {
            instream.close();
        }
        return trustStore;
    }
}
