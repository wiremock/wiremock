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
package com.github.tomakehurst.wiremock.junit5;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.JvmProxyConfigurer;
import com.github.tomakehurst.wiremock.junit.DslWrapper;
import java.util.Optional;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

public class WireMockExtension extends DslWrapper
    implements ParameterResolver,
        BeforeEachCallback,
        BeforeAllCallback,
        AfterEachCallback,
        AfterAllCallback {

  private static final Options DEFAULT_OPTIONS = WireMockConfiguration.options().dynamicPort();

  private final boolean configureStaticDsl;
  private final boolean failOnUnmatchedRequests;

  private Options options;
  private WireMockServer wireMockServer;
  private boolean isNonStatic = false;

  private Boolean proxyMode;

  public WireMockExtension() {
    configureStaticDsl = true;
    failOnUnmatchedRequests = false;
  }

  // Intended to be called from the builder
  protected WireMockExtension(
      Options options,
      boolean configureStaticDsl,
      boolean failOnUnmatchedRequests,
      boolean proxyMode) {
    this.options = options;
    this.configureStaticDsl = configureStaticDsl;
    this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    this.proxyMode = proxyMode;
  }

  public static Builder newInstance() {
    return new Builder();
  }

  @Override
  public boolean supportsParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterIsWireMockRuntimeInfo(parameterContext);
  }

  @Override
  public Object resolveParameter(
      final ParameterContext parameterContext, final ExtensionContext extensionContext)
      throws ParameterResolutionException {

    if (parameterIsWireMockRuntimeInfo(parameterContext)) {
      return new WireMockRuntimeInfo(wireMockServer);
    }

    return null;
  }

  private void startServerIfRequired(ExtensionContext extensionContext) {
    if (wireMockServer == null || !wireMockServer.isRunning()) {
      wireMockServer = new WireMockServer(resolveOptions(extensionContext));
      wireMockServer.start();

      this.admin = wireMockServer;
      this.stubbing = wireMockServer;

      if (configureStaticDsl) {
        WireMock.configureFor(new WireMock(this));
      }
    }
  }

  private void setAdditionalOptions(ExtensionContext extensionContext) {
    if (proxyMode == null) {
      proxyMode =
          extensionContext
              .getElement()
              .flatMap(
                  annotatedElement ->
                      AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class))
              .<Boolean>map(WireMockTest::proxyMode)
              .orElse(false);
    }
  }

  private Options resolveOptions(ExtensionContext extensionContext) {
    return extensionContext
        .getElement()
        .flatMap(
            annotatedElement ->
                AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class))
        .<Options>map(this::buildOptionsFromWireMockTestAnnotation)
        .orElse(Optional.ofNullable(this.options).orElse(DEFAULT_OPTIONS));
  }

  private Options buildOptionsFromWireMockTestAnnotation(WireMockTest annotation) {
    WireMockConfiguration options =
        WireMockConfiguration.options()
            .port(annotation.httpPort())
            .enableBrowserProxying(annotation.proxyMode());

    if (annotation.httpsEnabled()) {
      options.httpsPort(annotation.httpsPort());
    }

    return options;
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
    setAdditionalOptions(context);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    if (wireMockServer == null) {
      isNonStatic = true;
      startServerIfRequired(context);
    } else {
      resetToDefaultMappings();
    }

    setAdditionalOptions(context);

    if (proxyMode) {
      JvmProxyConfigurer.configureFor(wireMockServer);
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

    if (proxyMode) {
      JvmProxyConfigurer.restorePrevious();
    }
  }

  public WireMockRuntimeInfo getRuntimeInfo() {
    return new WireMockRuntimeInfo(wireMockServer);
  }

  public String baseUrl() {
    return wireMockServer.baseUrl();
  }

  public String url(String path) {
    return wireMockServer.url(path);
  }

  public int getHttpsPort() {
    return wireMockServer.httpsPort();
  }

  public int getPort() {
    return wireMockServer.port();
  }

  public static class Builder {

    private Options options = WireMockConfiguration.wireMockConfig().dynamicPort();
    private boolean configureStaticDsl = false;
    private boolean failOnUnmatchedRequests = false;
    private boolean proxyMode = false;

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

    public Builder proxyMode(boolean proxyMode) {
      this.proxyMode = proxyMode;
      return this;
    }

    public WireMockExtension build() {
      if (proxyMode
          && !options.browserProxySettings().enabled()
          && (options instanceof WireMockConfiguration)) {
        ((WireMockConfiguration) options).enableBrowserProxying(true);
      }

      return new WireMockExtension(options, configureStaticDsl, failOnUnmatchedRequests, proxyMode);
    }
  }
}
