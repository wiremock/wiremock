package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import net.sf.json.test.JSONAssert;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tomakehurst.wiremock.testsupport.MappingJsonSamples;

@RunWith(JMock.class)
public class WireMockClientTest {

	private Mockery context;
	private AdminClient adminClient;
	private WireMock wireMock;
	
	@Before
	public void init() {
		context = new Mockery();
		adminClient = context.mock(AdminClient.class);
		wireMock = new WireMock();
		wireMock.setAdminClient(adminClient);
	}
	
	@Test
	public void shouldAddBasicResponse() {
		context.checking(new Expectations() {{
			one(adminClient).addResponse(with(jsonEqualTo(MappingJsonSamples.BASIC_GET)));
		}});
		
		wireMock.register(
				get(urlEqualTo("/basic/get"))
				.willReturn(
						aResponse()
						.withStatus(304)));
	}
	
	private Matcher<String> jsonEqualTo(final String expectedJson) {
		return new TypeSafeMatcher<String>() {

			@Override
			public void describeTo(Description desc) {
			}

			@Override
			public boolean matchesSafely(String actualJson) {
				try {
					JSONAssert.assertJsonEquals(expectedJson, actualJson);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			
		};
	}
}
