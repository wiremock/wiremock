/*
 * Copyright (C) 2021-2022 Thomas Akehurst
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
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * JUnit Jupiter extension that manages a WireMock server instance's lifecycle and configuration.
 *
 * <p>See http://wiremock.org/docs/junit-jupiter/ for full documentation.
 */
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
  private WireMockRuntimeInfo runtimeInfo;
  private boolean isNonStatic = false;

  private Boolean proxyMode;

  private static final String WIRE_MOCK_SERVER_KEY = "wireMockServer";
  private static final String WIRE_MOCK_RUNTIME_INFO_KEY = "wireMockRuntimeInfo";
  private static final String PROXY_MODE_KEY = "proxyMode";

  public WireMockExtension() {
    configureStaticDsl = true;
    failOnUnmatchedRequests = false;
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
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterEach(WireMockRuntimeInfo wireMockRuntimeInfo) {}

  /**
   * To be overridden in subclasses in order to run code immediately after per-class cleanup of
   * WireMock.
   *
   * @param wireMockRuntimeInfo port numbers, base URLs and HTTPS info for the running WireMock
   *     instance/
   */
  protected void onAfterAll(WireMockRuntimeInfo wireMockRuntimeInfo) {}

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
      Store store = getStore(extensionContext);
      return store.get(WIRE_MOCK_RUNTIME_INFO_KEY, WireMockRuntimeInfo.class);
    }

    return null;
  }

  private Store getStore(ExtensionContext extensionContext) {
    Class<?> clazz =
        getAnnotatedClass(extensionContext)
            .orElseThrow(() -> new NoSuchElementException("No value present"));
    return extensionContext.getStore(Namespace.create(getClass(), clazz));
  }

  private Optional<Class<?>> getAnnotatedClass(ExtensionContext extensionContext) {
    Optional<ExtensionContext> current = Optional.of(extensionContext);
    while (current.isPresent()) {
      Optional<Class<?>> clazz = current.get().getTestClass();
      if (AnnotationUtils.isAnnotated(clazz, WireMockTest.class)) {
        return Optional.of(clazz.get());
      }
      current = current.get().getParent();
    }
    return Optional.empty();
  }

  private Optional<WireMockTest> getWireMockTestAnnotation(ExtensionContext extensionContext) {
    Optional<Class<?>> clazz = getAnnotatedClass(extensionContext);
    if (!clazz.isPresent()) {
      return Optional.empty();
    }

    return AnnotationUtils.findAnnotation(clazz.get(), WireMockTest.class);
  }

  private WireMockServer startServer(Options options) {
    WireMockServer wireMockServer = new WireMockServer(options);
    wireMockServer.start();

    this.wireMockServer = wireMockServer;
    this.admin = wireMockServer;
    this.stubbing = wireMockServer;

    return wireMockServer;
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

  private void stopServerIfRunning(WireMockServer wireMockServer) {
    if (wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  private void startServerIfNotRunning(WireMockServer wireMockServer) {
    if (!wireMockServer.isRunning()) {
      wireMockServer.start();
    }
  }

  private boolean parameterIsWireMockRuntimeInfo(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(WireMockRuntimeInfo.class);
  }

  @Override
  public final void beforeAll(ExtensionContext context) throws Exception {
    Optional<WireMockTest> wireMockTestAnnotation = getWireMockTestAnnotation(context);

    WireMockRuntimeInfo runtimeInfo = null;
    if (wireMockTestAnnotation.isPresent()) {
      Options options =
          wireMockTestAnnotation
              .<Options>map(this::buildOptionsFromWireMockTestAnnotation)
              .orElse(DEFAULT_OPTIONS);

      Store store = getStore(context);
      WireMockServer wireMockServer =
          store.getOrComputeIfAbsent(
              WIRE_MOCK_SERVER_KEY, (key) -> startServer(options), WireMockServer.class);

      runtimeInfo =
          store.getOrComputeIfAbsent(
              WIRE_MOCK_RUNTIME_INFO_KEY,
              (key) -> new WireMockRuntimeInfo(wireMockServer),
              WireMockRuntimeInfo.class);

      boolean proxyMode =
          store.getOrComputeIfAbsent(
              PROXY_MODE_KEY,
              (key) -> wireMockTestAnnotation.<Boolean>map(WireMockTest::proxyMode).orElse(false),
              boolean.class);

      this.options = options;
      this.runtimeInfo = runtimeInfo;
      this.proxyMode = proxyMode;
    } else {
      if (this.wireMockServer == null) {
        Options options = Optional.ofNullable(this.options).orElse(DEFAULT_OPTIONS);
        startServer(options);
        runtimeInfo = new WireMockRuntimeInfo(wireMockServer);
        this.options = options;
        this.runtimeInfo = runtimeInfo;
      } else {
        startServerIfNotRunning(this.wireMockServer);
        runtimeInfo = this.runtimeInfo;
      }
    }

    onBeforeAll(runtimeInfo);
  }

  @Override
  public final void beforeEach(ExtensionContext context) throws Exception {
    Optional<WireMockTest> wireMockTestAnnotation = getWireMockTestAnnotation(context);

    WireMockServer wireMockServer = null;
    WireMockRuntimeInfo runtimeInfo = null;
    boolean proxyMode = false;
    if (wireMockTestAnnotation.isPresent()) {
      Store store = getStore(context);
      wireMockServer = store.get(WIRE_MOCK_SERVER_KEY, WireMockServer.class);
      runtimeInfo = store.get(WIRE_MOCK_RUNTIME_INFO_KEY, WireMockRuntimeInfo.class);
      proxyMode = store.get(PROXY_MODE_KEY, boolean.class);

      startServerIfNotRunning(wireMockServer);
      wireMockServer.resetToDefaultMappings();

      WireMock.configureFor(new WireMock(wireMockServer));
    } else {
      if (this.wireMockServer == null) {
        isNonStatic = true;
        Options options = Optional.ofNullable(this.options).orElse(DEFAULT_OPTIONS);
        wireMockServer = startServer(options);
        runtimeInfo = new WireMockRuntimeInfo(wireMockServer);
        this.options = options;
        this.runtimeInfo = runtimeInfo;
      } else {
        startServerIfNotRunning(this.wireMockServer);
        wireMockServer = this.wireMockServer;
        resetToDefaultMappings();
      }
      proxyMode = this.proxyMode;
      if (configureStaticDsl) {
        WireMock.configureFor(new WireMock(wireMockServer));
      }
    }

    if (proxyMode) {
      JvmProxyConfigurer.configureFor(wireMockServer);
    }

    onBeforeEach(runtimeInfo);
  }

  @Override
  public final void afterAll(ExtensionContext context) throws Exception {
    Optional<WireMockTest> wireMockTestAnnotation = getWireMockTestAnnotation(context);

    WireMockServer wireMockServer = null;
    WireMockRuntimeInfo runtimeInfo = null;

    if (wireMockTestAnnotation.isPresent()) {
      Store store = getStore(context);
      wireMockServer = store.get(WIRE_MOCK_SERVER_KEY, WireMockServer.class);
      runtimeInfo = store.get(WIRE_MOCK_RUNTIME_INFO_KEY, WireMockRuntimeInfo.class);
    } else {
      wireMockServer = this.wireMockServer;
      runtimeInfo = this.runtimeInfo;
    }

    stopServerIfRunning(wireMockServer);

    onAfterAll(runtimeInfo);
  }

  @Override
  public final void afterEach(ExtensionContext context) throws Exception {
    Optional<WireMockTest> wireMockTestAnnotation = getWireMockTestAnnotation(context);

    WireMockServer wireMockServer = null;
    WireMockRuntimeInfo runtimeInfo = null;
    boolean proxyMode = false;

    if (wireMockTestAnnotation.isPresent()) {
      Store store = getStore(context);
      wireMockServer = store.get(WIRE_MOCK_SERVER_KEY, WireMockServer.class);
      runtimeInfo = store.get(WIRE_MOCK_RUNTIME_INFO_KEY, WireMockRuntimeInfo.class);
      proxyMode = store.get(PROXY_MODE_KEY, boolean.class);
    } else {
      wireMockServer = this.wireMockServer;
      runtimeInfo = this.runtimeInfo;
      proxyMode = this.proxyMode;
    }

    if (failOnUnmatchedRequests) {
      wireMockServer.checkForUnmatchedRequests();
    }

    if (isNonStatic) {
      stopServerIfRunning(wireMockServer);
    }

    if (proxyMode) {
      JvmProxyConfigurer.restorePrevious();
    }

    onAfterEach(runtimeInfo);
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
