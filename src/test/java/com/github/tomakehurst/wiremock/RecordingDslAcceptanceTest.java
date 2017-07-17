package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.findMappingWithUrl;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RecordingDslAcceptanceTest extends AcceptanceTestBase {

    private WireMockServer targetService;
    private WireMockServer proxyingService;
    private WireMockTestClient client;
    private WireMock adminClient;
    private String targetBaseUrl;

    public void init() {
        proxyingService = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .withRootDirectory(setupTempFileRoot().getAbsolutePath()));
        proxyingService.start();

        targetService = wireMockServer;
        targetBaseUrl = "http://localhost:" + targetService.port();

        client = new WireMockTestClient(proxyingService.port());
        WireMock.configureFor(proxyingService.port());
        adminClient = new WireMock(proxyingService.port());
    }

    @After
    public void proxyServerShutdown() {
        proxyingService.resetMappings();
        proxyingService.stop();
    }

    @Test
    public void startsRecordingWithDefaultSpecFromTheSpecifiedProxyBaseUrlWhenServeEventsAlreadyExist() {
        targetService.stubFor(get("/record-this").willReturn(okForContentType("text/plain","Got it")));
        targetService.stubFor(get(urlPathMatching("/do-not-record-this/.*")).willReturn(noContent()));

        client.get("/do-not-record-this/1");
        client.get("/do-not-record-this/2");
        client.get("/do-not-record-this/3");

        startRecording(targetBaseUrl);

        client.get("/record-this");

        List<StubMapping> returnedMappings = stopRecording().getStubMappings();
        client.get("/do-not-record-this/4");


        assertThat(returnedMappings.size(), is(1));
        assertThat(returnedMappings.get(0).getRequest().getUrl(), is("/record-this"));

        StubMapping mapping = findMappingWithUrl(proxyingService.getStubMappings(), "/record-this");
        assertThat(mapping.getResponse().getBody(), is("Got it"));
    }

    @Test
    public void startsRecordingWithDefaultSpecFromTheSpecifiedProxyBaseUrlWhenNoServeEventsAlreadyExist() {
        targetService.stubFor(get("/record-this").willReturn(okForContentType("text/plain","Got it")));

        startRecording(targetBaseUrl);

        client.get("/record-this");

        List<StubMapping> returnedMappings = stopRecording().getStubMappings();

        assertThat(returnedMappings.size(), is(1));
        assertThat(returnedMappings.get(0).getRequest().getUrl(), is("/record-this"));

        StubMapping mapping = findMappingWithUrl(proxyingService.getStubMappings(), "/record-this");
        assertThat(mapping.getResponse().getBody(), is("Got it"));
    }

    @Test
    public void recordsNothingWhenNoServeEventsAreRecievedDuringRecording() {
        targetService.stubFor(get(urlPathMatching("/do-not-record-this/.*")).willReturn(noContent()));

        client.get("/do-not-record-this/1");
        client.get("/do-not-record-this/2");

        startRecording(targetBaseUrl);
        List<StubMapping> returnedMappings = stopRecording().getStubMappings();
        client.get("/do-not-record-this/3");

        assertThat(returnedMappings.size(), is(0));
        assertThat(proxyingService.getStubMappings(), Matchers.<StubMapping>empty());
    }

    @Test
    public void recordsNothingWhenNoServeEventsAreRecievedAtAll() {
        startRecording(targetBaseUrl);
        List<StubMapping> returnedMappings = stopRecording().getStubMappings();

        assertThat(returnedMappings.size(), is(0));
        assertThat(proxyingService.getStubMappings(), Matchers.<StubMapping>empty());
    }

    @Test
    public void honoursRecordSpecWhenPresent() {
        targetService.stubFor(get("/record-this-with-header").willReturn(ok()));

        startRecording(snapshotSpec()
            .forTarget(targetBaseUrl)
            .captureHeader("Accept")
        );

        client.get("/record-this", withHeader("Accept", "text/plain"));

        List<StubMapping> returnedMappings = stopRecording().getStubMappings();

        assertThat(returnedMappings.get(0).getRequest().getHeaders().get("Accept").getExpected(), is("text/plain"));
    }

    @Test
    public void supportsInstanceClientWithDefaultSpec() {
        targetService.stubFor(get("/record-this").willReturn(okForContentType("text/plain","Got it")));

        adminClient.startStubRecording(targetBaseUrl);

        client.get("/record-this");

        List<StubMapping> returnedMappings = adminClient.stopStubRecording().getStubMappings();

        assertThat(returnedMappings.size(), is(1));
        assertThat(returnedMappings.get(0).getRequest().getUrl(), is("/record-this"));

        StubMapping mapping = findMappingWithUrl(proxyingService.getStubMappings(), "/record-this");
        assertThat(mapping.getResponse().getBody(), is("Got it"));
    }

    @Test
    public void supportsInstanceClientWithSpec() {
        targetService.stubFor(post("/record-this-with-body").willReturn(ok()));

        adminClient.startStubRecording(
            snapshotSpec()
                .forTarget(targetBaseUrl)
                .jsonBodyMatchFlags(true, true)
        );

        client.postJson("/record-this-with-body", "{}");

        List<StubMapping> returnedMappings = adminClient.stopStubRecording().getStubMappings();

        EqualToJsonPattern bodyPattern = (EqualToJsonPattern) returnedMappings.get(0).getRequest().getBodyPatterns().get(0);
        assertThat(bodyPattern.isIgnoreArrayOrder(), is(true));
        assertThat(bodyPattern.isIgnoreExtraElements(), is(true));
    }

}
