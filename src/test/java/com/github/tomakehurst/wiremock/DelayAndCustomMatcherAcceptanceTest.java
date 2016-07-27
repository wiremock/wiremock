package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.google.common.base.Stopwatch;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DelayAndCustomMatcherAcceptanceTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().extensions(BodyChanger.class));

    @Test
    public void delayIsAddedWhenCustomResponseTransformerPresent() {
        stubFor(get(urlEqualTo("/delay-this"))
            .willReturn(aResponse()
                .withStatus(200)
                .withTransformers("response-body-changer")
                .withUniformRandomDelay(500, 1000)));

        WireMockTestClient client = new WireMockTestClient(wireMockRule.port());

        Stopwatch stopwatch = Stopwatch.createStarted();
        WireMockResponse response = client.get("/delay-this");
        stopwatch.stop();

        assertThat(stopwatch.elapsed(MILLISECONDS), greaterThanOrEqualTo(500L));
        assertThat(response.statusCode(), is(200));
        assertThat(response.content(), is("Transformed body"));
    }

    public static class BodyChanger extends ResponseDefinitionTransformer {

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {
            return ResponseDefinitionBuilder
                .like(responseDefinition).but()
                .withBody("Transformed body")
                .build();
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

        @Override
        public String getName() {
            return "response-body-changer";
        }
    }
}
