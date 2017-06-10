package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.tasks.SnapshotTask;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import com.toomuchcoding.jsonassert.JsonAssertion;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_OK;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(JMock.class)
public class SnapshotTaskTest {
    private Mockery context;
    private Admin mockAdmin;

    @Before
    public void init() {
        context = new Mockery();
        mockAdmin = context.mock(Admin.class);
    }

    @Test
    public void persistsStubMappingWhenPersistSet() {
        setServeEvents(
            serveEvent(
                mockRequest().url("/foo").method(GET),
                response().body("body"),
                true
            )
        );
        context.checking(new Expectations() {{
            exactly(2).of(mockAdmin).addStubMapping(with(any(StubMapping.class)));
        }});
        setReturnForCountRequestsMatching(0);

        // Check when explicitly set
        JsonAssertion.assertThat(execute("{ \"persist\": true, \"outputFormat\": \"ids\"}"))
            .hasSize(1)
            .arrayField()
            .matches("[a-z0-9\\-]{36}");

        // Check with default value of true
        JsonAssertion.assertThat(execute("{ \"outputFormat\": \"ids\"}"))
            .hasSize(1)
            .arrayField()
            .matches("[a-z0-9\\-]{36}");
    }

    @Test
    public void shouldNotPersistWhenSetToFalse() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setReturnForCountRequestsMatching(0);
        JsonAssertion.assertThat(executeWithoutPersist())
            .hasSize(1)
            .arrayField()
            .matches("[a-z0-9\\-]{36}");
    }

    @Test
    public void returnsEmptyArrayWithNoServeEvents() {
        setServeEvents();
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsEmptyArrayWithUnproxiedServeEvent() {
        setServeEvents(serveEvent(mockRequest(), response(), false));
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsEmptyArrayForExistingStubMapping() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setReturnForCountRequestsMatching(1);
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsTwoMappingsForTwoServeEvents() {
        setServeEvents(
            serveEvent(mockRequest(), response(), true),
            serveEvent(mockRequest().url("/foo"), response(), true)
        );
        setReturnForCountRequestsMatching(0);
        JsonAssertion.assertThat(executeWithoutPersist())
            .hasSize(2)
            .arrayField();
    }

    private static final String FILTER_BY_REQUEST_PATTERN_SNAPSHOT_REQUEST =
        "{                                                 \n" +
        "    \"outputFormat\": \"full\",                   \n" +
        "    \"persist\": \"false\",                       \n" +
        "    \"filters\": {                                \n" +
        "        \"urlPattern\": \"/foo.*\",               \n" +
        "        \"headers\": {                            \n" +
        "            \"A\": { \"equalTo\": \"B\" }         \n" +
        "        }                                         \n" +
        "    }                                             \n" +
        "}                                                   ";

    private static final String FILTER_BY_REQUEST_PATTERN_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        }                                                   \n" +
        "    },                                                      \n" +
        "    {                                                       \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar/baz\",                     \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        }                                                   \n" +
        "    }                                                       \n" +
        " ]                                                            ";

    @Test
    public void returnsFilteredRequestsWithJustRequestPatternsAndFullOutputFormat() {
        setServeEvents(
            // Matches both
            serveEvent(mockRequest().url("/foo/bar").header("A","B"), response(), true),
            // Fails header match
            serveEvent(mockRequest().url("/foo"), response(), true),
            // Fails URL match
            serveEvent(mockRequest().url("/bar").header("A", "B"), response(), true),
            // Fails header match
            serveEvent(mockRequest().url("/foo/").header("A", "C"), response(), true),
            // Matches both
            serveEvent(mockRequest().url("/foo/bar/baz").header("A","B"), response(), true)
        );
        setReturnForCountRequestsMatching(0);
        assertThat(
            execute(FILTER_BY_REQUEST_PATTERN_SNAPSHOT_REQUEST),
            equalToJson(FILTER_BY_REQUEST_PATTERN_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_REQUEST =
        "{                                                     \n" +
        "    \"outputFormat\": \"full\",                       \n" +
        "    \"persist\": \"false\",                           \n" +
        "    \"filters\": {                                    \n" +
        "        \"ids\": [                                    \n" +
        "            \"00000000-0000-0000-0000-000000000001\", \n" +
        "            \"00000000-0000-0000-0000-000000000002\"  \n" +
        "        ],                                            \n" +
        "        \"urlPattern\": \"/foo.*\"                    \n" +
        "    }                                                 \n" +
        "}                                                       ";

    private static final String FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        }                                                   \n" +
        "    }                                                       \n" +
        " ]                                                            ";

    @Test
    public void returnsFilteredRequestsWithRequestPatternAndIdsWithFullOutputFormat() {
        setServeEvents(
            // Matches both
            serveEvent(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                mockRequest().url("/foo/bar"),
                response(),
                true
            ),
            // Fails URL match
            serveEvent(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                mockRequest().url("/bar"),
                response(),
                true
            ),
            // Fails ID match
            serveEvent(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                mockRequest().url("/foo/bar"),
                response(),
                true
            )
        );
        setReturnForCountRequestsMatching(0);
        assertThat(
            execute(FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_REQUEST),
            equalToJson(FILTER_BY_REQUEST_PATTERN_AND_IDS_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER)
        );
    }

    private static final String CAPTURE_HEADERS_SNAPSHOT_REQUEST =
        "{                                  \n" +
        "    \"outputFormat\": \"full\",    \n" +
        "    \"persist\": \"false\",        \n" +
        "    \"captureHeaders\": {          \n" +
        "        \"Accept\": {              \n" +
        "            \"anything\": true     \n" +
        "        },                         \n" +
        "        \"X-NoMatch\": {           \n" +
        "            \"equalTo\": \"!\"     \n" +
        "        }                          \n" +
        "    }                              \n" +
        "}                                    ";

    private static final String CAPTURE_HEADERS_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
        "            \"method\" : \"POST\",                          \n" +
        "            \"headers\": {                                  \n" +
        "                \"Accept\": {                               \n" +
        "                    \"equalTo\": \"B\"                      \n" +
        "                }                                           \n" +
        "            }                                               \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        }                                                   \n" +
        "    }                                                       \n" +
        "]                                                             ";

    @Test
    public void returnsStubMappingWithCapturedHeaders() {
        setServeEvents(
            serveEvent(
                mockRequest()
                    .url("/foo/bar")
                    .method(POST)
                    .header("Accept","B")
                    .header("X-NoMatch","should be ignored"),
                response(),
                true
            )
        );
        setReturnForCountRequestsMatching(0);

        String actual = execute(CAPTURE_HEADERS_SNAPSHOT_REQUEST);
        assertThat(actual, equalToJson(CAPTURE_HEADERS_SNAPSHOT_RESPONSE, JSONCompareMode.STRICT_ORDER));
        assertFalse(actual.contains("X-NoMatch"));
    }

    private String executeWithoutPersist() {
        return execute("{ \"persist\": false, \"outputFormat\": \"ids\" }");
    }

    private String execute(String requestBody) {
        SnapshotTask snapshotTask = new SnapshotTask();
        Request request = mockRequest().body(requestBody);
        ResponseDefinition responseDefinition = snapshotTask.execute(mockAdmin, request, PathParams.empty());

        assertEquals(HTTP_OK, responseDefinition.getStatus());
        assertEquals("application/json", responseDefinition.getHeaders().getContentTypeHeader().firstValue());

        return responseDefinition.getBody();
    }

    private void setServeEvents(ServeEvent... serveEvents) {
        final GetServeEventsResult results = new GetServeEventsResult(
            Arrays.asList(serveEvents),
            new PaginatedResult.Meta(0), // ignored parameter
            false
        );
        context.checking(new Expectations() {{
            allowing(mockAdmin).getServeEvents(); will(returnValue(results));
        }});
    }

    private void setReturnForCountRequestsMatching(final int numMatching) {
        context.checking(new Expectations() {{
            allowing(mockAdmin).countRequestsMatching(with(any(RequestPattern.class)));
            will(returnValue(new VerificationResult(numMatching, false)));
        }});
    }

    private static ServeEvent serveEvent(Request request, Response.Builder responseBuilder, boolean wasProxied) {
        return serveEvent(UUID.randomUUID(), request, responseBuilder, wasProxied);
    }

    private static ServeEvent serveEvent(UUID id, Request request, Response.Builder responseBuilder, boolean wasProxied) {
        ResponseDefinitionBuilder responseDefinition = responseDefinition();
        if (wasProxied) {
            responseDefinition.proxiedFrom("/foo");
        }
        return new ServeEvent(
            id,
            LoggedRequest.createFrom(request),
            null,
            responseDefinition.build(),
            LoggedResponse.from(responseBuilder.build()),
            false
        );
    }
}
