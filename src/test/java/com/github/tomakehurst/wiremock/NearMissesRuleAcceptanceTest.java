package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.github.tomakehurst.wiremock.verification.Diff.junitStyleDiffMessage;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NearMissesRuleAcceptanceTest {

    @ClassRule
    public static WireMockRule wm = new WireMockRule(options().dynamicPort());

    WireMockTestClient client;

    @Before
    public void init() {
        client = new WireMockTestClient(wm.port());
    }

    @Test
    public void verificationErrorMessageReportsNearMisses() {
        client.get("/my-near-miss");
        client.get("/near-miss");

        try {
            wm.verify(getRequestedFor(urlEqualTo("/a-near-miss")));
            fail();
        } catch (VerificationException e) {
            assertThat(e.getMessage(), containsString(
                junitStyleDiffMessage(
                    "/a-near-miss\n",
                    "/my-near-miss\n"
                )
            ));
        }
    }

    @Ignore
    @Test
    public void showFullUnmatchedRequest() throws Exception {
        client.post("/my-near-miss",
            new StringEntity("{\"data\": { \"one\": 1}}", APPLICATION_JSON),
            withHeader("Content-Type", "application/json")
        );

        wm.verify(putRequestedFor(urlEqualTo("/a-near-miss"))
            .withHeader("Content-Type", equalTo("text/json"))
            .withRequestBody(equalToJson("{\"data\": { \"two\": 1}}")));
    }

    @Test
    public void logsUnmatchedRequestsAtErrorWithNearMisses() {
        wm.stubFor(get(urlEqualTo("/near-miss")).willReturn(aResponse().withStatus(200)));
        wm.stubFor(get(urlEqualTo("/miss")).willReturn(aResponse().withStatus(200)));
    }
}
