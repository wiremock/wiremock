package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class PortNumberTest {

	@Test
	public void canRunOnAnotherPortThan8080() {
		WireMockServer wireMockServer = new WireMockServer(8090);
		wireMockServer.start();
		WireMockTestClient wireMockClient = new WireMockTestClient(8090);
		
		wireMockClient.addResponse(MappingJsonSamples.BASIC_MAPPING_REQUEST);
		WireMockResponse response = wireMockClient.get("/a/registered/resource");
		assertThat(response.statusCode(), is(401));
		
	}
}
