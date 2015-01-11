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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Stopwatch;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class RequestDelayAcceptanceTest {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    private static final int LONGER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 3;

    private HttpClient httpClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT, Options.DYNAMIC_PORT);

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(50, SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void addsDelayBeforeServingHttpRequest() throws Exception {
        executeRequestAndVerifyDelayAdded("http");
    }

    @Test
    public void addsDelayBeforeServingHttpsRequest() throws Exception {
        executeRequestAndVerifyDelayAdded("https");
    }

    private void executeRequestAndVerifyDelayAdded(String protocol) throws Exception {
        long delayMillis = SOCKET_TIMEOUT_MILLISECONDS / 2;
        WireMock.addRequestProcessingDelay((int) delayMillis);

        Stopwatch stopwatch = Stopwatch.createStarted();
        executeGetRequest(protocol);
        stopwatch.stop();

        assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(delayMillis));
    }

    @Test(expected=SocketTimeoutException.class)
    public void causesSocketTimeoutExceptionWhenDelayGreaterThanSoTimeoutSetting() throws Exception {
        WireMock.addRequestProcessingDelay(LONGER_THAN_SOCKET_TIMEOUT);
        executeHttpGetRequest();
    }

    @Ignore("This currently causes an SSLPeerUnverifiedException. Not sure it's possible to do SocketTimeoutException over HTTPS in Java.")
    @Test(expected=SocketTimeoutException.class)
    public void causesSocketTimeoutExceptionOverHttpsWhenDelayGreaterThanSoTimeoutSetting() throws Exception {
        WireMock.addRequestProcessingDelay(LONGER_THAN_SOCKET_TIMEOUT);
        executeHttpsGetRequest();
    }

    @Test
    public void resetResetsRequestDelay() throws Exception {
        WireMock.addRequestProcessingDelay(LONGER_THAN_SOCKET_TIMEOUT);
        try {
            executeHttpGetRequest();
        } catch (IOException e) {
            assertThat(e, instanceOf(SocketTimeoutException.class));
        }

        WireMock.reset();

        executeHttpGetRequest();
        // No exception expected
    }

    private void executeHttpGetRequest() throws IOException {
        executeGetRequest("http");
    }

    private void executeHttpsGetRequest() throws IOException {
        executeGetRequest("https");
    }

    private void executeGetRequest(String protocol) throws IOException {
        int port = protocol.equals("https") ? wireMockRule.httpsPort() : wireMockRule.port();
        String url = String.format("%s://localhost:%d/anything", protocol, port);
        HttpGet get = new HttpGet(url);
        httpClient.execute(get);
    }
}
