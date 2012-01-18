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
package com.github.tomakehurst.wiremock.mapping;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.OPTIONS;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;

@RunWith(JMock.class)
public class InMemoryMappingsTest {

	private InMemoryMappings mappings;
	private Mockery context;
	private Notifier notifier;
	
	@Before
	public void init() {
		mappings = new InMemoryMappings();
		context = new Mockery();
		
		notifier = context.mock(Notifier.class);
	}
	
	@After
    public void cleanUp() {
        LocalNotifier.set(null);
    }
	
	@Test
	public void correctlyAcceptsMappingAndReturnsCorrespondingResponse() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new ResponseDefinition(204, "")));
		
		Request request = aRequest(context).withMethod(PUT).withUrl("/some/resource").build();
		ResponseDefinition response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(204));
	}
	
	@Test
	public void returnsNotFoundWhenMethodIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new ResponseDefinition(204, "")));
		
		Request request = aRequest(context).withMethod(POST).withUrl("/some/resource").build();
		ResponseDefinition response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotFoundWhenUrlIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new ResponseDefinition(204, "")));
		
		Request request = aRequest(context).withMethod(PUT).withUrl("/some/bad/resource").build();
		ResponseDefinition response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotConfiguredResponseForUnmappedRequest() {
		Request request = aRequest(context).withMethod(OPTIONS).withUrl("/not/mapped").build();
		ResponseDefinition response = mappings.getFor(request);
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
		assertThat(response.wasConfigured(), is(false));
	}
	
	@Test
	public void returnsMostRecentlyInsertedResponseIfTwoOrMoreMatch() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(GET, "/duplicated/resource"),
				new ResponseDefinition(204, "Some content")));
		
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(GET, "/duplicated/resource"),
				new ResponseDefinition(201, "Desired content")));
		
		ResponseDefinition response = mappings.getFor(aRequest(context).withMethod(GET).withUrl("/duplicated/resource").build());
		
		assertThat(response.getStatus(), is(201));
		assertThat(response.getBody(), is("Desired content"));
	}
	
	@Test
	public void notifiesWhenNoMappingFound() {
	    context.checking(new Expectations() {{
            one(notifier).info("No mapping found matching URL /match/not/found");
        }});
	    
	    LocalNotifier.set(notifier);
        
        mappings.getFor(aRequest(context).withMethod(GET).withUrl("/match/not/found").build());
	}
}
