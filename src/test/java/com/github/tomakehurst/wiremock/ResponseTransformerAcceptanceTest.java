package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ResponseTransformerAcceptanceTest {

    WireMockTestClient client;

    @Test
    public void transformerSpecifiedByClassTransformsHeadersStatusAndBody() {
        WireMockServer wm = new WireMockServer(wireMockConfig()
                .port(0)
                .extensions("com.github.tomakehurst.wiremock.ResponseTransformerAcceptanceTest$ExampleTransformer"));
        wm.start();
        client = new WireMockTestClient(wm.port());

        wm.stubFor(get(urlEqualTo("/to-transform")).willReturn(aResponse()
                .withHeader("MyHeader", "Initial")
                .withStatus(300)
                .withBody("Should not see this")));

        WireMockResponse response = client.get("/to-transform");
        assertThat(response.statusCode(), is(200));
        assertThat(response.firstHeader("MyHeader"), is("Transformed"));
        assertThat(response.content(), is("Transformed body"));
    }

    public static class ExampleTransformer implements ResponseTransformer {

        @Override
        public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition) {
            return new ResponseDefinitionBuilder()
                    .withHeader("MyHeader", "Transformed")
                    .withStatus(200)
                    .withBody("Transformed body")
                    .build();
        }

        @Override
        public String name() {
            return "example";
        }
    }
}
