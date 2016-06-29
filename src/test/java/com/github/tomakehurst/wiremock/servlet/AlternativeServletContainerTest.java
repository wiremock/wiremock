package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AlternativeServletContainerTest {

    @Rule
    public WireMockRule wm = new WireMockRule(options().httpServerFactory(new AltHttpServerFactory()));
    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
        WireMock.configureFor(wm.port());
    }

    @Test
    public void supportsAlternativeHttpServerForBasicStub() {
        stubFor(get(urlEqualTo("/alt-server")).willReturn(aResponse().withStatus(204)));

        assertThat(client.get("/alt-server").statusCode(), is(204));
    }

    @Test
    public void supportsAlternativeHttpServerForFaultInjection() {
        stubFor(get(urlEqualTo("/alt-server")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        WireMockResponse response = client.get("/alt-server");

        assertThat(response.statusCode(), is(418));
        assertThat(response.content(), is("No fault injector is configured!"));
    }
}
