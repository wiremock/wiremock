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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tomakehurst.wiremock.client.WireMock;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class StubbingAcceptanceTest {
	
	private static WireMockServer wireMockServer;
	private static WireMockTestClient testClient;
	
	@BeforeClass
	public static void setupServer() {
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		testClient = new WireMockTestClient();
	}
	
	@AfterClass
	public static void serverShutdown() {
		wireMockServer.stop();
	}
	
	@Before
	public void init() {
		WireMock.reset();
	}
	
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
}
