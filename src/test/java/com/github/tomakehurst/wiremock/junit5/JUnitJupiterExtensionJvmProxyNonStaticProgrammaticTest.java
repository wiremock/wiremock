package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.http.HttpClient4Factory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JUnitJupiterExtensionJvmProxyNonStaticProgrammaticTest {

    @RegisterExtension
    WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .configureStaticDsl(true)
            .proxyMode(true)
            .build();

    CloseableHttpClient client;

    @BeforeEach
    void init() {
        client = HttpClient4Factory.createClient();
    }

    @Test
    void configures_jvm_proxy_and_enables_browser_proxying() throws Exception {
        stubFor(get("/things")
                .withHost(equalTo("one.my.domain"))
                .willReturn(ok("1")));

        stubFor(get("/things")
                .withHost(equalTo("two.my.domain"))
                .willReturn(ok("2")));

        assertThat(getContent("http://one.my.domain/things"), is("1"));
        assertThat(getContent("https://two.my.domain/things"), is("2"));
    }

    private String getContent(String url) throws Exception {
        try (CloseableHttpResponse response = client.execute(new HttpGet(url))) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}
