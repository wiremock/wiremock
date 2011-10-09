package com.tomakehurst.wiremock;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RequestPatternTest {
	
	@Test
	public void matchesOnExactMethodAndUri() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		Request request = new Request(RequestMethod.POST, "/some/resource/path");
		assertTrue(uriPattern.isMatchedBy(request));
	}
	
	@Test
	public void shouldNotMatchWhenMethodIsCorrectButUriIsWrong() {
		RequestPattern uriPattern = new RequestPattern(RequestMethod.POST, "/some/resource/path");
		
		Request request = new Request(RequestMethod.POST, "/wrong/path");
		assertFalse(uriPattern.isMatchedBy(request));
	}
}
