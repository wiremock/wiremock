package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomMatchingAcceptanceTest {

    @Rule
    public WireMockRule wm = new WireMockRule(wireMockConfig().dynamicPort());

    @Test
    public void customMatcher() {
        wm.stubFor(requestMatching(new MyRequestMatcher()).willReturn(aResponse().withStatus(200)));

        WireMockTestClient client = new WireMockTestClient(wm.port());

        assertThat(client.get("/correct").statusCode(), is(200));
        assertThat(client.get("/wrong").statusCode(), is(404));
    }

    public static class MyRequestMatcher implements RequestMatcher {

        @Override
        public boolean isMatchedBy(Request request) {
            return request.getUrl().contains("correct");
        }
    }
}
