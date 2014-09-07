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
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProxyAcceptanceTest {

	WireMockServer targetService;
	WireMock targetServiceAdmin;

    WireMockServer proxyingService;
    WireMock proxyingServiceAdmin;

    WireMockTestClient testClient;
	
	void init(Options proxyingServiceOptions) {
		targetService = new WireMockServer(8087);
		targetService.start();
		targetServiceAdmin = new WireMock("localhost", 8087);

        proxyingService = new WireMockServer(proxyingServiceOptions);
        proxyingService.start();
        proxyingServiceAdmin = new WireMock();
        testClient = new WireMockTestClient();
        WireMock.configure();
	}

    void initWithDefaultConfig() {
        init(wireMockConfig());
    }
	
	@After
	public void stop() {
		targetService.stop();
        proxyingService.stop();
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxy() {
        initWithDefaultConfig();

		targetServiceAdmin.register(get(urlEqualTo("/proxied/resource?param=value"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Proxied content")));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource?param=value")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.get("/proxied/resource?param=value");
		
		assertThat(response.content(), is("Proxied content"));
		assertThat(response.header("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void successfullyPostsResponseToOtherServiceViaProxy() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/proxied/resource"))
                .willReturn(aResponse()
                        .withStatus(204)));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.postWithBody("/proxied/resource", "Post content", "text/plain", "utf-8");
		
		assertThat(response.statusCode(), is(204));
		targetServiceAdmin.verifyThat(postRequestedFor(urlEqualTo("/proxied/resource")).withRequestBody(matching("Post content")));
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxyWithEscapeCharsInUrl() {
        initWithDefaultConfig();

        targetServiceAdmin.register(get(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26"))
                .willReturn(aResponse()
                        .withStatus(200)));

        proxyingServiceAdmin.register(any(urlEqualTo("/%26%26The%20Lord%20of%20the%20Rings%26%26")).atPriority(10)
                .willReturn(aResponse()
                        .proxiedFrom("http://localhost:8087")));
		
		WireMockResponse response = testClient.get("/%26%26The%20Lord%20of%20the%20Rings%26%26");
		
		assertThat(response.statusCode(), is(200));
	}

    @Test
    public void sendsContentLengthHeaderWhenPostingIfPresentInOriginalRequest() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/with/length")).willReturn(aResponse().withStatus(201)));
        proxyingServiceAdmin.register(post(urlEqualTo("/with/length")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.postWithBody("/with/length", "TEST", "application/x-www-form-urlencoded", "utf-8");

        targetServiceAdmin.verifyThat(postRequestedFor(urlEqualTo("/with/length")).withHeader("Content-Length", equalTo("4")));
    }

    @Test
    public void sendsTransferEncodingChunkedWhenPostingIfPresentInOriginalRequest() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/chunked")).willReturn(aResponse().withStatus(201)));
        proxyingServiceAdmin.register(post(urlEqualTo("/chunked")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.postWithChunkedBody("/chunked", "TEST".getBytes());

        targetServiceAdmin.verifyThat(postRequestedFor(urlEqualTo("/chunked"))
                .withHeader("Transfer-Encoding", equalTo("chunked")));
    }

    @Test
    public void preservesHostHeaderWhenSpecified() {
        init(wireMockConfig().preserveHostHeader(true));

        targetServiceAdmin.register(get(urlEqualTo("/preserve-host-header")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/preserve-host-header")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.get("/preserve-host-header", withHeader("Host", "my.host"));

        proxyingServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/preserve-host-header")).withHeader("Host", equalTo("my.host")));
        targetServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/preserve-host-header")).withHeader("Host", equalTo("my.host")));
    }

    @Test
    public void usesProxyUrlBasedHostHeaderWhenPreserveHostHeaderNotSpecified() {
        init(wireMockConfig().preserveHostHeader(false));

        targetServiceAdmin.register(get(urlEqualTo("/host-header")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/host-header")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.get("/host-header", withHeader("Host", "my.host"));

        proxyingServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("my.host")));
        targetServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("localhost")));
    }

    @Test
    public void proxiesPatchRequestsWithBody() {
        initWithDefaultConfig();

        targetServiceAdmin.register(patch(urlEqualTo("/patch")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(patch(urlEqualTo("/patch")).willReturn(aResponse().proxiedFrom("http://localhost:8087")));

        testClient.patchWithBody("/patch", "Patch body", "text/plain", "utf-8");

        targetServiceAdmin.verifyThat(patchRequestedFor(urlEqualTo("/patch")).withRequestBody(equalTo("Patch body")));
    }
}
