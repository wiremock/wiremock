package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class HeaderMatchingAcceptanceTest extends AcceptanceTestBase {
	
	static final String MAPPING_REQUEST_WITH_EXACT_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/dependent\",				\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"equalTo\": \"text/xml\"			\n" +
		"			},										\n" +
		"			\"If-None-Match\": {					\n" +
		"				\"equalTo\": \"abcd1234\"			\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	static final String MAPPING_REQUEST_WITH_REGEX_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/match/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"matches\": \"(.*)xml(.*)\"		\n" +
		"			},										\n" +
		"			\"If-None-Match\": {					\n" +
		"				\"matches\": \"([a-z0-9]*)\"		\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 304,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	static final String MAPPING_REQUEST_WITH_NEGATIVE_REGEX_HEADERS =
		"{ 													\n" +
		"	\"request\": {									\n" +
		"		\"method\": \"GET\",						\n" +
		"		\"url\": \"/header/match/dependent\",		\n" +
		"		\"headers\": {								\n" +
		"			\"Accept\": {							\n" +
		"				\"doesNotMatch\": \"(.*)xml(.*)\"	\n" +
		"			}										\n" +
		"		}											\n" +
		"	},												\n" +
		"	\"response\": {									\n" +
		"		\"status\": 200,							\n" +
		"		\"headers\": {								\n" +
		"			\"Content-Type\": \"text/xml\"			\n" +
		"		}											\n" +
		"	}												\n" +
		"}													";
	
	@Test
	public void mappingWithExactUrlMethodAndHeaderMatchingIsCreatedAndReturned() {
		wireMockClient.addResponse(MAPPING_REQUEST_WITH_EXACT_HEADERS);
		
		WireMockResponse response = wireMockClient.get("/header/dependent",
				withHeader("Accept", "text/xml"),
				withHeader("If-None-Match", "abcd1234"));
		
		assertThat(response.statusCode(), is(304));
	}

	@Test
	public void mappingMatchedWithRegexHeaders() {
		wireMockClient.addResponse(MAPPING_REQUEST_WITH_REGEX_HEADERS);
		
		WireMockResponse response = wireMockClient.get("/header/match/dependent",
				withHeader("Accept", "text/xml"),
				withHeader("If-None-Match", "abcd1234"));
		
		assertThat(response.statusCode(), is(304));
	}
	
	@Test
	public void mappingMatchedWithNegativeRegexHeader() {
		wireMockClient.addResponse(MAPPING_REQUEST_WITH_NEGATIVE_REGEX_HEADERS);
		
		WireMockResponse response = wireMockClient.get("/header/match/dependent",
				withHeader("Accept", "text/xml"));
		assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
		
		response = wireMockClient.get("/header/match/dependent",
				withHeader("Accept", "application/json"));
		assertThat(response.statusCode(), is(200));
	}
}
