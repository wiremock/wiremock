/*
 * Copyright (C) 2016-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class PostServeActionExtensionTest {

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
      wm.close();
    }
  }

  @Test
  public void triggersActionWhenAppliedToAStubMapping() throws Exception {
    initWithOptions(options().dynamicPort().extensions(new NamedCounterAction()));

    StubMapping stubMapping =
        wm.stubFor(
            get(urlPathEqualTo("/count-me"))
                .withPostServeAction("count-request", counterNameParameter().withName("things"))
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
            "postServeActions",
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
  public void continuesWithNoEffectIfANonExistentActionIsReferenced() {
    initWithOptions(options().dynamicPort());

    wm.stubFor(
        get(urlPathEqualTo("/as-normal"))
            .withPostServeAction("does-not-exist", counterNameParameter().withName("things"))
            .willReturn(aResponse().withStatus(200)));

    assertThat(client.get("/as-normal").statusCode(), is(200));
  }

  @Test
  public void providesServeEventWithResponseFieldPopulated() throws InterruptedException {
    final AtomicInteger finalStatus = new AtomicInteger();
    initWithOptions(
        options()
            .dynamicPort()
            .extensions(
                new PostServeAction() {
                  @Override
                  public String getName() {
                    return "response-field-test";
                  }

                  @Override
                  public void doGlobalAction(ServeEvent serveEvent, Admin admin) {
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
  public void canBeSpecifiedAsAJsonObject() {
    initWithOptions(
        options()
            .dynamicPort()
            .notifier(new ConsoleNotifier(true))
            .extensions(new NamedCounterAction()));

    WireMockResponse response =
        client.postJson(
            "/__admin/mappings",
            "{\n"
                + "  \"request\" : {\n"
                + "    \"urlPath\" : \"/count-me\",\n"
                + "    \"method\" : \"GET\"\n"
                + "  },\n"
                + "  \"response\" : {\n"
                + "    \"status\" : 200\n"
                + "  },\n"
                + "  \"postServeActions\": {\n"
                + "    \"count-request\": {\n"
                + "      \"counterName\": \"things\"\n"
                + "    } \n"
                + "  }\n"
                + "}");

    assertThat(response.content(), response.statusCode(), is(201));

    client.get("/count-me");
    client.get("/count-me");

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/things"), is("2"));
  }

  @Test
  public void multipleActionsOfTheSameNameCanBeSpecifiedViaTheDSL() {
    initWithOptions(
        options()
            .dynamicPort()
            .notifier(new ConsoleNotifier(true))
            .extensions(new NamedCounterAction()));

    wm.stubFor(
        get(urlPathEqualTo("/count-me"))
            .willReturn(ok())
            .withPostServeAction("count-request", counterNameParameter().withName("one"))
            .withPostServeAction("count-request", counterNameParameter().withName("two")));

    client.get("/count-me");
    client.get("/count-me");
    client.get("/count-me");

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/one"), is("3"));

    await().atMost(5, SECONDS).until(getContent("/__admin/named-counter/two"), is("3"));
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
                + "  \"postServeActions\": [\n"
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

  private Callable<Integer> getValue(final AtomicInteger value) {
    return value::get;
  }

  private Callable<String> getContent(final String url) {
    return () -> client.get(url).content();
  }

  public static class NamedCounterAction extends PostServeAction implements AdminApiExtension {

    private final ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();

    @Override
    public String getName() {
      return "count-request";
    }

    @Override
    public void contributeAdminApiRoutes(Router router) {
      router.add(
          GET,
          "/named-counter/{name}",
          (admin, serveEvent, pathParams) -> {
            String name = pathParams.get("name");
            Integer count = getFirstNonNull(counters.get(name), 0);
            return responseDefinition().withStatus(200).withBody(String.valueOf(count)).build();
          });
    }

    @Override
    public void doAction(ServeEvent serveEvent, Admin admin, Parameters parameters) {
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
