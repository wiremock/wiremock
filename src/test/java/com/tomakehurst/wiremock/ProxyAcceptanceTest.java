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
package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.tomakehurst.wiremock.client.WireMock.any;
import static com.tomakehurst.wiremock.client.WireMock.get;
import static com.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.tomakehurst.wiremock.client.WireMock.post;
import static com.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.client.WireMock;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class ProxyAcceptanceTest extends AcceptanceTestBase {

	private WireMockServer otherService;
	private WireMock otherServiceClient;
	
	@Before
	public void init() {
		otherService = new WireMockServer(8087);
		otherService.start();
		otherServiceClient = new WireMock("localhost", 8087);
	}
	
	@After
	public void stop() {
		otherService.stop();
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxy() {
		otherServiceClient.register(get(urlEqualTo("/proxied/resource?param=value"))
				.willReturn(aResponse()
				.withStatus(200)
				.withHeader("Content-Type", "text/plain")
				.withBody("Proxied content")));
		
		givenThat(any(urlEqualTo("/proxied/resource?param=value")).atLowPriority()
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.get("/proxied/resource?param=value");
		
		assertThat(response.content(), is("Proxied content"));
		assertThat(response.header("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void successfullyPostsResponseToOtherServiceViaProxy() {
		otherServiceClient.register(post(urlEqualTo("/proxied/resource"))
				.willReturn(aResponse()
				.withStatus(204)));
		
		givenThat(any(urlEqualTo("/proxied/resource")).atLowPriority()
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.postWithBody("/proxied/resource", "Post content", "text/plain", "utf-8");
		
		assertThat(response.statusCode(), is(204));
		otherServiceClient.verifyThat(postRequestedFor(urlEqualTo("/proxied/resource")).withBodyMatching("Post content"));
	}
	
}
