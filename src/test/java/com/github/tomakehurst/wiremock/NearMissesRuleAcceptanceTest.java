package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.Matchers.is;
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
            assertThat(e.getMessage(), is(""));
        }
    }

    @Test
    public void tmp() {
//        throw new VerificationException("\nExpected: is \"one\"\n     but: was \"two\"");
        throw new VerificationException("\n" +
            "Expected: is \"" +
            "/expected" +
            "\"\n" +
            "     but: was \"" +
            "/actual" +
            "\"");
//        assertThat("one", is("two"));
    }

    @Test
    public void logsUnmatchedRequestsAtErrorWithNearMisses() {
        wm.stubFor(get(urlEqualTo("/near-miss")).willReturn(aResponse().withStatus(200)));
        wm.stubFor(get(urlEqualTo("/miss")).willReturn(aResponse().withStatus(200)));
    }
}
