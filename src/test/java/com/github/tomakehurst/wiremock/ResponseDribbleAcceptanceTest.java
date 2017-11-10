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

import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class ResponseDribbleAcceptanceTest {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    private static final int DOUBLE_THE_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 2;

    private static final byte[] BODY_BYTES = "the long sentence being sent".getBytes();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(DYNAMIC_PORT, DYNAMIC_PORT);

    private HttpClient httpClient;

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void requestIsSuccessfulButTakesLongerThanSocketTimeoutWhenDribbleIsEnabled() throws Exception {
        stubFor(get("/delayedDribble").willReturn(
                ok()
                    .withBody(BODY_BYTES)
                    .withChunkedDribbleDelay(BODY_BYTES.length, DOUBLE_THE_SOCKET_TIMEOUT)));

        long start = System.currentTimeMillis();
        HttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/delayedDribble", wireMockRule.port())));
        byte[] responseBody = IOUtils.toByteArray(response.getEntity().getContent());
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(responseBody, is(BODY_BYTES));
        assertThat(duration, greaterThanOrEqualTo(SOCKET_TIMEOUT_MILLISECONDS));
        assertThat((double) duration, closeTo(DOUBLE_THE_SOCKET_TIMEOUT, 100.0));
    }

    @Test
    public void servesAStringBodyInChunks() throws Exception {
        final int TOTAL_TIME = 300;

        stubFor(get("/delayedDribble").willReturn(
            ok()
                .withBody("Send this in many pieces please!!!")
                .withChunkedDribbleDelay(2, TOTAL_TIME)));

        long start = System.currentTimeMillis();
        HttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/delayedDribble", wireMockRule.port())));
        String responseBody = EntityUtils.toString(response.getEntity());
        double duration = (double) (System.currentTimeMillis() - start);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(responseBody, is("Send this in many pieces please!!!"));
        assertThat(duration, closeTo(TOTAL_TIME, 50.0));
    }

    @Test
    public void requestIsSuccessfulAndBelowSocketTimeoutWhenDribbleIsDisabled() throws Exception {
        stubFor(get("/nonDelayedDribble").willReturn(
                ok()
                    .withBody(BODY_BYTES)));

        long start = System.currentTimeMillis();
        HttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/nonDelayedDribble", wireMockRule.port())));
        byte[] responseBody = IOUtils.toByteArray(response.getEntity().getContent());
        int duration = (int) (System.currentTimeMillis() - start);

        assertThat(response.getStatusLine().getStatusCode(), is(200));
        assertThat(BODY_BYTES, is(responseBody));
        assertThat(duration, lessThan(SOCKET_TIMEOUT_MILLISECONDS));
    }
}