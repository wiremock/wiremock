package com.github.tomakehurst.wiremock;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.PostServeActionExtensionTest.CounterNameParameter.counterNameParameter;
import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.google.common.base.MoreObjects.firstNonNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PostServeActionExtensionTest {

    WireMockServer wm;
    WireMockTestClient client;

    void initWithOptions(Options options) {
        wm = new WireMockServer(options);
        wm.start();
        client = new WireMockTestClient(wm.port());
    }

    @After
    public void cleanup() {
        if (wm != null) {
            wm.stop();
        }
    }

    @Test
    public void triggersActionWhenAppliedToAStubMapping() {
        initWithOptions(options()
            .dynamicPort()
            .extensions(new NamedCounterAction()));

        wm.stubFor(get(urlPathEqualTo("/count-me"))
            .withPostServeAction("count-request",
                counterNameParameter()
                    .withName("things")
            )
            .willReturn(aResponse()));

        client.get("/count-me");
        client.get("/count-me");
        client.get("/count-me");
        client.get("/count-me");

        String count = client.get("/__admin/named-counter/things").content();
        assertThat(count, is("4"));
    }

    @Test
    public void continuesWithNoEffectIfANonExistentActionIsReferenced() {
        initWithOptions(options().dynamicPort());

        wm.stubFor(get(urlPathEqualTo("/as-normal"))
            .withPostServeAction("does-not-exist",
                counterNameParameter()
                    .withName("things")
            )
            .willReturn(aResponse().withStatus(200))
        );

        assertThat(client.get("/as-normal").statusCode(), is(200));
    }

    public static class NamedCounterAction extends PostServeAction implements AdminApiExtension {

        private final ConcurrentHashMap<String, Integer> counters = new ConcurrentHashMap<>();

        @Override
        public String getName() {
            return "count-request";
        }

        @Override
        public void contributeAdminApiRoutes(Router router) {
            router.add(GET, "/named-counter/{name}", new AdminTask() {
                @Override
                public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {
                    String name = pathParams.get("name");
                    Integer count = firstNonNull(counters.get(name), 0);
                    return responseDefinition()
                        .withStatus(200)
                        .withBody(String.valueOf(count))
                        .build();
                }
            });
        }

        @Override
        public void doAction(ServeEvent serveEvent, Admin admin, Parameters parameters) {
            CounterNameParameter counterNameParam = parameters.as(CounterNameParameter.class);

            String counterName = counterNameParam.counterName;

            Integer count = firstNonNull(counters.get(counterName), 0);

            counters.putIfAbsent(counterName, 0);
            counters.replace(counterName, ++count);

        }
    }

    public static class CounterNameParameter {

        public String counterName;

        public CounterNameParameter(@JsonProperty("counterName") String counterName) {
            this.counterName = counterName;
        }

        public CounterNameParameter() {
        }

        public static CounterNameParameter counterNameParameter() {
            return new CounterNameParameter();
        }

        public CounterNameParameter withName(String name) {
            this.counterName = name;
            return this;
        }

    }
}
