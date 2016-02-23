package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BasicAuthAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void matchesPreemptiveBasicAuthWhenCredentialAreCorrect() {
        stubFor(get(urlEqualTo("/basic/auth/preemptive"))
            .withBasicAuth("the-username", "thepassword")
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response = testClient.getWithPreemptiveCredentials(
            "/basic/auth/preemptive", wireMockServer.port(), "the-username", "thepassword");

        assertThat(response.statusCode(), is(200));
    }

    @Test
    public void doesNotMatchPreemptiveBasicAuthWhenCredentialsAreIncorrect() {
        stubFor(get(urlEqualTo("/basic/auth/preemptive"))
            .withBasicAuth("the-username", "thepassword")
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response = testClient.getWithPreemptiveCredentials(
            "/basic/auth/preemptive", wireMockServer.port(), "the-username", "WRONG!!!");

        assertThat(response.statusCode(), is(404));
    }

}
