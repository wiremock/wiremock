package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseTransformerAcceptanceTest {

    WireMockServer wm;
    WireMockTestClient client;

    @Test
    public void transformsStubResponse() {
        startWithExtensions(StubResponseTransformer.class);

        wm.stubFor(get(urlEqualTo("/response-transform")).willReturn(aResponse().withBody("Original body")));

        assertThat(client.get("/response-transform").content(), is("Modified body"));
    }

    @Test
    public void acceptsTransformerParameters() {
        startWithExtensions(StubResponseTransformerWithParams.class);

        wm.stubFor(get(urlEqualTo("/response-transform-with-params")).willReturn(
                aResponse()
                        .withTransformerParameter("name", "John")
                        .withTransformerParameter("number", 66)
                        .withTransformerParameter("flag", true)
                        .withBody("Original body")));

        assertThat(client.get("/response-transform-with-params").content(), is("John, 66, true"));
    }

    @SuppressWarnings("unchecked")
    private void startWithExtensions(Class<? extends Extension> extensionClasses) {
        wm = new WireMockServer(wireMockConfig().dynamicPort().extensions(extensionClasses));
        wm.start();
        client = new WireMockTestClient(wm.port());
    }

    public static class StubResponseTransformer extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            return Response.Builder.like(response)
                    .but().body("Modified body")
                    .build();
        }

        @Override
        public boolean applyGlobally() {
            return true;
        }

        @Override
        public String name() {
            return "stub-transformer";
        }
    }

    public static class StubResponseTransformerWithParams extends ResponseTransformer {

        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
            return Response.Builder.like(response)
                    .but().body(parameters.getString("name") + ", "
                            + parameters.getInt("number") + ", "
                            + parameters.getBoolean("flag"))
                    .build();
        }

        @Override
        public boolean applyGlobally() {
            return true;
        }

        @Override
        public String name() {
            return "stub-transformer-with-params";
        }
    }
}
