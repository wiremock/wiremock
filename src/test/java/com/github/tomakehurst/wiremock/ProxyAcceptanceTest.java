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
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.testsupport.TestHttpHeader;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.After;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static com.google.common.collect.Iterables.getLast;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ProxyAcceptanceTest {

    private static final int TARGET_SERVICE_PORT = 8087;
    private static final int TARGET_SERVICE_HTTPS_PORT = 8487;
    private static final String TARGET_SERVICE_BASE_URL = "http://localhost:" + TARGET_SERVICE_PORT;
    private static final String TARGET_SERVICE_BASE_HTTPS_URL = "https://localhost:" + TARGET_SERVICE_HTTPS_PORT;

    WireMockServer targetService;
	WireMock targetServiceAdmin;

    WireMockServer proxyingService;
    WireMock proxyingServiceAdmin;

    WireMockTestClient testClient;

	void init(Options proxyingServiceOptions) {
		targetService = new WireMockServer(wireMockConfig().port(TARGET_SERVICE_PORT).httpsPort(TARGET_SERVICE_HTTPS_PORT));
		targetService.start();
		targetServiceAdmin = new WireMock("localhost", TARGET_SERVICE_PORT);

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
	
	public void successfullyGetsResponseFromOtherServiceViaProxy() {
        initWithDefaultConfig();

		targetServiceAdmin.register(get(urlEqualTo("/proxied/resource?param=value"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Proxied content")));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource?param=value")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom(TARGET_SERVICE_BASE_URL)));
		
		WireMockResponse response = testClient.get("/proxied/resource?param=value");
		
		assertThat(response.content(), is("Proxied content"));
		assertThat(response.firstHeader("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxyInjectingHeaders() {
        initWithDefaultConfig();

		targetServiceAdmin.register(get(urlEqualTo("/proxied/resource?param=value"))
				.withHeader("a", equalTo("b"))
				.withHeader("c", equalTo("d"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Proxied content")));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource?param=value")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom(TARGET_SERVICE_BASE_URL)
				.withInjectedHeader("a", "b")
				.withInjectedHeader("c", "d")));
		
		WireMockResponse response = testClient.get("/proxied/resource?param=value");
		
		assertThat(response.content(), is("Proxied content"));
	}
	
	@Test
	public void successfullyGetsResponseFromOtherServiceViaProxyInjectingHeadersOverridingSentHeaders() {
        initWithDefaultConfig();

		targetServiceAdmin.register(get(urlEqualTo("/proxied/resource?param=value"))
				.withHeader("a", equalTo("b"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Proxied content")));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource?param=value")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom(TARGET_SERVICE_BASE_URL)
				.withInjectedHeader("a", "b")));
		
		WireMockResponse response = testClient.get("/proxied/resource?param=value", 
				TestHttpHeader.withHeader("a", "doh"));
		
		assertThat(response.content(), is("Proxied content"));
	}
	
	@Test
	public void successfullyPostsResponseToOtherServiceViaProxy() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/proxied/resource"))
                .willReturn(aResponse()
                        .withStatus(204)));

        proxyingServiceAdmin.register(any(urlEqualTo("/proxied/resource")).atPriority(10)
				.willReturn(aResponse()
				.proxiedFrom(TARGET_SERVICE_BASE_URL)));
		
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
                        .proxiedFrom(TARGET_SERVICE_BASE_URL)));
		
		WireMockResponse response = testClient.get("/%26%26The%20Lord%20of%20the%20Rings%26%26");
		
		assertThat(response.statusCode(), is(200));
	}

    @Test
    public void sendsContentLengthHeaderWhenPostingIfPresentInOriginalRequest() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/with/length")).willReturn(aResponse().withStatus(201)));
        proxyingServiceAdmin.register(post(urlEqualTo("/with/length")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.postWithBody("/with/length", "TEST", "application/x-www-form-urlencoded", "utf-8");

        targetServiceAdmin.verifyThat(postRequestedFor(urlEqualTo("/with/length")).withHeader("Content-Length", equalTo("4")));
    }

    @Test
    public void sendsTransferEncodingChunkedWhenPostingIfPresentInOriginalRequest() {
        initWithDefaultConfig();

        targetServiceAdmin.register(post(urlEqualTo("/chunked")).willReturn(aResponse().withStatus(201)));
        proxyingServiceAdmin.register(post(urlEqualTo("/chunked")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.postWithChunkedBody("/chunked", "TEST".getBytes());

        targetServiceAdmin.verifyThat(postRequestedFor(urlEqualTo("/chunked"))
                .withHeader("Transfer-Encoding", equalTo("chunked")));
    }

    @Test
    public void preservesHostHeaderWhenSpecified() {
        init(wireMockConfig().preserveHostHeader(true));

        targetServiceAdmin.register(get(urlEqualTo("/preserve-host-header")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/preserve-host-header")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.get("/preserve-host-header", withHeader("Host", "my.host"));

        proxyingServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/preserve-host-header")).withHeader("Host", equalTo("my.host")));
        targetServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/preserve-host-header")).withHeader("Host", equalTo("my.host")));
    }

    @Test
    public void usesProxyUrlBasedHostHeaderWhenPreserveHostHeaderNotSpecified() {
        init(wireMockConfig().preserveHostHeader(false));

        targetServiceAdmin.register(get(urlEqualTo("/host-header")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/host-header")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.get("/host-header", withHeader("Host", "my.host"));

        proxyingServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("my.host")));
        targetServiceAdmin.verifyThat(getRequestedFor(urlEqualTo("/host-header")).withHeader("Host", equalTo("localhost")));
    }

    @Test
    public void proxiesPatchRequestsWithBody() {
        initWithDefaultConfig();

        targetServiceAdmin.register(patch(urlEqualTo("/patch")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(patch(urlEqualTo("/patch")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.patchWithBody("/patch", "Patch body", "text/plain", "utf-8");

        targetServiceAdmin.verifyThat(patchRequestedFor(urlEqualTo("/patch")).withRequestBody(equalTo("Patch body")));
    }

    @Test
    public void addsSpecifiedHeadersToResponse() {
        initWithDefaultConfig();

        targetServiceAdmin.register(get(urlEqualTo("/extra/headers"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Proxied content")));

        proxyingServiceAdmin.register(any(urlEqualTo("/extra/headers"))
                .willReturn(aResponse()
                        .withHeader("X-Additional-Header", "Yep")
                        .proxiedFrom(TARGET_SERVICE_BASE_URL)));

        WireMockResponse response = testClient.get("/extra/headers");

        assertThat(response.firstHeader("Content-Type"), is("text/plain"));
        assertThat(response.firstHeader("X-Additional-Header"), is("Yep"));
    }

    @Test
    public void doesNotDuplicateCookieHeaders() {
        initWithDefaultConfig();

        targetServiceAdmin.register(get(urlEqualTo("/duplicate/cookies"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Set-Cookie", "session=1234")));
        proxyingServiceAdmin.register(get(urlEqualTo("/duplicate/cookies")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.get("/duplicate/cookies");
        testClient.get("/duplicate/cookies", withHeader("Cookie", "session=1234"));

        LoggedRequest lastRequest = getLast(targetServiceAdmin.find(getRequestedFor(urlEqualTo("/duplicate/cookies"))));
        assertThat(lastRequest.getHeaders().getHeader("Cookie").values().size(), is(1));
    }

    //TODO: This is passing even when it probably shouldn't - investigate
    @Test
    public void doesNotDuplicateConnectionHeader() {
        initWithDefaultConfig();
        targetServiceAdmin.register(get(urlEqualTo("/duplicate/connection-header")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/duplicate/connection-header")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        testClient.get("/duplicate/connection-header");
        LoggedRequest lastRequest = getLast(targetServiceAdmin.find(getRequestedFor(urlEqualTo("/duplicate/connection-header"))));
        assertThat(lastRequest.getHeaders().getHeader("Connection").values().size(), is(1));
    }

    @Test
    public void acceptsSelfSignedSslCertFromProxyTarget() {
        initWithDefaultConfig();
        targetServiceAdmin.register(get(urlEqualTo("/ssl-cert")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/ssl-cert")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_HTTPS_URL)));

        assertThat(testClient.get("/ssl-cert").statusCode(), is(200));
    }

    @Test
    public void canProxyViaAForwardProxy() {
        WireMockServer forwardProxy = new WireMockServer(wireMockConfig().port(8187).enableBrowserProxying(true));
        forwardProxy.start();
        init(wireMockConfig().proxyVia(new ProxySettings("localhost", 8187)));

        targetServiceAdmin.register(get(urlEqualTo("/proxy-via")).willReturn(aResponse().withStatus(200)));
        proxyingServiceAdmin.register(get(urlEqualTo("/proxy-via")).willReturn(aResponse().proxiedFrom(TARGET_SERVICE_BASE_URL)));

        assertThat(testClient.get("/proxy-via").statusCode(), is(200));
    }
}
