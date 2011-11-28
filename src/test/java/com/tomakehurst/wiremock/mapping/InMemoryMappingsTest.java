package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;
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
	public void returnsLowPriorityMappingsAfterNormal() {
		RequestResponseMapping lowPriorityMapping = new RequestResponseMapping(
				new RequestPattern(GET, "/whatever"),
				new ResponseDefinition(200, "Low"));
		lowPriorityMapping.setPriority(Priority.LOW);
		RequestResponseMapping normalPriorityMapping = new RequestResponseMapping(
				new RequestPattern(GET, "/whatever"),
				new ResponseDefinition(200, "Normal"));
		normalPriorityMapping.setPriority(Priority.NORMAL);
		mappings.addMapping(normalPriorityMapping);
		mappings.addMapping(lowPriorityMapping);
		
		ResponseDefinition response = mappings.getFor(aRequest(context).withMethod(GET).withUrl("/whatever").build());
		
		assertThat(response.getBody(), is("Normal"));
	}
	
}
