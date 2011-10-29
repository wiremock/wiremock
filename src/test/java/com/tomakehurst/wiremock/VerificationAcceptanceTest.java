package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.tomakehurst.wiremock.client.WireMock.verify;
import static com.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;

import org.junit.Test;

import com.tomakehurst.wiremock.client.VerificationException;
import com.tomakehurst.wiremock.client.WireMock;

public class VerificationAcceptanceTest extends FluentAPITestBase {

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
	
	@Test(expected=VerificationException.class)
	public void resetErasesCounters() {
		testClient.get("/count/this");
		testClient.get("/count/this");
		testClient.get("/count/this");
		
		WireMock.reset();
		
		verify(getRequestedFor(urlEqualTo("/count/this")));
	}
}
