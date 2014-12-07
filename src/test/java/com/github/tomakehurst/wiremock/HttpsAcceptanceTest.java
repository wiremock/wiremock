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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.google.common.io.Resources;
import com.jayway.restassured.RestAssured;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.jayway.restassured.config.SSLConfig.sslConfig;
import static org.hamcrest.Matchers.is;

public class HttpsAcceptanceTest {

    private static final int HTTPS_PORT = 8443;
    private WireMockServer wireMockServer;

    private void startServerWithSSL(int port,
                                    String keystorePath, String keystorePassword,
                                    String truststorePath, String truststorePassword,
                                    boolean needsClientAuth) {
        WireMockConfiguration config = wireMockConfig().httpsPort(port)
                .keystore(keystorePath)
                .keyPassword(keystorePassword)
                .truststore(truststorePath)
                .trustPassword(truststorePassword)
                .needClientAuth(needsClientAuth);

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        WireMock.configure();
    }

    private void startDefaultHttpsServer() {
        startServerWithSSL(HTTPS_PORT, null, null, null, null, false);
    }

    private void startHttpsServerWithKeystore(final String keystorePath) {
        startServerWithSSL(HTTPS_PORT, keystorePath, null, null, null, false);
    }

    private void startHttpsServerWithKeystore(final String keystorePath, final String keyPassword) {
        startServerWithSSL(HTTPS_PORT, keystorePath, keyPassword, null, null, false);
    }

    private void startHttpsServerWithClientAuth(final String keystorePath, final String truststorePath) {
        startServerWithSSL(HTTPS_PORT, keystorePath, "password", truststorePath, "password", true);
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = HTTPS_PORT;
    }

    /**
     * trust all hosts regardless if the SSL certificate is invalid
     */
    private void relaxHttpsValidation() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @After
    public void teardown() {
        wireMockServer.stop();
        RestAssured.reset();
    }

    @Test
    public void shouldReturnStubOnSpecifiedPort() throws Exception {
        relaxHttpsValidation();
        startDefaultHttpsServer();

        expectGetHttpsContent();
    }

    @Test(expected = NoHttpResponseException.class)
    public void emptyResponseFault() throws Throwable {
        relaxHttpsValidation();
        startDefaultHttpsServer();
        stubFor(get(urlEqualTo("/empty/response")).willReturn(
                aResponse()
                        .withFault(Fault.EMPTY_RESPONSE)));

        getAndThrowUnderlyingException("/empty/response");
    }

    @Test(expected = MalformedChunkCodingException.class)
    public void malformedResponseChunkFault() throws Throwable {
        relaxHttpsValidation();
        startDefaultHttpsServer();
        stubFor(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                        .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        getAndThrowUnderlyingException("/malformed/response");
    }

    @Test(expected = ProtocolException.class)
    public void randomDataOnSocketFault() throws Throwable {
        relaxHttpsValidation();
        startDefaultHttpsServer();
        stubFor(get(urlEqualTo("/random/data")).willReturn(
                aResponse()
                        .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));

        getAndThrowUnderlyingException("/random/data");
    }

    @Test(expected = FatalStartupException.class)
    public void throwsExceptionWhenBadAlternativeKeystore() {
        String testKeystorePath = Resources.getResource("stores/bad-keystore.jks").getFile();
        startHttpsServerWithKeystore(testKeystorePath);
    }

    @Test(expected = FatalStartupException.class)
    public void throwsExceptionWhenKeypasswordIsWrong() {
        String testKeystorePath = Resources.getResource("stores/test-keystore.jks").getFile();
        startHttpsServerWithKeystore(testKeystorePath, "wrong-pass");
    }

    @Test
    public void acceptsAlternativeKeystore() throws Exception {
        relaxHttpsValidation();

        String testKeystorePath = Resources.getResource("stores/test-keystore.jks").getFile();
        startHttpsServerWithKeystore(testKeystorePath);

        expectGetHttpsContent();
    }

    @Test(expected = SSLHandshakeException.class)
    public void expectDenyRequestIfNotTrusted() {
        String testKeystorePath = Resources.getResource("stores/test-keystore.jks").getFile();
        String testTruststorePath = Resources.getResource("stores/test-truststore.jks").getFile();
        startHttpsServerWithClientAuth(testKeystorePath, testTruststorePath);

        expectGetHttpsContent();
    }

    @Test
    public void expectGetHttpsContentWithClientAuth() throws Exception {
        String testKeystorePath = Resources.getResource("stores/test-keystore.jks").getFile();
        String testTruststorePath = Resources.getResource("stores/test-truststore.jks").getFile();
        startHttpsServerWithClientAuth(testKeystorePath, testTruststorePath);

        // Client carry certificate that the server trusts
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(createKeyStore(testTruststorePath, "password"), new TrustSelfSignedStrategy())
                .loadKeyMaterial(createKeyStore(testKeystorePath, "password"), "password".toCharArray())
                .useTLS()
                .build();

        RestAssured.config = RestAssured.config().sslConfig(sslConfig().sslSocketFactory(new org.apache.http.conn.ssl.SSLSocketFactory(sslcontext)));

        expectGetHttpsContent();
    }

    private KeyStore createKeyStore(final String path, final String password)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, password.toCharArray());

            return keyStore;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void expectGetHttpsContent() {
        final String path = "/https-test";
        final String content = "HTTPS content";

        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse().withStatus(200).withBody(content)));

        RestAssured.expect().statusCode(200)
                .when().get(path)
                .then().body(is(content));
    }

    private void getAndThrowUnderlyingException(String url) throws Throwable {
        try {
            RestAssured.get(url).body().asString();
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause != null) {
                throw ExceptionUtils.getRootCause(e);
            } else {
                throw e;
            }
        }
    }
}
