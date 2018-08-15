package com.github.tomakehurst.wiremock;

import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MultiValuedParameterAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void onlyMatchIfAllExpectedValuesOfAHeaderAreMatched() {
        stubFor(get("/multi-value-headers")
            .withHeader("X-My-Header", matchingAll(
                equalTo("1"),
                containing("2"),
                matching("th.*"))
            )
            .willReturn(ok()));

        int statusCode = testClient.get(
            "/multi-value-headers",
            withHeader("X-My-Header", "1"),
            withHeader("X-My-Header", "222"),
            withHeader("X-My-Header", "three")
        ).statusCode();


        assertThat(statusCode, is(200));
    }
}
