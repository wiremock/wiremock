package com.github.tomakehurst.wiremock.junit;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class WireMockStaticRule implements MethodRule {

	private final WireMockServer wireMockServer;
	
	public WireMockStaticRule(int port) {
		wireMockServer = new WireMockServer(port);
		wireMockServer.start();
		WireMock.configureFor("localhost", port);
	}
	
	public WireMockStaticRule() {
		this(WireMockServer.DEFAULT_PORT);
	}
	
	public void stopServer() {
		wireMockServer.stop();
	}

	@Override
	public Statement apply(final Statement base, FrameworkMethod method, Object target) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				base.evaluate();
				WireMock.reset();
			}
			
		};
	}
}
