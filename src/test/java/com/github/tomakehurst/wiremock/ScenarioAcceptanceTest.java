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
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ScenarioAcceptanceTest extends AcceptanceTestBase {

	@Test
	public void createMappingsInScenarioAndChangeResponseWithStateChange() {
		givenThat(get(urlEqualTo("/some/resource"))
				.willReturn(aResponse().withBody("Initial"))
				.inScenario("SomeResourceUpdate")
				.whenScenarioStateIs(STARTED));
		
		givenThat(put(urlEqualTo("/some/resource"))
				.willReturn(aResponse().withStatus(HTTP_OK))
				.inScenario("SomeResourceUpdate")
				.willSetStateTo("BodyModified")
				.whenScenarioStateIs(STARTED));
		
		givenThat(get(urlEqualTo("/some/resource"))
				.willReturn(aResponse().withBody("Modified"))
				.inScenario("SomeResourceUpdate")
				.whenScenarioStateIs("BodyModified"));
		
		assertThat(testClient.get("/some/resource").content(), is("Initial"));
		testClient.put("/some/resource");
		assertThat(testClient.get("/some/resource").content(), is("Modified"));
	}
	
	@Test
	public void mappingInScenarioIndependentOfCurrentState() {
		givenThat(get(urlEqualTo("/state/independent/resource"))
				.willReturn(aResponse().withBody("Some content"))
				.inScenario("StateIndependent"));
		
		givenThat(put(urlEqualTo("/state/modifying/resource"))
				.willReturn(aResponse().withStatus(HTTP_OK))
				.inScenario("StateIndependent")
				.willSetStateTo("BodyModified"));
		
		WireMockResponse response = testClient.get("/state/independent/resource");
		assertThat(response.statusCode(), is(HTTP_OK));
		assertThat(response.content(), is("Some content"));
		
		testClient.put("/state/modifying/resource");
		
		response = testClient.get("/state/independent/resource");
		assertThat(response.statusCode(), is(HTTP_OK));
		assertThat(response.content(), is("Some content"));
	}
	
	@Test
	public void resetAllScenariosState() {
		givenThat(get(urlEqualTo("/stateful/resource"))
				.willReturn(aResponse().withBody("Expected content"))
				.inScenario("ResetScenario")
				.whenScenarioStateIs(STARTED));
		
		givenThat(put(urlEqualTo("/stateful/resource"))
				.willReturn(aResponse().withStatus(HTTP_OK))
				.inScenario("ResetScenario")
				.willSetStateTo("Changed"));
		
		testClient.put("/stateful/resource");
		WireMock.resetAllScenarios();
		
		assertThat(testClient.get("/stateful/resource").content(), is("Expected content"));
	}

    @Test(expected = IllegalStateException.class)
    public void scenarioStateCannotBeSetIfScenarioIsNotNamed() {
        givenThat(get(urlEqualTo("/some/resource"))
                .willReturn(aResponse().withBody("Initial"))
                .whenScenarioStateIs(STARTED));
    }

    @Test(expected = IllegalStateException.class)
    public void scenarioStateTransitionCannotBeSetIfScenarioIsNotNamed() {
        givenThat(put(urlEqualTo("/some/resource"))
                .willReturn(aResponse().withStatus(HTTP_OK))
                .willSetStateTo("BodyModified"));
    }
}
