package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SocketAcceptDelayAcceptanceTest {

    public static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    public static final int A_BIT_LONGER_THAN_SOCKET_TIMEOUT = 550;
    public static final int LONG_ENOUGH_FOR_SERVER_THREAD_TO_FINISH_SLEEPING = 60;

    private HttpClient httpClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(50, SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test(expected=SocketTimeoutException.class)
    public void addsDelayToSocketAcceptanceForDefinedNumberOfRequests() throws Exception {
        WireMock.addRequestProcessingDelay(3000);
        executeGetRequest();
    }

    @Test
    public void resetAlsoResetsRequestDelay() throws Exception {
        WireMock.addRequestProcessingDelay(A_BIT_LONGER_THAN_SOCKET_TIMEOUT);
        try {
            executeGetRequest();
        } catch (IOException e) {
            assertThat(e, instanceOf(SocketTimeoutException.class));
        }

        WireMock.reset();
        Thread.sleep(LONG_ENOUGH_FOR_SERVER_THREAD_TO_FINISH_SLEEPING);

        executeGetRequest();
        // No exception expected
    }

    private void executeGetRequest() throws IOException {
        HttpGet get = new HttpGet("http://localhost:8080/anything");
        httpClient.execute(get);
    }
}
