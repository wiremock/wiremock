package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class TransferEncodingAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void matchingOnRequestBodyWithTwoRegexes() {
        final String url = "/content-length-encoding";
        final String body = "Body content";

        stubFor(get(url).willReturn(ok(body)));

        WireMockResponse response = testClient.get(url);
        assertThat(response.statusCode(), is(200));

        String expectedContentLength = String.valueOf(body.getBytes().length);
        assertThat(response.firstHeader("Transfer-Encoding"), nullValue());
        assertThat(response.firstHeader("Content-Length"), is(expectedContentLength));
    }
}
