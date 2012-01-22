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
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_BAD_METHOD;
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
		givenThat(get(urlEqualTo("/a/registered/resource")).willReturn(
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
		givenThat(get(urlEqualTo("/search?name=John&postcode=N44LL")).willReturn(
				aResponse()
				.withHeader("Location", "/nowhere")
				.withStatus(302)));
		
		WireMockResponse response = testClient.get("/search?name=John&postcode=N44LL");
		
		assertThat(response.statusCode(), is(302));
	}
	
	@Test
	public void mappingWithHeaderMatchers() {
		givenThat(put(urlEqualTo("/some/url"))
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
		givenThat(put(urlEqualTo("/case/insensitive"))
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
		givenThat(get(urlEqualTo("/my/file")).willReturn(
				aResponse()
				.withStatus(200)
				.withBodyFile("plain-example.txt")));
		
		WireMockResponse response = testClient.get("/my/file");
		
		assertThat(response.content(), is("Some example test from a file"));
	}
	
	@Test
	public void matchingOnRequestBody() {
	    givenThat(put(urlEqualTo("/match/this/body"))
	            .withBodyMatching(".*Blah.*")
	            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBodyFile("plain-example.txt")));
        
        WireMockResponse response = testClient.putWithBody("/match/this/body", "Not what we asked for", "text/plain");
        assertThat(response.statusCode(), is(HTTP_BAD_METHOD));
        
        response = testClient.putWithBody("/match/this/body", "BlahBlahBlah", "text/plain");
        assertThat(response.statusCode(), is(HTTP_OK));
	}
	
	@Test
    public void matchingOnRequestBodyContains() {
        givenThat(put(urlEqualTo("/match/this/body/too"))
                .withBodyContaining("Blah")
                .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBodyFile("plain-example.txt")));
        
        WireMockResponse response = testClient.putWithBody("/match/this/body/too", "Not what we asked for", "text/plain");
        assertThat(response.statusCode(), is(HTTP_BAD_METHOD));
        
        response = testClient.putWithBody("/match/this/body/too", "BlahBlahBlah", "text/plain");
        assertThat(response.statusCode(), is(HTTP_OK));
    }
	
	@Test
	public void responseWithFixedDelay() {
	    givenThat(get(urlEqualTo("/delayed/resource")).willReturn(
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
		givenThat(get(urlMatching("/priority/.*")).atPriority(10)
				.willReturn(aResponse()
                .withStatus(500)));
		givenThat(get(urlEqualTo("/priority/resource")).atPriority(2).willReturn(aResponse().withStatus(200)));
		
		assertThat(testClient.get("/priority/resource").statusCode(), is(200));
	}
	
	@Test
	public void emptyResponseFault() {
		givenThat(get(urlEqualTo("/empty/response")).willReturn(
                aResponse()
                .withFault(Fault.EMPTY_RESPONSE)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/empty/response", NoHttpResponseException.class);
	}
	
	@Test
	public void malformedResponseChunkFault() {
		givenThat(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/malformed/response", MalformedChunkCodingException.class);
	}
	
	@Test
	public void randomDataOnSocketFault() {
		givenThat(get(urlEqualTo("/random/data")).willReturn(
                aResponse()
                .withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
		
		getAndAssertUnderlyingExceptionInstanceClass("/random/data", ClientProtocolException.class);
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
