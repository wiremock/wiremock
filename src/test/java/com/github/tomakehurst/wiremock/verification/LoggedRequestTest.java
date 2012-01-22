/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
