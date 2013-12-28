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
package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.testsupport.RequestResponseMappingBuilder.aMapping;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class AdminRequestHandlerTest {
	
	private Mockery context;
	private Admin admin;

	private AdminRequestHandler handler;
	
	
	@Before
	public void init() {
		context = new Mockery();
        admin = context.mock(Admin.class);

		handler = new AdminRequestHandler(admin, new BasicResponseRenderer());
	}
	
	@Test
	public void shouldAddNewMappingWhenCalledWithValidRequest() {
		Request request = aRequest(context)
			.withUrl("/mappings/new")
			.withMethod(POST)
			.withBody(BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER)
			.build();
		
		context.checking(new Expectations() {{
			one(admin).addStubMapping(aMapping()
                    .withMethod(RequestMethod.GET)
                    .withUrl("/a/registered/resource")
                    .withResponseStatus(401)
                    .withResponseBody("Not allowed!")
                    .withHeader("Content-Type", "text/plain")
                    .build());
		}});
		
		Response response = handler.handle(request);
		
		assertThat(response.getStatus(), is(HTTP_CREATED));
	}

    @Test
    public void shouldSaveMappingsWhenSaveCalled() {
        Request request = aRequest(context)
                .withUrl("/mappings/save")
                .withMethod(POST)
                .build();

        context.checking(new Expectations() {{
            one(admin).saveMappings();
        }});

        Response response = handler.handle(request);

        assertThat(response.getStatus(), is(HTTP_OK));
    }
	
	@Test
	public void shouldClearMappingsJournalAndRequestDelayWhenResetCalled() {
		Request request = aRequest(context)
			.withUrl("/reset")
			.withMethod(POST)
			.build();
		
		context.checking(new Expectations() {{
			one(admin).resetMappings();
		}});
		
		Response response = handler.handle(request);
		
		assertThat(response.getStatus(), is(HTTP_OK));
	}
	
	private static final String REQUEST_PATTERN_SAMPLE = 
		"{												\n" +
		"	\"method\": \"DELETE\",						\n" +
		"	\"url\": \"/some/resource\"					\n" +
		"}												";
	
	@Test
	public void shouldReturnCountOfMatchingRequests() {
		context.checking(new Expectations() {{
			RequestPattern requestPattern = new RequestPattern(DELETE, "/some/resource");
			allowing(admin).countRequestsMatching(requestPattern); will(returnValue(VerificationResult.withCount(5)));
		}});
		
		Response response = handler.handle(aRequest(context)
				.withUrl("/requests/count")
				.withMethod(POST)
				.withBody(REQUEST_PATTERN_SAMPLE)
				.build());
		
		assertThat(response.getStatus(), is(HTTP_OK));
		assertThat(response.getBodyAsString(), equalToJson("{ \"count\": 5, \"requestJournalDisabled\" : false}"));
	}
	
	private static final String GLOBAL_SETTINGS_JSON =
		"{												\n" +
		"	\"fixedDelay\": 2000						\n" +
		"}												";
	
	@Test
	public void shouldUpdateGlobalSettings() {
        context.checking(new Expectations() {{
            GlobalSettings expectedSettings = new GlobalSettings();
            expectedSettings.setFixedDelay(2000);
            allowing(admin).updateGlobalSettings(expectedSettings);
        }});

		handler.handle(aRequest(context)
				.withUrl("/settings")
				.withMethod(POST)
				.withBody(GLOBAL_SETTINGS_JSON)
				.build());
		
	}
	
}
