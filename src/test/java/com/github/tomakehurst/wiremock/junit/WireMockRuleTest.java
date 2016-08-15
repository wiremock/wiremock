package com.github.tomakehurst.wiremock.junit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class WireMockRuleTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public WireMockRule rule;

    @Before
    public void setup() {
        rule = new WireMockRule(0);
        rule.stubFor(get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(200)));
        rule.start();
    }

    @Test
    public void shouldFindExactMatch() {
        WireMockTestClient testClient = new WireMockTestClient(rule.port());
        testClient.get(String.format("http://localhost:%d/", rule.port()));

        rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));
    }

    @Test
    public void shouldFindNearMatch() {
        WireMockTestClient testClient = new WireMockTestClient(rule.port());
        testClient.get(String.format("http://localhost:%d/1", rule.port()));

        thrown.expect(com.github.tomakehurst.wiremock.client.VerificationException.class);
        thrown.expectMessage("No requests exactly matched. Most similar request was:");
        rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));
    }


    @Test
    public void shouldNotFindMatchAfterReset() {
        WireMockTestClient testClient = new WireMockTestClient(rule.port());
        testClient.get(String.format("http://localhost:%d/", rule.port()));

        rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));

        rule.resetRequests();

        thrown.expect(com.github.tomakehurst.wiremock.client.VerificationException.class);
        thrown.expectMessage("Expected at least one request matching:");
        rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));
    }

    @Test
    public void shouldFindMatchWithoutHeaderVerification() throws IOException {
        WireMockTestClient testClient = new WireMockTestClient(rule.port());
        testClient.get(String.format("http://localhost:%d/", rule.port()));

        rule.verify(1, getRequestedFor(urlEqualTo("/")));
    }

}
