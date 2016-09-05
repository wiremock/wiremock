package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AdminCrossOriginTest extends AcceptanceTestBase {

    @Test
    public void sendsCorsHeadersInResponseToOPTIONSQuery() {
        WireMockResponse response = testClient.options("/__admin/",
            withHeader("Origin", "http://my.corp.com"),
            withHeader("Access-Control-Request-Method", "POST")
        );

        assertThat(response.statusCode(), is(200));
        assertThat(response.firstHeader("Access-Control-Allow-Origin"), is("http://my.corp.com"));
        assertThat(response.firstHeader("Access-Control-Allow-Methods"), is("OPTIONS,GET,POST,PUT"));
    }
}
