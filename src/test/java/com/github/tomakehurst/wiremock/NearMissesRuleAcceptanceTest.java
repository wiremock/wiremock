package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class NearMissesRuleAcceptanceTest {

    @ClassRule
    public static WireMockRule wm = new WireMockRule(options()
        .dynamicPort()
        .withRootDirectory("src/main/resources/empty"));

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
    }

    @Ignore
    @Test
    public void showFullUnmatchedRequest() throws Exception {
        client.get("/some-other-thing");
        client.get("/totally-something-else");
        client.get("/whatever");
        client.post("/my-near-miss",
            new StringEntity("{\"data\": { \"one\": 1}}", APPLICATION_JSON),
            withHeader("Content-Type", "application/json"),
            withHeader("X-Expected", "yes"),
            withHeader("X-Matched-1", "yes"),
            withHeader("Cookie", "this=that"),
            withHeader("Authorization", new BasicCredentials("user", "wrong-pass").asAuthorizationHeaderValue())
        );

        wm.verify(postRequestedFor(urlEqualTo("/a-near-miss"))
            .withHeader("Content-Type", equalTo("text/json"))
            .withHeader("X-Expected", equalTo("yes"))
            .withHeader("X-Matched-1", matching("ye.*"))
            .withHeader("X-Matched-2", containing("no"))
            .withCookie("this", equalTo("other"))
            .withBasicAuth(new BasicCredentials("user", "pass"))
            .withRequestBody(equalToJson("{\"data\": { \"two\": 1}}")));
    }

    @Test
    public void logsUnmatchedRequestsAtErrorWithNearMisses() {
        wm.stubFor(get(urlEqualTo("/near-miss")).willReturn(aResponse().withStatus(200)));
        wm.stubFor(get(urlEqualTo("/miss")).willReturn(aResponse().withStatus(200)));
    }
}
