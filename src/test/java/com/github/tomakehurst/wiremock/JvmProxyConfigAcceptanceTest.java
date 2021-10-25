package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.HttpClient4Factory;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.google.common.io.ByteStreams;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class JvmProxyConfigAcceptanceTest {

    WireMockServer wireMockServer;

    @AfterEach
    public void cleanup() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    public void configuresHttpProxyingOnlyFromAWireMockServer() throws Exception {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(get("/stuff")
                              .withHost(equalTo("example.com"))
                .willReturn(ok("Proxied stuff")));

        assertThat(getContentUsingDefaultJvmHttpClient("http://example.com/stuff"), is("Proxied stuff"));
    }

    @Test
    public void configuresHttpsProxyingOnlyFromAWireMockServer() throws Exception {
        CloseableHttpClient httpClient = HttpClient4Factory.createClient();

        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().enableBrowserProxying(true));
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        wireMockServer.stubFor(get("/stuff")
                              .withHost(equalTo("example.com"))
                .willReturn(ok("Proxied stuff")));

        try (CloseableHttpResponse response = httpClient.execute(new HttpGet("https://example.com/stuff"))) {
            assertThat(EntityUtils.toString(response.getEntity()), is("Proxied stuff"));
        }
    }

    @Test
    public void restoresPreviousSettings() {
        String previousHttpProxyHost = "prevhttpproxyhost";
        String previousHttpProxyPort = "1234";
        String previousHttpsProxyHost = "prevhttpsproxyhost";
        String previousHttpsProxyPort = "4321";
        String previousNonProxyHosts = "blah.com";
        System.setProperty("http.proxyHost", previousHttpProxyHost);
        System.setProperty("http.proxyPort", previousHttpProxyPort);
        System.setProperty("https.proxyHost", previousHttpsProxyHost);
        System.setProperty("https.proxyPort", previousHttpsProxyPort);
        System.setProperty("http.nonProxyHosts", previousNonProxyHosts);

        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        JvmProxyConfigurer.configureFor(wireMockServer);

        assertThat(System.getProperty("http.proxyHost"), is("localhost"));
        assertThat(System.getProperty("http.proxyPort"), is(String.valueOf(wireMockServer.port())));
        assertThat(System.getProperty("https.proxyHost"), is("localhost"));
        assertThat(System.getProperty("https.proxyPort"), is(String.valueOf(wireMockServer.port())));
        assertThat(System.getProperty("http.nonProxyHosts"), is("localhost|127.*|[::1]"));

        JvmProxyConfigurer.restorePrevious();

        assertThat(System.getProperty("http.proxyHost"), is(previousHttpProxyHost));
        assertThat(System.getProperty("http.proxyPort"), is(previousHttpProxyPort));
        assertThat(System.getProperty("https.proxyHost"), is(previousHttpsProxyHost));
        assertThat(System.getProperty("https.proxyPort"), is(previousHttpsProxyPort));
        assertThat(System.getProperty("http.nonProxyHosts"), is(previousNonProxyHosts));
    }

    private String getContentUsingDefaultJvmHttpClient(String url) throws Exception {
        final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        try (InputStream in = urlConnection.getInputStream()) {
            return new String(ByteStreams.toByteArray(in));
        }
    }
}
