package com.github.tomakehurst.wiremock.direct;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectCallHttpServerIntegrationTest {

    @Test
    void exampleUsage() {
        DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
        WireMockServer wm = new WireMockServer(wireMockConfig().httpServerFactory(factory));
        wm.start(); // no-op

        DirectCallHttpServer server = factory.getHttpServer();

        Response response = server.stubRequest(mockRequest());
        assertEquals(404, response.getStatus());
    }

    @Test
    void withDelay() {
        DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
        WireMockServer wm = new WireMockServer(wireMockConfig().usingFilesUnderClasspath("classpath-filesource").httpServerFactory(factory));
        wm.start(); // no-op

        DirectCallHttpServer server = factory.getHttpServer();

        MockRequest mockRequest = mockRequest().url("/slow-response").method(GET);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Response response = server.stubRequest(mockRequest);
        stopwatch.stop();

        assertEquals(200, response.getStatus());
        assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(500L));
    }

    @Test
    void withFileBody() {
        DirectCallHttpServerFactory factory = new DirectCallHttpServerFactory();
        WireMockServer wm = new WireMockServer(wireMockConfig().usingFilesUnderClasspath("classpath-filesource").httpServerFactory(factory));
        wm.start(); // no-op

        DirectCallHttpServer server = factory.getHttpServer();

        MockRequest mockRequest = mockRequest().url("/test").method(GET);
        Response response = server.stubRequest(mockRequest);
        assertEquals("THINGS!", response.getBodyAsString());
    }
}
