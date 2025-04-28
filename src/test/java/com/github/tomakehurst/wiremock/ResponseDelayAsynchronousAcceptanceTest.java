/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.jetty12.JettyHttpServerFactory;
import com.github.tomakehurst.wiremock.jetty12.JettySettings;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ResponseDelayAsynchronousAcceptanceTest {

  private static final int SOCKET_TIMEOUT_MILLISECONDS = 500;
  private static final int SHORTER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS / 2;

  private ExecutorService httpClientExecutor = Executors.newCachedThreadPool();

  @RegisterExtension
  public WireMockExtension wireMockRule =
      WireMockExtension.newInstance().configureStaticDsl(true).options(getOptions()).build();

  private static WireMockConfiguration getOptions() {
    WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();
    wireMockConfiguration
        .httpServerFactory(
            new JettyHttpServerFactory(
                JettySettings.Builder.aJettySettings().withAcceptors(1).build()))
        .containerThreads(8);
    wireMockConfiguration.asynchronousResponseEnabled(true);
    wireMockConfiguration.asynchronousResponseThreads(10);
    wireMockConfiguration.dynamicPort();
    return wireMockConfiguration;
  }

  @Test
  public void addsFixedDelayAsynchronously() throws Exception {
    stubFor(get("/delayed").willReturn(ok().withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));

    List<Future<TimedHttpResponse>> responses =
        httpClientExecutor.invokeAll(getHttpRequestCallables(5));

    for (Future<TimedHttpResponse> response : responses) {
      TimedHttpResponse timedResponse = response.get();
      assertThat(timedResponse.status, is(200));
      assertThat(timedResponse.milliseconds, greaterThan((long) SHORTER_THAN_SOCKET_TIMEOUT));
    }
  }

  @Test
  public void addsRandomDelayAsynchronously() throws Exception {
    stubFor(get("/delayed").willReturn(ok().withUniformRandomDelay(100, 300)));

    List<Future<TimedHttpResponse>> responses =
        httpClientExecutor.invokeAll(getHttpRequestCallables(5));

    for (Future<TimedHttpResponse> response : responses) {
      TimedHttpResponse timedResponse = response.get();
      assertThat(timedResponse.status, is(200));
      assertThat(timedResponse.milliseconds, greaterThan(100L));
    }
  }

  @Test
  public void addsChunkedDribbleDelayAsynchronously() throws Exception {
    String body = "chunked-body-to-return";
    stubFor(
        get("/delayed")
            .willReturn(
                ok().withBody(body).withChunkedDribbleDelay(5, SHORTER_THAN_SOCKET_TIMEOUT)));

    List<Future<TimedHttpResponse>> responses =
        httpClientExecutor.invokeAll(getHttpRequestCallables(5));

    for (Future<TimedHttpResponse> response : responses) {
      TimedHttpResponse timedResponse = response.get();
      assertThat(timedResponse.status, is(200));
      assertThat(timedResponse.body, is(body));
      assertThat(timedResponse.milliseconds, greaterThan((long) SHORTER_THAN_SOCKET_TIMEOUT));
    }
  }

  private List<Callable<TimedHttpResponse>> getHttpRequestCallables(int requestCount) {
    List<Callable<TimedHttpResponse>> requests = new ArrayList<>();
    for (int i = 0; i < requestCount; i++) {
      final Stopwatch stopwatch = Stopwatch.createStarted();
      requests.add(
          () -> {
            CloseableHttpResponse response =
                HttpClientFactory.createClient(SOCKET_TIMEOUT_MILLISECONDS)
                    .execute(new HttpGet(wireMockRule.url("/delayed")));
            int status = response.getCode();
            String body = EntityUtils.toString(response.getEntity());
            long milliseconds = stopwatch.elapsed(MILLISECONDS);
            return new TimedHttpResponse(status, body, milliseconds);
          });
    }
    return requests;
  }

  private static class TimedHttpResponse {

    public final int status;
    public final String body;
    public final long milliseconds;

    public TimedHttpResponse(int status, String body, long milliseconds) {
      this.status = status;
      this.body = body;
      this.milliseconds = milliseconds;
    }
  }
}
