package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NearMissesAcceptanceTest extends AcceptanceTestBase {

    @Test
    public void nearMisses() {
        stubFor(get(urlEqualTo("/mypath")).withHeader("My-Header", equalTo("matched"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/otherpath")).withHeader("My-Header", equalTo("otherheaderval"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/yet/another/path")).withHeader("X-Alt-Header", equalTo("matchonthis"))
            .willReturn(aResponse().withStatus(200)));

        testClient.get("/otherpath", withHeader("My-Header", "notmatched"));

        List<NearMiss> nearMisses = WireMock.findNearMissesForAllUnmatched();

        assertThat(nearMisses.get(0).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(0).getStubMapping().getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(1).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(1).getStubMapping().getRequest().getUrl(), is("/mypath"));
        assertThat(nearMisses.get(2).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(2).getStubMapping().getRequest().getUrl(), is("/yet/another/path"));
    }

    @Test
    public void returnsAllUnmatchedRequests() {
        stubFor(get(urlEqualTo("/mypath")).withHeader("My-Header", equalTo("matched"))
            .willReturn(aResponse().withStatus(200)));

        testClient.get("/unmatched/path");

        List<LoggedRequest> unmatched = WireMock.findUnmatchedRequests();

        assertThat(unmatched.size(), is(1));
        assertThat(unmatched.get(0).getUrl(), is("/unmatched/path"));
    }

    @Test
    public void returnsStubMappingNearMissesForARequest() {
        stubFor(get(urlEqualTo("/mypath")).withHeader("My-Header", equalTo("matched"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/otherpath")).withHeader("My-Header", equalTo("otherheaderval"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/yet/another/path")).withHeader("X-Alt-Header", equalTo("matchonthis"))
            .willReturn(aResponse().withStatus(200)));

        List<NearMiss> nearMisses = WireMock.findNearMissesFor(LoggedRequest.createFrom(
            mockRequest().url("/otherpath").header("My-Header", "notmatched")
        ));

        assertThat(nearMisses.get(0).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(0).getStubMapping().getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(1).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(1).getStubMapping().getRequest().getUrl(), is("/mypath"));
        assertThat(nearMisses.get(2).getRequest().getUrl(), is("/otherpath"));
        assertThat(nearMisses.get(2).getStubMapping().getRequest().getUrl(), is("/yet/another/path"));
    }

    @Test
    public void returnsRequestNearMissesForARequestPattern() {
        testClient.get("/actual11");
        testClient.get("/actual42");

        List<NearMiss> nearMisses = WireMock.findNearMissesFor(
            getRequestedFor(urlEqualTo("/actual4"))
                .withRequestBody(containing("thing"))
        );

        assertThat(nearMisses.size(), is(2));
        assertThat(nearMisses.get(0).getRequest().getUrl(), is("/actual42"));
        assertThat(nearMisses.get(1).getRequest().getUrl(), is("/actual11"));
    }
}
