package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.Response;
import com.tomakehurst.wiremock.testsupport.WebClient;

public class BasicResponseAcceptanceTest {
	
	private WireMock wireMock;
	private WebClient webClient;
	
	
	@Before
	public void init() {
		wireMock = new WireMock();
		wireMock.start();
		webClient = new WebClient();
	}
	
	@After
	public void stopWireMock() {
		wireMock.stop();
	}

	@Test
	public void cannedResponseIsReturnedForPreciseUrl() {
		Response response = webClient.get("http://localhost:8080/canned/resource");
		assertThat(response.getStatusCode(), is(200));
		assertThat(response.getBodyAsString(), is("{ \"somekey\": \"My value\" }"));
	}
	
}
