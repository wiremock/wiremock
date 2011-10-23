package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.WireMockServer;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;


public class WireMockClientAcceptanceTest {
	
	private WireMockServer wireMockServer;
	private WireMockTestClient testClient;
	
	@Before
	public void init() {
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configure();
		testClient = new WireMockTestClient();
	}
	
	@After
	public void stopServer() {
		wireMockServer.stop();
	}

	@Test
	public void buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse() {
		WireMock wireMock = new WireMock();
		wireMock.register(
				get(urlEqualTo("/my/new/resource"))
				.willReturn(
						aResponse()
						.withStatus(304)));
		
		assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
	}
	
	@Test
	public void buildsMappingFromStaticSyntax() {
		givenThat(get(urlEqualTo("/my/new/resource"))
					.willReturn(aResponse()
						.withStatus(304)));
		
		assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
	}
}
