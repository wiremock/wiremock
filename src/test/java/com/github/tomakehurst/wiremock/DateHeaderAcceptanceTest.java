package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateHeaderAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void returnsOnlyStubbedDateHeader() {

        stubFor(get(urlEqualTo("/stubbed/dateheader"))
            .willReturn(aResponse().withStatus(200).withHeader("Date", "Sun, 06 Nov 1994 08:49:37 GMT")));

        WireMockResponse response = testClient.get("/stubbed/dateheader");

        assertThat(response.headers().get("Date"), contains("Sun, 06 Nov 1994 08:49:37 GMT"));
    }

    @Test
    public void returnsNoDateHeaderIfNotStubbed() {

        stubFor(get(urlEqualTo("/nodateheader")).willReturn(aResponse().withStatus(200)));

        WireMockResponse response = testClient.get("/nodateheader");

        assertThat(response.headers().get("Date"), is(Matchers.<String>empty()));
    }

}
