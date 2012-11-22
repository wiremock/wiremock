/**
 *
 */
package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aNewRequestTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

/**
 * @author sandarenu
 *
 *         <pre>
 *
 *        --- 1. send req ------>
 * Client <-- 2. http resp ----- WireMock
 *        <-- 3. new http req --
 *        --- 4. http resp ---->
 * </pre>
 */
public class WireMockSendNewRequestAsResponseTest {

	private WireMockTestClient testClient;
	private WireMockServer wireMockServer;
	private WireMockServer clientServer;

	@Before
	public void init() {
		wireMockServer = new WireMockServer(10080);
		wireMockServer.start();
		clientServer = new WireMockServer(10090);
		clientServer.start();
		// otherServer = new WireMockServer(8084);
		// otherServer.start();
		// return new WireMock("localhost", 8084);
		testClient = new WireMockTestClient(10080);
	}

	@After
	public void stopServers() {
		wireMockServer.stop();
		clientServer.stop();
	}

	@Test
	public void testNewRequestAsResponse() throws InterruptedException {
		WireMock mock4WireMock = new WireMock("localhost", 10080);
		WireMock mock4ClientServer = new WireMock("localhost", 10090);

		mock4WireMock.register(get(urlEqualTo("/standalone/test/resource"))
				.willReturn(
						aResponse()
								.withStatus(200)
								.withBody("Content")
								.withNewRequest(
										aNewRequestTo("localHost", 10090)
												.withMethod(RequestMethod.POST)
												.toUrl("/client/new/request")
												.withBody("New Request")
												.withDelay(100))));

		mock4ClientServer
				.register(get(urlEqualTo("/client/new/request")).willReturn(
						aResponse().withStatus(200).withBody("New Request")));

		assertThat(testClient.get("/standalone/test/resource").content(),
				is("Content"));
		Thread.sleep(2000);
		mock4ClientServer.verifyThat(1,
				postRequestedFor(urlEqualTo("/client/new/request"))
						.withRequestBody(matching("New Request")));
	}
}
