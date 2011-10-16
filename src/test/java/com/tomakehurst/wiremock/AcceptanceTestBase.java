package com.tomakehurst.wiremock;

import org.junit.After;
import org.junit.Before;

import com.tomakehurst.wiremock.testsupport.WireMockClient;

public class AcceptanceTestBase {

	protected WireMock wireMock;
	protected WireMockClient wireMockClient;

	@Before
	public void init() {
		constructWireMock();
		wireMock.start();
		wireMockClient = new WireMockClient();
	}

	@After
	public void stopWireMock() {
		wireMock.stop();
	}
	
	protected void constructWireMock() {
		wireMock = new WireMock();
	}

}
