package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.jupiter.api.extension.*;

import java.util.List;

public class WireMockExtension extends WireMockServer implements ParameterResolver, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    private final boolean configureStaticDsl;
    private final boolean failOnUnmatchedRequests;

    private boolean isNonStatic = false;

    public WireMockExtension() {
        super(WireMockConfiguration.options().dynamicPort());
        configureStaticDsl = true;
        failOnUnmatchedRequests = false;
    }

    public WireMockExtension(Options options, boolean configureStaticDsl, boolean failOnUnmatchedRequests) {
        super(options);
        this.configureStaticDsl = configureStaticDsl;
        this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    }

    public static Builder newInstance() {
        return new Builder();
    }

    @Override
    public boolean supportsParameter(
            final ParameterContext parameterContext,
            final ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterIsWireMockRuntimeInfo(parameterContext);
    }

    @Override
    public Object resolveParameter(
            final ParameterContext parameterContext,
            final ExtensionContext extensionContext) throws ParameterResolutionException {

        if (parameterIsWireMockRuntimeInfo(parameterContext)) {
            return new WireMockRuntimeInfo(this);
        }

        return null;
    }

    private void startServerIfRequired() {
        if (!isRunning()) {
            start();

            if (configureStaticDsl) {
                WireMock.configureFor(new WireMock(this));
            }
        }
    }

    private void stopServerIfRunning() {
        if (isRunning()) {
            stop();
        }
    }

    private boolean parameterIsWireMockRuntimeInfo(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType().equals(WireMockRuntimeInfo.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        startServerIfRequired();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (!isRunning()) {
            isNonStatic = true;
            startServerIfRequired();
        } else {
            resetToDefaultMappings();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        stopServerIfRunning();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (failOnUnmatchedRequests) {
            checkForUnmatchedRequests();
        }

        if (isNonStatic) {
            stopServerIfRunning();
        }
    }

    public WireMockRuntimeInfo getRuntimeInfo() {
        return new WireMockRuntimeInfo(this);
    }

    public static class Builder {

        private Options options = WireMockConfiguration.wireMockConfig().dynamicPort();
        private boolean configureStaticDsl = false;
        private boolean failOnUnmatchedRequests = false;

        public Builder options(Options options) {
            this.options = options;
            return this;
        }

        public Builder configureStaticDsl(boolean configureStaticDsl) {
            this.configureStaticDsl = configureStaticDsl;
            return this;
        }

        public Builder failOnUnmatchedRequests(boolean failOnUnmatched) {
            this.failOnUnmatchedRequests = failOnUnmatched;
            return this;
        }

        public WireMockExtension build() {
            return new WireMockExtension(options, configureStaticDsl, failOnUnmatchedRequests);
        }
    }
}
