package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.WireMockServer;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;


public class WireMockClientAcceptanceTest {
	
	private WireMockServer wireMockServer;
	private WireMock wireMock;
	private WireMockTestClient testClient;
	
	@Before
	public void init() {
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		wireMock = new WireMock();
		testClient = new WireMockTestClient();
	}
	
	@After
	public void stopServer() {
		wireMockServer.stop();
	}

	@Test
	public void buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse() {
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
	
	@Test
	public void useStaticSyntaxOnAlternativeHostAndPort() throws Exception {
		WireMockServer altServer = new WireMockServer(8081);
		altServer.start();
		
		String thisHostName = InetAddress.getLocalHost().getHostName();
		WireMock.setStaticHost(thisHostName);
		WireMock.setStaticPort(8081);
		
		givenThat(get(urlEqualTo("/resource/on/other/address"))
				.willReturn(aResponse()
					.withStatus(206)));
		assertThat(testClient.get("/resource/on/other/address").statusCode(), is(206));
		
		altServer.stop();
	}
	
	
}
