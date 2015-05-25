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
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.VerificationResult;
import com.google.common.collect.ImmutableList;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(JMock.class)
public class WireMockClientTest {

	private Mockery context;
	private Admin admin;
	private WireMock wireMock;
	
	@Before
	public void init() {
		context = new Mockery();
		admin = context.mock(Admin.class);
		wireMock = new WireMock(admin);
	}
	
	@Test
	public void shouldAddBasicGetMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_GET);
		wireMock.register(
				get(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicPostMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_POST);
		wireMock.register(
				post(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicPutMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_PUT);
		wireMock.register(
				put(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicDeleteMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_DELETE);
		wireMock.register(
				delete(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicPatchMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_PATCH);
		wireMock.register(
				patch(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicHeadMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_HEAD);
		wireMock.register(
				head(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicOptionsMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_OPTIONS);
		wireMock.register(
				options(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicTraceMapping() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_TRACE);
		wireMock.register(
				trace(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddBasicMappingWithAnyMethod() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_ANY_METHOD);
		wireMock.register(
				WireMock.any(urlEqualTo("/basic/mapping/resource"))
				.willReturn(aResponse().withStatus(304)));
	}
	
	@Test
	public void shouldAddMappingWithResponseBody() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.WITH_RESPONSE_BODY);
		wireMock.register(
				get(urlEqualTo("/with/body"))
				.willReturn(aResponse().withStatus(200).withBody("Some content")));
	}
	
	@Test
	public void shouldAddMappingWithUrlRegexMatch() {
		String expectedJson = String.format(MappingJsonSamples.STATUS_ONLY_GET_MAPPING_TEMPLATE, "/match/with/[a-z]+/here");
		expectExactlyOneAddResponseCallWithJson(expectedJson);
		wireMock.register(
				get(urlMatching("/match/with/[a-z]+/here"))
				.willReturn(aResponse().withStatus(200)));
	}
	
	@Test
	public void shouldAddMappingWithResponseHeader() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.BASIC_MAPPING_REQUEST_WITH_RESPONSE_HEADER);
		wireMock.register(
				get(urlEqualTo("/a/registered/resource"))
				.willReturn(aResponse().withStatus(401).withBody("Not allowed!")
						.withHeader("Content-Type", "text/plain")));
	}
	
	@Test
	public void shouldAddMappingWithAll3TypesOfRequestHeaderMatch() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.WITH_REQUEST_HEADERS);
		wireMock.register(
				put(urlEqualTo("/header/matches/dependent"))
				.withHeader("Content-Type", equalTo("text/xml"))
				.withHeader("Cache-Control", containing("private"))
				.withHeader("If-None-Match", matching("([a-z0-9]*)"))
				.withHeader("Accept", notMatching("(.*)xml(.*)"))
				.willReturn(aResponse().withStatus(201)));
	}
	
	@Test
	public void shouldAddMappingWithAll4BodyPatternTypes() {
		expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.WITH_BODY_PATTERNS);
		wireMock.register(
				put(urlEqualTo("/body/patterns/dependent"))
				.withRequestBody(equalTo("the number is 1234"))
				.withRequestBody(containing("number"))
				.withRequestBody(matching(".*[0-9]{4}"))
				.withRequestBody(notMatching(".*5678.*"))
				.willReturn(aResponse().withStatus(201)));
	}
	
	@Test
	public void shouldVerifyRequestMadeWhenCountMoreThan0() {
		context.checking(new Expectations() {{
			allowing(admin).countRequestsMatching(
                    new RequestPattern(RequestMethod.DELETE, "/to/delete")); will(returnValue(VerificationResult.withCount(3)));
		}});
		
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/to/delete");
		wireMock.verifyThat(new RequestPatternBuilder(RequestMethod.DELETE, urlStrategy));
	}
	
	@Test(expected=VerificationException.class)
	public void shouldThrowVerificationExceptionWhenVerifyingRequestNotMatching() {
		context.checking(new Expectations() {{
			allowing(admin).countRequestsMatching(with(any(RequestPattern.class))); will(returnValue(VerificationResult.withCount(0)));
            allowing(admin).findRequestsMatching(with(any(RequestPattern.class)));
                will(returnValue(FindRequestsResult.withRequests(ImmutableList.<LoggedRequest>of())));
		}});
		
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/wrong/url");
		wireMock.verifyThat(new RequestPatternBuilder(RequestMethod.DELETE, urlStrategy));
	}

    @Test
    public void shouldRegisterStubMapping() {
        expectExactlyOneAddResponseCallWithJson(MappingJsonSamples.WITH_RESPONSE_BODY);
        StubMapping mapping = StubMapping.buildFrom(MappingJsonSamples.SPEC_WITH_RESPONSE_BODY);

        wireMock.register(mapping);
    }
	
	public void expectExactlyOneAddResponseCallWithJson(final String json) {
        final StubMapping stubMapping = Json.read(json, StubMapping.class);

		context.checking(new Expectations() {{
			one(admin).addStubMapping(stubMapping);
		}});
	}
	
	
}
