package com.tomakehurst.wiremock.mapping;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;

public class RequestPatternTest {
	
	@Test
	public void matchesOnExactMethodAndUri() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = new ImmutableRequest(RequestMethod.POST, "/some/resource/path");
		assertTrue(uriPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenMethodIsCorrectButUriIsWrong() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		
		Request request = new ImmutableRequest(RequestMethod.POST, "/wrong/path");
		assertFalse(uriPattern.isMatchedBy(request));
	}
}
