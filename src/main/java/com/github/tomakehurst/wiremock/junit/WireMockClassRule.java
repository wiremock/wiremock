package com.github.tomakehurst.wiremock.junit;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockClassRule implements MethodRule, TestRule {

    private final Options options;
    private final WireMockServer wireMockServer;

    public WireMockClassRule(Options options) {
        this.options = options;
        this.wireMockServer = new WireMockServer(options);
    }

    public WireMockClassRule(int port, Integer httpsPort) {
        this(wireMockConfig().port(port).httpsPort(httpsPort));
    }

    public WireMockClassRule(int port) {
        this(wireMockConfig().port(port));
    }

    public WireMockClassRule() {
        this(wireMockConfig());
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
                    WireMock.configureFor("localhost", options.portNumber());
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
