package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.google.common.net.HttpHeaders.COOKIE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CookieMatchingAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void matchesOnWellFormedCookie() {
        stubFor(get(urlEqualTo("/good/cookie"))
            .withCookie("my_cookie", "mycookievalue")
            .willReturn(aResponse().withStatus(200)));

        WireMockResponse response =
            testClient.get("/good/cookie", withHeader(COOKIE, "my_cookie=mycookievalue"));

        assertThat(response.statusCode(), is(200));

    }
}
