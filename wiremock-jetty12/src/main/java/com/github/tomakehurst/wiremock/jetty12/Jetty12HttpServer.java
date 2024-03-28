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
package com.github.tomakehurst.wiremock.jetty12;

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
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty.JettyFaultInjectorFactory;
import com.github.tomakehurst.wiremock.jetty.JettyHttpServer;
import com.github.tomakehurst.wiremock.jetty.JettyHttpUtils;
import com.github.tomakehurst.wiremock.jetty11.Jetty11Utils;
import com.github.tomakehurst.wiremock.jetty11.SslContexts;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.FaultInjectorFactory;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import com.github.tomakehurst.wiremock.servlet.NotMatchedServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;
import jakarta.servlet.DispatcherType;
import java.util.*;
import java.util.stream.Stream;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class Jetty12HttpServer extends JettyHttpServer {

  private ServerConnector mitmProxyConnector;

  public Jetty12HttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    super(options, adminRequestHandler, stubRequestHandler);
  }

  @Override
  protected ServerConnector createHttpConnector(
      String bindAddress, int port, JettySettings jettySettings, NetworkTrafficListener listener) {

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    ConnectionFactory[] connectionFactories =
        Stream.of(
                new HttpConnectionFactory(httpConfig),
                options.getHttp2PlainEnabled()
                    ? new HTTP2CServerConnectionFactory(httpConfig)
                    : null)
            .filter(Objects::nonNull)
            .toArray(ConnectionFactory[]::new);

    return Jetty11Utils.createServerConnector(
        jettyServer, bindAddress, jettySettings, port, listener, connectionFactories);
  }

  @Override
  protected ServerConnector createHttpsConnector(
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener) {

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    ConnectionFactory[] connectionFactories;

    if (options.getHttp2TlsEnabled()) {

      SslContextFactory.Server http2SslContextFactory =
          SslContexts.buildHttp2SslContextFactory(httpsSettings);

      HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
      HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(httpConfig);

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
    } else {
      final SslContextFactory.Server sslContextFactory =
          SslContexts.buildHttp1_1SslContextFactory(httpsSettings);
      final SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, "http/1.1");
      final HttpConnectionFactory http = new HttpConnectionFactory(httpConfig);
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
            adminContext,
            stubRequestHandler,
            // Setting the files to the real path here since Jetty 12 does not include
            // the servlet context path in forwarded request (and at the moment, there
            // is not way to tell it to do so), the RequestDispatcher.FORWARD_SERVLET_PATH is
            // ignored (only RequestDispatcher.INCLUDE_SERVLET_PATH is taken into account but
            // that requires change from to RequestDispatcher#forward() to
            // RequestDispatcher#include()).
            options.filesRoot().child(WireMockApp.FILES_ROOT),
            options.getAsynchronousResponseSettings(),
            options.getChunkedEncodingPolicy(),
            options.getStubCorsEnabled(),
            options.browserProxySettings().enabled(),
            notifier);

    final List<Handler> handlers = new ArrayList<>();
    Handler.Abstract asyncTimeoutSettingHandler =
        new Handler.Abstract() {
          @Override
          public boolean handle(Request request, Response response, Callback callback) {
            if (request instanceof ServletContextRequest) {
              final ServletContextRequest r = (ServletContextRequest) request;
              r.getState().setTimeout(options.timeout());
            }
            return false;
          }
        };
    handlers.addAll(Arrays.asList(extensionHandlers()));
    handlers.add(adminContext);
    handlers.add(asyncTimeoutSettingHandler);

    if (options.getGzipDisabled()) {
      handlers.add(mockServiceContext);
    } else {
      addGZipHandler(mockServiceContext, handlers);
    }

    if (options.browserProxySettings().enabled()) {
      handlers.add(0, new HttpProxyDetectingHandler(httpConnector));
      handlers.add(0, new HttpsProxyDetectingHandler(mitmProxyConnector));
      handlers.add(0, new ManInTheMiddleSslConnectHandler(mitmProxyConnector));
    }

    return new Handler.Sequence(handlers);
  }

  protected void decorateAdminServiceContextBeforeConfig(
      ServletContextHandler adminServiceContext) {}

  protected void decorateAdminServiceContextAfterConfig(
      ServletContextHandler adminServiceContext) {}

  private void addCorsFilter(ServletContextHandler context) {
    context.addFilter(buildCorsFilter(), "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  private ServletContextHandler addAdminContext(
      AdminRequestHandler adminRequestHandler, Notifier notifier) {
    ServletContextHandler adminContext = new ServletContextHandler();
    adminContext.setServer(jettyServer);
    adminContext.setContextPath(ADMIN_CONTEXT_ROOT);

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
    // Jetty 12 changed the way welcome files are being served (the context path is not being
    // taken into account anymore). It is possible to somewhat change this behavior by altering
    // welcome mode of ServletResourceService but this parameter is only partially supported by
    // DefaultServlet (not all modes) and in general needs servlet subclassing. Taking an easier
    // approach here with extended welcome files list.
    adminContext.setWelcomeFiles(new String[] {"index.html", "index.jsp", "swagger-ui/index.html"});

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

  private ServletContextHandler addMockServiceContext(
      ServletContextHandler adminContext,
      StubRequestHandler stubRequestHandler,
      FileSource fileSource,
      AsynchronousResponseSettings asynchronousResponseSettings,
      Options.ChunkedEncodingPolicy chunkedEncodingPolicy,
      boolean stubCorsEnabled,
      boolean browserProxyingEnabled,
      Notifier notifier) {
    ServletContextHandler mockServiceContext = new ServletContextHandler();
    mockServiceContext.setServer(jettyServer);
    mockServiceContext.setContextPath("/");

    decorateMockServiceContextBeforeConfig(mockServiceContext);

    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");
    mockServiceContext.setInitParameter(
        "org.eclipse.jetty.servlet.Default.resourceBase", fileSource.getPath());
    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

    final Jetty12HttpUtils utils = new Jetty12HttpUtils();
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

    MimeTypes.Mutable mimeTypes = mockServiceContext.getMimeTypes();
    // For files without extension, use "application/json" as the default in case
    // file extension is not provided(and content type could not be detected).
    mimeTypes.addMimeMapping("*", "application/json");
    mimeTypes.addMimeMapping("json", "application/json");
    mimeTypes.addMimeMapping("html", "text/html");
    mimeTypes.addMimeMapping("xml", "application/xml");
    mimeTypes.addMimeMapping("txt", "text/plain");

    mockServiceContext.setWelcomeFiles(
        new String[] {"index.json", "index.html", "index.xml", "index.txt"});

    // Jetty 12 does not currently support cross context dispatch, we
    // need to use the adminContext explicitly.
    NotFoundHandler errorHandler = new NotFoundHandler(adminContext);
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

  private void addGZipHandler(ServletContextHandler mockServiceContext, List<Handler> handlers) {
    try {
      GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.addIncludedMethods(GZIPPABLE_METHODS);
      gzipHandler.setHandler(mockServiceContext);
      gzipHandler.setVary(null);
      handlers.add(gzipHandler);
    } catch (Exception e) {
      throwUnchecked(e);
    }
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
}
