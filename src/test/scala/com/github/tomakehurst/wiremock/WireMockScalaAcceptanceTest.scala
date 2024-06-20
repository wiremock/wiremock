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
package com.github.tomakehurst.wiremock

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.Options
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.github.tomakehurst.wiremock.client.WireMock._
import org.hamcrest.Matchers.is
import org.hamcrest.MatcherAssert.assertThat

class WireMockScalaAcceptanceTest {
	
	var wireMockServer: WireMockServer = _
	var testClient: WireMockTestClient = _

	@BeforeEach
	def init(): Unit = {
		wireMockServer = new WireMockServer(Options.DYNAMIC_PORT)
		wireMockServer.start()
		WireMock.configureFor(wireMockServer.port())
		testClient = new WireMockTestClient(wireMockServer.port())
	}
	
	@AfterEach
	def stopServer(): Unit = {
		wireMockServer.stop()
	}

	@Test
	def buildsMappingWithUrlOnlyRequestAndStatusOnlyResponse(): Unit = {
		val wireMock = new WireMock(wireMockServer.port())
		wireMock.register(get(urlEqualTo("/my/new/resource"))
					.willReturn(aResponse()
						.withStatus(304)))
		
		assertThat(testClient.get("/my/new/resource").statusCode(), is(304))
	}
	
	@Test
	def buildsMappingFromStaticSyntax(): Unit = {
		givenThat(get(urlEqualTo("/my/new/resource"))
					.willReturn(aResponse()
						.withStatus(304)))
		
		assertThat(testClient.get("/my/new/resource").statusCode(), is(304))
	}
	
	@Test
	def buildsMappingWithUrlOnyRequestAndResponseWithJsonBodyWithDiacriticSigns(): Unit = {
		val wireMock = new WireMock(wireMockServer.port())
		wireMock.register(
				get(urlEqualTo("/my/new/resource"))
				.willReturn(
						aResponse()
						.withBody("{\"address\":\"Puerto Banús, Málaga\"}")
						.withStatus(200)))

		assertThat(testClient.get("/my/new/resource").content(), is("{\"address\":\"Puerto Banús, Málaga\"}"))
	}
}
