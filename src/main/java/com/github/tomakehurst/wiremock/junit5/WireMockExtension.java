package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.*;

public class WireMockExtension extends WireMockServer implements ParameterResolver, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    private final boolean configureStaticDsl;

    private boolean isNonStatic = false;

    public WireMockExtension() {
        super(WireMockConfiguration.options().dynamicPort());
        configureStaticDsl = true;
    }

    public WireMockExtension(Options options, boolean configureStaticDsl) {
        super(options);
        this.configureStaticDsl = configureStaticDsl;
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

        public Builder options(Options options) {
            this.options = options;
            return this;
        }

        public Builder configureStaticDsl(boolean configureStaticDsl) {
            this.configureStaticDsl = configureStaticDsl;
            return this;
        }

        public WireMockExtension build() {
            return new WireMockExtension(options, configureStaticDsl);
        }
    }
}
