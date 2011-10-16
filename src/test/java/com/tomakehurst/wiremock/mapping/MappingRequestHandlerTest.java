package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.testsupport.MappingJsonSamples.BASIC_MAPPING_REQUEST;
import static com.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.tomakehurst.wiremock.testsupport.RequestResponseMappingBuilder.aMapping;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
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
	
	private MappingRequestHandler handler;
	
	
	@Before
	public void init() {
		context = new Mockery();
		mappings = context.mock(Mappings.class);
		
		handler = new MappingRequestHandler(mappings);
	}
	
	@Test
	public void shouldAddNewMappingWhenCalledWithValidRequest() {
		Request request = aRequest(context)
			.withUri("/mappings/new")
			.withMethod(POST)
			.withBody(BASIC_MAPPING_REQUEST)
			.build();
		
		context.checking(new Expectations() {{
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
	
	@Test
	public void shouldClearMappingsWhenResetCalled() {
		Request request = aRequest(context)
			.withUri("/mappings/reset")
			.withMethod(POST)
			.build();
		
		context.checking(new Expectations() {{
			one(mappings).reset();
		}});
		
		Response response = handler.handle(request);
		
		assertThat(response.getStatus(), is(HTTP_OK));
	}
}
