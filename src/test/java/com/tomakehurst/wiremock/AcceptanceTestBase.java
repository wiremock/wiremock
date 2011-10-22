package com.tomakehurst.wiremock;

import org.junit.After;
import org.junit.Before;

import com.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class AcceptanceTestBase {

	protected WireMockServer wireMockServer;
	protected WireMockTestClient wireMockClient;

	@Before
	public void init() {
		constructWireMock();
		wireMockServer.start();
		wireMockClient = new WireMockTestClient();
	}

	@After
	public void stopWireMock() {
		wireMockServer.stop();
	}
	
	protected void constructWireMock() {
		wireMockServer = new WireMockServer();
	}

}
