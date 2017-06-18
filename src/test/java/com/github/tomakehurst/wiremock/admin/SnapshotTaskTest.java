package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.admin.model.PaginatedResult;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.tasks.SnapshotTask;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.toomuchcoding.jsonassert.JsonAssertion;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static java.net.HttpURLConnection.HTTP_OK;
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
        withOptions(wireMockConfig());
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
        withOptions(wireMockConfig());
        setServeEvents(serveEvent(mockRequest(), response(), true));
        JsonAssertion.assertThat(executeWithoutPersist())
            .hasSize(1)
            .arrayField()
            .matches("[a-z0-9\\-]{36}");
    }

    @Test
    public void returnsEmptyArrayWithNoServeEvents() {
        withOptions(wireMockConfig());
        setServeEvents();
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsEmptyArrayWithUnproxiedServeEvent() {
        withOptions(wireMockConfig());
        setServeEvents(serveEvent(mockRequest(), response(), false));
        assertEquals("[ ]", executeWithoutPersist());
    }

    @Test
    public void returnsSingleStubMappingForRepeatedRequests() {
        withOptions(wireMockConfig());
        setServeEvents(
            serveEvent(mockRequest().url("/foo"), response(), true),
            serveEvent(mockRequest().url("/foo"), response(), true)
        );
        JsonAssertion.assertThat(executeWithoutPersist())
            .hasSize(1)
            .arrayField()
            .matches("[a-z0-9\\-]{36}");
    }

    @Test
    public void returnsTwoMappingsForTwoServeEvents() {
        withOptions(wireMockConfig());
        setServeEvents(
            serveEvent(mockRequest(), response(), true),
            serveEvent(mockRequest().url("/foo"), response(), true)
        );
        JsonAssertion.assertThat(executeWithoutPersist())
            .hasSize(2)
            .arrayField();
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

    private void withOptions(final WireMockConfiguration options) {
        context.checking(new Expectations() {{
            allowing(mockAdmin).getOptions(); will(returnValue(options));
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
