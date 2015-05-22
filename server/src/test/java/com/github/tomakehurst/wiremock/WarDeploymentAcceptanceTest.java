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

import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.Network;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class WarDeploymentAcceptanceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

	private Server jetty;
	
	private WireMockTestClient testClient;
	
	@Before
	public void init() throws Exception {
        int port = Network.findFreePort();
		jetty = new Server(port);
		WebAppContext context = new WebAppContext("sample-war/src/main/webapp", "/wiremock");
		jetty.addHandler(context);
		jetty.start();
		
		WireMock.configureFor("localhost", port, "/wiremock");
		testClient = new WireMockTestClient(port);
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

    @Test
    public void tryingToAddSocketAcceptDelayGives500() {
        try {
            addRequestProcessingDelay(1000);
            fail("Expected a VerificationException");
        } catch (VerificationException e) {
            assertThat(e.getMessage(), containsString("500"));
        }
    }

    @Test
    public void tryingToShutDownGives500() {
        try {
            shutdownServer();
            fail("Expected a VerificationException");
        } catch (VerificationException e) {
            assertThat(e.getMessage(), containsString("500"));
        }
    }

    @Test
    public void tryingToSaveMappingsGives500() {
        try {
            saveAllMappings();
            fail("Expected a VerificationException");
        } catch (VerificationException e) {
            assertThat(e.getMessage(), containsString("500"));
        }
    }
}
