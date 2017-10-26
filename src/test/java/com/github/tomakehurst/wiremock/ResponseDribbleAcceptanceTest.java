package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class ResponseDribbleAcceptanceTest {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    private static final int DOUBLE_THE_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 2;

    private static final byte[] BODY_OF_THREE_BYTES = new byte[] {1, 2, 3};

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT, Options.DYNAMIC_PORT);

    private HttpClient httpClient;

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void requestIsSuccessfulButTakesLongerThanSocketTimeoutWhenDribbleIsEnabled() throws Exception {

        stubFor(get(urlEqualTo("/delayedDribble")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withBody(BODY_OF_THREE_BYTES)
                        .withChunkedDribbleDelay(BODY_OF_THREE_BYTES.length, DOUBLE_THE_SOCKET_TIMEOUT)));

        HttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/delayedDribble", wireMockRule.port())));

        assertThat(response.getStatusLine().getStatusCode(), is(200));

        long start = System.currentTimeMillis();
        byte[] responseBody = IOUtils.toByteArray(response.getEntity().getContent());
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(BODY_OF_THREE_BYTES, is(responseBody));
        assertThat(duration, greaterThanOrEqualTo(SOCKET_TIMEOUT_MILLISECONDS));
    }

    @Test
    public void requestIsSuccessfulAndBelowSocketTimeoutWhenDribbleIsDisabled() throws Exception {
        stubFor(get(urlEqualTo("/nonDelayedDribble")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withBody(BODY_OF_THREE_BYTES)));

        HttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/nonDelayedDribble", wireMockRule.port())));

        assertThat(response.getStatusLine().getStatusCode(), is(200));

        long start = System.currentTimeMillis();
        byte[] responseBody = IOUtils.toByteArray(response.getEntity().getContent());
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(BODY_OF_THREE_BYTES, is(responseBody));
        assertThat(duration, lessThan(SOCKET_TIMEOUT_MILLISECONDS));
    }
}