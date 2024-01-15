package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseDefinitionBuilderAcceptanceTest {

    private WireMockServer wm;
    private WireMockTestClient client;

    @AfterEach
    public void stopServer() {
        wm.stop();
    }

    private void initialise() {
        wm = new WireMockServer(options().dynamicPort().dynamicHttpsPort());
        wm.start();
        client = new WireMockTestClient(wm.port());
    }

    @Test
    void wireMockServerWithStubForWithGzipDisabledTrue() {
        initialise();

        wm.stubFor(
                get(urlEqualTo("/todo/items"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("Here is some kind of response body"
                                                + "Here is some kind of response body"
                                                + "Here is some kind of response body"
                                        )));

        WireMockResponse compressedResponse =
                client.get("/todo/items", new TestHttpHeader("Accept-Encoding", "gzip"));

        wm.stubFor(
                get(urlEqualTo("/todo/items"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withGzipDisabled(true)
                                        .withBody("Here is some kind of response body"
                                                + "Here is some kind of response body"
                                                + "Here is some kind of response body"
                                        )));

        WireMockResponse ordinaryResponse =
                client.get("/todo/items", new TestHttpHeader("Accept-Encoding", "gzip"));

        assertTrue(compressedResponse.content().length() < ordinaryResponse.content().length());
        assertTrue(Gzip.isGzipped(compressedResponse.binaryContent()));
        assertFalse(Gzip.isGzipped(ordinaryResponse.binaryContent()));
    }
}
