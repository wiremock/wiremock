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

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.apache.http.MalformedChunkCodingException;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
		assertThat(response.firstHeader("Content-Type"), is("text/plain"));
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
    public void doesNotMatchOnAbsentHeader() {
        stubFor(post(urlEqualTo("/some/url"))
                .withRequestBody(containing("BODY"))
                .withHeader("NoSuchHeader", equalTo("This better not be here"))
                .willReturn(aResponse().withStatus(200)));

        assertThat(testClient.postWithBody("/some/url", "BODY", "text/plain", "utf-8").statusCode(), is(404));
    }

    @Test
    public void matchesIfRequestContainsHeaderNotSpecified() {
        stubFor(get(urlEqualTo("/some/extra/header"))
                .withHeader("ExpectedHeader", equalTo("expected-value"))
                .willReturn(aResponse().withStatus(200)));

        WireMockResponse response = testClient.get("/some/extra/header",
                withHeader("ExpectedHeader", "expected-value"),
                withHeader("UnexpectedHeader", "unexpected-value"));

        assertThat(response.statusCode(), is(200));
    }

    @Test
    public void matchesOnUrlPathAndQueryParameters() {
        stubFor(get(urlPathEqualTo("/path-and-query/match"))
                .withQueryParam("search", containing("WireMock"))
                .withQueryParam("since", equalTo("2014-10-14"))
                .willReturn(aResponse().withStatus(200)));

        assertThat(testClient.get("/path-and-query/match?since=2014-10-14&search=WireMock%20stubbing").statusCode(), is(200));
    }

	@Test
	public void doesNotMatchIfSpecifiedQueryParameterNotInRequest() {
		stubFor(get(urlPathEqualTo("/path-and-query/match"))
				.withQueryParam("search", containing("WireMock"))
				.willReturn(aResponse().withStatus(200)));

		assertThat(testClient.get("/path-and-query/match?wrongParam=wrongVal").statusCode(), is(404));
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

    @Test
    public void returningBinaryBody() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        stubFor(get(urlEqualTo("/binary/content")).willReturn(aResponse().withBody(bytes)));

        assertThat(testClient.get("/binary/content").binaryContent(), is(bytes));
    }

    @Test
    public void listingAllStubMappings() {
        stubFor(get(urlEqualTo("/stub/one")).willReturn(aResponse().withBody("One")));
        stubFor(post(urlEqualTo("/stub/two")).willReturn(aResponse().withBody("Two").withStatus(201)));

        ListStubMappingsResult listingResult = listAllStubMappings();
        StubMapping mapping1 = listingResult.getMappings().get(0);
        assertThat(mapping1.getRequest().getMethod(), is(POST));
        assertThat(mapping1.getRequest().getUrl(), is("/stub/two"));
        assertThat(mapping1.getResponse().getBody(), is("Two"));
        assertThat(mapping1.getResponse().getStatus(), is(201));

        StubMapping mapping2 = listingResult.getMappings().get(1);
        assertThat(mapping2.getRequest().getMethod(), is(GET));
        assertThat(mapping2.getRequest().getUrl(), is("/stub/one"));
        assertThat(mapping2.getResponse().getBody(), is("One"));
    }

    @Test
    public void stubbingPatch() {
        stubFor(patch(urlEqualTo("/a/registered/resource")).withRequestBody(equalTo("some body"))
                .willReturn(aResponse().withStatus(204)));

        WireMockResponse response = testClient.patchWithBody("/a/registered/resource", "some body", "text/plain");

        assertThat(response.statusCode(), is(204));
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
