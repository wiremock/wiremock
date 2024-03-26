/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty11;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.github.tomakehurst.wiremock.common.ResourceUtil.getResource;
import static com.github.tomakehurst.wiremock.core.WireMockApp.ADMIN_CONTEXT_ROOT;
import static com.github.tomakehurst.wiremock.jetty11.Jetty11Utils.createHttpConfig;
import static com.github.tomakehurst.wiremock.jetty11.SslContexts.buildManInTheMiddleSslContextFactory;
import static java.util.concurrent.Executors.newScheduledThreadPool;

import com.github.tomakehurst.wiremock.common.AsynchronousResponseSettings;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty.JettyFaultInjectorFactory;
import com.github.tomakehurst.wiremock.jetty.JettyHttpServer;
import com.github.tomakehurst.wiremock.jetty.JettyHttpUtils;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.FaultInjectorFactory;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.servlet.NotMatchedServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Jetty11HttpServer extends JettyHttpServer {

  private ServerConnector mitmProxyConnector;

  public Jetty11HttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    super(options, adminRequestHandler, stubRequestHandler);
  }

  @Override
  protected ServerConnector createHttpConnector(
      String bindAddress, int port, JettySettings jettySettings, NetworkTrafficListener listener) {

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    HTTP2CServerConnectionFactory h2c = new HTTP2CServerConnectionFactory(httpConfig);

    return Jetty11Utils.createServerConnector(
        jettyServer,
        bindAddress,
        jettySettings,
        port,
        listener,
        new HttpConnectionFactory(httpConfig),
        h2c);
  }

  @Override
  protected ServerConnector createHttpsConnector(
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener) {
    SslContextFactory.Server http2SslContextFactory =
        SslContexts.buildHttp2SslContextFactory(httpsSettings);

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
    HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);

    ConnectionFactory[] connectionFactories;
    try {
      ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();

      SslConnectionFactory ssl =
          new SslConnectionFactory(http2SslContextFactory, alpn.getProtocol());

      connectionFactories = new ConnectionFactory[] {ssl, alpn, h2, http};
    } catch (IllegalStateException e) {
      SslConnectionFactory ssl =
          new SslConnectionFactory(http2SslContextFactory, http.getProtocol());

      connectionFactories = new ConnectionFactory[] {ssl, http};
    }

    return Jetty11Utils.createServerConnector(
        jettyServer,
        bindAddress,
        jettySettings,
        httpsSettings.port(),
        listener,
        connectionFactories);
  }

  @Override
  protected void applyAdditionalServerConfiguration(Server jettyServer, Options options) {
    if (options.browserProxySettings().enabled()) {
      final SslConnectionFactory ssl =
          new SslConnectionFactory(
              buildManInTheMiddleSslContextFactory(
                  options.httpsSettings(), options.browserProxySettings(), options.notifier()),
              /*
              If the proxy CONNECT request is made over HTTPS, and the
              actual content request is made using HTTP/2 tunneled over
              HTTPS, and an exception is thrown, the server blocks for 30
              seconds before flushing the response.

              To fix this, force HTTP/1.1 over TLS when tunneling HTTPS.

              This also means the HTTP mitmProxyConnector does not need the alpn &
              h2 connection factories as it will not use them.

              Unfortunately it has proven too hard to write a test to
              demonstrate the bug; it requires an HTTP client capable of
              doing ALPN & HTTP/2, which will only offer HTTP/1.1 in the
              ALPN negotiation when using HTTPS for the initial CONNECT
              request but will then offer both HTTP/1.1 and HTTP/2 for the
              actual request (this is how curl 7.64.1 behaves!). Neither
              Apache HTTP 4, 5, 5 Async, OkHttp, nor the Jetty client
              could do this. It might be possible to write one using
              Netty, but it would be hard and time consuming.
               */
              HttpVersion.HTTP_1_1.asString());

      JettySettings jettySettings = options.jettySettings();
      HttpConfiguration httpConfig = createHttpConfig(jettySettings);
      HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
      mitmProxyConnector =
          new NetworkTrafficServerConnector(jettyServer, null, null, null, 2, 2, ssl, http);

      mitmProxyConnector.setPort(0);
      mitmProxyConnector.setShutdownIdleTimeout(
          jettySettings.getShutdownIdleTimeout().orElse(100L));

      jettyServer.addConnector(mitmProxyConnector);
    }
  }

  @Override
  protected Handler createHandler(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    Notifier notifier = options.notifier();
    ServletContextHandler adminContext = addAdminContext(adminRequestHandler, notifier);
    ServletContextHandler mockServiceContext =
        addMockServiceContext(
            stubRequestHandler,
            options.filesRoot(),
            options.getAsynchronousResponseSettings(),
            options.getChunkedEncodingPolicy(),
            options.getStubCorsEnabled(),
            options.browserProxySettings().enabled(),
            notifier);

    HandlerCollection handlers = new HandlerCollection();
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
        Stream.concat(
                Arrays.stream(extensionHandlers()),
                Stream.of(adminContext, asyncTimeoutSettingHandler))
            .toArray(Handler[]::new));

    if (options.getGzipDisabled()) {
      handlers.addHandler(mockServiceContext);
    } else {
      addGZipHandler(mockServiceContext, handlers);
    }

    if (options.browserProxySettings().enabled()) {
      handlers.prependHandler(new HttpsProxyDetectingHandler(mitmProxyConnector));
      handlers.prependHandler(new ManInTheMiddleSslConnectHandler(mitmProxyConnector));
    }

    return handlers;
  }

  private ServletContextHandler addMockServiceContext(
      StubRequestHandler stubRequestHandler,
      FileSource fileSource,
      AsynchronousResponseSettings asynchronousResponseSettings,
      Options.ChunkedEncodingPolicy chunkedEncodingPolicy,
      boolean stubCorsEnabled,
      boolean browserProxyingEnabled,
      Notifier notifier) {
    ServletContextHandler mockServiceContext = new ServletContextHandler(jettyServer, "/");

    decorateMockServiceContextBeforeConfig(mockServiceContext);

    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");
    mockServiceContext.setInitParameter(
        "org.eclipse.jetty.servlet.Default.resourceBase", fileSource.getPath());
    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

    final Jetty11HttpUtils utils = new Jetty11HttpUtils();
    mockServiceContext.setAttribute(JettyHttpUtils.class.getName(), utils);

    mockServiceContext.setAttribute(
        JettyFaultInjectorFactory.class.getName(), new JettyFaultInjectorFactory(utils));
    mockServiceContext.setAttribute(StubRequestHandler.class.getName(), stubRequestHandler);
    mockServiceContext.setAttribute(Notifier.KEY, notifier);
    mockServiceContext.setAttribute(
        Options.ChunkedEncodingPolicy.class.getName(), chunkedEncodingPolicy);
    mockServiceContext.setAttribute("browserProxyingEnabled", browserProxyingEnabled);
    ServletHolder servletHolder =
        mockServiceContext.addServlet(WireMockHandlerDispatchingServlet.class, "/");
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

    MimeTypes mimeTypes = new MimeTypes();
    mimeTypes.addMimeMapping("json", "application/json");
    mimeTypes.addMimeMapping("html", "text/html");
    mimeTypes.addMimeMapping("xml", "application/xml");
    mimeTypes.addMimeMapping("txt", "text/plain");
    mockServiceContext.setMimeTypes(mimeTypes);
    mockServiceContext.setWelcomeFiles(
        new String[] {"index.json", "index.html", "index.xml", "index.txt"});

    NotFoundHandler errorHandler = new NotFoundHandler(mockServiceContext);
    mockServiceContext.setErrorHandler(errorHandler);

    mockServiceContext.addFilter(
        ContentTypeSettingFilter.class, FILES_URL_MATCH, EnumSet.of(DispatcherType.FORWARD));
    mockServiceContext.addFilter(
        TrailingSlashFilter.class, FILES_URL_MATCH, EnumSet.allOf(DispatcherType.class));

    if (stubCorsEnabled) {
      addCorsFilter(mockServiceContext);
    }

    decorateMockServiceContextAfterConfig(mockServiceContext);

    return mockServiceContext;
  }

  protected void decorateMockServiceContextBeforeConfig(ServletContextHandler mockServiceContext) {}

  protected void decorateMockServiceContextAfterConfig(ServletContextHandler mockServiceContext) {}

  private ServletContextHandler addAdminContext(
      AdminRequestHandler adminRequestHandler, Notifier notifier) {
    ServletContextHandler adminContext = new ServletContextHandler(jettyServer, ADMIN_CONTEXT_ROOT);

    decorateAdminServiceContextBeforeConfig(adminContext);

    adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");

    String javaVendor = System.getProperty("java.vendor");
    if (javaVendor != null && javaVendor.toLowerCase().contains("android")) {
      // Special case for Android, fixes IllegalArgumentException("resource assets not found."):
      //  The Android ClassLoader apparently does not resolve directories.
      //  Furthermore, lib assets will be merged into a single asset directory when a jar file is
      // assimilated into an apk.
      //  As resources can be addressed like "assets/swagger-ui/index.html", a static path element
      // will suffice.
      adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.resourceBase", "assets");
    } else {
      adminContext.setInitParameter(
          "org.eclipse.jetty.servlet.Default.resourceBase",
          getResource(JettyHttpServer.class, "assets").toString());
    }

    getResource(JettyHttpServer.class, "assets/swagger-ui/index.html");

    adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
    ServletHolder swaggerUiServletHolder =
        adminContext.addServlet(DefaultServlet.class, "/swagger-ui/*");
    swaggerUiServletHolder.setAsyncSupported(false);
    adminContext.addServlet(DefaultServlet.class, "/recorder/*");

    ServletHolder servletHolder =
        adminContext.addServlet(WireMockHandlerDispatchingServlet.class, "/");
    servletHolder.setInitParameter(
        RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
    adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
    adminContext.setAttribute(Notifier.KEY, notifier);

    adminContext.setAttribute(MultipartRequestConfigurer.KEY, buildMultipartRequestConfigurer());

    adminContext.addServlet(NotMatchedServlet.class, "/not-matched");

    addCorsFilter(adminContext);

    decorateAdminServiceContextAfterConfig(adminContext);

    return adminContext;
  }

  protected void decorateAdminServiceContextBeforeConfig(
      ServletContextHandler adminServiceContext) {}

  protected void decorateAdminServiceContextAfterConfig(
      ServletContextHandler adminServiceContext) {}

  private void addCorsFilter(ServletContextHandler context) {
    context.addFilter(buildCorsFilter(), "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  private FilterHolder buildCorsFilter() {
    FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
    filterHolder.setInitParameters(
        Map.of(
            "chainPreflight",
            "false",
            "allowedOrigins",
            "*",
            "allowedHeaders",
            "*",
            "allowedMethods",
            "OPTIONS,GET,POST,PUT,PATCH,DELETE"));
    return filterHolder;
  }

  private void addGZipHandler(
      ServletContextHandler mockServiceContext, HandlerCollection handlers) {
    try {
      GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.addIncludedMethods(GZIPPABLE_METHODS);
      gzipHandler.setHandler(mockServiceContext);
      gzipHandler.setVary(null);
      handlers.addHandler(gzipHandler);
    } catch (Exception e) {
      throwUnchecked(e);
    }
  }
}
