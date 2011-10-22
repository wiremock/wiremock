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

public class WireMockAltHostAndPortAcceptanceTest {
	
	private WireMockServer defaultServer;
	private WireMockServer altServer;
	
	@Before
	public void init() {
		defaultServer = new WireMockServer();
		defaultServer.start();
		altServer = new WireMockServer(8081);
		altServer.start();
	}
	
	@After
	public void stopServer() {
		defaultServer.stop();
		altServer.stop();
	}

	@Test
	public void useStaticSyntaxOnAlternativeHostAndPort() throws Exception {
		WireMockTestClient defaultTestClient = new WireMockTestClient(8080);
		WireMockTestClient altTestClient = new WireMockTestClient(8081);
		
		String thisHostName = InetAddress.getLocalHost().getHostName();
		WireMock.configureFor(thisHostName, 8081);
		
		givenThat(get(urlEqualTo("/resource/on/other/address"))
				.willReturn(aResponse()
					.withStatus(206)));
		
		assertThat(altTestClient.get("/resource/on/other/address").statusCode(), is(206));
		assertThat(defaultTestClient.get("/resource/on/other/address").statusCode(), is(404));
	}
}
