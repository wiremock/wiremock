package com.github.tomakehurst.wiremock.junit;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class WireMockClassRule implements MethodRule, TestRule {

    private int port;
    private final WireMockServer wireMockServer;

    public WireMockClassRule(int port) {
        this.port = port;
        this.wireMockServer = new WireMockServer(port);
    }

    public WireMockClassRule() {
        this(WireMockServer.DEFAULT_PORT);
    }

    @Override
    public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        return apply(base, null);
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                if (wireMockServer.isRunning()) {
                    try {
                        base.evaluate();
                    } finally {
                        WireMock.reset();
                    }
                } else {
                    wireMockServer.start();
                    WireMock.configureFor("localhost", port);
                    try {
                        base.evaluate();
                    } finally {
                        wireMockServer.stop();
                    }
                }
            }

        };
    }

}
