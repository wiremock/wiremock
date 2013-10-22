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
package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockRule implements MethodRule, TestRule, Stubbing {

    private final Options options;
    private final WireMock wireMock;

    private WireMockServer wireMockServer;

    public WireMockRule(Options options) {
        this.options = options;
        this.wireMock = new WireMock("localhost", options.portNumber());
    }

    public WireMockRule(int port) {
		this(wireMockConfig().port(port));
	}

    public WireMockRule(int port, Integer httpsPort) {
        this(wireMockConfig().port(port).httpsPort(httpsPort));
    }
	
	public WireMockRule() {
		this(wireMockConfig());
	}

    @Override
    public Statement apply(final Statement base, Description description) {
        return apply(base, null, null);
    }

	@Override
	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				wireMockServer = new WireMockServer(options);
				wireMockServer.start();
				WireMock.configureFor("localhost", options.portNumber());
				try {
                    base.evaluate();
                } finally {
                    wireMockServer.stop();
                }
			}
			
		};
	}

    @Override
    public void givenThat(MappingBuilder mappingBuilder) {
        wireMock.register(mappingBuilder);
    }

    @Override
    public void stubFor(MappingBuilder mappingBuilder) {
        givenThat(mappingBuilder);
    }

    @Override
    public void verify(RequestPatternBuilder requestPatternBuilder) {
        wireMock.verifyThat(requestPatternBuilder);
    }

    @Override
    public void verify(int count, RequestPatternBuilder requestPatternBuilder) {
        wireMock.verifyThat(count, requestPatternBuilder);
    }

    @Override
    public List<LoggedRequest> findAll(RequestPatternBuilder requestPatternBuilder) {
        return wireMock.find(requestPatternBuilder);
    }

    @Override
    public void setGlobalFixedDelay(int milliseconds) {
        wireMock.setGlobalFixedDelayVariable(milliseconds);
    }

    @Override
    public void addRequestProcessingDelay(int milliseconds) {
        wireMock.addDelayBeforeProcessingRequests(milliseconds);
    }

    public int port() {
        return wireMockServer.port();
    }

    public int httpsPort() {
        return wireMockServer.httpsPort();
    }
}
