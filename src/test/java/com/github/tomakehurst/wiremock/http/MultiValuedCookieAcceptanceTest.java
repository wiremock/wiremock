package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.AcceptanceTestBase;
import com.github.tomakehurst.wiremock.junit.Stubbing;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MultiValuedCookieAcceptanceTest extends AcceptanceTestBase {

    static Stubbing dsl = wireMockServer;

    @Test
    public void acceptsMultiValuedCookie() throws Exception {
        dsl.stubFor(get(urlPathEqualTo("/api"))
            .willReturn(aResponse()
            .withStatus(200)));
      
        WireMockResponse response =  testClient.get("/api", new TestHttpHeader("Cookie", "k1=v2; k1=v1"));

        assertThat(response.statusCode(), is(200));
    }
}
