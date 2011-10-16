package com.tomakehurst.wiremock.mapping;

import static com.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tomakehurst.wiremock.http.HttpHeaders;
import com.tomakehurst.wiremock.http.RequestMethod;

@RunWith(JMock.class)
public class RequestPatternTest {
	
	private Mockery context;
	private HttpHeaders headers;
	
	@Before
	public void init() {
		context = new Mockery();
		headers = new HttpHeaders();
	}
	
	@Test
	public void matchesOnExactMethodAndUri() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = aRequest(context)
			.withUri("/some/resource/path")
			.withMethod(POST)
			.build();
		assertTrue(uriPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenMethodIsCorrectButUriIsWrong() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = aRequest(context)
			.withUri("/wrong/path")
			.withMethod(POST)
			.build();
		assertFalse(uriPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldMatchWhenSpecifiedHeadersArePresent() {
		headers.put("Accept", "text/plain");
		headers.put("Content-Type", "application/json");
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headers);
		
		Request request = aRequest(context)
			.withUri("/header/dependent/resource")
			.withMethod(GET)
			.withHeader("Accept", "text/plain")
			.withHeader("Content-Type", "application/json")
			.build();
		
		assertTrue(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenASpecifiedHeaderIsAbsent() {
		headers.put("Accept", "text/plain");
		headers.put("Content-Type", "application/json");
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headers);
		
		Request request = aRequest(context)
			.withUri("/header/dependent/resource")
			.withMethod(GET)
			.withHeader("Accept", "text/plain")
			.build();
		
		assertFalse(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenASpecifiedHeaderHasAnIncorrectValue() {
		headers.put("Accept", "text/plain");
		headers.put("Content-Type", "application/json");
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headers);
		
		Request request = aRequest(context)
			.withUri("/header/dependent/resource")
			.withMethod(GET)
			.withHeader("Accept", "text/plain")
			.withHeader("Content-Type", "text/xml")
			.build();
		
		assertFalse(requestPattern.isMatchedBy(request));
	}
}
