package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class MappingsLoaderAcceptanceTest {
	
	private WireMockServer wireMockServer;
	private WireMockTestClient testClient;

	@Before
	public void init() {
		constructWireMock();
		wireMockServer.start();
		testClient = new WireMockTestClient();
	}

	@After
	public void stopWireMock() {
		wireMockServer.stop();
	}
	
	private void constructWireMock() {
		wireMockServer = new WireMockServer();
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader("src/test/resources/test-requests");
		wireMockServer.loadMappingsUsing(mappingsLoader);
	}
	
	@Test
	public void mappingsLoadedFromJsonFiles() {
		WireMockResponse response = testClient.get("/canned/resource/1");
		assertThat(response.statusCode(), is(200));
		
		response = testClient.get("/canned/resource/2");
		assertThat(response.statusCode(), is(401));
	}
}
