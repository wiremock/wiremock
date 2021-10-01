/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.tasks.OldRemoveStubMappingTask;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static com.github.tomakehurst.wiremock.stubbing.StubMapping.buildJsonStringFor;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoveStubMappingTaskTest {

    private static final StubMapping MOCK_MAPPING = new StubMapping(null, new ResponseDefinition());

    private Mockery context;
    private Admin mockAdmin;

    private Request mockRequest;
    private OldRemoveStubMappingTask removeStubMappingTask;


    @BeforeEach
    public void setUp() {

        context = new Mockery();
        mockAdmin = context.mock(Admin.class);
        mockRequest = context.mock(Request.class);

        removeStubMappingTask = new OldRemoveStubMappingTask();

    }
    @Test
    public void delegatesSavingMappingsToAdmin() {
        context.checking(new Expectations() {{
            oneOf(mockRequest).getBodyAsString();
            will(returnValue(buildJsonStringFor(MOCK_MAPPING)));
            oneOf(mockAdmin).removeStubMapping(with(any(StubMapping.class)));
        }});

        removeStubMappingTask.execute(mockAdmin, mockRequest, PathParams.empty());
    }
    @Test
    public void returnsOKResponse() {

        context.checking(new Expectations() {{
            oneOf(mockRequest).getBodyAsString();
            will(returnValue(buildJsonStringFor(MOCK_MAPPING)));
            oneOf(mockAdmin).removeStubMapping(with(any(StubMapping.class)));
        }});

        ResponseDefinition response = removeStubMappingTask.execute(mockAdmin, mockRequest, PathParams.empty());

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
    }
}