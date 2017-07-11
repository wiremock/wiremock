package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.admin.model.SnapshotSpec;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SnapshotDslAcceptanceTest extends AcceptanceTestBase {

    private WireMockServer targetService;
    private WireMockServer proxyingService;
    private WireMockTestClient client;

    public void init() {
        proxyingService = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .withRootDirectory(setupTempFileRoot().getAbsolutePath()));
        proxyingService.start();
        proxyingService.stubFor(proxyAllTo("http://localhost:" + wireMockServer.port()));

        targetService = wireMockServer;
        targetService.stubFor(any(anyUrl()).willReturn(ok()));

        client = new WireMockTestClient(proxyingService.port());
        WireMock.configureFor(proxyingService.port());
    }

    @After
    public void proxyServerShutdown() {
        proxyingService.resetMappings();
        proxyingService.stop();
    }

    @Test
    public void snapshotRecordsAllLoggedRequestsWhenNoParametersPassed() throws Exception {
        targetService.stubFor(get("/one").willReturn(
            aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("Number one")));

        client.get("/one");
        client.get("/two");
        client.postJson("/three", "{ \"counter\": 55 }");

        List<StubMapping> returnedMappings = proxyingService.snapshotRecord();
        List<StubMapping> serverMappings = proxyingService.getStubMappings();

        assertTrue("All of the returned mappings should be present in the server", serverMappings.containsAll(returnedMappings));
        assertThat(returnedMappings.size(), is(3));

        assertThat(returnedMappings.get(2).getRequest().getUrl(), is("/one"));
        assertThat(returnedMappings.get(2).getRequest().getHeaders(), nullValue());
        assertThat(returnedMappings.get(2).getRequest().getMethod(), is(RequestMethod.GET));
        assertThat(returnedMappings.get(2).getResponse().getHeaders().getHeader("Content-Type").firstValue(), is("text/plain"));
        assertThat(returnedMappings.get(2).getResponse().getBody(), is("Number one"));

        assertThat(returnedMappings.get(1).getRequest().getUrl(), is("/two"));

        assertThat(returnedMappings.get(0).getRequest().getUrl(), is("/three"));

        StringValuePattern bodyPattern = returnedMappings.get(0).getRequest().getBodyPatterns().get(0);
        assertThat(bodyPattern, instanceOf(EqualToJsonPattern.class));
        JSONAssert.assertEquals("{ \"counter\": 55 }", bodyPattern.getExpected(), true);

        EqualToJsonPattern equalToJsonPattern = (EqualToJsonPattern) bodyPattern;
        assertThat(equalToJsonPattern.isIgnoreArrayOrder(), is(true));
        assertThat(equalToJsonPattern.isIgnoreExtraElements(), is(true));
    }

    @Test
    public void snapshotRecordsAllLoggedRequestsWhenFilterIsSpecified() throws Exception {
        client.get("/things/1");
        client.get("/things/2");
        client.get("/stuff/1");
        client.get("/things/3");
        client.get("/stuff/2");

        List<StubMapping> mappings = proxyingService.snapshotRecord(
            snapshotSpec()
                .onlyRequestsMatching(getRequestedFor(urlPathMatching("/things/.*")))
        );

        assertThat(mappings.size(), is(3));
    }

    @Test
    public void staticClientIsSupportedWithDefaultSpec() {
        client.get("/get-this");

        snapshotRecord();

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this")));
    }

    @Test
    public void instanceClientIsSupportedWithDefaultSpec() {
        WireMock adminClient = new WireMock(proxyingService.port());

        client.get("/get-this-too");
        adminClient.takeSnapshotRecording();

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this-too")));
    }

    private static TypeSafeDiagnosingMatcher<StubMapping> stubMappingWithUrl(final String url) {
        return new TypeSafeDiagnosingMatcher<StubMapping>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("a stub mapping with a request URL of " + url);
            }

            @Override
            protected boolean matchesSafely(StubMapping item, Description mismatchDescription) {
                return url.equals(item.getRequest().getUrl());
            }
        };
    }
}
