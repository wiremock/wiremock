package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class StubbingWithBrowserProxyAcceptanceTest {

    @ClassRule
    public static WireMockClassRule wm = new WireMockClassRule(wireMockConfig()
            .dynamicPort()
            .enableBrowserProxying(true)
            .notifier(new ConsoleNotifier(true))
    );

    @Rule
    public WireMockClassRule instance = wm;

    static CloseableHttpClient client;

    @BeforeClass
    public static void init() {
        client = HttpClientBuilder.create()
                .setDnsResolver(new CustomLocalTldDnsResolver("internal"))
                .setProxy(new HttpHost("localhost", wm.port()))
                .build();
    }

    @Test
    public void matchesOnHostname() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withHost(equalTo("righthost.internal"))
                .willReturn(ok("Got it"))
        );

        HttpUriRequest request = RequestBuilder.get("http://righthost.internal/mypath").build();
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(EntityUtils.toString(response.getEntity()), is("Got it"));
        }
    }

    @Test
    public void doesNotMatchOnHostnameWhenIncorrect() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withHost(equalTo("righthost.internal"))
                .willReturn(ok("Got it"))
        );

        HttpUriRequest request = RequestBuilder.get("http://wronghost.internal/mypath").build();
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(EntityUtils.toString(response.getEntity()), not(is("Got it")));
        }
    }

    @Test
    public void matchesAnyHostnameWhenNotSpecified() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .willReturn(ok("Got it"))
        );

        HttpUriRequest request = RequestBuilder.get("http://whatever.internal/mypath").build();
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(EntityUtils.toString(response.getEntity()), is("Got it"));
        }
    }

    private static class CustomLocalTldDnsResolver implements DnsResolver {

        private final String tldToSendToLocalhost;

        public CustomLocalTldDnsResolver(String tldToSendToLocalhost) {
            this.tldToSendToLocalhost = tldToSendToLocalhost;
        }

        @Override
        public InetAddress[] resolve(String host) throws UnknownHostException {

            if (host.endsWith("." + tldToSendToLocalhost)) {
                return new InetAddress[] { InetAddress.getLocalHost() };
            } else {
                return new SystemDefaultDnsResolver().resolve(host);
            }
        }
    }
}
