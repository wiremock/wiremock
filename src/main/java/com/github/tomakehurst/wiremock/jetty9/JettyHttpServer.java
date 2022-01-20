/*
 * Copyright (C) 2014-2021 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty9;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.jetty9.websockets.WebSocketEndpoint;
import com.github.tomakehurst.wiremock.servlet.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.concurrent.ScheduledExecutorService;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public class JettyHttpServer implements HttpServer {
  private static final String FILES_URL_MATCH = String.format("/%s/*", WireMockApp.FILES_ROOT);
  private static final String[] GZIPPABLE_METHODS = new String[]{"POST", "PUT", "PATCH", "DELETE"};
  private static final int DEFAULT_ACCEPTORS = 3;
  private static final int DEFAULT_HEADER_SIZE = 8192;
  private static final MutableBoolean STRICT_HTTP_HEADERS_APPLIED = new MutableBoolean(false);

  private final Server jettyServer;
  private final ServerConnector httpConnector;
  private final ServerConnector httpsConnector;

  private ScheduledExecutorService scheduledExecutorService;

  public JettyHttpServer(
      final Options options,
            final AdminRequestHandler adminRequestHandler,
            final StubRequestHandler stubRequestHandler) {
    if (!options.getDisableStrictHttpHeaders() && STRICT_HTTP_HEADERS_APPLIED.isFalse()) {
      System.setProperty("org.eclipse.jetty.http.HttpGenerator.STRICT", String.valueOf(true));
      STRICT_HTTP_HEADERS_APPLIED.setTrue();
    }

    this.jettyServer = this.createServer(options);

    NetworkTrafficListenerAdapter networkTrafficListenerAdapter =
        new NetworkTrafficListenerAdapter(options.networkTrafficListener());

    if (options.getHttpDisabled()) {
      httpConnector = null;
    } else {
      httpConnector =
          createHttpConnector(
              options.bindAddress(),
              options.portNumber(),
              options.jettySettings(),
              networkTrafficListenerAdapter);
      jettyServer.addConnector(httpConnector);
    }

    if (options.httpsSettings().enabled()) {
      this.httpsConnector =
          this.createHttpsConnector(
              jettyServer,
              options.bindAddress(),
              options.httpsSettings(),
              options.jettySettings(),
              networkTrafficListenerAdapter);
      this.jettyServer.addConnector(this.httpsConnector);
    } else {
      this.httpsConnector = null;
    }

    applyAdditionalServerConfiguration(jettyServer, options);

    final HandlerCollection handlers =
        createHandler(options, adminRequestHandler, stubRequestHandler);
    jettyServer.setHandler(handlers);

    this.finalizeSetup(options);
  }

  protected void applyAdditionalServerConfiguration(Server jettyServer, Options options) {
    }

  protected HandlerCollection createHandler(
      final Options options, final AdminRequestHandler adminRequestHandler,
                                              final StubRequestHandler stubRequestHandler) {
    final Notifier notifier = options.notifier();
        final ServletContextHandler adminContext = this.addAdminContext(adminRequestHandler, notifier);
    final ServletContextHandler mockServiceContext = this.addMockServiceContext(
            stubRequestHandler,
            options.filesRoot(),
            options.getAsynchronousResponseSettings(),
            options.getChunkedEncodingPolicy(),
            options.getStubCorsEnabled(),
            options.browserProxySettings().enabled(),
            notifier);

    final HandlerCollection handlers = new HandlerCollection();
    AbstractHandler asyncTimeoutSettingHandler =
        new AbstractHandler() {
          @Override
          public void handle(
              final String target,
              final Request baseRequest,
              final HttpServletRequest request,
              final HttpServletResponse response) {
            baseRequest.getHttpChannel().getState().setTimeout(options.timeout());
          }
        };
    handlers.setHandlers(
        ArrayUtils.addAll(this.extensionHandlers(), adminContext, asyncTimeoutSettingHandler));

    if (options.getGzipDisabled()) {
      handlers.addHandler(mockServiceContext);
    } else {
      addGZipHandler(mockServiceContext, handlers);
    }

    return handlers;
  }

  private void addGZipHandler(
      final ServletContextHandler mockServiceContext, final HandlerCollection handlers) {
        Class<?> gzipHandlerClass = null;

    try {
      gzipHandlerClass = Class.forName("org.eclipse.jetty.servlets.gzip.GzipHandler");
    } catch (final ClassNotFoundException e) {
      try {
        gzipHandlerClass = Class.forName("org.eclipse.jetty.server.handler.gzip.GzipHandler");
      } catch (final ClassNotFoundException e1) {
        throwUnchecked(e1);
      }
    }

    try {
      final HandlerWrapper gzipWrapper = (HandlerWrapper) gzipHandlerClass.getDeclaredConstructor().newInstance();
      setGZippableMethods(gzipWrapper, gzipHandlerClass);
      gzipWrapper.setHandler(mockServiceContext);
      handlers.addHandler(gzipWrapper);
    } catch (final Exception e) {
            throwUnchecked(e);
        }
    }

    private static void setGZippableMethods(HandlerWrapper gzipHandler, Class<?> gzipHandlerClass) {
        try {
            Method addIncludedMethods = gzipHandlerClass.getDeclaredMethod("addIncludedMethods", String[].class);
            addIncludedMethods.invoke(gzipHandler, new Object[]{GZIPPABLE_METHODS});
        } catch (Exception ignored) {

    }
  }

  protected void finalizeSetup(final Options options) {
        if (!options.jettySettings().getStopTimeout().isPresent()) {
            this.jettyServer.setStopTimeout(1000);
    }
  }

  protected Server createServer(final Options options) {
    final Server server = new Server(options.threadPoolFactory().buildThreadPool(options));
    final JettySettings jettySettings = options.jettySettings();
    final Optional<Long> stopTimeout = jettySettings.getStopTimeout();
    if (stopTimeout.isPresent()) {
      server.setStopTimeout(stopTimeout.get());
    }
    return server;
  }

  /** Extend only this method if you want to add additional handlers to Jetty. */
  protected Handler[] extensionHandlers() {
    return new Handler[] {};
  }

  @Override
  public void start() {
    try {
      this.jettyServer.start();
    } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final long timeout = System.currentTimeMillis() + 30000;
        while (!this.jettyServer.isStarted()) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
                // no-op
            }
            if (System.currentTimeMillis() > timeout) {
                throw new RuntimeException("Server took too long to start up.");
            }
        }
    }

  @Override
  public void stop() {
    try {
      if (scheduledExecutorService != null) {
        scheduledExecutorService.shutdown();
      }

      jettyServer.stop();
      jettyServer.join();
    } catch (Exception e) {
      throwUnchecked(e);
    }
  }

  @Override
  public boolean isRunning() {
    return this.jettyServer.isRunning();
  }

  @Override
  public int port() {
    return this.httpConnector.getLocalPort();
  }

  @Override
  public int httpsPort() {
    return this.httpsConnector.getLocalPort();
  }

  public long stopTimeout() {
    return this.jettyServer.getStopTimeout();
  }

  protected ServerConnector createHttpConnector(
      final String bindAddress,
            final int port,
            final JettySettings jettySettings,
            final NetworkTrafficListener listener) {

    final HttpConfiguration httpConfig = this.createHttpConfig(jettySettings);

    final ServerConnector connector = this.createServerConnector(
            bindAddress, jettySettings, port, listener, new HttpConnectionFactory(httpConfig));

    return connector;
  }

  protected ServerConnector createHttpsConnector(
      Server server,
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener) {

    // Added to support Android https communication.
    SslContextFactory sslContextFactory = buildSslContextFactory();

    sslContextFactory.setKeyStorePath(httpsSettings.keyStorePath());
    sslContextFactory.setKeyStorePassword(httpsSettings.keyStorePassword());
    sslContextFactory.setKeyManagerPassword(httpsSettings.keyManagerPassword());
    sslContextFactory.setKeyStoreType(httpsSettings.keyStoreType());
    if (httpsSettings.hasTrustStore()) {
      sslContextFactory.setTrustStorePath(httpsSettings.trustStorePath());
      sslContextFactory.setTrustStorePassword(httpsSettings.trustStorePassword());
    }
    sslContextFactory.setNeedClientAuth(httpsSettings.needClientAuth());

    final HttpConfiguration httpConfig = this.createHttpConfig(jettySettings);
    httpConfig.addCustomizer(new SecureRequestCustomizer());

    final int port = httpsSettings.port();

    HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
    SslConnectionFactory sslConnectionFactory =
        new SslConnectionFactory(sslContextFactory, "http/1.1");
    ConnectionFactory[] connectionFactories =
        ArrayUtils.addAll(
            new ConnectionFactory[]{sslConnectionFactory, httpConnectionFactory},
            buildAdditionalConnectionFactories(
                httpsSettings, httpConnectionFactory, sslConnectionFactory));

    return this.createServerConnector(bindAddress, jettySettings, port, listener, connectionFactories);
  }

  protected ConnectionFactory[] buildAdditionalConnectionFactories(
      HttpsSettings httpsSettings,
      HttpConnectionFactory httpConnectionFactory,
      SslConnectionFactory sslConnectionFactory) {
    return new ConnectionFactory[]{};
  }

  // Override this for platform-specific impls
  protected SslContextFactory buildSslContextFactory() {
    return new SslContextFactory();
  }

  protected HttpConfiguration createHttpConfig(final JettySettings jettySettings) {
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setRequestHeaderSize(
                jettySettings.getRequestHeaderSize().or(DEFAULT_HEADER_SIZE));
    httpConfig.setResponseHeaderSize(jettySettings.getResponseHeaderSize().or(DEFAULT_HEADER_SIZE));
    httpConfig.setSendDateHeader(false);
    return httpConfig;
  }

  protected ServerConnector createServerConnector(
      final String bindAddress,
                                                    final JettySettings jettySettings,
                                                    final int port, final NetworkTrafficListener listener,
      final ConnectionFactory... connectionFactories) {
        final int acceptors = jettySettings.getAcceptors().or(DEFAULT_ACCEPTORS);
    final NetworkTrafficServerConnector connector = new NetworkTrafficServerConnector(
                this.jettyServer, null, null, null, acceptors, 2, connectionFactories);

    connector.setPort(port);
    connector.addNetworkTrafficListener(listener);
    this.setJettySettings(jettySettings, connector);
    connector.setHost(bindAddress);
    return connector;
  }

  private void setJettySettings(final JettySettings jettySettings, final ServerConnector connector) {
    if (jettySettings.getAcceptQueueSize().isPresent()) {
      connector.setAcceptQueueSize(jettySettings.getAcceptQueueSize().get());
    }

    if (jettySettings.getIdleTimeout().isPresent()) {
      connector.setIdleTimeout(jettySettings.getIdleTimeout().get());
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ServletContextHandler addMockServiceContext(
      StubRequestHandler stubRequestHandler,
      FileSource fileSource,
      AsynchronousResponseSettings asynchronousResponseSettings,
      Options.ChunkedEncodingPolicy chunkedEncodingPolicy,
      boolean stubCorsEnabled,
      boolean browserProxyingEnabled,
      Notifier notifier) {
    final ServletContextHandler mockServiceContext = new ServletContextHandler(this.jettyServer, "/");

    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");
    mockServiceContext.setInitParameter(
        "org.eclipse.jetty.servlet.Default.resourceBase", fileSource.getPath());
    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    mockServiceContext.addServlet(DefaultServlet.class, JettyHttpServer.FILES_URL_MATCH);

    mockServiceContext.setAttribute(
        JettyFaultInjectorFactory.class.getName(), new JettyFaultInjectorFactory());
    mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
    mockServiceContext.setAttribute(Notifier.KEY, notifier);
    mockServiceContext.setAttribute(
        Options.ChunkedEncodingPolicy.class.getName(), chunkedEncodingPolicy);
    mockServiceContext.setAttribute("browserProxyingEnabled", browserProxyingEnabled);
    final ServletHolder servletHolder = mockServiceContext.addServlet(WireMockHandlerDispatchingServlet.class, "/");
    servletHolder.setInitOrder(1);
    servletHolder.setInitParameter(
        RequestHandler.HANDLER_CLASS_KEY, StubRequestHandler.class.getName());
    servletHolder.setInitParameter(
        FaultInjectorFactory.INJECTOR_CLASS_KEY, JettyFaultInjectorFactory.class.getName());
    servletHolder.setInitParameter(
        WireMockHandlerDispatchingServlet.SHOULD_FORWARD_TO_FILES_CONTEXT, "true");

    if (asynchronousResponseSettings.isEnabled()) {
      scheduledExecutorService = newScheduledThreadPool(asynchronousResponseSettings.getThreads());
      mockServiceContext.setAttribute(
          WireMockHandlerDispatchingServlet.ASYNCHRONOUS_RESPONSE_EXECUTOR,
          scheduledExecutorService);
    }

    mockServiceContext.setAttribute(
        MultipartRequestConfigurer.KEY, buildMultipartRequestConfigurer());

    final MimeTypes mimeTypes = new MimeTypes();
        mimeTypes.addMimeMapping("json", "application/json");
        mimeTypes.addMimeMapping("html", "text/html");
        mimeTypes.addMimeMapping("xml", "application/xml");
        mimeTypes.addMimeMapping("txt", "text/plain");
        mockServiceContext.setMimeTypes(mimeTypes);
        mockServiceContext.setWelcomeFiles(new String[]{"index.json", "index.html", "index.xml", "index.txt"});

    NotFoundHandler errorHandler = new NotFoundHandler(mockServiceContext);
    mockServiceContext.setErrorHandler(errorHandler);

    mockServiceContext.addFilter(
        ContentTypeSettingFilter.class, JettyHttpServer.FILES_URL_MATCH, EnumSet.of(DispatcherType.FORWARD));
    mockServiceContext.addFilter(
        TrailingSlashFilter.class, JettyHttpServer.FILES_URL_MATCH, EnumSet.allOf(DispatcherType.class));

    if (stubCorsEnabled) {
      addCorsFilter(mockServiceContext);
    }

    return mockServiceContext;
  }

  private ServletContextHandler addAdminContext(
      final AdminRequestHandler adminRequestHandler,
            final Notifier notifier) {
    final ServletContextHandler adminContext = new ServletContextHandler(this.jettyServer, ADMIN_CONTEXT_ROOT);

    adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");

    final String javaVendor = System.getProperty("java.vendor");
        if (javaVendor != null && javaVendor.toLowerCase().contains("android")) {
            //Special case for Android, fixes IllegalArgumentException("resource assets not found."):
            //  The Android ClassLoader apparently does not resolve directories.
            //  Furthermore, lib assets will be merged into a single asset directory when a jar file is// assimilated into an apk.
      //  As resources can be addressed like "assets/swagger-ui/index.html", a static path element
      // will suffice.
      adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase", "assets");
    } else {
      adminContext.setInitParameter(
          "org.eclipse.jetty.servlet.Default.resourceBase",
          Resources.getResource("assets").toString());
    }

    Resources.getResource("assets/swagger-ui/index.html");

    adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    ServletHolder swaggerUiServletHolder =
        adminContext.addServlet(DefaultServlet.class, "/swagger-ui/*");
    swaggerUiServletHolder.setAsyncSupported(false);
    adminContext.addServlet(DefaultServlet.class, "/recorder/*");

    ServletHolder webapp = adminContext.addServlet(DefaultServlet.class, "/webapp/*");
    webapp.setAsyncSupported(false);

        WebSocketServerContainerInitializer.configure(adminContext, (servletContext, serverContainer) -> {
            serverContainer.addEndpoint(WebSocketEndpoint.class);
        });

        final RewriteHandler rewrite = new RewriteHandler();
        rewrite.setRewriteRequestURI(true);
        rewrite.setRewritePathInfo(true);

        RewriteRegexRule rewriteRule = new RewriteRegexRule();
        rewriteRule.setRegex("/webapp/(mappings|matched|unmatched|state).*");
        rewriteRule.setReplacement("/index.html");
        rewrite.addRule(rewriteRule);

        adminContext.insertHandler(rewrite);

        ServletHolder servletHolder = adminContext.addServlet(WireMockHandlerDispatchingServlet.class, "/");
        servletHolder.setInitParameter(RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
        adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
        adminContext.setAttribute(Notifier.KEY, notifier);

    adminContext.setAttribute(MultipartRequestConfigurer.KEY, buildMultipartRequestConfigurer());

    addCorsFilter(adminContext);

    return adminContext;
  }

  private void addCorsFilter(ServletContextHandler context) {
    context.addFilter(buildCorsFilter(), "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  private FilterHolder buildCorsFilter() {
    FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
    filterHolder.setInitParameters(
        ImmutableMap.of(
            "chainPreflight", "false",
            "allowedOrigins", "*",
            "allowedHeaders", "*",
            "allowedMethods", "OPTIONS,GET,POST,PUT,PATCH,DELETE"));
    return filterHolder;
  }

  // Override this for platform-specific impls
  protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
    return new DefaultMultipartRequestConfigurer();
  }

  private static class NetworkTrafficListenerAdapter implements NetworkTrafficListener {
    private final WiremockNetworkTrafficListener wiremockNetworkTrafficListener;

    NetworkTrafficListenerAdapter(final WiremockNetworkTrafficListener wiremockNetworkTrafficListener) {
      this.wiremockNetworkTrafficListener = wiremockNetworkTrafficListener;
    }

    @Override
    public void opened(final Socket socket) {
            this.wiremockNetworkTrafficListener.opened(socket);
    }

    @Override
    public void incoming(final Socket socket, final ByteBuffer bytes) {
            this.wiremockNetworkTrafficListener.incoming(socket, bytes);
    }

    @Override
    public void outgoing(final Socket socket, final ByteBuffer bytes) {
            this.wiremockNetworkTrafficListener.outgoing(socket, bytes);
    }

    @Override
    public void closed(final Socket socket) {
            this.wiremockNetworkTrafficListener.closed(socket);
    }
  }
}
