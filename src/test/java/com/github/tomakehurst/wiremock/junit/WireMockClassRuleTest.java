package com.github.tomakehurst.wiremock.junit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class WireMockClassRuleTest {
	public static class WithExactMatchTest {
		@ClassRule
		public static WireMockClassRule rule = new WireMockClassRule(0);

		static {
			rule.stubFor(get(urlPathEqualTo("/")).willReturn(aResponse().withStatus(200)));
			rule.start();
		}

		@Test
		public void shouldFindExactMatch() {
			WireMockTestClient testClient = new WireMockTestClient(rule.port());
			testClient.get(String.format("http://localhost:%d/", rule.port()));

			rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));
		}
	}

	public static class WithNearMatchTest {
		@Rule
		public ExpectedException thrown = ExpectedException.none();

		@ClassRule
		public static WireMockClassRule rule = new WireMockClassRule(0);

		static {
			rule.stubFor(get(urlPathEqualTo("/asdf1")).willReturn(aResponse().withStatus(200)));
			rule.start();
		}

		@Test
		public void shouldFindNearMatch() {
			WireMockTestClient testClient = new WireMockTestClient(rule.port());
			testClient.get(String.format("http://localhost:%d/asdf2", rule.port()));

			thrown.expect(com.github.tomakehurst.wiremock.client.VerificationException.class);
			thrown.expectMessage("No requests exactly matched. Most similar request was:");
			rule.verify(RequestPatternBuilder.newRequestPattern(RequestMethod.GET, urlPathEqualTo("/")));
		}
	}

}
