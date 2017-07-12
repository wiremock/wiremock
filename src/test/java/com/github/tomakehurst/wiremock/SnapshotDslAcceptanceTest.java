package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SnapshotDslAcceptanceTest extends AcceptanceTestBase {

    private WireMockServer targetService;
    private WireMockServer proxyingService;
    private WireMockTestClient client;
    private WireMock adminClient;

    public void init() {
        proxyingService = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .extensions(new TestParameterisedTransformer())
            .withRootDirectory(setupTempFileRoot().getAbsolutePath()));
        proxyingService.start();
        proxyingService.stubFor(proxyAllTo("http://localhost:" + wireMockServer.port()));

        targetService = wireMockServer;
        targetService.stubFor(any(anyUrl()).willReturn(ok()));

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

        assertThat(returnedMappings.get(0).getRequest().getUrl(), is("/one"));
        assertThat(returnedMappings.get(0).getRequest().getHeaders(), nullValue());
        assertThat(returnedMappings.get(0).getRequest().getMethod(), is(RequestMethod.GET));
        assertThat(returnedMappings.get(0).getResponse().getHeaders().getHeader("Content-Type").firstValue(), is("text/plain"));
        assertThat(returnedMappings.get(0).getResponse().getBody(), is("Number one"));

        assertThat(returnedMappings.get(1).getRequest().getUrl(), is("/two"));

        assertThat(returnedMappings.get(2).getRequest().getUrl(), is("/three"));

        StringValuePattern bodyPattern = returnedMappings.get(2).getRequest().getBodyPatterns().get(0);
        assertThat(bodyPattern, instanceOf(EqualToJsonPattern.class));
        JSONAssert.assertEquals("{ \"counter\": 55 }", bodyPattern.getExpected(), true);

        EqualToJsonPattern equalToJsonPattern = (EqualToJsonPattern) bodyPattern;
        assertThat(equalToJsonPattern.isIgnoreArrayOrder(), is(true));
        assertThat(equalToJsonPattern.isIgnoreExtraElements(), is(true));
    }

    @Test
    public void supportsFilteringByCriteria() throws Exception {
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
        assertThat(mappings, everyItem(stubMappingWithUrl(urlPathMatching("/things.*"))));
        assertThat(mappings, not(hasItem(stubMappingWithUrl(urlPathMatching("/stuff.*")))));
    }

    @Test
    public void supportsFilteringByServeEventId() throws Exception {
        client.get("/1");
        client.get("/2");
        client.get("/3");

        UUID serveEventId = findServeEventWithUrl(proxyingService.getAllServeEvents(), "/2").getId();

        List<StubMapping> mappings = adminClient.takeSnapshotRecording(
            snapshotSpec()
                .onlyRequestIds(singletonList(serveEventId))
        );

        assertThat(mappings.size(), is(1));
        assertThat(mappings.get(0).getRequest().getUrl(), is("/2"));
    }

    @Test
    public void supportsRequestHeaderCriteria() {
        client.get("/one", withHeader("Yes", "1"), withHeader("No", "1"));
        client.get("/two", withHeader("Yes", "2"), withHeader("Also-Yes", "BBB"));

        List<StubMapping> mappings = snapshotRecord(
            snapshotSpec()
                .captureHeader("Yes")
                .captureHeader("Also-Yes", true)
        );

        StringValuePattern yesValuePattern = mappings.get(0).getRequest().getHeaders().get("Yes").getValuePattern();
        assertThat(yesValuePattern, instanceOf(EqualToPattern.class));
        assertThat(((EqualToPattern) yesValuePattern).getCaseInsensitive(), is(false));
        assertFalse(mappings.get(0).getRequest().getHeaders().containsKey("No"));

        StringValuePattern alsoYesValuePattern = mappings.get(1).getRequest().getHeaders().get("Also-Yes").getValuePattern();
        assertThat(alsoYesValuePattern, instanceOf(EqualToPattern.class));
        assertThat(((EqualToPattern) alsoYesValuePattern).getCaseInsensitive(), is(true));
    }

    @Test
    public void supportsBodyExtractCriteria() throws Exception {
        targetService.stubFor(get("/small/text").willReturn(aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody("123")));
        targetService.stubFor(get("/large/text").willReturn(aResponse()
            .withHeader("Content-Type", "text/plain")
            .withBody("12345678901234567")));
        targetService.stubFor(get("/small/binary").willReturn(aResponse()
            .withHeader("Content-Type", "application/octet-stream")
            .withBody(new byte[] { 1, 2, 3 })));
        targetService.stubFor(get("/large/binary").willReturn(aResponse()
            .withHeader("Content-Type", "application/octet-stream")
            .withBody(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 })));

        client.get("/small/text");
        client.get("/large/text");
        client.get("/small/binary");
        client.get("/large/binary");

        List<StubMapping> mappings = snapshotRecord(
            snapshotSpec()
                .extractTextBodiesOver(10)
                .extractBinaryBodiesOver(5)
        );

        assertThat(mappings.size(), is(4));
        assertThat(findMappingWithUrl(mappings, "/small/text").getResponse().getBodyFileName(), nullValue());
        assertThat(findMappingWithUrl(mappings, "/large/text").getResponse().getBodyFileName(), containsString("large-text"));
        assertThat(findMappingWithUrl(mappings, "/small/binary").getResponse().getBodyFileName(), nullValue());
        assertThat(findMappingWithUrl(mappings, "/large/binary").getResponse().getBodyFileName(), containsString("large-binary"));
    }

    @Test
    public void supportsDisablingRecordedStubPersistence() {
        client.get("/transient");

        List<StubMapping> mappings = snapshotRecord(
            snapshotSpec()
                .makeStubsPersistent(false)
        );

        assertThat(findMappingWithUrl(mappings, "/transient").isPersistent(), nullValue());
    }

    @Test
    public void buildsAScenarioForRepeatedIdenticalRequests() {
        targetService.stubFor(get("/stateful").willReturn(ok("One")));
        client.get("/stateful");

        targetService.stubFor(get("/stateful").willReturn(ok("Two")));
        client.get("/stateful");

        targetService.stubFor(get("/stateful").willReturn(ok("Three")));
        client.get("/stateful");

        // Scenario creation is the default
        List<StubMapping> mappings = snapshotRecord();

        assertThat(client.get("/stateful").content(), is("One"));
        assertThat(client.get("/stateful").content(), is("Two"));
        assertThat(client.get("/stateful").content(), is("Three"));

        assertThat(mappings, everyItem(isInAScenario()));
        assertThat(mappings.get(0).getRequiredScenarioState(), is(Scenario.STARTED));
        assertThat(mappings.get(1).getRequiredScenarioState(), is("scenario-stateful-2"));
        assertThat(mappings.get(2).getRequiredScenarioState(), is("scenario-stateful-3"));
    }

    @Test
    public void appliesTransformerWithParameters() {
        client.get("/transform-this");

        List<StubMapping> mappings = snapshotRecord(
            snapshotSpec()
                .transformers("test-transformer")
                .transformerParameters(Parameters.from(
                    ImmutableMap.<String, Object>of(
                        "headerKey", "X-Key",
                        "headerValue", "My value"
                    )
                )));

        assertThat(mappings.get(0).getResponse().getHeaders().getHeader("X-Key").firstValue(), is("My value"));
    }

    @Test
    public void staticClientIsSupportedWithDefaultSpec() {
        client.get("/get-this");

        snapshotRecord();

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this")));
    }

    @Test
    public void staticClientIsSupportedWithSpecProvided() {
        client.get("/get-this");
        client.get("/but-not-this");

        snapshotRecord(snapshotSpec().onlyRequestsMatching(getRequestedFor(urlEqualTo("/get-this"))));

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this")));
        assertThat(serverMappings, not(hasItem(stubMappingWithUrl("/but-not-this"))));
    }

    @Test
    public void instanceClientIsSupportedWithDefaultSpec() {
        client.get("/get-this-too");
        adminClient.takeSnapshotRecording();

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this-too")));
    }

    @Test
    public void instanceClientIsSupportedWithSpecProvided() {
        client.get("/get-this");
        client.get("/but-not-this");

        adminClient.takeSnapshotRecording(snapshotSpec().onlyRequestsMatching(getRequestedFor(urlEqualTo("/get-this"))));

        List<StubMapping> serverMappings = proxyingService.getStubMappings();
        assertThat(serverMappings, hasItem(stubMappingWithUrl("/get-this")));
        assertThat(serverMappings, not(hasItem(stubMappingWithUrl("/but-not-this"))));
    }

    private static ServeEvent findServeEventWithUrl(List<ServeEvent> serveEvents, final String url) {
        return find(serveEvents, new Predicate<ServeEvent>() {
            @Override
            public boolean apply(ServeEvent input) {
                return url.equals(input.getRequest().getUrl());
            }
        });
    }

    private static StubMapping findMappingWithUrl(List<StubMapping> stubMappings, final String url) {
        return find(stubMappings, withUrl(url));
    }

    private static List<StubMapping> findMappingsWithUrl(List<StubMapping> stubMappings, final String url) {
        return ImmutableList.copyOf(filter(stubMappings, withUrl(url)));
    }

    private static Predicate<StubMapping> withUrl(final String url) {
        return new Predicate<StubMapping>() {
            @Override
            public boolean apply(StubMapping input) {
                return url.equals(input.getRequest().getUrl());
            }
        };
    }

    private static TypeSafeDiagnosingMatcher<StubMapping> stubMappingWithUrl(final String url) {
        return stubMappingWithUrl(urlEqualTo(url));
    }

    private static TypeSafeDiagnosingMatcher<StubMapping> stubMappingWithUrl(final UrlPattern urlPattern) {
        return new TypeSafeDiagnosingMatcher<StubMapping>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("a stub mapping with a request URL matching " + urlPattern);
            }

            @Override
            protected boolean matchesSafely(StubMapping item, Description mismatchDescription) {
                return urlPattern.match(item.getRequest().getUrl()).isExactMatch();
            }
        };
    }

    private TypeSafeDiagnosingMatcher<StubMapping> isInAScenario() {
        return new TypeSafeDiagnosingMatcher<StubMapping>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("a stub mapping with a scenario name");
            }

            @Override
            protected boolean matchesSafely(StubMapping item, Description mismatchDescription) {
                return item.getScenarioName() != null;
            }
        };
    }

    public static class TestParameterisedTransformer extends StubMappingTransformer {

        @Override
        public StubMapping transform(StubMapping stubMapping, FileSource files, Parameters parameters) {
            ResponseDefinition newResponse = ResponseDefinitionBuilder.like(stubMapping.getResponse())
                .but()
                .withHeader(parameters.getString("headerKey"), parameters.getString("headerValue"))
                .build();
            stubMapping.setResponse(newResponse);
            return stubMapping;
        }

        @Override
        public boolean applyGlobally() {
            return false;
        }

        @Override
        public String getName() {
            return "test-transformer";
        }
    }
}
