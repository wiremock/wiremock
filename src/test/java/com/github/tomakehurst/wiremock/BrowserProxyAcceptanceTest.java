package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BrowserProxyAcceptanceTest {

    @ClassRule
    @Rule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig().port(8080).httpsPort(8081));

    private WireMockServer proxy;
    private WireMockTestClient testClient;

    @Before
    public void addAResourceToProxy() {
        stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));
        testClient = new WireMockTestClient();
    }

    @After
    public void stopServer() {
        if (proxy.isRunning()) {
            proxy.stop();
        }
    }

    @Test
    public void canProxyHttp() {
        proxy = new WireMockServer(wireMockConfig()
                .port(9090)
                .enableBrowserProxying(true));
        proxy.start();

        stubFor(get(urlEqualTo("/whatever")).willReturn(aResponse().withBody("Got it")));

        assertThat(testClient.getViaProxy("http://localhost:8080/whatever", proxy.port()).content(), is("Got it"));
    }

}
