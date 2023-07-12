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
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.wiremock.webhooks.Webhooks.webhook;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.testsupport.CompositeNotifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.wiremock.webhooks.WebhookDefinition;
import org.wiremock.webhooks.Webhooks;
import testsupport.CompositeNotifier;
import testsupport.TestNotifier;
import testsupport.WireMockTestClient;

public class WebhooksAcceptanceViaPostServeActionTest extends WebhooksAcceptanceTest {

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
                      })
                  .notifier(new ConsoleNotifier("Target", true)))
          .build();

  CompositeNotifier notifier =
      new CompositeNotifier(testNotifier, new ConsoleNotifier("Main", true));
  WireMockTestClient client;

  @RegisterExtension
  public WireMockExtension rule =
      WireMockExtension.newInstance()
          .options(
              options()
                  .dynamicPort()
                  .notifier(notifier)
                  .limitProxyTargets(
                      NetworkAddressRules.builder().deny("169.254.0.0-169.254.255.255").build()))
          .configureStaticDsl(true)
          .build();

  @BeforeEach
  public void init() {
    testNotifier.reset();
    targetServer.stubFor(any(anyUrl()).willReturn(ok()));
    latch = new CountDownLatch(1);
    client = new WireMockTestClient(rule.getPort());
    WireMock.configureFor(targetServer.getPort());

    System.out.println("Target server port: " + targetServer.getPort());
    System.out.println("Under test server port: " + rule.getPort());
  }

  @Test
  public void firesASingleWebhookWhenRequested() throws Exception {
    rule.stubFor(
        post(urlPathEqualTo("/something-async"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withHeader("X-Multi", "one", "two")
                    .withBody("{ \"result\": \"SUCCESS\" }")));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/something-async", new StringEntity("", TEXT_PLAIN));

    waitForRequestToTargetServer();

    targetServer.verify(
        1,
        postRequestedFor(urlEqualTo("/callback"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(equalToJson("{ \"result\": \"SUCCESS\" }")));

    List<String> multiHeaderValues =
        targetServer
            .findAll(postRequestedFor(urlEqualTo("/callback")))
            .get(0)
            .header("X-Multi")
            .values();
    assertThat(multiHeaderValues, hasItems("one", "two"));

    printAllInfoNotifications();

    waitAtMost(5, SECONDS)
        .until(
            () -> testNotifier.getInfoMessages(),
            hasItem(
                allOf(
                    containsString("Webhook POST request to"),
                    containsString("/callback returned status"),
                    containsString("200"))));

    // should be two sub events - the request and the response
    List<SubEvent> subEvents = new ArrayList<>(rule.getAllServeEvents().get(0).getSubEvents());
    assertThat(subEvents, hasSize(2));
    Map<String, Object> expectedRequestEntries =
        Map.of(
            "url", "/callback",
            "method", "POST",
            "host", "localhost",
            "scheme", "http",
            "body", "{ \"result\": \"SUCCESS\" }");
    assertSubEvent(subEvents.get(0), WEBHOOK_REQUEST_SUB_EVENT_NAME, expectedRequestEntries);
    Map<String, Object> expectedResponseEntries = Map.of("status", 200, "body", "");
    assertSubEvent(subEvents.get(1), WEBHOOK_RESPONSE_SUB_EVENT_NAME, expectedResponseEntries);
  }

  @Test
  public void webhookCanBeConfiguredFromJson() throws Exception {
    latch = new CountDownLatch(2);

    client.postJson(
        "/__admin/mappings",
        "{\n"
            + "  \"request\": {\n"
            + "    \"urlPath\": \"/hook\",\n"
            + "    \"method\": \"POST\"\n"
            + "  },\n"
            + "  \"response\": {\n"
            + "    \"status\": 204\n"
            + "  },\n"
            + "  \"postServeActions\": [\n"
            + "    {\n"
            + "      \"name\": \"webhook\",\n"
            + "      \"parameters\": {\n"
            + "        \"headers\": {\n"
            + "          \"Content-Type\": \"application/json\"\n"
            + "        },\n"
            + "        \"method\": \"POST\",\n"
            + "        \"body\": \"{ \\\"result\\\": \\\"SUCCESS\\\" }\",\n"
            + "        \"url\" : \""
            + targetServer.baseUrl()
            + "/callback1\"\n"
            + "      }\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"webhook\",\n"
            + "      \"parameters\": {\n"
            + "        \"method\": \"POST\",\n"
            + "        \"url\" : \""
            + targetServer.baseUrl()
            + "/callback2\"\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}");

    verify(0, postRequestedFor(anyUrl()));

    client.post("/hook", new StringEntity("", TEXT_PLAIN));

    waitForRequestToTargetServer();

    verify(postRequestedFor(urlPathEqualTo("/callback1")));
    verify(postRequestedFor(urlPathEqualTo("/callback2")));
  }

  public void appliesTemplatingToUrlMethodHeadersAndBodyFileNameViaDSL() throws Exception {
    rule.stubFor(
        post(urlPathEqualTo("/templating"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",

                webhook()
                    .withBodyFileName("myFile.json")
                    .withMethod("{{jsonPath originalRequest.body '$.method'}}")
                    .withUrl(
                        targetServer.baseUrl()
                            + "{{{jsonPath originalRequest.body '$.callbackPath'}}}")
                    .withHeader("X-Single", "{{math 1 '+' 2}}")
                    .withHeader("X-Multi", "{{math 3 'x' 2}}", "{{parameters.one}}")
                    .withBody("{{jsonPath originalRequest.body '$.name'}}")
                    .withExtraParameter("one", "param-one-value")));

    verify(0, postRequestedFor(anyUrl()));

    client.postJson(
        "/templating",
        "{\n"
            + "  \"callbackPath\": \"/callback/123\",\n"
            + "  \"method\": \"POST\",\n"
            + "  \"name\": \"Tom\"\n"
            + "}");

    waitForRequestToTargetServer();

    LoggedRequest request =
        targetServer.findAll(postRequestedFor(urlEqualTo("/callback/123"))).get(0);

    assertThat(request.header("X-Single").firstValue(), is("3"));
    assertThat(request.header("X-Multi").values(), hasItems("6", "param-one-value"));
    assertThat(request.getBodyAsString(), is("Tom"));
  }

  public void appliesTemplatingToUrlMethodHeadersAndBodyFileNameViaJSON() throws Exception {
    client.postJson(
        "/__admin/mappings",
        "{\n"
            + "  \"id\" : \"8a58e190-4a83-4244-a064-265fcca46884\",\n"
            + "  \"request\" : {\n"
            + "    \"urlPath\" : \"/templating\",\n"
            + "    \"method\" : \"POST\"\n"
            + "  },\n"
            + "  \"response\" : {\n"
            + "    \"status\" : 200\n"
            + "  },\n"
            + "  \"uuid\" : \"8a58e190-4a83-4244-a064-265fcca46884\",\n"
            + "  \"postServeActions\" : [{\n"
            + "    \"name\" : \"webhook\",\n"
            + "    \"parameters\" : {\n"
            + "      \"method\" : \"{{jsonPath originalRequest.body '$.method'}}\",\n"
            + "      \"url\" : \""
            + targetServer.baseUrl()
            + "{{{jsonPath originalRequest.body '$.callbackPath'}}}\",\n"
            + "      \"headers\" : {\n"
            + "        \"X-Single\" : \"{{math 1 '+' 2}}\",\n"
            + "        \"X-Multi\" : [ \"{{math 3 'x' 2}}\", \"{{parameters.one}}\" ]\n"
            + "      },\n"
            + "      \"bodyFileName\" : \"myFile.json\",\n"
            + "      \"one\" : \"param-one-value\"\n"
            + "    }\n"
            + "  }]\n"
            + "}\n");

    verify(0, postRequestedFor(anyUrl()));

    client.postJson(
        "/templating",
        "{\n"
            + "  \"callbackPath\": \"/callback/123\",\n"
            + "  \"method\": \"POST\",\n"
            + "  \"name\": \"Tom\"\n"
            + "}");

    waitForRequestToTargetServer();

    LoggedRequest request =
        targetServer.findAll(postRequestedFor(urlEqualTo("/callback/123"))).get(0);

    assertThat(request.header("X-Single").firstValue(), is("3"));
    assertThat(request.header("X-Multi").values(), hasItems("6", "param-one-value"));
    assertThat(request.getBodyAsString(), is("Tom"));
  }

  public void appliesTemplatingToUrlMethodHeadersAndBodyViaDSL() throws Exception {
    rule.stubFor(
        post(urlPathEqualTo("/templating"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",
                webhook()
                    .withMethod("{{jsonPath originalRequest.body '$.method'}}")
                    .withUrl(
                        targetServer.baseUrl()
                            + "{{{jsonPath originalRequest.body '$.callbackPath'}}}")
                    .withHeader("X-Single", "{{math 1 '+' 2}}")
                    .withHeader("X-Multi", "{{math 3 'x' 2}}", "{{parameters.one}}")
                    .withBody("{{jsonPath originalRequest.body '$.name'}}")
                    .withExtraParameter("one", "param-one-value")));

    verify(0, postRequestedFor(anyUrl()));

    client.postJson(
        "/templating",
        "{\n"
            + "  \"callbackPath\": \"/callback/123\",\n"
            + "  \"method\": \"POST\",\n"
            + "  \"name\": \"Tom\"\n"
            + "}");

    waitForRequestToTargetServer();

    LoggedRequest request =
        targetServer.findAll(postRequestedFor(urlEqualTo("/callback/123"))).get(0);

    assertThat(request.header("X-Single").firstValue(), is("3"));
    assertThat(request.header("X-Multi").values(), hasItems("6", "param-one-value"));
    assertThat(request.getBodyAsString(), is("Tom"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "myFile.json"})
  public void appliesTemplatingToUrlMethodHeadersAndBodyViaJSON(String bodyFileNameParam) throws Exception {
    client.postJson(
        "/__admin/mappings",
        "{\n"
            + "  \"id\" : \"8a58e190-4a83-4244-a064-265fcca46884\",\n"
            + "  \"request\" : {\n"
            + "    \"urlPath\" : \"/templating\",\n"
            + "    \"method\" : \"POST\"\n"
            + "  },\n"
            + "  \"response\" : {\n"
            + "    \"status\" : 200\n"
            + "  },\n"
            + "  \"uuid\" : \"8a58e190-4a83-4244-a064-265fcca46884\",\n"
            + "  \"postServeActions\" : [{\n"
            + "    \"name\" : \"webhook\",\n"
            + "    \"parameters\" : {\n"
            + "      \"method\" : \"{{jsonPath originalRequest.body '$.method'}}\",\n"
            + "      \"url\" : \""
            + targetServer.baseUrl()
            + "{{{jsonPath originalRequest.body '$.callbackPath'}}}\",\n"
            + "      \"headers\" : {\n"
            + "        \"X-Single\" : \"{{math 1 '+' 2}}\",\n"
            + "        \"X-Multi\" : [ \"{{math 3 'x' 2}}\", \"{{parameters.one}}\" ]\n"
            + "      },\n"
            + "      \"body\" : \"{{jsonPath originalRequest.body '$.name'}}\",\n"
            + "      \"one\" : \"param-one-value\"\n"
            + "    }\n"
            + "  }]\n"
            + "}\n");

    verify(0, postRequestedFor(anyUrl()));

    client.postJson(
        "/templating",
        "{\n"
            + "  \"callbackPath\": \"/callback/123\",\n"
            + "  \"method\": \"POST\",\n"
            + "  \"name\": \"Tom\"\n"
            + "}");

    waitForRequestToTargetServer();

    LoggedRequest request =
        targetServer.findAll(postRequestedFor(urlEqualTo("/callback/123"))).get(0);

    assertThat(request.header("X-Single").firstValue(), is("3"));
    assertThat(request.header("X-Multi").values(), hasItems("6", "param-one-value"));
    assertThat(request.getBodyAsString(), is("Tom"));
  }

  @Test
  public void addsFixedDelayViaDSL() throws Exception {
    final int DELAY_MILLISECONDS = 1_000;

    rule.stubFor(
        post(urlPathEqualTo("/delayed"))
            .willReturn(ok())
            .withPostServeAction(
                "webhook",
                webhook()
                    .withFixedDelay(DELAY_MILLISECONDS)
                    .withMethod(RequestMethod.GET)
                    .withUrl(targetServer.url("/callback"))));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/delayed", new StringEntity("", TEXT_PLAIN));

    Stopwatch stopwatch = Stopwatch.createStarted();
    waitForRequestToTargetServer();
    stopwatch.stop();

    double elapsedMilliseconds = stopwatch.elapsed(MILLISECONDS);
    assertThat(elapsedMilliseconds, closeTo(DELAY_MILLISECONDS, 500.0));

    verify(1, getRequestedFor(urlEqualTo("/callback")));
  }

  @Test
  public void addsRandomDelayViaJSON() throws Exception {
    client.postJson(
        "/__admin/mappings",
        "{\n"
            + "  \"request\" : {\n"
            + "    \"urlPath\" : \"/delayed\",\n"
            + "    \"method\" : \"POST\"\n"
            + "  },\n"
            + "  \"postServeActions\" : [{\n"
            + "    \"name\" : \"webhook\",\n"
            + "    \"parameters\" : {\n"
            + "      \"method\" : \"GET\",\n"
            + "      \"url\" : \""
            + targetServer.baseUrl()
            + "/callback\",\n"
            + "      \"delay\" : {\n"
            + "        \"type\" : \"uniform\",\n"
            + "        \"lower\": 500,\n"
            + "        \"upper\": 1000\n"
            + "      }\n"
            + "    }\n"
            + "  }]\n"
            + "}");

    verify(0, postRequestedFor(anyUrl()));

    client.post("/delayed", new StringEntity("", TEXT_PLAIN));

    Stopwatch stopwatch = Stopwatch.createStarted();
    waitForRequestToTargetServer();
    stopwatch.stop();

    long elapsedMilliseconds = stopwatch.elapsed(MILLISECONDS);
    assertThat(elapsedMilliseconds, greaterThanOrEqualTo(500L));
    assertThat(elapsedMilliseconds, lessThanOrEqualTo(1500L));

    verify(1, getRequestedFor(urlEqualTo("/callback")));
  }

  @Test
  public void doesNotFireAWebhookWhenRequestedForDeniedTarget() {
    StubMapping stub =
        rule.stubFor(
            post(urlPathEqualTo("/webhook"))
                .willReturn(ok())
                .withPostServeAction(
                    "webhook",
                    webhook()
                        .withMethod(POST)
                        .withUrl("http://169.254.2.34/foo")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Multi", "one", "two")
                        .withBody("{ \"result\": \"SUCCESS\" }")));

    client.post("/webhook", new StringEntity("", TEXT_PLAIN));

    printAllInfoNotifications();

    final String expectedErrorMessage =
        "The target webhook address http://169.254.2.34/foo specified by stub "
            + stub.getId()
            + " is denied in WireMock's configuration.";
    assertErrorMessage(expectedErrorMessage);

    // should be two sub events - the request and the error
    List<SubEvent> subEvents = new ArrayList<>(rule.getAllServeEvents().get(0).getSubEvents());
    assertThat(subEvents, hasSize(2));
    Map<String, Object> expectedRequestEntries =
        Map.of(
            "url", "/foo",
            "absoluteUrl", "http://169.254.2.34/foo",
            "method", "POST",
            "scheme", "http",
            "body", "{ \"result\": \"SUCCESS\" }");
    assertSubEvent(subEvents.get(0), WEBHOOK_REQUEST_SUB_EVENT_NAME, expectedRequestEntries);
    assertSubEvent(subEvents.get(1), SubEvent.ERROR, expectedErrorMessage);
  }
}
