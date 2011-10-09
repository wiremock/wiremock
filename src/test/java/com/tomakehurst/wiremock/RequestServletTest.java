package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.RequestMethod.GET;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(JMock.class)
public class RequestServletTest {

	private static final String A_URI = "/my/resource";
	private static final String SOME_TEXT = "Some text";
	private static final int A_STATUS_CODE = 200;
	
	private Mockery context;
	private Responses responses;
	private RequestServlet requestServlet;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	
	@Before
	public void init() {
		context = new Mockery();
		responses = context.mock(Responses.class);
		RequestServlet.setResponseDefinitions(responses);
		requestServlet = new RequestServlet();
		
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void shouldReturnResponseProvidedByRouterWhenMatchFound() throws Exception {
		context.checking(new Expectations() {{
			allowing(responses).getFor(new Request(GET, A_URI));
			will(returnValue(new Response(A_STATUS_CODE, SOME_TEXT)));
		}});
		
		request.setRequestURI(A_URI);
		request.setMethod("GET");
		requestServlet.service(request, response);
		
		assertThat(response.getStatus(), is(A_STATUS_CODE));
		assertThat(response.getContentAsString(), is(SOME_TEXT));
	}
}
