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
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class WireMockRule implements MethodRule {
	
	private final int port;
    private final Integer httpsPort;
	
	public WireMockRule(int port) {
		this(port, null);
	}

    public WireMockRule(int port, Integer httpsPort) {
        this.port = port;
        this.httpsPort = httpsPort;
    }
	
	public WireMockRule() {
		this(WireMockServer.DEFAULT_PORT);
	}

	@Override
	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				WireMockServer wireMockServer = new WireMockServer(port, httpsPort);
				wireMockServer.start();
				WireMock.configureFor("localhost", port);
				try {
                    base.evaluate();
                } finally {
                    wireMockServer.stop();
                }
			}
			
		};
	}

}
