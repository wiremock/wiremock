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

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ResponseDelaySynchronousFailureAcceptanceTest {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
    private static final int SHORTER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS / 2;

    private ExecutorService httpClientExecutor = Executors.newCachedThreadPool();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(getOptions());

    private WireMockConfiguration getOptions() {
        WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
        wireMockConfiguration.jettyAcceptors(1).containerThreads(4);
        wireMockConfiguration.asynchronousResponseEnabled(false);
        return wireMockConfiguration;
    }

    @Test
    public void requestIsFailedWhenMultipleRequestsHitSynchronousServer() throws Exception {
        stubFor(get(urlEqualTo("/delayed")).willReturn(
                aResponse()
                        .withStatus(200)
                        .withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));
        List<Future<HttpResponse>> responses = httpClientExecutor.invokeAll(getHttpRequestCallables(10));
        try {
            for (Future<HttpResponse> response : responses) {
                assertThat(response.get().getStatusLine().getStatusCode(), is(200));
            }
            fail("A timeout exception expected reading multiple responses from synchronous WireMock server");
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(SocketTimeoutException.class));
            assertThat(e.getCause().getMessage(), is("Read timed out"));
        }
    }

    private List<Callable<HttpResponse>> getHttpRequestCallables(int requestCount) throws IOException {
        List<Callable<HttpResponse>> requests = new ArrayList<>();
        for (int i = 0; i < requestCount; i++) {
            requests.add(new Callable<HttpResponse>() {
                @Override
                public HttpResponse call() throws Exception {
                    return HttpClientFactory
                            .createClient(SOCKET_TIMEOUT_MILLISECONDS)
                            .execute(new HttpGet(String.format("http://localhost:%d/delayed", wireMockRule.port())));
                }
            });
        }
        return requests;
    }

}
