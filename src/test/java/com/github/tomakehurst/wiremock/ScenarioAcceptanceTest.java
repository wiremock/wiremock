package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.mapping.Scenario.STARTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;

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
}
