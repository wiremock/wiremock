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

import com.github.tomakehurst.wiremock.junit.WireMockStaticRule;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class WireMockStaticJUnitRuleTest {

	@Rule
	public static WireMockStaticRule wireMockRule = new WireMockStaticRule(8089);

    private WireMockTestClient testClient;

    @Before
    public void init() {
        testClient = new WireMockTestClient(8089);
    }
	
	@AfterClass
	public static void stopWireMock() {
		wireMockRule.stopServer();
	}
	
	@Test
	public void canRegisterStubAndFetchOnCorrectPort() {
		givenThat(get(urlEqualTo("/rule/test")).willReturn(aResponse().withBody("Rule test body")));
		assertThat(testClient.get("/rule/test").content(), is("Rule test body"));
	}

    @Ignore("Generates a failure to illustrate a Rule bug whereby a failed test would cause WireMock not to be reset between test cases")
    @Test
    public void fail() {
        givenThat(get(urlEqualTo("/should/never/see/this")).willReturn(aResponse().withStatus(200)));
        assertTrue(false);
    }

    @Test
    public void succeedIfWireMockHasBeenReset() {
        assertThat(testClient.get("/should/never/see/this").statusCode(), is(404));
    }
}
