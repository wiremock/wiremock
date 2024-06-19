/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.wiremock.webhooks.Webhooks.webhook;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.testsupport.ThrowingWebhookTransformer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class FailingWebhookTest extends WebhooksAcceptanceTest {

  @RegisterExtension
  public WireMockExtension targetServer =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .extensions(
                      new PostServeAction() {
                        @Override
                        public void doGlobalAction(ServeEvent serveEvent, Admin admin) {
                          if (serveEvent.getRequest().getUrl().startsWith("/callback")) {
                            latch.countDown();
                          }
                        }

                        @Override
                        public String getName() {
                          return "test-latch";
                        }
                      }))
          .build();

  WireMockTestClient client;

  @RegisterExtension
  public WireMockExtension requestThrowingExtention =
      WireMockExtension.newInstance()
          .configureStaticDsl(true)
          .options(
              options()
                  .dynamicPort()
                  .notifier(testNotifier)
                  .extensions(new ThrowingWebhookTransformer()))
          .build();

  @RegisterExtension
  public WireMockExtension nonThrowingExtention =
      WireMockExtension.newInstance()
          .configureStaticDsl(true)
          .options(options().dynamicPort().notifier(testNotifier))
          .build();

  @BeforeEach
  public void init() {
    testNotifier.reset();
    targetServer.stubFor(post("/callback").willReturn(ok()));
    latch = new CountDownLatch(1);
    client = new WireMockTestClient(requestThrowingExtention.getPort());
    WireMock.configureFor(targetServer.getPort());
  }

  @Test
  public void failWhenCreatingWebhookRequestAddsSubEvent() throws Exception {
    requestThrowingExtention.stubFor(
        post(urlPathEqualTo("/something-async"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"result\": \"SUCCESS\" }")));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/something-async", new StringEntity("", TEXT_PLAIN));
    assertFalse(latch.await(1, SECONDS));

    printAllErrorNotifications();
    assertThat("No webhook should have been made", latch.getCount(), is(1L));

    assertErrorMessage("Exception thrown while configuring webhook");
    List<SubEvent> subEvents =
        new ArrayList<>(requestThrowingExtention.getAllServeEvents().get(0).getSubEvents());
    assertThat(subEvents, hasSize(1));
    assertSubEvent(
        subEvents.get(0), SubEvent.ERROR, "Exception thrown while configuring webhook: oh no");
  }

  @Test
  public void genericExceptionWhileMakingWebhookRequestAddsSubEvent() throws Exception {
    nonThrowingExtention.stubFor(
        post(urlPathEqualTo("/error"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",
                webhook()
                    .withMethod(POST)
                    // this url contains no port so shouldn't connect
                    .withUrl("http://localhost/callback-errors")
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"result\": \"ERROR\" }")));

    client = new WireMockTestClient(nonThrowingExtention.getPort());
    client.post("/error", new StringEntity("", TEXT_PLAIN));
    assertFalse(latch.await(1, SECONDS));

    printAllErrorNotifications();
    assertThat("No webhook should have been made", latch.getCount(), is(1L));

    assertErrorMessage("Failed to fire webhook POST http://localhost/callback-errors");

    // should be two sub events - the request and the error
    List<SubEvent> subEvents =
        new ArrayList<>(nonThrowingExtention.getAllServeEvents().get(0).getSubEvents());
    assertThat(subEvents, hasSize(2));
    Map<String, Object> expectedRequestEntries =
        Map.of(
            "url", "/callback-errors",
            "absoluteUrl", "http://localhost/callback-errors",
            "method", "POST",
            "scheme", "http",
            "body", "{ \"result\": \"ERROR\" }");
    assertSubEvent(subEvents.get(0), SubEvent.INFO, expectedRequestEntries);
    assertSubEvent(
        subEvents.get(1),
        SubEvent.ERROR,
        "Failed to fire webhook POST http://localhost/callback-errors: Connect to http://localhost:80 [localhost/127.0.0.1] failed: Connection refused");
  }
}
