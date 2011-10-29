package com.tomakehurst.wiremock.client;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.delete;
import static com.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.head;
import static com.tomakehurst.wiremock.client.WireMock.matching;
import static com.tomakehurst.wiremock.client.WireMock.notMatching;
import static com.tomakehurst.wiremock.client.WireMock.options;
import static com.tomakehurst.wiremock.client.WireMock.post;
import static com.tomakehurst.wiremock.client.WireMock.put;
import static com.tomakehurst.wiremock.client.WireMock.trace;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.tomakehurst.wiremock.testsupport.WireMatchers.jsonEqualTo;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tomakehurst.wiremock.http.RequestMethod;
import com.tomakehurst.wiremock.mapping.RequestPattern;
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
				.withHeader("If-None-Match", matching("([a-z0-9]*)"))
				.withHeader("Accept", notMatching("(.*)xml(.*)"))
				.willReturn(aResponse().withStatus(201)));
	}
	
	@Test
	public void shouldVerifyRequestMadeWhenCountMoreThan0() {
		context.checking(new Expectations() {{
			allowing(adminClient).getRequestsMatching(
					new RequestPattern(RequestMethod.DELETE, "/to/delete")); will(returnValue(3));
		}});
		
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/to/delete");
		wireMock.verifyThat(new RequestPatternBuilder(RequestMethod.DELETE, urlStrategy));
	}
	
	@Test(expected=VerificationException.class)
	public void shouldThrowVerificationExceptionWhenVerifyingRequestNotMatching() {
		context.checking(new Expectations() {{
			allowing(adminClient).getRequestsMatching(with(any(RequestPattern.class))); will(returnValue(0));
		}});
		
		UrlMatchingStrategy urlStrategy = new UrlMatchingStrategy();
		urlStrategy.setUrl("/wrong/url");
		wireMock.verifyThat(new RequestPatternBuilder(RequestMethod.DELETE, urlStrategy));
	}
	
	public void expectExactlyOneAddResponseCallWithJson(final String json) {
		context.checking(new Expectations() {{
			one(adminClient).addResponse(with(jsonEqualTo(json)));
		}});
	}
	
	
}
