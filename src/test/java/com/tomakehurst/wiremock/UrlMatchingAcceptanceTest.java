package com.tomakehurst.wiremock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class UrlMatchingAcceptanceTest extends AcceptanceTestBase {

	@Test
	public void mappingMatchedWithRegexUrl() {
		String REGEX_URL_MAPPING_REQUEST =
			"{ 													\n" +
			"	\"request\": {									\n" +
			"		\"method\": \"GET\",						\n" +
			"		\"urlPattern\": \"/one/(.*?)/three\"		\n" +
			"	},												\n" +
			"	\"response\": {									\n" +
			"		\"body\": \"Matched!\"						\n" +
			"	}												\n" +
			"}													  ";
		
		wireMockClient.addResponse(REGEX_URL_MAPPING_REQUEST);
		WireMockResponse response = wireMockClient.get("/one/two/three");
		
		assertThat(response.statusCode(), is(200));
		assertThat(response.content(), is("Matched!"));
	}
}
