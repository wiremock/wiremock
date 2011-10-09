package com.tomakehurst.wiremock;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RequestPatternTest {
	
	@Test
	public void shouldMatchUriForExactPattern() {
		RequestPattern uriPattern = new RequestPattern("/some/resource/path");
		assertTrue(uriPattern.isMatchedBy("/some/resource/path"));
	}
	
	@Test
	public void shouldNotMatchWhenPatternIsExactAndUriIsNotEqual() {
		RequestPattern uriPattern = new RequestPattern("/some/resource/path");
		assertFalse(uriPattern.isMatchedBy("/wrong/path"));
	}
}
