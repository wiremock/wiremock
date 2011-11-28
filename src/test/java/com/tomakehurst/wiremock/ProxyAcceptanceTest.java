package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.any;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.post;
import static com.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.client.WireMock;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class ProxyAcceptanceTest extends AcceptanceTestBase {

	private WireMockServer otherService;
	private WireMock otherServiceClient;
	
	@Before
	public void init() {
		otherService = new WireMockServer(8087);
		otherService.start();
		otherServiceClient = new WireMock("localhost", 8087);
	}
	
	@After
	public void stop() {
		otherService.stop();
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxy() {
		otherServiceClient.register(get(urlEqualTo("/proxied/resource"))
				.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Proxied content")));
		
		givenThat(any(urlEqualTo("/proxied/resource")).atHighPriority()
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.get("/proxied/resource");
		
		assertThat(response.content(), is("Proxied content"));
		assertThat(response.header("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void successfullyPostsResponseToOtherServiceViaProxy() {
		otherServiceClient.register(post(urlEqualTo("/proxied/resource"))
				.willReturn(aResponse()
				.withStatus(204)));
		
		givenThat(any(urlEqualTo("/proxied/resource")).atHighPriority()
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.postWithBody("/proxied/resource", "Post content", "text/plain", "utf-8");
		
		assertThat(response.statusCode(), is(204));
		otherServiceClient.verifyThat(postRequestedFor(urlEqualTo("/proxied/resource")).withBodyMatching("Post content"));
	}
	
}
