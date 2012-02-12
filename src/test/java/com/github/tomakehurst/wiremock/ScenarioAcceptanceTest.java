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
}
