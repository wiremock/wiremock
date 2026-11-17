/*
 * Copyright (C) 2021-2026 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.Strings.bytesFromString;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.TestFiles.defaultTestFilesRoot;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.wiremock.webhooks.Webhooks.webhook;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.TemplateModelDataProviderExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.stubbing.SubEvent;
import com.github.tomakehurst.wiremock.testsupport.CompositeNotifier;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class WebhooksAcceptanceViaServeEventTest extends WebhooksAcceptanceTest {

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
                  .extensions(
                      new TemplateModelDataProviderExtension() {
                        @Override
                        public Map<String, Object> provideTemplateModelData(ServeEvent serveEvent) {
                          return Map.of(
                              "customData", Map.of("path", serveEvent.getRequest().getUrl()));
                        }

                        @Override
                        public String getName() {
                          return "custom-model-data";
                        }
                      })
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
            .withServeEventListener(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withHeader("X-Multi", "one", "two")
                    .withBody("{ \"result\": \"SUCCESS\" }")));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/something-async");

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
  public void originalRequestIdIsTheSameAsRequestId() throws Exception {
    rule.stubFor(
        post("/request-id")
            .willReturn(ok("{{request.id}}").withTransformers("response-template"))
            .withServeEventListener(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"requestId\": \"{{originalRequest.id}}\" }")));

    verify(0, postRequestedFor(anyUrl()));

    WireMockResponse response = client.post("/request-id");
    String requestId = response.content();

    waitForRequestToTargetServer();

    targetServer.verify(
        1,
        postRequestedFor(urlEqualTo("/callback"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(equalToJson("{ \"requestId\": \"" + requestId + "\" }")));
  }

  @Test
  public void webhooksHaveAccessToTemplateModelDataProviders() throws Exception {
    rule.stubFor(
        post("/helpers")
            .willReturn(ok("{{request.id}}").withTransformers("response-template"))
            .withServeEventListener(
                "webhook",
                webhook()
                    .withMethod(POST)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withBody("{ \"url\": \"{{ customData.path }}\" }")));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/helpers");

    waitForRequestToTargetServer();

    targetServer.verify(
        1,
        postRequestedFor(urlEqualTo("/callback"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(equalToJson("{ \"url\": \"/helpers\" }")));
  }

  @Test
  public void webhookCanBeConfiguredFromJson() throws Exception {
    latch = new CountDownLatch(2);

    client.postJson(
        "/__admin/mappings",
        // language=json
        """
        {
          "request": {
            "urlPath": "/hook",
            "method": "POST"
          },
          "response": {
            "status": 204
          },
          "serveEventListeners": [
            {
              "name": "webhook",
              "parameters": {
                "headers": {
                  "Content-Type": "application/json"
                },
                "method": "POST",
                "body": "{ \\"result\\": \\"SUCCESS\\" }",
                "url": "%s/callback1"
              }
            },
            {
              "name": "webhook",
              "parameters": {
                "method": "POST",
                "url": "%s/callback2"
              }
            }
          ]
        }
        """
            .formatted(targetServer.baseUrl(), targetServer.baseUrl()));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/hook");

    waitForRequestToTargetServer();

    verify(postRequestedFor(urlPathEqualTo("/callback1")));
    verify(postRequestedFor(urlPathEqualTo("/callback2")));
  }

  @Test
  public void appliesTemplatingToUrlMethodHeadersAndBodyViaDSL() throws Exception {
    rule.stubFor(
        post(urlPathEqualTo("/templating"))
            .willReturn(ok())
            .withServeEventListener(
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
        """
        {
          "callbackPath": "/callback/123",
          "method": "POST",
          "name": "Tom"
        }""");

    waitForRequestToTargetServer();

    // Ensure we only call it once, not once per API interface
    verify(1, postRequestedFor(anyUrl()));

    LoggedRequest request =
        targetServer.findAll(postRequestedFor(urlEqualTo("/callback/123"))).get(0);

    assertThat(request.header("X-Single").firstValue(), is("3"));
    assertThat(request.header("X-Multi").values(), hasItems("6", "param-one-value"));
    assertThat(request.getBodyAsString(), is("Tom"));
  }

  @Test
  public void appliesTemplatingToUrlMethodHeadersAndBodyViaJSON() throws Exception {
    client.postJson(
        "/__admin/mappings",
        // language=json
        """
        {
          "id": "8a58e190-4a83-4244-a064-265fcca46884",
          "request": {
            "urlPath": "/templating",
            "method": "POST"
          },
          "response": {
            "status": 200
          },
          "uuid": "8a58e190-4a83-4244-a064-265fcca46884",
          "serveEventListeners": [{
            "name": "webhook",
            "parameters": {
              "method": "{{jsonPath originalRequest.body '$.method'}}",
              "url": "%s{{{jsonPath originalRequest.body '$.callbackPath'}}}",
              "headers": {
                "X-Single": "{{math 1 '+' 2}}",
                "X-Multi": ["{{math 3 'x' 2}}", "{{parameters.one}}"]
              },
              "body": "{{jsonPath originalRequest.body '$.name'}}",
              "one": "param-one-value"
            }
          }]
        }
        """
            .formatted(targetServer.baseUrl()));

    verify(0, postRequestedFor(anyUrl()));

    client.postJson(
        "/templating",
        """
        {
          "callbackPath": "/callback/123",
          "method": "POST",
          "name": "Tom"
        }""");

    waitForRequestToTargetServer();

    // Ensure we only call it once, not once per API interface
    verify(1, postRequestedFor(anyUrl()));

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
            .withServeEventListener(
                "webhook",
                webhook()
                    .withFixedDelay(DELAY_MILLISECONDS)
                    .withMethod(RequestMethod.GET)
                    .withUrl(targetServer.url("/callback"))));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/delayed");

    Stopwatch stopwatch = Stopwatch.createStarted();
    waitForRequestToTargetServer();
    stopwatch.stop();

    // Ensure we only call it once, not once per API interface
    verify(1, getRequestedFor(anyUrl()));

    double elapsedMilliseconds = stopwatch.elapsed(MILLISECONDS);
    assertThat(elapsedMilliseconds, closeTo(DELAY_MILLISECONDS, 500.0));

    verify(1, getRequestedFor(urlEqualTo("/callback")));
  }

  @Test
  public void addsRandomDelayViaJSON() throws Exception {
    client.postJson(
        "/__admin/mappings",
        // language=json
        """
        {
          "request": {
            "urlPath": "/delayed",
            "method": "POST"
          },
          "serveEventListeners": [{
            "name": "webhook",
            "parameters": {
              "method": "GET",
              "url": "%s/callback",
              "delay": {
                "type": "uniform",
                "lower": 500,
                "upper": 1000
              }
            }
          }]
        }
        """
            .formatted(targetServer.baseUrl()));

    verify(0, postRequestedFor(anyUrl()));

    client.post("/delayed");

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
                .withServeEventListener(
                    "webhook",
                    webhook()
                        .withMethod(POST)
                        .withUrl("http://169.254.2.34/foo")
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Multi", "one", "two")
                        .withBody("{ \"result\": \"SUCCESS\" }")));

    client.post("/webhook");

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

  @ParameterizedTest
  @MethodSource("allHttpMethodsForWebhooks")
  public void firesWebhookForAllHttpMethods(RequestMethod method) throws Exception {
    String body = "{ \"test\": \"data\" }";
    rule.stubFor(
        post(urlPathEqualTo("/trigger-webhook"))
            .willReturn(ok())
            .withServeEventListener(
                "webhook",
                webhook()
                    .withMethod(method)
                    .withUrl(targetServer.url("/callback"))
                    .withHeader("Content-Type", "application/json")
                    .withBody(body)));

    client.post("/trigger-webhook");

    waitForRequestToTargetServer();

    // Verify the webhook was called with the correct method
    List<LoggedRequest> requests = targetServer.findAll(anyRequestedFor(urlEqualTo("/callback")));
    assertThat(requests, hasSize(1));
    assertThat(requests.get(0).getMethod(), is(method));
    if (method.hasEntity()) {
      assertThat(requests.get(0).getBodyAsString(), is(body));
    }
  }

  @Test
  public void webhookBodyCanBeLoadedFromFile() throws Exception {
    // This test needs a separate rule with file root configured
    WireMockServer fileServer =
        new WireMockServer(
            options().dynamicPort().withRootDirectory(defaultTestFilesRoot()).notifier(notifier));

    try {
      fileServer.start();
      latch = new CountDownLatch(1);

      // Set up target server to receive the webhook
      targetServer.stubFor(any(urlPathMatching("/callback.*")).willReturn(ok()));

      // Stub with webhook that uses bodyFileName via JSON API
      new WireMockTestClient(fileServer.port())
          .postJson(
              "/__admin/mappings",
              // language=json
              """
              {
                "request": {
                  "urlPath": "/trigger-file-webhook",
                  "method": "POST"
                },
                "response": {
                  "status": 200
                },
                "serveEventListeners": [
                  {
                    "name": "webhook",
                    "parameters": {
                      "method": "POST",
                      "url": "%s/callback-file",
                      "headers": {
                        "Content-Type": "application/json"
                      },
                      "body": {
                        "filePath": "webhook-body.json"
                      }
                    }
                  }
                ]
              }
              """
                  .formatted(targetServer.baseUrl()));

      new WireMockTestClient(fileServer.port()).post("/trigger-file-webhook");

      waitForRequestToTargetServer();

      targetServer.verify(
          1,
          postRequestedFor(urlEqualTo("/callback-file"))
              .withHeader("Content-Type", equalTo("application/json"))
              .withRequestBody(
                  equalToJson("{ \"source\": \"file\", \"message\": \"Hello from file\" }")));
    } finally {
      fileServer.stop();
    }
  }

  @Test
  public void webhookBodyCanBeLoadedFromFileViaDSL() throws Exception {
    // This test needs a separate rule with file root configured
    WireMockServer fileServer =
        new WireMockServer(
            options().dynamicPort().withRootDirectory(defaultTestFilesRoot()).notifier(notifier));

    try {
      fileServer.start();
      latch = new CountDownLatch(1);

      // Set up target server to receive the webhook
      targetServer.stubFor(any(urlPathMatching("/callback.*")).willReturn(ok()));

      // Create stub using DSL
      fileServer.stubFor(
          post(urlPathEqualTo("/trigger-file-dsl-webhook"))
              .willReturn(ok())
              .withServeEventListener(
                  "webhook",
                  webhook()
                      .withMethod(POST)
                      .withUrl(targetServer.url("/callback-file-dsl"))
                      .withHeader("Content-Type", "application/json")
                      .withBodyFileName("webhook-body.json")));

      new WireMockTestClient(fileServer.port()).post("/trigger-file-dsl-webhook");

      waitForRequestToTargetServer();

      targetServer.verify(
          1,
          postRequestedFor(urlEqualTo("/callback-file-dsl"))
              .withHeader("Content-Type", equalTo("application/json"))
              .withRequestBody(
                  equalToJson("{ \"source\": \"file\", \"message\": \"Hello from file\" }")));
    } finally {
      fileServer.stop();
    }
  }

  @Test
  public void webhookBodyCanBeLoadedFromDataStore() throws Exception {
    // This test uses the rule server and puts data in its store
    String webhookBody = "{ \"source\": \"dataStore\", \"message\": \"Hello from store\" }";
    rule.getOptions()
        .getStores()
        .getBlobStore("webhookStore")
        .put("webhookData", bytesFromString(webhookBody));

    latch = new CountDownLatch(1);

    // Set up target server to receive the webhook
    targetServer.stubFor(any(urlPathMatching("/callback.*")).willReturn(ok()));

    // Stub with webhook that uses data store ref via JSON API
    client.postJson(
        "/__admin/mappings",
        // language=json
        """
        {
          "request": {
            "urlPath": "/trigger-store-webhook",
            "method": "POST"
          },
          "response": {
            "status": 200
          },
          "serveEventListeners": [{
            "name": "webhook",
            "parameters": {
              "method": "POST",
              "url": "%s/callback-store",
              "headers": {
                "Content-Type": "application/json"
              },
              "body": {
                "dataStore": "webhookStore",
                "dataRef": "webhookData"
              }
            }
          }]
        }
        """
            .formatted(targetServer.baseUrl()));

    client.post("/trigger-store-webhook");

    waitForRequestToTargetServer();

    targetServer.verify(
        1,
        postRequestedFor(urlEqualTo("/callback-store"))
            .withHeader("Content-Type", equalTo("application/json"))
            .withRequestBody(
                equalToJson("{ \"source\": \"dataStore\", \"message\": \"Hello from store\" }")));
  }

  private static Stream<RequestMethod> allHttpMethodsForWebhooks() {
    return Arrays.stream(RequestMethod.values())
        .filter(m -> !m.equals(RequestMethod.ANY) && !m.equals(RequestMethod.GET_OR_HEAD));
  }
}
