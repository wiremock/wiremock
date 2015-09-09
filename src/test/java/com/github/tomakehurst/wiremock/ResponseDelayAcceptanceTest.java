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

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.SocketTimeoutException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseDelayAcceptanceTest {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    private static final int LONGER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 2;
    private static final int SHORTER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS / 2;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(Options.DYNAMIC_PORT, Options.DYNAMIC_PORT);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private HttpClient httpClient;

    @Before
    public void init() {
        httpClient = HttpClientFactory.createClient(SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Test
    public void requestTimesOutWhenDelayIsLongerThanSocketTimeout() throws Exception {
        stubFor(get(urlEqualTo("/delayed")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(LONGER_THAN_SOCKET_TIMEOUT)));

        exception.expect(SocketTimeoutException.class);
        httpClient.execute(new HttpGet(String.format("http://localhost:%d/delayed", wireMockRule.port())));
    }

    @Test
    public void requestIsSuccessfulWhenDelayIsShorterThanSocketTimeout() throws Exception {
        stubFor(get(urlEqualTo("/delayed")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));

        final HttpResponse execute = httpClient.execute(new HttpGet(String.format("http://localhost:%d/delayed", wireMockRule.port())));
        assertThat(execute.getStatusLine().getStatusCode(), is(200));
    }
}
