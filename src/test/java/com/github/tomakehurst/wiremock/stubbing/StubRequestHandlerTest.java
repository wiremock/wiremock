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

import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.http.*;

import org.apache.velocity.VelocityContext;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JMock.class)
public class StubRequestHandlerTest {

	private Mockery context;
	private StubServer stubServer;
	private ResponseRenderer responseRenderer;
	
	private StubRequestHandler requestHandler;
	
	private VelocityContext velocityContext;
	
	@Before
	public void init() {
		context = new Mockery();
        stubServer = context.mock(StubServer.class);
		responseRenderer = context.mock(ResponseRenderer.class);
		requestHandler = new StubRequestHandler(stubServer, responseRenderer,
		velocityContext = new VelocityContext());
	}
	
	@Test
	public void returnsResponseIndicatedByMappings() {
		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(with(any(Request.class))); will(returnValue(new ResponseDefinition(200, "Body content")));

            Response response = response().status(200).body("Body content").build();
			allowing(responseRenderer).render(with(any(ResponseDefinition.class))); will(returnValue(response));
		}});
		
		Request request = aRequest(context)
			.withUrl("/the/required/resource")
			.withMethod(GET)
			.build();
		Response response = requestHandler.handle(request);
		
		assertThat(response.getStatus(), is(200));
		assertThat(response.getBodyAsString(), is("Body content"));
	}
	
	@Test
	public void shouldNotifyListenersOnRequest() {
		final Request request = aRequest(context).build();
		final RequestListener listener = context.mock(RequestListener.class);
		requestHandler.addRequestListener(listener);
		
		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(request); will(returnValue(ResponseDefinition.notConfigured()));
			one(listener).requestReceived(with(equal(request)), with(any(Response.class)));
			allowing(responseRenderer).render(with(any(ResponseDefinition.class)));
		}});
		
		requestHandler.handle(request);
	}
	
	@Test
	public void velocityContextContainsRequest() {
		Request request = aRequest(context)
				.withUrl("/the/required/resource")
				.withMethod(GET)
				.build();		
		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(with(any(Request.class))); will(returnValue(new ResponseDefinition(200, "Body content")));

            Response response = response().status(200).body("Body content").build();
			allowing(responseRenderer).render(with(any(ResponseDefinition.class))); will(returnValue(response));
		}});		
		final ResponseDefinition response = requestHandler.handleRequest(request);
		final String requestMethod = velocityContext.get("requestMethod").toString();
		final String requestAbsoluteURL = velocityContext.get("requestAbsoluteUrl").toString();
		final String requestUrl = velocityContext.get("requestUrl").toString();
		assertThat(response,notNullValue());
		assertThat(requestAbsoluteURL,is("http://localhost:8080/the/required/resource"));
		assertThat(requestUrl,is("/the/required/resource"));
		assertThat(requestMethod,is("GET"));
	}
}
