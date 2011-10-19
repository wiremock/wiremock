package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.tomakehurst.wiremock.standalone.MappingsLoader;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class MappingsLoaderAcceptanceTest extends AcceptanceTestBase {
	
	protected void constructWireMock() {
		wireMockServer = new WireMockServer();
		MappingsLoader mappingsLoader = new JsonFileMappingsLoader("src/test/resources/test-requests");
		wireMockServer.loadMappingsUsing(mappingsLoader);
	}
	
	@Test
	public void mappingsLoadedFromJsonFiles() {
		WireMockResponse response = wireMockClient.get("/canned/resource/1");
		assertThat(response.statusCode(), is(200));
		
		response = wireMockClient.get("/canned/resource/2");
		assertThat(response.statusCode(), is(401));
	}
}
