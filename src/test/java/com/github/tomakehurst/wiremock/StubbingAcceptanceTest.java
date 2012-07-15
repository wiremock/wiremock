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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;

public class StubbingAcceptanceTest extends AcceptanceTestBase {
	
	@Test
	public void mappingWithExactUrlAndMethodMatch() {
		stubFor(get(urlEqualTo("/a/registered/resource")).willReturn(
				aResponse()
				.withStatus(401)
				.withHeader("Content-Type", "text/plain")
				.withBody("Not allowed!")));
		
		WireMockResponse response = testClient.get("/a/registered/resource");
		
		assertThat(response.statusCode(), is(401));
		assertThat(response.content(), is("Not allowed!"));
		assertThat(response.header("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void mappingWithUrlContainingQueryParameters() {
		stubFor(get(urlEqualTo("/search?name=John&postcode=N44LL")).willReturn(
				aResponse()
				.withHeader("Location", "/nowhere")
				.withStatus(302)));
		
		WireMockResponse response = testClient.get("/search?name=John&postcode=N44LL");
		
		assertThat(response.statusCode(), is(302));
	}
	
	@Test
	public void mappingWithHeaderMatchers() {
		stubFor(put(urlEqualTo("/some/url"))
			.withHeader("One", equalTo("abcd1234"))
			.withHeader("Two", matching("[a-z]{5}"))
			.withHeader("Three", notMatching("[A-Z]+"))
			.willReturn(aResponse().withStatus(204)));
		
		WireMockResponse response = testClient.put("/some/url",
				withHeader("One", "abcd1234"),
				withHeader("Two", "thing"),
				withHeader("Three", "something"));
		
		assertThat(response.statusCode(), is(204));
	}
	
	@Test
	public void mappingWithCaseInsensitiveHeaderMatchers() {
		stubFor(put(urlEqualTo("/case/insensitive"))
			.withHeader("ONE", equalTo("abcd1234"))
			.withHeader("two", matching("[a-z]{5}"))
			.withHeader("Three", notMatching("[A-Z]+"))
			.willReturn(aResponse().withStatus(204)));
		
		WireMockResponse response = testClient.put("/case/insensitive",
				withHeader("one", "abcd1234"),
				withHeader("TWO", "thing"),
				withHeader("tHrEe", "something"));
		
		assertThat(response.statusCode(), is(204));
	}
	
	@Test
	public void responseBodyLoadedFromFile() {
		stubFor(get(urlEqualTo("/my/file")).willReturn(
				aResponse()
				.withStatus(200)
				.withBodyFile("plain-example.txt")));
		
		WireMockResponse response = testClient.get("/my/file");
		
		assertThat(response.content(), is("Some example test from a file"));
	}
	
	@Test
	public void matchingOnRequestBodyWithTwoRegexes() {
		stubFor(put(urlEqualTo("/match/this/body"))
	            .withRequestBody(matching(".*Blah.*"))
	            .withRequestBody(matching(".*@[0-9]{5}@.*"))
	            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBodyFile("plain-example.txt")));
        
        WireMockResponse response = testClient.putWithBody("/match/this/body", "Blah...but not the rest", "text/plain");
        assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
        response = testClient.putWithBody("/match/this/body", "@12345@...but not the rest", "text/plain");
        assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
        
        response = testClient.putWithBody("/match/this/body", "BlahBlah@56565@Blah", "text/plain");
        assertThat(response.statusCode(), is(HTTP_OK));
	}
	
	@Test
    public void matchingOnRequestBodyWithAContainsAndANegativeRegex() {
		stubFor(put(urlEqualTo("/match/this/body/too"))
                .withRequestBody(containing("Blah"))
                .withRequestBody(notMatching(".*[0-9]+.*"))
                .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBodyFile("plain-example.txt")));
        
        WireMockResponse response = testClient.putWithBody("/match/this/body/too", "Blah12345", "text/plain");
        assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
        
        response = testClient.putWithBody("/match/this/body/too", "BlahBlahBlah", "text/plain");
        assertThat(response.statusCode(), is(HTTP_OK));
    }
	
	@Test
    public void matchingOnRequestBodyWithEqualTo() {
        stubFor(put(urlEqualTo("/match/this/body/too"))
                .withRequestBody(equalTo("BlahBlahBlah"))
                .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBodyFile("plain-example.txt")));
        
        WireMockResponse response = testClient.putWithBody("/match/this/body/too", "Blah12345", "text/plain");
        assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
        
        response = testClient.putWithBody("/match/this/body/too", "BlahBlahBlah", "text/plain");
        assertThat(response.statusCode(), is(HTTP_OK));
    }
	
	@Test
	public void responseWithFixedDelay() {
	    stubFor(get(urlEqualTo("/delayed/resource")).willReturn(
                aResponse()
                .withStatus(200)
                .withBody("Content")
                .withFixedDelay(500)));
        
	    long start = System.currentTimeMillis();
        testClient.get("/delayed/resource");
        int duration = (int) (System.currentTimeMillis() - start);
        
        assertThat(duration, greaterThanOrEqualTo(500));
	}
	
	@Test
	public void highPriorityMappingMatchedFirst() {
		stubFor(get(urlMatching("/priority/.*")).atPriority(10)
				.willReturn(aResponse()
                .withStatus(500)));
		stubFor(get(urlEqualTo("/priority/resource")).atPriority(2).willReturn(aResponse().withStatus(200)));
		
		assertThat(testClient.get("/priority/resource").statusCode(), is(200));
	}
	
	@Test
	public void emptyResponseFault() {
		stubFor(get(urlEqualTo("/empty/response")).willReturn(
                aResponse()
                .withFault(Fault.EMPTY_RESPONSE)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/empty/response", NoHttpResponseException.class);
	}
	
	@Test
	public void malformedResponseChunkFault() {
		stubFor(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/malformed/response", MalformedChunkCodingException.class);
	}
	
	@Test
	public void randomDataOnSocketFault() {
		stubFor(get(urlEqualTo("/random/data")).willReturn(
                aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/random/data", ClientProtocolException.class);
	}
	
	@Test
	public void matchingUrlsWithEscapeCharacters() {
		stubFor(get(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26")).willReturn(aResponse().withStatus(HTTP_OK)));
		assertThat(testClient.get("/%26%26The%20Lord%20of%20the%20Rings%26%26").statusCode(), is(HTTP_OK));
	}
	
	@Test
	public void default200ResponseWhenStatusCodeNotSpecified() {
		stubFor(get(urlEqualTo("/default/two-hundred")).willReturn(aResponse()));
		assertThat(testClient.get("/default/two-hundred").statusCode(), is(HTTP_OK));
	}
	
	private void getAndAssertUnderlyingExceptionInstanceClass(String url, Class<?> expectedClass) {
		boolean thrown = false;
		try {
			WireMockResponse response = testClient.get(url);
			response.content();
		} catch (Exception e) {
			assertThat(e.getCause(), instanceOf(expectedClass));
			thrown = true;
		}
		
		assertTrue("No exception was thrown", thrown);
	}
}
