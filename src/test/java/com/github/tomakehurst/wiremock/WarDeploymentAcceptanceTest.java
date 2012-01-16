package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class WarDeploymentAcceptanceTest {

	private Server jetty;
	
	private WireMockTestClient testClient;
	
	@Before
	public void init() throws Exception {
		jetty = new Server(8085);
		WebAppContext context = new WebAppContext("sample-war/src/main/webapp", "/wiremock");
		jetty.addHandler(context);
		jetty.start();
		
		WireMock.configureFor("localhost", 8085, "/wiremock");
		testClient = new WireMockTestClient(8085);
	}
	
	@After
	public void cleanup() throws Exception {
		jetty.stop();
		WireMock.configure();
	}
	
	@Test
	public void servesBakedInStubResponse() {
		WireMockResponse response = testClient.get("/wiremock/api/mytest");
		assertThat(response.content(), containsString("YES"));
	}
	
	@Test
	public void acceptsAndReturnsStubMapping() {
		givenThat(get(urlEqualTo("/war/stub")).willReturn(
				aResponse().withStatus(HTTP_OK).withBody("War stub OK")));
		
		assertThat(testClient.get("/wiremock/war/stub").content(), is("War stub OK"));
	}
}
