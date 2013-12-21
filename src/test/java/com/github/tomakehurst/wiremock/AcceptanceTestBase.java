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
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class AcceptanceTestBase {

	protected static WireMockServer wireMockServer;
	protected static WireMockTestClient testClient;

	@BeforeClass
	public static void setupServer() {
		setupServer(wireMockConfig());
	}

	@AfterClass
	public static void serverShutdown() {
		wireMockServer.stop();
	}

    public static void setupServer(Options options) {
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        testClient = new WireMockTestClient();
        WireMock.configure();
    }

	@Before
	public void init() throws InterruptedException {
		WireMock.resetToDefault();
	}

}
