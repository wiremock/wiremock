package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static com.github.tomakehurst.wiremock.verification.LoggedRequest.createFrom;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class LoggedRequestTest {
	
	private Mockery context;
	
	@Before
	public void init() {
		context = new Mockery();
	}

	@Test
	public void headerMatchingIsCaseInsensitive() {
		
		
		LoggedRequest loggedRequest = createFrom(aRequest(context)
				.withUrl("/for/logging")
				.withMethod(POST)
				.withBody("Actual Content")
				.withHeader("Content-Type", "text/plain")
				.withHeader("ACCEPT", "application/json")
				.build());
		
		assertTrue(loggedRequest.containsHeader("content-type"));
		assertNotNull(loggedRequest.getHeader("content-type"));
		assertTrue(loggedRequest.containsHeader("CONTENT-TYPE"));
		assertNotNull(loggedRequest.getHeader("CONTENT-TYPE"));
		assertTrue(loggedRequest.containsHeader("Accept"));
		assertNotNull(loggedRequest.getHeader("Accept"));
	}
}
