package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class MockServiceRequestHandlerTest {

	private Mockery context;
	private Mappings mappings;
	
	private MockServiceRequestHandler requestHandler;
	
	@Before
	public void init() {
		context = new Mockery();
		mappings = context.mock(Mappings.class);
		requestHandler = new MockServiceRequestHandler(mappings);
	}
	
	@Test
	public void returnsResponseIndicatedByMappings() {
		context.checking(new Expectations() {{
			allowing(mappings).getFor(with(any(Request.class))); will(returnValue(new Response(200, "Body content")));
		}});
		
		Request request = aRequest(context)
			.withUrl("/the/required/resource")
			.withMethod(GET)
			.build();
		Response response = requestHandler.handle(request);
		
		assertThat(response.getStatus(), is(200));
		assertThat(response.getBody(), is("Body content"));
	}
	
	@Test
	public void shouldNotifyListenersOnRequest() {
		final Request request = aRequest(context).build();
		final RequestListener listener = context.mock(RequestListener.class);
		requestHandler.addRequestListener(listener);
		
		context.checking(new Expectations() {{
			allowing(mappings).getFor(request); will(returnValue(Response.notConfigured()));
			one(listener).requestReceived(with(equal(request)), with(any(Response.class)));
		}});
		
		requestHandler.handle(request);
	}
}
