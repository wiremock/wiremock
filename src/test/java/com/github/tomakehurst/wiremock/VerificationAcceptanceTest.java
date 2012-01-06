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
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;

import org.junit.Test;

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;

public class VerificationAcceptanceTest extends AcceptanceTestBase {

	@Test
	public void verifiesRequestBasedOnUrlOnly() {
		testClient.get("/this/got/requested");
		verify(getRequestedFor(urlEqualTo("/this/got/requested")));
	}
	
	@Test(expected=VerificationException.class)
	public void throwsVerificationExceptionWhenNoMatch() {
		testClient.get("/this/got/requested");
		verify(getRequestedFor(urlEqualTo("/this/did/not")));
	}
	
	@Test
	public void verifiesWithHeaders() {
		testClient.put("/update/this", withHeader("Content-Type", "application/json"), withHeader("Encoding", "UTF-8"));
		verify(putRequestedFor(urlMatching("/[a-z]+/this"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withHeader("Encoding", notMatching("LATIN-1")));
	}
	
	@Test(expected=VerificationException.class)
	public void throwsVerificationExceptionWhenHeadersDoNotMatch() {
		testClient.put("/to/modify", withHeader("Content-Type", "application/json"), withHeader("Encoding", "LATIN-1"));
		verify(putRequestedFor(urlEqualTo("/to/modify"))
				.withHeader("Content-Type", equalTo("application/json"))
				.withHeader("Encoding", notMatching("LATIN-1")));
	}
	
	
	private static final String SAMPLE_JSON =
		"{ 													\n" +
		"	\"thing\": {									\n" +
		"		\"importantKey\": \"Important value\",		\n" +
		"	}												\n" +
		"}													";
	
	@Test
	public void verifiesWithBody() {
		testClient.postWithBody("/add/this", SAMPLE_JSON, "application/json", "utf-8");
		verify(postRequestedFor(urlEqualTo("/add/this"))
				.withBodyMatching(".*\"importantKey\": \"Important value\".*"));
	}
	
	@Test
	public void verifiesWithBodyContaining() {
		testClient.postWithBody("/body/contains", SAMPLE_JSON, "application/json", "utf-8");
		verify(postRequestedFor(urlEqualTo("/body/contains"))
				.withBodyContaining("Important value"));
	}
	
	@Test(expected=VerificationException.class)
	public void resetErasesCounters() {
		testClient.get("/count/this");
		testClient.get("/count/this");
		testClient.get("/count/this");
		
		WireMock.reset();
		
		verify(getRequestedFor(urlEqualTo("/count/this")));
	}
	
	@Test
	public void verifiesArbitraryRequestCount() {
		testClient.get("/add/to/count");
		testClient.get("/add/to/count");
		testClient.get("/add/to/count");
		testClient.get("/add/to/count");
		
		verify(4, getRequestedFor(urlEqualTo("/add/to/count")));
	}
}
