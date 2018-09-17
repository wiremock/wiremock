package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DebugHeadersAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void returnsMatchedStubIdHeaderWhenStubMatched() {
        UUID stubId = UUID.randomUUID();
        wireMockServer.stubFor(get("/the-match")
            .withId(stubId)
            .willReturn(ok()));

        WireMockResponse response = testClient.get("/the-match");

        assertThat(response.firstHeader("Matched-Stub-Id"), is(stubId.toString()));
        assertThat(response.firstHeader("Matched-Stub-Name"), nullValue());
    }

    @Test
    public void returnsMatchedStubNameHeaderWhenNamedStubMatched() {
        UUID stubId = UUID.randomUUID();
        String name = "My Stub";

        wireMockServer.stubFor(get("/the-match")
            .withId(stubId)
            .withName(name)
            .willReturn(ok()));

        WireMockResponse response = testClient.get("/the-match");

        assertThat(response.firstHeader("Matched-Stub-Id"), is(stubId.toString()));
        assertThat(response.firstHeader("Matched-Stub-Name"), is(name));
    }

    @Test
    public void doesNotReturnEitherHeaderIfNoStubMatched() {
        WireMockResponse response = testClient.get("/the-non-match");

        assertThat(response.firstHeader("Matched-Stub-Id"), nullValue());
        assertThat(response.firstHeader("Matched-Stub-Name"), nullValue());
    }
}
