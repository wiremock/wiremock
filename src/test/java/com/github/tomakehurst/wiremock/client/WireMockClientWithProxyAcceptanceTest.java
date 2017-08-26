/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class WireMockClientWithProxyAcceptanceTest {
	
	private WireMockServer wireMockServer;
	private WireMockTestClient testClient;
	private HttpProxyServer proxyServer;

	@Before
	public void init() throws Exception {

		wireMockServer = new WireMockServer(Options.DYNAMIC_PORT);
		wireMockServer.start();
		proxyServer = DefaultHttpProxyServer.bootstrap().withPort(0).start();

		WireMock.configureFor("http", "localhost", wireMockServer.port(), proxyServer.getListenAddress().getHostString(), proxyServer.getListenAddress().getPort());
		testClient = new WireMockTestClient(wireMockServer.port());
	}
	
	@After
	public void stopServer() {
		wireMockServer.stop();
		proxyServer.stop();
	}

	@Test
	public void buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse() {
		WireMock wireMock = new WireMock("http", "localhost", wireMockServer.port(), "", null, proxyServer.getListenAddress().getHostString(), proxyServer.getListenAddress().getPort());
		wireMock.register(
				get(urlEqualTo("/my/new/resource"))
				.willReturn(
						aResponse()
						.withStatus(304)));
		
		assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
	}

	@Test
	public void buildsMappingFromStaticSyntax() {
		givenThat(get(urlEqualTo("/my/new/resource"))
					.willReturn(aResponse()
						.withStatus(304)));

		assertThat(testClient.get("/my/new/resource").statusCode(), is(304));
	}

	@Test
	public void buildsMappingWithUrlOnyRequestAndResponseWithJsonBodyWithDiacriticSigns() {
		WireMock wireMock = new WireMock("http", "localhost", wireMockServer.port(), "", null, proxyServer.getListenAddress().getHostString(), proxyServer.getListenAddress().getPort());
		wireMock.register(
				get(urlEqualTo("/my/new/resource"))
				.willReturn(
						aResponse()
						.withBody("{\"address\":\"Puerto Banús, Málaga\"}")
						.withStatus(200)));

		assertThat(testClient.get("/my/new/resource").content(), is("{\"address\":\"Puerto Banús, Málaga\"}"));
	}
}
