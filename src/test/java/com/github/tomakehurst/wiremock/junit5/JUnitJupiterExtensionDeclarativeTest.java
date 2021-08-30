package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class JUnitJupiterExtensionDeclarativeTest {

    CloseableHttpClient client;

    @BeforeEach
    void init() {
        client = HttpClientFactory.createClient();
    }

    @Test
    void provides_wiremock_info_as_method_parameter(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        assertNotNull(wmRuntimeInfo);
        assertNotNull(wmRuntimeInfo.getWireMock());

        assertThrows(IllegalStateException.class, wmRuntimeInfo::getHttpsPort); // HTTPS is off by default

        WireMock wireMock = wmRuntimeInfo.getWireMock();
        wireMock.register(get("/instance-dsl").willReturn(ok()));
        HttpGet request = new HttpGet(wmRuntimeInfo.getHttpBaseUrl() + "/instance-dsl");
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(response.getStatusLine().getStatusCode(), is(200));
        }

        stubFor(get("/static-dsl").willReturn(ok()));
        request = new HttpGet(wmRuntimeInfo.getHttpBaseUrl() + "/static-dsl");
        try (CloseableHttpResponse response = client.execute(request)) {
            assertThat(response.getStatusLine().getStatusCode(), is(200));
        }
    }

}
