/*
 * Copyright (C) 2021-2024 Thomas Akehurst
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

/**
 * JUnit Jupiter extension that manages a WireMock server instance's lifecycle and configuration.
 *
 * <p>See <a
 * href="http://wiremock.org/docs/junit-jupiter/">http://wiremock.org/docs/junit-jupiter/</a> for
 * full documentation.
 */
public class WireMockExtension extends DslWrapper
    implements ParameterResolver,
        BeforeEachCallback,
        BeforeAllCallback,
        AfterEachCallback,
        AfterAllCallback {

  private final boolean configureStaticDsl;
  private final boolean failOnUnmatchedRequests;

  private final boolean isDeclarative;

  private Options options;
  private WireMockServer wireMockServer;
  private WireMockRuntimeInfo runtimeInfo;
  private boolean isNonStatic = false;
  private Boolean proxyMode;

  WireMockExtension() {
    configureStaticDsl = true;
    failOnUnmatchedRequests = false;
    isDeclarative = true;
  }

  /**
   * Constructor intended for subclasses.
   *
   * <p>The parameter is a builder so that we can avoid a constructor explosion or
   * backwards-incompatible changes when new options are added.
   *
   * @param builder a {@link com.github.tomakehurst.wiremock.junit5.WireMockExtension.Builder}
   *     instance holding the initialisation parameters for the extension.
   */
  protected WireMockExtension(Builder builder) {
    this.options = builder.options;
    this.configureStaticDsl = builder.configureStaticDsl;
    this.failOnUnmatchedRequests = builder.failOnUnmatchedRequests;
    this.proxyMode = builder.proxyMode;
    this.isDeclarative = false;
  }

  private WireMockExtension(
      Options options,
      boolean configureStaticDsl,
      boolean failOnUnmatchedRequests,
      boolean proxyMode) {
    this.options = options;
    this.configureStaticDsl = configureStaticDsl;
    this.failOnUnmatchedRequests = failOnUnmatchedRequests;
    this.proxyMode = proxyMode;
    this.isDeclarative = false;
  }

  /**
   * Alias for {@link #newInstance()} for use with custom subclasses, with a more relevant name for
   * that use.
   *
   * @return a new {@link com.github.tomakehurst.wiremock.junit5.WireMockExtension.Builder}
   *     instance.
   */
  public static Builder extensionOptions() {
    return newInstance();
  }

  /**
   * Create a new builder for the extension.
   *
   * @return a new {@link com.github.tomakehurst.wiremock.junit5.WireMockExtension.Builder}
   *     instance.
   */
  public static Builder newInstance() {
    return new Builder();
  }

  /**
   * To be overridden in subclasses in order to run code immediately after per-class WireMock setup.
   *
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onBeforeAll(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  /**
   * To be overridden in subclasses in order to run code immediately after per-class WireMock setup.
   *
   * @param extensionContext the current extension context; never {@code null}
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onBeforeAll(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onBeforeAll(wireMockRuntimeInfo);
  }

  /**
   * To be overridden in subclasses in order to run code immediately after per-test WireMock setup.
   *
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onBeforeEach(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  /**
   * To be overridden in subclasses in order to run code immediately after per-test cleanup of
   * WireMock and its associated resources.
   *
   * @param extensionContext the current extension context; never {@code null}
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onBeforeEach(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onBeforeEach(wireMockRuntimeInfo);
  }

  /**
   * To be overridden in subclasses in order to run code immediately after per-test cleanup of
   * WireMock and its associated resources.
   *
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterEach(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  /**
   * To be overridden in subclasses in order to run code immediately after per-class cleanup of
   * WireMock.
   *
   * @param extensionContext the current extension context; never {@code null}
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterEach(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onAfterEach(wireMockRuntimeInfo);
  }

  /**
   * To be overridden in subclasses in order to run code immediately after per-class cleanup of
   * WireMock.
   *
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterAll(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  /**
   * To be overridden in subclasses in order to run code immediately after per-class cleanup of
   * WireMock.
   *
   * @param extensionContext the current extension context; never {@code null}
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterAll(
      ExtensionContext extensionContext, WireMockRuntimeInfo wireMockRuntimeInfo) {
    this.onAfterAll(wireMockRuntimeInfo);
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
      return runtimeInfo;
    }

    return null;
  }

  private void startServerIfRequired(ExtensionContext extensionContext) {
    if (wireMockServer == null || !wireMockServer.isRunning()) {
      wireMockServer = new WireMockServer(resolveOptions(extensionContext));
      wireMockServer.start();

      runtimeInfo = new WireMockRuntimeInfo(wireMockServer);

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
              .map(WireMockTest::proxyMode)
              .orElse(false);
    }
  }

  private Options resolveOptions(ExtensionContext extensionContext) {
    final Options defaultOptions = WireMockConfiguration.options().dynamicPort();
    return extensionContext
        .getElement()
        .flatMap(
            annotatedElement ->
                this.isDeclarative
                    ? AnnotationSupport.findAnnotation(annotatedElement, WireMockTest.class)
                    : Optional.empty())
        .map(this::buildOptionsFromWireMockTestAnnotation)
        .orElseGet(() -> Optional.ofNullable(this.options).orElse(defaultOptions));
  }

  private Options buildOptionsFromWireMockTestAnnotation(WireMockTest annotation) {
    WireMockConfiguration options =
        WireMockConfiguration.options()
            .port(annotation.httpPort())
            .extensionScanningEnabled(annotation.extensionScanningEnabled())
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
    return parameterContext.getParameter().getType().equals(WireMockRuntimeInfo.class)
        && this.isDeclarative;
  }

  @Override
  public final void beforeAll(ExtensionContext context) throws Exception {
    startServerIfRequired(context);
    setAdditionalOptions(context);

    onBeforeAll(context, runtimeInfo);
  }

  @Override
  public final void beforeEach(ExtensionContext context) throws Exception {
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

    onBeforeEach(context, runtimeInfo);
  }

  @Override
  public final void afterAll(ExtensionContext context) throws Exception {
    stopServerIfRunning();

    onAfterAll(context, runtimeInfo);
  }

  @Override
  public final void afterEach(ExtensionContext context) throws Exception {
    if (failOnUnmatchedRequests) {
      wireMockServer.checkForUnmatchedRequests();
    }

    if (isNonStatic) {
      stopServerIfRunning();
    }

    if (proxyMode) {
      JvmProxyConfigurer.restorePrevious();
    }

    onAfterEach(context, runtimeInfo);
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
