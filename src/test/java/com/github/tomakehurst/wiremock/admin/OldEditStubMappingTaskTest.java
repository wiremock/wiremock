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
import com.github.tomakehurst.wiremock.admin.tasks.OldEditStubMappingTask;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;

import static com.github.tomakehurst.wiremock.stubbing.StubMapping.buildJsonStringFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OldEditStubMappingTaskTest {

	private static final StubMapping MOCK_MAPPING = new StubMapping(null, new ResponseDefinition());

	private Admin mockAdmin = mock(Admin.class);
	private Request mockRequest = mock(Request.class);

	private OldEditStubMappingTask editStubMappingTask = new OldEditStubMappingTask();

	@Test
	public void delegatesSavingMappingsToAdmin() {
		when(mockRequest.getBodyAsString()).thenReturn(buildJsonStringFor(MOCK_MAPPING));

		editStubMappingTask.execute(mockAdmin, mockRequest, PathParams.empty());

		verify(mockAdmin).editStubMapping(any(StubMapping.class));
	}

	@Test
	public void returnsNoContentResponse() {
		when(mockRequest.getBodyAsString()).thenReturn(buildJsonStringFor(MOCK_MAPPING));

		ResponseDefinition response = editStubMappingTask.execute(mockAdmin, mockRequest, PathParams.empty());

		assertThat(response.getStatus(), is(HttpURLConnection.HTTP_NO_CONTENT));
		verify(mockAdmin).editStubMapping(any(StubMapping.class));
	}
}