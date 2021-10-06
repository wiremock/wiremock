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

import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.core.StubServer;
import com.github.tomakehurst.wiremock.extension.PostServeAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestListener;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.http.ResponseRenderer;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.testsupport.MockHttpResponder;
import com.github.tomakehurst.wiremock.testsupport.TestNotifier;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.RequestJournal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StubRequestHandlerTest {

	private Mockery context;
	private StubServer stubServer;
	private ResponseRenderer responseRenderer;
	private MockHttpResponder httpResponder;
    private Admin admin;
	private RequestJournal requestJournal;

	private StubRequestHandler requestHandler;

	@Before
	public void init() {
		context = new Mockery();
        stubServer = Mockito.mock(StubServer.class);
		responseRenderer = Mockito.mock(ResponseRenderer.class);
		httpResponder = new MockHttpResponder();
        admin = Mockito.mock(Admin.class);
		requestJournal = Mockito.mock(RequestJournal.class);

		requestHandler = new StubRequestHandler(stubServer, responseRenderer, admin, Collections.<String, PostServeAction>emptyMap(), requestJournal, Collections.<RequestFilter>emptyList(), false);

        context.checking(new Expectations() {{
            allowing(requestJournal);
        }});
	}

	@Test
	public void returnsResponseIndicatedByMappings() {
		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(with(any(Request.class))); will(returnValue(
				ServeEvent.of(mockRequest().asLoggedRequest(), new ResponseDefinition(200, "Body content")))
            );

            Response response = response().status(200).body("Body content").build();
			when(responseRenderer.render(with(any(ServeEvent.class)))).thenReturn(response);
		}});

		Request request = aRequest()
			.withUrl("/the/required/resource")
			.withMethod(GET)
			.build();
		requestHandler.handle(request, httpResponder);
        Response response = httpResponder.response;

        assertThat(response.getStatus(), is(200));
		assertThat(response.getBodyAsString(), is("Body content"));
	}

	@Test
	public void shouldNotifyListenersOnRequest() {
		final Request request = aRequest().build();
		final RequestListener listener = Mockito.mock(RequestListener.class);
		requestHandler.addRequestListener(listener);

		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(request); will(returnValue(
                ServeEvent.of(LoggedRequest.createFrom(request), ResponseDefinition.notConfigured())));
			one(listener).requestReceived(with(equal(request)), with(any(Response.class)));
            allowing(responseRenderer).render(with(any(ServeEvent.class)));
                will(returnValue(new Response.Builder().build()));
		}});

        requestHandler.handle(request, httpResponder);
	}

	@Test
	public void shouldLogInfoOnRequest() {
		final Request request = aRequest()
				.withUrl("/")
				.withMethod(GET)
				.withClientIp("1.2.3.5")
				.build();

		context.checking(new Expectations() {{
			allowing(stubServer).serveStubFor(request);
			    will(returnValue(ServeEvent.forUnmatchedRequest(LoggedRequest.createFrom(request))));
			allowing(responseRenderer).render(with(any(ServeEvent.class)));
                will(returnValue(new Response.Builder().build()));
		}});

		TestNotifier notifier = TestNotifier.createAndSet();

        requestHandler.handle(request, httpResponder);
		notifier.revert();

		assertThat(notifier.getErrorMessages().isEmpty(), is(true));
		assertThat(notifier.getInfoMessages().size(), is(1));
		assertThat(notifier.getInfoMessages().get(0), containsString("1.2.3.5 - GET /"));
	}

}
