package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class MappingsFileAcceptanceTest extends AcceptanceTestBase {
	
	protected void constructWireMock() {
		wireMock = new WireMock();
		wireMock.setRequestsDirectory("src/test/resources/test-requests");
	}
	
	@Test
	public void mappingsLoadedFromJsonFiles() {
		WireMockResponse response = wireMockClient.get("/canned/resource/1");
		assertThat(response.statusCode(), is(200));
		
		response = wireMockClient.get("/canned/resource/2");
		assertThat(response.statusCode(), is(401));
	}
}
