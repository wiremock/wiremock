package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.matching;
import static com.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.tomakehurst.wiremock.client.WireMock.put;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.WireMockResponse;

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
	public void responseBodyLoadedFromFile() {
		givenThat(get(urlEqualTo("/my/file")).willReturn(
				aResponse()
				.withStatus(200)
				.withBodyFile("plain-example.txt")));
		
		WireMockResponse response = testClient.get("/my/file");
		
		assertThat(response.content(), is("Some example test from a file"));
	}
}
