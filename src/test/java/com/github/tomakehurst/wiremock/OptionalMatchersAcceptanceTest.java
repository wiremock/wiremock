package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OptionalMatchersAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void matchesWhenOptionalFieldIsPresent() {
        final String URL = "/optional/present";
        final String body = "optional";

        stubFor(post(urlEqualTo(URL))
                .withRequestBody(optionalEqualTo("optional"))
                .willReturn(aResponse().withStatus(201)));

        WireMockResponse response = testClient.postJson(URL, body);

        assertThat(response.statusCode(), is(201));
    }

    @Test
    public void matchesWhenOptionalFieldIsAbsent() {
        final String URL = "/optional/present";
        final String body = "";

        stubFor(post(urlEqualTo(URL))
                .withRequestBody(optionalEqualTo("optional"))
                .willReturn(aResponse().withStatus(201)));

        WireMockResponse response = testClient.postJson(URL, null);
        assertThat(response.statusCode(), is(201));
    }
}
