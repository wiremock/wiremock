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

public class WarDeploymentAcceptanceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Test
    public void tryingToAddSocketAcceptDelayGives500() {
        expectVerificationExceptionFor500();
        addRequestProcessingDelay(1000);
    }

    @Test
    public void tryingToShutDownGives500() {
        expectVerificationExceptionFor500();
        shutdownServer();
    }

    @Test
    public void tryingToSaveMappingsGives500() {
        expectVerificationExceptionFor500();
        saveAllMappings();
    }

    private void expectVerificationExceptionFor500() {
        expectedException.expect(VerificationException.class);
        expectedException.expectMessage(containsString("500"));
    }
}
