package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.google.common.io.Resources;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Http2AcceptanceTest {
    private static final String TRUST_STORE_PATH = toPath("test-clientstore");
    private static final String KEY_STORE_PATH = toPath("test-keystore");
    private static final String TRUST_STORE_PASSWORD = "mytruststorepassword";

    private WireMockServer wireMockServer;
    private WireMockServer proxy;
    private org.apache.http.client.HttpClient httpClient;

    @After
    public void serverShutdown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }

        if (proxy != null) {
            proxy.shutdown();
        }
    }


    private static String toPath(String resourcePath) {
        try {
            return new File(Resources.getResource(resourcePath).toURI()).getCanonicalPath();
        } catch (Exception e) {
            return throwUnchecked(e, String.class);
        }
    }

    @Test
    public void shouldReturnStubOnSpecifiedPortHttp2() throws Exception {
        String testTrustStorePath = TRUST_STORE_PATH;
        String testClientCertPath = TRUST_STORE_PATH;

        startServerEnforcingClientCert(KEY_STORE_PATH, testTrustStorePath, TRUST_STORE_PASSWORD);
        wireMockServer.stubFor(get(urlEqualTo("/http2-test")).willReturn(aResponse().withStatus(200).withBody("HTTP2 content")));

        assertThat(http2ContentFor(url("/http2-test"), testClientCertPath, TRUST_STORE_PASSWORD), is("HTTP2 content"));
    }

    @SuppressWarnings("Duplicates")
    private void startServerEnforcingClientCert(String keystorePath, String truststorePath, String trustStorePassword) {
        WireMockConfiguration config = wireMockConfig().dynamicPort().dynamicHttpsPort();
        if (keystorePath != null) {
            config.keystorePath(keystorePath);
        }
        if (truststorePath != null) {
            config.trustStorePath(truststorePath);
            config.trustStorePassword(trustStorePassword);
            config.needClientAuth(true);
        }
        config.bindAddress("localhost");

        wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        WireMock.configureFor("https", "localhost", wireMockServer.httpsPort());

        httpClient = HttpClientFactory.createClient();
    }

    private String url(String path) {
        return String.format("https://localhost:%d%s", wireMockServer.httpsPort(), path);
    }

    private String http2ContentFor(String url, String clientTrustStore, String trustStorePassword) throws Exception {
        KeyStore trustStore = readKeyStore(clientTrustStore, trustStorePassword);

        final KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm());
        final Set<KeyManager> keymanagers = new HashSet<>();

        kmfactory.init(trustStore, trustStorePassword.toCharArray());
        final KeyManager[] kms =  kmfactory.getKeyManagers();
        if (kms != null) {
            for (final KeyManager km : kms) {
                keymanagers.add(km);
            }
        }

        SSLContext sslContext = SSLContext.getInstance("TLS", "Conscrypt");
        sslContext.init(keymanagers.toArray(new KeyManager[0]), SslContextFactory.TRUST_ALL_CERTS, null);

        HttpClientTransport transport = new HttpClientTransportOverHTTP2(
            new HTTP2Client());

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setProvider("Conscrypt");
        sslContextFactory.setSslContext(sslContext);
        org.eclipse.jetty.client.HttpClient httpClient = new org.eclipse.jetty.client.HttpClient(transport, sslContextFactory);
        httpClient.setFollowRedirects(false);
        httpClient.start();

        ContentResponse response = httpClient.GET(url);
        return response.getContentAsString();
    }

    static KeyStore readKeyStore(String path, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore trustStore  = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream instream = new FileInputStream(path);
        try {
            trustStore.load(instream, password.toCharArray());
        } finally {
            instream.close();
        }
        return trustStore;
    }
}
