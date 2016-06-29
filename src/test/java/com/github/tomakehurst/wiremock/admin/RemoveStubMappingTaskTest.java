package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;

import static com.github.tomakehurst.wiremock.stubbing.StubMapping.buildJsonStringFor;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RemoveStubMappingTaskTest {

    private static final StubMapping MOCK_MAPPING = new StubMapping(null, new ResponseDefinition());

    private Mockery context;
    private Admin mockAdmin;

    private Request mockRequest;
    private RemoveStubMappingTask removeStubMappingTask;


    @Before
    public void setUp() {

        context = new Mockery();
        mockAdmin = context.mock(Admin.class);
        mockRequest = context.mock(Request.class);

        removeStubMappingTask = new RemoveStubMappingTask();

    }
    @Test
    public void delegatesSavingMappingsToAdmin() {
        context.checking(new Expectations() {{
            oneOf(mockRequest).getBodyAsString();
            will(returnValue(buildJsonStringFor(MOCK_MAPPING)));
            oneOf(mockAdmin).removeStubMapping(with(any(StubMapping.class)));
        }});

        removeStubMappingTask.execute(mockAdmin, mockRequest);
    }
    @Test
    public void returnsOKResponse() {

        context.checking(new Expectations() {{
            oneOf(mockRequest).getBodyAsString();
            will(returnValue(buildJsonStringFor(MOCK_MAPPING)));
            oneOf(mockAdmin).removeStubMapping(with(any(StubMapping.class)));
        }});

        ResponseDefinition response = removeStubMappingTask.execute(mockAdmin, mockRequest);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    }
}