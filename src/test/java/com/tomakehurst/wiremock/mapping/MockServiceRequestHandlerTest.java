package com.tomakehurst.wiremock.mapping;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tomakehurst.wiremock.http.RequestMethod;

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
		
		Request request = new ImmutableRequest(RequestMethod.GET, "/the/required/resource");
		Response response = requestHandler.handle(request);
		
		assertThat(response.getStatus(), is(200));
		assertThat(response.getBody(), is("Body content"));
	}
	
	
}
