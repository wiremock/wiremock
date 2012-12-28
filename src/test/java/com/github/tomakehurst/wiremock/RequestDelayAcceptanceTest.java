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
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class RequestDelayAcceptanceTest {

    public static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    public static final int LONGER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 3;

    private HttpClient httpClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(50, SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void addsDelayBeforeServingRequest() throws Exception {
        int delayMillis = SOCKET_TIMEOUT_MILLISECONDS / 2;

        WireMock.addRequestProcessingDelay(delayMillis);
        long start = System.currentTimeMillis();
        executeGetRequest();
        int timeTaken = (int) (System.currentTimeMillis() - start);

        assertThat(timeTaken, greaterThanOrEqualTo(delayMillis));
    }

    @Test(expected=SocketTimeoutException.class)
    public void causesSocketTimeoutExceptionWhenDelayGreaterThanSoTimeoutSetting() throws Exception {
        WireMock.addRequestProcessingDelay(SOCKET_TIMEOUT_MILLISECONDS * 2);
        executeGetRequest();
    }

    @Test
    public void resetResetsRequestDelay() throws Exception {
        WireMock.addRequestProcessingDelay(LONGER_THAN_SOCKET_TIMEOUT);
        try {
            executeGetRequest();
        } catch (IOException e) {
            assertThat(e, instanceOf(SocketTimeoutException.class));
        }

        WireMock.reset();

        executeGetRequest();
        // No exception expected
    }

    private void executeGetRequest() throws IOException {
        HttpGet get = new HttpGet("http://localhost:8080/anything");
        httpClient.execute(get);
    }
}
