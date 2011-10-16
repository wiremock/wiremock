package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_MAPPING_REQUEST;
import static com.tomakehurst.wiremock.testsupport.RequestResponseMappingBuilder.aMapping;
import static java.net.HttpURLConnection.HTTP_CREATED;
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
public class MappingRequestHandlerTest {
	
	private Mockery context;
	private Mappings mappings;
	private Request request;
	
	private MappingRequestHandler handler;
	
	
	@Before
	public void init() {
		context = new Mockery();
		mappings = context.mock(Mappings.class);
		request = context.mock(Request.class);
		
		handler = new MappingRequestHandler(mappings);
	}
	
	@Test
	public void shouldAddNewMappingWhenCalledWithValidRequest() {
		context.checking(new Expectations() {{
			allowing(request).getBodyAsString(); will(returnValue(BASIC_MAPPING_REQUEST));
			allowing(request).getMethod(); will(returnValue(POST));
			allowing(request).getUri(); will(returnValue("/mappings/new"));
			one(mappings).addMapping(aMapping()
					.withMethod(RequestMethod.GET)
					.withUriExpression("/a/registered/resource")
					.withResponseStatus(401)
					.withResponseBody("Not allowed!")
					.withHeader("Content-Type", "text/plain")
					.build());
		}});
		
		Response response = handler.handle(request);
		
		assertThat(response.getStatus(), is(HTTP_CREATED));
	}
}
