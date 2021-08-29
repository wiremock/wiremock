package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.DslWrapper;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;

public class WireMockExtension extends DslWrapper implements ParameterResolver, BeforeEachCallback, BeforeAllCallback, AfterEachCallback, AfterAllCallback {

    private static final Options DEFAULT_OPTIONS = WireMockConfiguration.options().dynamicPort();

    private final boolean configureStaticDsl;
    private final boolean failOnUnmatchedRequests;

    private Options options;
    private WireMockServer wireMockServer;
    private boolean isNonStatic = false;

    public WireMockExtension() {
        configureStaticDsl = true;
        failOnUnmatchedRequests = false;
    }

    public WireMockExtension(Options options, boolean configureStaticDsl, boolean failOnUnmatchedRequests) {
        this.options = options;
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
            return new WireMockRuntimeInfo(wireMockServer);
        }

        return null;
    }

    private void startServerIfRequired(ExtensionContext extensionContext) {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(resolveOptions(extensionContext));
            wireMockServer.start();

            this.admin = wireMockServer;
            this.stubbing = wireMockServer;

            if (configureStaticDsl) {
                WireMock.configureFor(new WireMock(this));
            }
        }
    }

    private Options resolveOptions(ExtensionContext extensionContext) {
        return extensionContext.getElement()
                .flatMap(annotatedElement -> AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class))
                .<Options>map(annotation -> WireMockConfiguration.options().port(annotation.httpPort()))
                .orElse(Optional.ofNullable(this.options)
                                .orElse(DEFAULT_OPTIONS));
    }

    private void stopServerIfRunning() {
        if (wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    private boolean parameterIsWireMockRuntimeInfo(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType().equals(WireMockRuntimeInfo.class);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        startServerIfRequired(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (wireMockServer == null) {
            isNonStatic = true;
            startServerIfRequired(context);
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
            wireMockServer.checkForUnmatchedRequests();
        }

        if (isNonStatic) {
            stopServerIfRunning();
        }
    }

    public WireMockRuntimeInfo getRuntimeInfo() {
        return new WireMockRuntimeInfo(wireMockServer);
    }

    public String baseUrl() {
        return wireMockServer.baseUrl();
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
