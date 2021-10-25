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

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class StubbingWithBrowserProxyAcceptanceTest {

    static final String EXPECTED_RESPONSE_BODY = "Got it";

    @RegisterExtension
    public static WireMockExtension wm = WireMockExtension.newInstance().options(options()
            .dynamicPort()
            .enableBrowserProxying(true)
            .notifier(new ConsoleNotifier(true)))
    .configureStaticDsl(true)
    .build();

    static CloseableHttpClient client;

    @BeforeAll
    public static void init() {
        client = HttpClientBuilder.create()
                .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                        .setDnsResolver(new CustomLocalTldDnsResolver("internal"))
                        .build())
                .setProxy(new HttpHost("localhost", wm.getPort()))
                .build();
    }

    @Test
    public void matchesOnHostname() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withHost(equalTo("righthost.internal"))
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://righthost.internal/mypath").build();
        makeRequestAndAssertOk(request);
    }

    @Test
    public void doesNotMatchOnHostnameWhenIncorrect() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withHost(equalTo("righthost.internal"))
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://wronghost.internal/mypath").build();
        makeRequestAndAssertNotOk(request);
    }

    @Test
    public void matchesAnyHostnameWhenNotSpecified() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://whatever.internal/mypath").build();
        makeRequestAndAssertOk(request);
    }
    
    @Test
    public void matchesPortNumber() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withPort(1234)
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://localhost:1234/mypath").build();
        makeRequestAndAssertOk(request);
    }

    @Test
    public void doesNotMatchOnPortNumberWhenIncorrect() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withPort(1234)
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://localhost:4321/mypath").build();
        makeRequestAndAssertNotOk(request);
    }

    @Test
    public void matchesOnScheme() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withScheme("http")
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://whatever/mypath").build();
        makeRequestAndAssertOk(request);
    }

    @Test
    public void doesNotMatchWhenSchemeIncorrect() throws Exception {
        stubFor(get(urlPathEqualTo("/mypath"))
                .withScheme("https")
                .willReturn(ok(EXPECTED_RESPONSE_BODY))
        );

        ClassicHttpRequest request = ClassicRequestBuilder.get("http://whatever/mypath").build();
        makeRequestAndAssertNotOk(request);
    }
    
    private void makeRequestAndAssertOk(ClassicHttpRequest request) throws Exception {
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(EntityUtils.toString(response.getEntity()), is(EXPECTED_RESPONSE_BODY));
        }
    }

    private void makeRequestAndAssertNotOk(ClassicHttpRequest request) throws Exception {
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(EntityUtils.toString(response.getEntity()), not(is(EXPECTED_RESPONSE_BODY)));
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

        @Override
        public String resolveCanonicalHostname(String host) throws UnknownHostException {
            final InetAddress[] resolvedAddresses = resolve(host);
            if (resolvedAddresses.length > 0) {
                return resolvedAddresses[0].getCanonicalHostName();
            }
            return host;
        }
    }
}
