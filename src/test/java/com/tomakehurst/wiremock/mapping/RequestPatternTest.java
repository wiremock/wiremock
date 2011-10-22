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
	public void matchesOnExactMethodAndUrl() {
		RequestPattern requestPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = aRequest(context)
			.withUrl("/some/resource/path")
			.withMethod(POST)
			.build();
		assertTrue(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenMethodIsCorrectButUrlIsWrong() {
		RequestPattern requestPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = aRequest(context)
			.withUrl("/wrong/path")
			.withMethod(POST)
			.build();
		assertFalse(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldMatchWhenSpecifiedHeadersArePresent() {
		headers.put("Accept", "text/plain");
		headers.put("Content-Type", "application/json");
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET, "/header/dependent/resource", headers);
		
		Request request = aRequest(context)
			.withUrl("/header/dependent/resource")
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
			.withUrl("/header/dependent/resource")
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
			.withUrl("/header/dependent/resource")
			.withMethod(GET)
			.withHeader("Accept", "text/plain")
			.withHeader("Content-Type", "text/xml")
			.build();
		
		assertFalse(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldMatchUrlPatternWithRegexes() {
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET);
		requestPattern.setUrlPattern("/resource/(.*?)/subresource");
		
		Request request = aRequest(context)
			.withUrl("/resource/1234-abcd/subresource")
			.withMethod(GET)
			.build();
		
		assertTrue(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchUrlWhenUsingRegexButCandidateIsNotMatch() {
		RequestPattern requestPattern = new RequestPattern(RequestMethod.GET);
		requestPattern.setUrlPattern("/resource/([A-Z]+?)/subresource");
		
		Request request = aRequest(context)
			.withUrl("/resource/12340987/subresource")
			.withMethod(GET)
			.build();
		
		assertFalse(requestPattern.isMatchedBy(request));
	}
	
	@Test(expected=IllegalStateException.class)
	public void shouldNotPermitBothUrlAndUrlPattern() {
		RequestPattern requestPattern = new RequestPattern();
		requestPattern.setUrlPattern("/(.*?");
		requestPattern.setUrl("/some/url");
		
		requestPattern.isMatchedBy(aRequest(context).build());
	}
}
