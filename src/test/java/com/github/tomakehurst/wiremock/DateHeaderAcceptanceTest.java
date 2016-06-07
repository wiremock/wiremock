package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DateHeaderAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void returnsStubbedDateHeader() {

        stubFor(get(urlEqualTo("/stubbed/dateheader"))
            .willReturn(aResponse().withStatus(200).withHeader("Date", "Sun, 06 Nov 1994 08:49:37 GMT")));

        WireMockResponse response = testClient.get("/stubbed/dateheader");

        assertThat(response.firstHeader("Date"), is("Sun, 06 Nov 1994 08:49:37 GMT"));
    }

    @Test
    public void returnsNullDateHeaderIfNotStubbed() {

        stubFor(get(urlEqualTo("/nodateheader")).willReturn(aResponse().withStatus(200)));

        WireMockResponse response = testClient.get("/nodateheader");

        assertThat(response.firstHeader("Date"), is(nullValue()));
    }

}
