package com.tomakehurst.wiremock.mapping;

import static com.google.common.collect.Maps.newHashMap;
import static com.tomakehurst.wiremock.http.RequestMethod.ANY;
import static com.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.tomakehurst.wiremock.mapping.HeaderPattern.equalTo;
import static com.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tomakehurst.wiremock.common.LocalNotifier;
import com.tomakehurst.wiremock.common.Notifier;
import com.tomakehurst.wiremock.http.RequestMethod;

@RunWith(JMock.class)
public class RequestPatternTest {
	
	private Mockery context;
	private Map<String, HeaderPattern> headers;
	private Notifier notifier;
	
	@Before
	public void init() {
		context = new Mockery();
		headers = newHashMap();
		notifier = context.mock(Notifier.class);
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
		headers.put("Accept", equalTo("text/plain"));
		headers.put("Content-Type", equalTo("application/json"));
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
		headers.put("Accept", equalTo("text/plain"));
		headers.put("Content-Type", equalTo("application/json"));
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
		headers.put("Accept", equalTo("text/plain"));
		headers.put("Content-Type", equalTo("application/json"));
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
	
	private static final String XML_SAMPLE =
		"<document>							\n" +
		"	<important>Value</important>	\n" +
		"</document>		  				";
	
	@Test
	public void shouldMatchOnBodyPattern() {
		RequestPattern requestPattern = new RequestPattern(GET, "/with/body");
		requestPattern.setBodyPattern(".*<important>Value</important>.*");
		
		Request request = aRequest(context)
			.withUrl("/with/body")
			.withMethod(GET)
			.withBody(XML_SAMPLE)
			.build();
		
		assertTrue(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenBodyDoesNotMatchPattern() {
		RequestPattern requestPattern = new RequestPattern(GET, "/with/body");
		requestPattern.setBodyPattern(".*<important>Value</important>.*");
		
		Request request = aRequest(context)
			.withUrl("/with/body")
			.withMethod(GET)
			.withBody("<important>Wrong value</important>")
			.build();
		
		assertFalse(requestPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldMatchAnyMethod() {
		RequestPattern requestPattern = new RequestPattern(ANY, "/any/method");
		
		for (RequestMethod method: RequestMethod.values()) {
			context = new Mockery();
			Request request = aRequest(context)
				.withUrl("/any/method")
				.withMethod(method)
				.build();
			assertTrue("Method in request pattern is ANY so any method should match", requestPattern.isMatchedBy(request));
		}
	}
	
	@Test
	public void shouldLogMessageIndicatingFailedMethodMatch() {
		context.checking(new Expectations() {{
			one(notifier).info("URL /for/logging is match, but method GET is not");
		}});
		
		LocalNotifier.set(notifier);
		RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");
		
		Request request = aRequest(context)
			.withUrl("/for/logging")
			.withMethod(GET)
			.build();
		
		requestPattern.isMatchedBy(request);
	}
	
	@Test
	public void shouldLogMessageIndicatingFailedHeaderMatch() {
		context.checking(new Expectations() {{
			one(notifier).info("URL /for/logging is match, but header Content-Type is not");
		}});
		
		LocalNotifier.set(notifier);
		RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");
		HeaderPattern headerPattern = new HeaderPattern();
		headerPattern.setEqualTo("text/xml");
		requestPattern.addHeader("Content-Type", headerPattern);
		
		Request request = aRequest(context)
			.withUrl("/for/logging")
			.withMethod(POST)
			.withHeader("Content-Type", "text/plain")
			.build();
		
		requestPattern.isMatchedBy(request);
	}
	
	@Test
	public void shouldLogMessageIndicatingFailedBodyMatch() {
		context.checking(new Expectations() {{
			one(notifier).info("URL /for/logging is match, but body is not: Actual Content");
		}});
		
		LocalNotifier.set(notifier);
		RequestPattern requestPattern = new RequestPattern(POST, "/for/logging");
		requestPattern.setBodyPattern("Expected content");
		
		Request request = aRequest(context)
			.withUrl("/for/logging")
			.withMethod(POST)
			.withBody("Actual Content")
			.build();
		
		requestPattern.isMatchedBy(request);
	}
	
}
