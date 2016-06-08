package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.NearMiss;
import com.github.tomakehurst.wiremock.stubbing.ServedStub;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NearMissesAcceptanceTest extends AcceptanceTestBase {

    @Ignore("to be deleted")
    @Test
    public void nearMisses() {
        stubFor(get(urlEqualTo("/mypath")).withHeader("My-Header", equalTo("matched"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/otherpath")).withHeader("My-Header", equalTo("otherheaderval"))
            .willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/yet/another/path")).withHeader("X-Alt-Header", equalTo("matchonthis"))
            .willReturn(aResponse().withStatus(200)));

        testClient.get("/otherpath", withHeader("My-Header", "notmatched"));

        List<NearMiss> nearMisses = WireMock.findAllNearMisses();

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
}
