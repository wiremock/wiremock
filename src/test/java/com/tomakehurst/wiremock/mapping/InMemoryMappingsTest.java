package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.OPTIONS;
import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.http.RequestMethod.PUT;
import static com.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class InMemoryMappingsTest {

	private InMemoryMappings mappings;
	private Mockery context;
	
	@Before
	public void init() {
		mappings = new InMemoryMappings();
		context = new Mockery();
	}
	
	@Test
	public void correctlyAcceptsMappingAndReturnsCorrespondingResponse() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = aRequest(context).withMethod(PUT).withUrl("/some/resource").build();
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(204));
	}
	
	@Test
	public void returnsNotFoundWhenMethodIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = aRequest(context).withMethod(POST).withUrl("/some/resource").build();
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotFoundWhenUrlIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = aRequest(context).withMethod(PUT).withUrl("/some/bad/resource").build();
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotFoundResponseForUnmappedRequest() {
		Request request = aRequest(context).withMethod(OPTIONS).withUrl("/not/mapped").build();
		Response response = mappings.getFor(request);
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
}
