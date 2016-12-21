package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ResponseTemplatingAcceptanceTest {

    public static class Local {

        WireMockTestClient client;

        @Rule
        public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(new ResponseTemplateTransformer(false))
        );

        @Before
        public void init() {
            client = new WireMockTestClient(wm.port());
        }

        @Test
        public void appliesResponseTemplateWhenAddedToStubMapping() {
            wm.stubFor(get(urlPathEqualTo("/templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")
                    .withTransformers("response-template")));

            assertThat(client.get("/templated").content(), is("templated"));
        }

        @Test
        public void doesNotApplyResponseTemplateWhenNotAddedToStubMapping() {
            wm.stubFor(get(urlPathEqualTo("/not-templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")));

            assertThat(client.get("/not-templated").content(), is("{{request.path.[0]}}"));
        }
    }

    public static class Global {

        WireMockTestClient client;

        @Rule
        public WireMockRule wm = new WireMockRule(options()
            .dynamicPort()
            .extensions(new ResponseTemplateTransformer(true))
        );

        @Before
        public void init() {
            client = new WireMockTestClient(wm.port());
        }

        @Test
        public void appliesResponseTemplate() {
            wm.stubFor(get(urlPathEqualTo("/templated"))
                .willReturn(aResponse()
                    .withBody("{{request.path.[0]}}")));

            assertThat(client.get("/templated").content(), is("templated"));
        }
    }
}
