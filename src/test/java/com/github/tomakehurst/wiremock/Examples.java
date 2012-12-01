package com.github.tomakehurst.wiremock;

import org.junit.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Examples extends AcceptanceTestBase {

    @Test
    public void exactUrlOnly() {
        stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "text/plain")
                    .withBody("Hello world!")));

        assertThat(testClient.get("/some/thing").statusCode(), is(200));
        assertThat(testClient.get("/some/thing/else").statusCode(), is(404));
    }

    @Test
    public void urlRegexMatch() {
        stubFor(put(urlMatching("/thing/matching/[0-9]+"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void headerMatching() {
        stubFor(post(urlEqualTo("/with/headers"))
                .withHeader("Content-Type", equalTo("text/xml"))
                .withHeader("Accept", matching("text/.*"))
                .withHeader("etag", notMatching("abcd.*"))
                .withHeader("etag", containing("2134"))
                    .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void bodyMatching() {
        stubFor(post(urlEqualTo("/with/body"))
                .withRequestBody(matching("<status>OK</status>"))
                .withRequestBody(notMatching("<status>ERROR</status>"))
                    .willReturn(aResponse().withStatus(200)));
    }

    @Test
    public void priorities() {

        //Catch-all case
        stubFor(get(urlMatching("/api/.*")).atPriority(5)
            .willReturn(aResponse().withStatus(401)));

        //Specific case
        stubFor(get(urlEqualTo("/api/specific-resource")).atPriority(1) //1 is highest
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody("Resource state")));
    }

}
