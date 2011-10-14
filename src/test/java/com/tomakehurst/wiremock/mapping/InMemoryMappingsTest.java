package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.http.RequestMethod.PUT;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.http.RequestMethod;

public class InMemoryMappingsTest {

	private InMemoryMappings mappings;
	
	@Before
	public void init() {
		mappings = new InMemoryMappings();
	}
	
	@Test
	public void correctlyAcceptsMappingAndReturnsCorrespondingResponse() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = new ImmutableRequest(PUT, "/some/resource");
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(204));
	}
	
	@Test
	public void returnsNotFoundWhenMethodIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = new ImmutableRequest(POST, "/some/resource");
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotFoundWhenUriIncorrect() {
		mappings.addMapping(new RequestResponseMapping(
				new RequestPattern(PUT, "/some/resource"),
				new Response(204, "")));
		
		Request request = new ImmutableRequest(PUT, "/some/bad/resource");
		Response response = mappings.getFor(request);
		
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void returnsNotFoundResponseForUnmappedRequest() {
		Request request = new ImmutableRequest(RequestMethod.OPTIONS, "/not/mapped");
		Response response = mappings.getFor(request);
		assertThat(response.getStatus(), is(HTTP_NOT_FOUND));
	}
	
}
