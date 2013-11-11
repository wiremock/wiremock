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
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProxyAcceptanceTest extends AcceptanceTestBase {

	private WireMockServer otherService;
	private WireMock otherServiceClient;
	
	@Override
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
		
		givenThat(any(urlEqualTo("/proxied/resource?param=value")).atPriority(10)
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
		
		givenThat(any(urlEqualTo("/proxied/resource")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.postWithBody("/proxied/resource", "Post content", "text/plain", "utf-8");
		
		assertThat(response.statusCode(), is(204));
		otherServiceClient.verifyThat(postRequestedFor(urlEqualTo("/proxied/resource")).withRequestBody(matching("Post content")));
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxyWithEscapeCharsInUrl() {
		otherServiceClient.register(get(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26"))
				.willReturn(aResponse()
                        .withStatus(200)));
		
		givenThat(any(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26")).atPriority(10)
                .willReturn(aResponse()
                        .proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.get("/%26%26The%20Lord%20of%20the%20Rings%26%26");
		
		assertThat(response.statusCode(), is(200));
	}

    @Test
    public void sendsContentLengthHeaderWhenPostingIfPresentInOriginalRequest() {
        otherServiceClient.register(post(urlEqualTo("/with/length")).willReturn(aResponse().withStatus(201)));
        stubFor(post(urlEqualTo("/with/length")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.postWithBody("/with/length", "TEST", "application/x-www-form-urlencoded", "utf-8");

        otherServiceClient.verifyThat(postRequestedFor(urlEqualTo("/with/length")).withHeader("Content-Length", equalTo("4")));
    }

    @Test
    public void sendsTransferEncodingChunkedWhenPostingIfPresentInOriginalRequest() {
        otherServiceClient.register(post(urlEqualTo("/chunked")).willReturn(aResponse().withStatus(201)));
        stubFor(post(urlEqualTo("/chunked")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.postWithChunkedBody("/chunked", "TEST".getBytes());

        otherServiceClient.verifyThat(postRequestedFor(urlEqualTo("/chunked"))
                .withHeader("Transfer-Encoding", equalTo("chunked")));
    }

    @Test
    public void preservesHostHeaderWhenSpecified() {
        otherServiceClient.register(get(urlEqualTo("/host-header")).willReturn(aResponse().withStatus(200)));
        stubFor(get(urlEqualTo("/host-header")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.get("/host-header", withHeader("Host", "my.host"));

        verify(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("my.host")));
        otherServiceClient.verifyThat(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("my.host")));
    }
}
