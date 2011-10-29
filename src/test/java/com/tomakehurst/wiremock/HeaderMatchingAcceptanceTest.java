package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class HeaderMatchingAcceptanceTest extends AcceptanceTestBase {
	
	@Test
	public void mappingWithExactUrlMethodAndHeaderMatchingIsCreatedAndReturned() {
		testClient.addResponse(MappingJsonSamples.MAPPING_REQUEST_WITH_EXACT_HEADERS);
		
		WireMockResponse response = testClient.get("/header/dependent",
				withHeader("Accept", "text/xml"),
				withHeader("If-None-Match", "abcd1234"));
		
		assertThat(response.statusCode(), is(304));
	}

	@Test
	public void mappingMatchedWithRegexHeaders() {
		testClient.addResponse(MappingJsonSamples.MAPPING_REQUEST_WITH_REGEX_HEADERS);
		
		WireMockResponse response = testClient.get("/header/match/dependent",
				withHeader("Accept", "text/xml"),
				withHeader("If-None-Match", "abcd1234"));
		
		assertThat(response.statusCode(), is(304));
	}
	
	@Test
	public void mappingMatchedWithNegativeRegexHeader() {
		testClient.addResponse(MappingJsonSamples.MAPPING_REQUEST_WITH_NEGATIVE_REGEX_HEADERS);
		
		WireMockResponse response = testClient.get("/header/match/dependent",
				withHeader("Accept", "text/xml"));
		assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
		
		response = testClient.get("/header/match/dependent",
				withHeader("Accept", "application/json"));
		assertThat(response.statusCode(), is(200));
	}
}
