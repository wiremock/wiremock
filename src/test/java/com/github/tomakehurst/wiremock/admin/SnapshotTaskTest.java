package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.model.SingleStubMappingResult;
import com.github.tomakehurst.wiremock.admin.tasks.SnapshotTask;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_OK;
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
               true)
        );

        final StubMapping expectedStub = new StubMapping(
            new RequestPatternBuilder(RequestMethod.GET, urlEqualTo("/foo")).build(),
            new ResponseDefinitionBuilder().withBody("body").build()
        );
        expectedStub.setPersistent(true);
        expectedStub.setId(UUID.fromString("79caf251-ad1f-3d1b-b7c7-a0dfab33d19d"));

        context.checking(new Expectations() {{
            exactly(2).of(mockAdmin).addStubMapping(with(expectedStub));
        }});

        setReturnForGetStubMapping(null);

        // Check when explicitly set
        assertThat(
            execute("{ \"persist\": true}"),
            equalToJson("[\"79caf251-ad1f-3d1b-b7c7-a0dfab33d19d\"]")
        );

        // Check with default value of true
        assertThat(
            execute("{}"),
            equalToJson("[\"79caf251-ad1f-3d1b-b7c7-a0dfab33d19d\"]")
        );
    }

    @Test
    public void shouldNotPersistWhenSetToFalse() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setReturnForGetStubMapping(null);
        assertThat(
            executeWithoutPersist(),
            equalToJson("[\"82df0a3e-c3a2-30c1-bd97-098668b3e5f4\"]")
        );
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
        setReturnForGetStubMapping(StubMapping.NOT_CONFIGURED);
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsOneMappingWithOneServeEvent() {
        setServeEvents(serveEvent(mockRequest(), response(), true));
        setReturnForGetStubMapping(null);
        // the UUID shouldn't change, as it's based on the hash of the request and response
        assertThat(executeWithoutPersist(), equalToJson("[\"82df0a3e-c3a2-30c1-bd97-098668b3e5f4\"]"));
    }

    @Test
    public void returnsTwoMappingsForTwoServeEvents() {
        setServeEvents(
            serveEvent(mockRequest(), response(), true),
            serveEvent(mockRequest().url("/foo"), response(), true)
        );
        setReturnForGetStubMapping(null);
        assertThat(
            executeWithoutPersist(),
            equalToJson("[\"82df0a3e-c3a2-30c1-bd97-098668b3e5f4\", \"4901a9f4-43ba-31f9-9e16-4a8eeba4ef6a\"]")
        );
    }

    private static final String FILTERED_SNAPSHOT_REQUEST =
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

    private static final String FILTERED_SNAPSHOT_RESPONSE =
        "[                                                           \n" +
        "    {                                                       \n" +
        "        \"id\" : \"e88ab645-69d5-34d1-8e4a-382ad56be0e4\",  \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar\",                         \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        },                                                  \n" +
        "        \"uuid\" : \"e88ab645-69d5-34d1-8e4a-382ad56be0e4\" \n" +
        "    },                                                      \n" +
        "    {                                                       \n" +
        "        \"id\" : \"8ce282ae-cdbb-3818-8d06-9dde11913217\",  \n" +
        "        \"request\" : {                                     \n" +
        "            \"url\" : \"/foo/bar/baz\",                     \n" +
        "            \"method\" : \"ANY\"                            \n" +
        "        },                                                  \n" +
        "        \"response\" : {                                    \n" +
        "            \"status\" : 200                                \n" +
        "        },                                                  \n" +
        "        \"uuid\" : \"8ce282ae-cdbb-3818-8d06-9dde11913217\" \n" +
        "    }                                                       \n" +
        " ]                                                            ";

    @Test
    public void returnsFilteredRequestsWithFullOutputFormat() {
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
        setReturnForGetStubMapping(null);
        assertThat(execute(FILTERED_SNAPSHOT_REQUEST), equalToJson(FILTERED_SNAPSHOT_RESPONSE));
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
        "        \"id\" : \"5c4d527c-bcc3-3c89-bb41-fc697a90ee9b\",  \n" +
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
        "        },                                                  \n" +
        "        \"uuid\" : \"5c4d527c-bcc3-3c89-bb41-fc697a90ee9b\" \n" +
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
        setReturnForGetStubMapping(null);
        assertThat(
            execute(CAPTURE_HEADERS_SNAPSHOT_REQUEST),
            equalToJson(CAPTURE_HEADERS_SNAPSHOT_RESPONSE)
        );
    }

    private static final String SORTED_SNAPSHOT_REQUEST =
        "{                                  \n" +
        "    \"outputFormat\": \"full\",    \n" +
        "    \"sortFields\": [ \"url\" ],   \n" +
        "    \"persist\": \"false\",        \n" +
        "    \"captureHeaders\": {          \n" +
        "        \"X-Foo\": {               \n" +
        "            \"matches\": \".ar\"   \n" +
        "        }                          \n" +
        "    }                              \n" +
        "}                                    ";

    private static final String SORTED_SNAPSHOT_RESPONSE =
        "[                                                                \n" +
        "    {                                                            \n" +
        "        \"id\" : \"0237fc26-8cd6-3985-af0d-83431fa128ad\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/a\",                                    \n" +
        "            \"method\" : \"POST\"                                \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"0237fc26-8cd6-3985-af0d-83431fa128ad\"      \n" +
        "    },                                                           \n" +
        "    {                                                            \n" +
        "        \"id\" : \"dd876a1d-58b9-3e42-9e24-5f283ddc6175\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/b\",                                    \n" +
        "            \"method\" : \"GET\",                                \n" +
        "            \"headers\" : {                                      \n" +
        "                \"X-Foo\" : {                                    \n" +
        "                    \"equalTo\" : \"bar\"                        \n" +
        "                }                                                \n" +
        "            }                                                    \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"dd876a1d-58b9-3e42-9e24-5f283ddc6175\"      \n" +
        "    },                                                           \n" +
        "    {                                                            \n" +
        "        \"id\" : \"373b99eb-49dc-360f-b46b-abf9f224a904\",       \n" +
        "        \"request\" : {                                          \n" +
        "            \"url\" : \"/z\",                                    \n" +
        "            \"method\" : \"GET\"                                 \n" +
        "        },                                                       \n" +
        "        \"response\" : {                                         \n" +
        "            \"status\" : 200                                     \n" +
        "        },                                                       \n" +
        "        \"uuid\" : \"373b99eb-49dc-360f-b46b-abf9f224a904\"      \n" +
        "    }                                                            \n" +
        "]                                                                  ";

    @Test
    public void returnsSortedStubMappings() {
        setServeEvents(
            serveEvent(
                mockRequest().method(GET).url("/b").header("X-Foo", "bar"),
                response(),
                true
            ),
            serveEvent(
                mockRequest().method(POST).url("/a").header("X-Foo", "no match"),
                response(),
                true
            ),
            serveEvent(
                mockRequest().method(GET).url("/z"),
                response(),
                true
            )
        );
        setReturnForGetStubMapping(null);
        assertThat(execute(SORTED_SNAPSHOT_REQUEST), equalToJson(SORTED_SNAPSHOT_RESPONSE));
    }

    private String executeWithoutPersist() {
        return execute("{ \"persist\": false }");
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

    private void setReturnForGetStubMapping(final StubMapping stubMapping) {
        context.checking(new Expectations() {{
            allowing(mockAdmin).getStubMapping(with(any(UUID.class)));
            will(returnValue(new SingleStubMappingResult(stubMapping)));
        }});
    }

    private static ServeEvent serveEvent(Request request, Response.Builder responseBuilder, boolean wasProxied) {
        ResponseDefinitionBuilder responseDefinition = responseDefinition();
        if (wasProxied) {
            responseDefinition.proxiedFrom("/foo");
        }
        return new ServeEvent(
            UUID.randomUUID(),
            LoggedRequest.createFrom(request),
            null,
            responseDefinition.build(),
            LoggedResponse.from(responseBuilder.build()),
            false
        );
    }
}
