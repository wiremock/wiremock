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

import com.github.tomakehurst.wiremock.common.SingleRootFileSource;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.standalone.JsonFileMappingsLoader;
import com.github.tomakehurst.wiremock.standalone.MappingsLoader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MappingsLoaderAcceptanceTest {
	
	private WireMockServer wireMockServer;
	private WireMockTestClient testClient;

	@Before
	public void init() {
		testClient = new WireMockTestClient();
	}

	@After
	public void stopWireMock() {
		wireMockServer.stop();
	}

    private void buildWireMock(Options options) {
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
    }

    @Test
	public void mappingsLoadedFromJsonFiles() {
        buildWireMock(wireMockConfig());
        wireMockServer.loadMappingsUsing(new JsonFileMappingsLoader(new SingleRootFileSource("src/test/resources/test-requests")));

		WireMockResponse response = testClient.get("/canned/resource/1");
		assertThat(response.statusCode(), is(200));

		response = testClient.get("/canned/resource/2");
		assertThat(response.statusCode(), is(401));
	}

    @Test
    public void mappingsLoadedViaClasspath() {
        buildWireMock(wireMockConfig().usingFilesUnderClasspath("classpath-filesource"));
        assertThat(testClient.get("/test").content(), is("THINGS!"));
    }
}
