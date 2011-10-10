package com.tomakehurst.wiremock;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.Response;
import com.tomakehurst.wiremock.testsupport.WireMockClient;

public class BasicResponseAcceptanceTest {
	
	private WireMock wireMock;
	private WireMockClient wireMockClient;
	
	
	@Before
	public void init() {
		wireMock = new WireMock();
		wireMock.start();
		wireMockClient = new WireMockClient();
	}
	
	@After
	public void stopWireMock() {
		wireMock.stop();
	}

	@Test
	public void cannedResponseIsReturnedForPreciseUrl() {
		Response response = wireMockClient.get("/canned/resource");
		assertThat(response.statusCode(), is(HTTP_OK));
		assertThat(response.content(), is("{ \"somekey\": \"My value\" }"));
	}
	
//	@Test
//	public void responseIsCreatedAndReturned() {
//		String responseSpecJson = 
//			"{ 											" +
//			"	'method': 'GET',						" +
//			"	'uriPattern': '/a/registered/resource',	" +
//			"	'response': {							" +
//			"		'status': 401,						" +
//			"		'body': 'Not allowed!',				" +
//			"	}										" +
//			"}											";
//		wireMockClient.addResponse(responseSpecJson);
//		
//		Response response = wireMockClient.get("/a/registered/resource");
//		
//		assertThat(response.statusCode(), is(401));
//	}
	
	@Test
	public void notFoundResponseIsReturnedForUnregisteredUrl() {
		Response response = wireMockClient.get("/non-existent/resource");
		assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
	}
	
}
