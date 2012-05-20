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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.tomakehurst.wiremock.AbstractWireMockServer;
import com.github.tomakehurst.wiremock.JettyWireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;


public class WireMockClientAcceptanceTest {
	
	private AbstractWireMockServer wireMockServer;
	private WireMockTestClient testClient;
	
	@Before
	public void init() {
		wireMockServer = new JettyWireMockServer();
		wireMockServer.start();
		WireMock.configure();
		testClient = new WireMockTestClient();
	}
	
	@After
	public void stopServer() {
		wireMockServer.stop();
	}

	@Test
	public void buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse() {
		WireMock wireMock = new WireMock();
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
}
