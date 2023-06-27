/*
 * Copyright (C) 2016-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.PostServeActionExtensionTest.CounterNameParameter.counterNameParameter;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.extension.ServeEventListener.RequestPhase.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ServeEventListener;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ServeEventListenerExtensionTest {

  WireMockServer wm;
  WireMockTestClient client;

  void initWithOptions(Options options) {
    wm = new WireMockServer(options);
    wm.start();
    client = new WireMockTestClient(wm.port());
  }

  @AfterEach
  public void cleanup() {
    if (wm != null) {
      wm.stop();
    }
  }

  @Test
  void eventTriggeredBeforeMatching() throws Exception {
    final CompletableFuture<Void> completed = new CompletableFuture<>();
    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {

                  @Override
                  public void beforeMatch(ServeEvent serveEvent, Parameters parameters) {
                    assertThat(serveEvent.getRequest().getUrl(), is("/get-this"));
                    assertThat(serveEvent.getResponseDefinition(), nullValue());
                    assertThat(serveEvent.getStubMapping(), nullValue());
                    assertThat(serveEvent.getResponse(), nullValue());

                    completed.complete(null);
                  }

                  @Override
                  public String getName() {
                    return "before-match";
                  }
                }));

    wm.stubFor(any(anyUrl()).willReturn(ok()));

    client.get("/get-this");

    completed.get(2, SECONDS);
  }

  @Test
  void eventTriggeredAfterMatching() throws Exception {
    final CompletableFuture<Void> completed = new CompletableFuture<>();
    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {

                  @Override
                  public void afterMatch(ServeEvent serveEvent, Parameters parameters) {
                    assertThat(serveEvent.getRequest().getUrl(), is("/get-this"));
                    assertThat(serveEvent.getResponseDefinition(), notNullValue());
                    assertThat(serveEvent.getStubMapping(), notNullValue());
                    assertThat(serveEvent.getResponse(), nullValue());

                    completed.complete(null);
                  }

                  @Override
                  public String getName() {
                    return "after-match";
                  }
                }));

    wm.stubFor(any(anyUrl()).willReturn(ok()));

    client.get("/get-this");

    completed.get(2, SECONDS);
  }

  @Test
  void eventTriggeredAfterCompletion() throws Exception {
    final CompletableFuture<Void> completed = new CompletableFuture<>();
    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {

                  @Override
                  public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
                    assertThat(serveEvent.getRequest().getUrl(), is("/get-this"));
                    assertThat(serveEvent.getResponseDefinition(), notNullValue());
                    assertThat(serveEvent.getStubMapping(), notNullValue());
                    assertThat(serveEvent.getResponse().getStatus(), is(200));

                    completed.complete(null);
                  }

                  @Override
                  public String getName() {
                    return "after-complete";
                  }
                }));

    wm.stubFor(any(anyUrl()).willReturn(ok()));

    client.get("/get-this");

    completed.get(2, SECONDS);
  }

  @Test
  void eventTriggeredWhenAppliedToAStubMapping() {
    initWithOptions(options().dynamicPort().extensions(new NamedCounterAction()));

    StubMapping stubMapping =
        wm.stubFor(
            get(urlPathEqualTo("/count-me"))
                .withServeEventListener("count-request", counterNameParameter().withName("things"))
                .willReturn(aResponse()));

    client.get("/count-me");
    client.get("/count-me");
    client.get("/count-me");
    client.get("/count-me");

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/things"), is("4"));

    // We should serialise out in array form
    assertThat(
        client.get("/__admin/mappings/" + stubMapping.getId()).content(),
        jsonPartEquals(
            "serveEventListeners",
            "[\n"
                + "    {\n"
                + "      \"name\": \"count-request\",\n"
                + "      \"parameters\": {\n"
                + "        \"counterName\": \"things\"\n"
                + "      }\n"
                + "    }\n"
                + "  ]"));
  }

  @Test
  void eventSelectedPerStubWithVaryingParameters() {
    final List<String> messages = new ArrayList<>();

    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {

                  @Override
                  public void onEvent(
                      RequestPhase requestPhase, ServeEvent serveEvent, Parameters parameters) {
                    messages.add(requestPhase.name() + ": " + parameters.getString("phase"));
                  }

                  @Override
                  public boolean applyGlobally() {
                    return false;
                  }

                  @Override
                  public String getName() {
                    return "request-phase-reporter";
                  }
                }));

    wm.stubFor(
        get(urlPathEqualTo("/report"))
            .withServeEventListener(
                AFTER_MATCH, "request-phase-reporter", Parameters.one("phase", "after-match"))
            .withServeEventListener(
                AFTER_COMPLETE, "request-phase-reporter", Parameters.one("phase", "after-complete"))
            .willReturn(aResponse()));

    client.get("/report");

    assertThat(messages, is(List.of("AFTER_MATCH: after-match", "AFTER_COMPLETE: after-complete")));
  }

  @Test
  void globalOnEventListenerIsTriggeredInAllRequestPhases() {
    final List<String> messages = new ArrayList<>();

    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {

                  @Override
                  public void onEvent(
                      RequestPhase requestPhase, ServeEvent serveEvent, Parameters parameters) {
                    messages.add(requestPhase.name());
                  }

                  @Override
                  public boolean applyGlobally() {
                    return true;
                  }

                  @Override
                  public String getName() {
                    return "request-phase-reporter";
                  }
                }));

    wm.stubFor(get(urlPathEqualTo("/report")).willReturn(aResponse()));

    client.get("/report");

    assertThat(messages, is(List.of("BEFORE_MATCH", "AFTER_MATCH", "AFTER_COMPLETE")));
  }

  @Test
  void continuesWithNoEffectIfANonExistentActionIsReferenced() {
    initWithOptions(options().dynamicPort());

    wm.stubFor(
        get(urlPathEqualTo("/as-normal"))
            .withServeEventListener("does-not-exist", counterNameParameter().withName("things"))
            .willReturn(aResponse().withStatus(200)));

    assertThat(client.get("/as-normal").statusCode(), is(200));
  }

  @Test
  void providesServeEventWithResponseFieldPopulated() {
    final AtomicInteger finalStatus = new AtomicInteger();
    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new ServeEventListener() {
                  @Override
                  public String getName() {
                    return "response-field-test";
                  }

                  @Override
                  public boolean applyGlobally() {
                    return true;
                  }

                  @Override
                  public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
                    if (serveEvent.getResponse() != null) {
                      finalStatus.set(serveEvent.getResponse().getStatus());
                    }
                  }
                }));

    wm.stubFor(get(urlPathEqualTo("/response-status")).willReturn(aResponse().withStatus(418)));

    client.get("/response-status");

    await().atMost(5, SECONDS).until(getValue(finalStatus), is(418));
  }

  @Test
  public void multipleActionsOfTheSameNameCanBeSpecifiedAsAJsonArray() {
    initWithOptions(
        options()
            .dynamicPort()
            .notifier(new ConsoleNotifier(true))
            .extensions(new NamedCounterAction()));

    WireMockResponse response =
        client.postJson(
            "/__admin/mappings",
            "{\n"
                + "  \"request\": {\n"
                + "    \"urlPath\": \"/count-me\",\n"
                + "    \"method\": \"GET\"\n"
                + "  },\n"
                + "  \"response\": {\n"
                + "    \"status\": 200\n"
                + "  },\n"
                + "  \"serveEventListeners\": [\n"
                + "    {\n"
                + "      \"name\": \"count-request\",\n"
                + "      \"parameters\": {\n"
                + "        \"counterName\": \"one\"  \n"
                + "      }\n"
                + "    },\n"
                + "    {\n"
                + "      \"name\": \"count-request\",\n"
                + "      \"parameters\": {\n"
                + "        \"counterName\": \"two\"\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}");

    assertThat(response.content(), response.statusCode(), is(201));

    client.get("/count-me");
    client.get("/count-me");
    client.get("/count-me");

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/one"), is("3"));

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/two"), is("3"));
  }

  @Test
  void multipleActionsOfTheSameNameCanBeSpecifiedViaTheDSL() {
    initWithOptions(
        options()
            .dynamicPort()
            .notifier(new ConsoleNotifier(true))
            .extensions(new NamedCounterAction()));

    wm.stubFor(
        get(urlPathEqualTo("/count-me"))
            .willReturn(ok())
            .withServeEventListener("count-request", counterNameParameter().withName("one"))
            .withServeEventListener("count-request", counterNameParameter().withName("two")));

    client.get("/count-me");
    client.get("/count-me");
    client.get("/count-me");

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/one"), is("3"));

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/two"), is("3"));
  }

  private Callable<Integer> getValue(final AtomicInteger value) {
    return value::get;
  }

  private Callable<String> getContent(final String url) {
    return () -> client.get(url).content();
  }

  public static class NamedCounterAction implements ServeEventListener, AdminApiExtension {

    private final ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();

    @Override
    public String getName() {
      return "count-request";
    }

    @Override
    public boolean applyGlobally() {
      return false;
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
      router.add(
          GET,
          "/named-counter/{name}",
          (admin, serveEvent, pathParams) -> {
            String name = pathParams.get("name");
            Integer count = firstNonNull(counters.get(name), 0);
            return responseDefinition().withStatus(200).withBody(String.valueOf(count)).build();
          });
    }

    @Override
    public void afterComplete(ServeEvent serveEvent, Parameters parameters) {
      CounterNameParameter counterNameParam = parameters.as(CounterNameParameter.class);

      String counterName = counterNameParam.counterName;

      counters.putIfAbsent(counterName, 0);
      Integer oldValue;
      Integer newValue;

      do {
        oldValue = counters.get(counterName);
        newValue = oldValue + 1;
      } while (!counters.replace(counterName, oldValue, newValue));
    }
  }

  public static class CounterNameParameter {

    public String counterName;

    public CounterNameParameter(@JsonProperty("counterName") String counterName) {
      this.counterName = counterName;
    }

    public CounterNameParameter() {}

    public static CounterNameParameter counterNameParameter() {
      return new CounterNameParameter();
    }

    public CounterNameParameter withName(String name) {
      this.counterName = name;
      return this;
    }
  }
}
