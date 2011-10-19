package com.tomakehurst.wiremock;

import org.junit.After;
import org.junit.Before;

import com.tomakehurst.wiremock.testsupport.WireMockClient;

public class AcceptanceTestBase {

	protected WireMockServer wireMockServer;
	protected WireMockClient wireMockClient;

	@Before
	public void init() {
		constructWireMock();
		wireMockServer.start();
		wireMockClient = new WireMockClient();
	}

	@After
	public void stopWireMock() {
		wireMockServer.stop();
	}
	
	protected void constructWireMock() {
		wireMockServer = new WireMockServer();
	}

}
