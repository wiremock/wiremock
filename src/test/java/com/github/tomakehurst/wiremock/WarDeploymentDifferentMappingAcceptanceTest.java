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
package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for war deployment using a different servlet mapping path (see webappCustomMapping). This
 * test is a stripped down version of he normal war deployment test which just tests that we can
 * install a wiremock mapping and request the stored data. More extensive test are done in the
 * normal war deployment test.
 */
public class WarDeploymentDifferentMappingAcceptanceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

	private Server jetty;
	
	private WireMockTestClient testClient;
	
	@Before
	public void init() throws Exception {
		jetty = new Server(8085);
		WebAppContext context = new WebAppContext("sample-war/src/main/webappCustomMapping", "/wiremock");
		jetty.addHandler(context);
		jetty.start();
		
		WireMock.configureFor("localhost", 8085, "/wiremock/mapping");
		testClient = new WireMockTestClient(8085);
	}
	
	@After
	public void cleanup() throws Exception {
		jetty.stop();
		WireMock.configure();
	}

	@Test
	public void acceptsAndReturnsStubMapping() {
		givenThat(get(urlEqualTo("/war/stub")).willReturn(
				aResponse().withStatus(HTTP_OK).withBody("War stub OK")));
		
		assertThat(testClient.get("/wiremock/mapping/war/stub").content(), is("War stub OK"));
	}
}
