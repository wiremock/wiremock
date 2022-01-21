/*
 * Copyright (C) 2015-2021 Thomas Akehurst
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
import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.HttpClientFactory;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ResponseDelayAcceptanceTest {

  private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000;
  private static final int LONGER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS * 2;
  private static final int SHORTER_THAN_SOCKET_TIMEOUT = SOCKET_TIMEOUT_MILLISECONDS / 2;
  private static final int BRIEF_DELAY_TO_ALLOW_CALL_TO_BE_MADE_MILLISECONDS = 300;

  @RegisterExtension
  public WireMockExtension wireMockRule =
      WireMockExtension.newInstance()
          .configureStaticDsl(true)
          .options(
              WireMockConfiguration.options()
                  .port(Options.DYNAMIC_PORT)
                  .httpsPort(Options.DYNAMIC_PORT))
          .build();

  private CloseableHttpClient httpClient;
  private WireMockTestClient testClient;

  @BeforeEach
  public void init() {
    httpClient = HttpClientFactory.createClient(SOCKET_TIMEOUT_MILLISECONDS);
    testClient = new WireMockTestClient(wireMockRule.getPort());
  }

  @Test
  public void responseWithFixedDelay() {
    stubFor(
        get(urlEqualTo("/delayed/resource"))
            .willReturn(aResponse().withStatus(200).withBody("Content").withFixedDelay(500)));

    long start = System.currentTimeMillis();
    testClient.get("/delayed/resource");
    int duration = (int) (System.currentTimeMillis() - start);

    assertThat(duration, greaterThanOrEqualTo(500));
  }

  @Test
  public void responseWithByteDribble() {
    byte[] body = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    int numberOfChunks = body.length / 2;
    int chunkedDuration = 1000;

    stubFor(
        get(urlEqualTo("/dribble"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(body)
                    .withChunkedDribbleDelay(numberOfChunks, chunkedDuration)));

    long start = System.currentTimeMillis();
    WireMockResponse response = testClient.get("/dribble");
    long timeTaken = System.currentTimeMillis() - start;

    assertThat(response.statusCode(), is(200));
    assertThat(timeTaken, greaterThanOrEqualTo((long) chunkedDuration));

    assertThat(body, is(response.binaryContent()));
  }

  @Test
  public void responseWithByteDribbleAndFixedDelay() {
    byte[] body = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    int numberOfChunks = body.length / 2;
    int fixedDelay = 1000;
    int chunkedDuration = 1000;
    int totalDuration = fixedDelay + chunkedDuration;

    stubFor(
        get(urlEqualTo("/dribbleWithFixedDelay"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(body)
                    .withChunkedDribbleDelay(numberOfChunks, chunkedDuration)
                    .withFixedDelay(fixedDelay)));

    long start = System.currentTimeMillis();
    WireMockResponse response = testClient.get("/dribbleWithFixedDelay");
    long timeTaken = System.currentTimeMillis() - start;

    assertThat(response.statusCode(), is(200));
    assertThat(timeTaken, greaterThanOrEqualTo((long) totalDuration));

    assertThat(body, is(response.binaryContent()));
  }

  @Test
  public void responseWithLogNormalDistributedDelay() {
    stubFor(
        get(urlEqualTo("/lognormal/delayed/resource"))
            .willReturn(
                aResponse().withStatus(200).withBody("Content").withLogNormalRandomDelay(90, 0.1)));

    long start = System.currentTimeMillis();
    testClient.get("/lognormal/delayed/resource");
    int duration = (int) (System.currentTimeMillis() - start);

    assertThat(duration, greaterThanOrEqualTo(60));
  }

  @Test
  public void responseWithCappedLogNormalDistributedDelay() {
    stubFor(
        get(urlEqualTo("/cappedlognormal/delayed/resource"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody("Content")
                    .withCappedLogNormalRandomDelay(90, 0.1, 95)));

    long start = System.currentTimeMillis();
    testClient.get("/cappedlognormal/delayed/resource");
    int duration = (int) (System.currentTimeMillis() - start);

    assertThat(duration, greaterThanOrEqualTo(60));
    // Ideally we'd check the response time was less than or equal to the CappedLogNormal
    // maxValue, but of course, there is also arbitrary request latency to take into account.
    // Following the same pattern as other tests and not checking that for now.
  }

  @Test
  public void responseWithUniformDistributedDelay() {
    stubFor(
        get(urlEqualTo("/uniform/delayed/resource"))
            .willReturn(
                aResponse().withStatus(200).withBody("Content").withUniformRandomDelay(50, 60)));

    long start = System.currentTimeMillis();
    testClient.get("/uniform/delayed/resource");
    int duration = (int) (System.currentTimeMillis() - start);

    assertThat(duration, greaterThanOrEqualTo(50));
  }

  @Test
  public void requestTimesOutWhenDelayIsLongerThanSocketTimeout() throws Exception {
    assertThrows(
        SocketTimeoutException.class,
        () -> {
          stubFor(
              get(urlEqualTo("/delayed"))
                  .willReturn(
                      aResponse().withStatus(200).withFixedDelay(LONGER_THAN_SOCKET_TIMEOUT)));
          httpClient.execute(new HttpGet(wireMockRule.url("/delayed")));
        });
  }

  @Test
  public void requestIsSuccessfulWhenDelayIsShorterThanSocketTimeout() throws Exception {
    stubFor(
        get(urlEqualTo("/delayed"))
            .willReturn(aResponse().withStatus(200).withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));

    final HttpResponse execute = httpClient.execute(new HttpGet(wireMockRule.url("/delayed")));
    assertThat(execute.getCode(), is(200));
  }

  @Test
  public void requestIsRecordedInJournalBeforePerformingDelay() throws Exception {
    stubFor(
        get(urlEqualTo("/delayed"))
            .willReturn(aResponse().withStatus(200).withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    final AtomicBoolean callSucceeded = callDelayedEndpointAsynchronously(executorService);

    sleep(BRIEF_DELAY_TO_ALLOW_CALL_TO_BE_MADE_MILLISECONDS);
    verify(getRequestedFor(urlEqualTo("/delayed")));

    executorService.awaitTermination(SHORTER_THAN_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
    verify(getRequestedFor(urlEqualTo("/delayed")));
    assertTrue(callSucceeded.get());
  }

  @Test
  public void inFlightDelayedRequestsAreNotRecordedInJournalAfterReset() throws Exception {
    stubFor(
        get(urlEqualTo("/delayed"))
            .willReturn(aResponse().withStatus(200).withFixedDelay(SHORTER_THAN_SOCKET_TIMEOUT)));

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    final AtomicBoolean callSucceeded = callDelayedEndpointAsynchronously(executorService);

    sleep(BRIEF_DELAY_TO_ALLOW_CALL_TO_BE_MADE_MILLISECONDS);
    assertExpectedCallCount(1, urlEqualTo("/delayed"));

    reset();

    executorService.awaitTermination(SHORTER_THAN_SOCKET_TIMEOUT, TimeUnit.MILLISECONDS);
    assertExpectedCallCount(0, urlEqualTo("/delayed"));
    assertTrue(callSucceeded.get());
  }

  private AtomicBoolean callDelayedEndpointAsynchronously(ExecutorService executorService) {
    final AtomicBoolean success = new AtomicBoolean(false);
    executorService.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              HttpGet request = new HttpGet(wireMockRule.url("/delayed"));
              final HttpResponse execute = httpClient.execute(request);
              assertThat(execute.getCode(), is(200));
              success.set(true);
            } catch (Throwable e) {
              e.printStackTrace();
            }
          }
        });
    return success;
  }

  private void assertExpectedCallCount(int expectedCount, UrlPattern urlPattern) {
    int count = wireMockRule.countRequestsMatching(getRequestedFor(urlPattern).build()).getCount();
    assertThat(count, is(expectedCount));
  }
}
