package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class SaveMappingsTaskTest {
    private Mockery context;
    private Admin mockAdmin;
    private Request mockRequest;

    private SaveMappingsTask saveMappingsTask;

    @Before
    public void setUp() {
        context = new Mockery();
        mockAdmin = context.mock(Admin.class);
        mockRequest = context.mock(Request.class);

        saveMappingsTask = new SaveMappingsTask();
    }

    @Test
    public void delegatesSavingMappingsToAdmin() {
        context.checking(new Expectations() {{
            oneOf(mockAdmin).saveMappings();
        }});

        saveMappingsTask.execute(mockAdmin, mockRequest);
    }

    @Test
    public void returnsOkResponse() {
        context.checking(new Expectations() {{
            oneOf(mockAdmin).saveMappings();
        }});

        ResponseDefinition response = saveMappingsTask.execute(mockAdmin, mockRequest);

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    }
}
