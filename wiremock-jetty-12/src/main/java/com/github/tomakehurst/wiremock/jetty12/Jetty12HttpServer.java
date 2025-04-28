/*
 * Copyright (C) 2019-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.jetty12.SslContexts.buildManInTheMiddleSslContextFactory;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.eclipse.jetty.http.UriCompliance.UNSAFE;

import com.github.tomakehurst.wiremock.common.AsynchronousResponseSettings;
import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.Notifier;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.RequestHandler;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.servlet.ContentTypeSettingFilter;
import com.github.tomakehurst.wiremock.servlet.DefaultMultipartRequestConfigElementBuilder;
import com.github.tomakehurst.wiremock.servlet.FaultInjectorFactory;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigElementBuilder;
import com.github.tomakehurst.wiremock.servlet.NotMatchedServlet;
import com.github.tomakehurst.wiremock.servlet.TrailingSlashFilter;
import com.github.tomakehurst.wiremock.servlet.WireMockHandlerDispatchingServlet;
import jakarta.servlet.DispatcherType;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.ee10.servlet.*;
import org.eclipse.jetty.ee10.servlets.CrossOriginFilter;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ThreadPool;

public class Jetty12HttpServer implements HttpServer {

  private static final int DEFAULT_ACCEPTORS = 3;
  private static final int DEFAULT_HEADER_SIZE = 32768;

  private static final AtomicBoolean STRICT_HTTP_HEADERS_APPLIED = new AtomicBoolean(false);
  private static final int MAX_RETRIES = 3;

  protected static final String FILES_URL_MATCH = String.format("/%s/*", WireMockApp.FILES_ROOT);
  protected static final String[] GZIPPABLE_METHODS =
      new String[] {"POST", "PUT", "PATCH", "DELETE"};

  private final Options options;
  private final JettySettings jettySettings;

  private final Server jettyServer;
  private final ServerConnector httpConnector;
  private final ServerConnector httpsConnector;
  private ServerConnector mitmProxyConnector;

  private ScheduledExecutorService scheduledExecutorService;

  public Jetty12HttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler,
      JettySettings jettySettings,
      ThreadPool threadPool) {
    this.options = options;
    this.jettySettings = jettySettings;

    if (!options.getDisableStrictHttpHeaders() && !STRICT_HTTP_HEADERS_APPLIED.get()) {
      System.setProperty("org.eclipse.jetty.http.HttpGenerator.STRICT", String.valueOf(true));
      STRICT_HTTP_HEADERS_APPLIED.set(true);
    }

    jettyServer = new Server(threadPool);
    jettySettings.getStopTimeout().ifPresent(jettyServer::setStopTimeout);

    NetworkTrafficListenerAdapter networkTrafficListenerAdapter =
        new NetworkTrafficListenerAdapter(options.networkTrafficListener());

    if (options.getHttpDisabled()) {
      httpConnector = null;
    } else {
      httpConnector =
          createHttpConnector(
              options.bindAddress(),
              options.portNumber(),
              jettySettings,
              networkTrafficListenerAdapter);
      jettyServer.addConnector(httpConnector);
    }

    if (options.httpsSettings().enabled()) {
      httpsConnector =
          createHttpsConnector(
              options.bindAddress(),
              options.httpsSettings(),
              jettySettings,
              networkTrafficListenerAdapter);
      jettyServer.addConnector(httpsConnector);
    } else {
      httpsConnector = null;
    }

    applyAdditionalServerConfiguration(jettyServer, options);

    final Handler handlers = createHandler(options, adminRequestHandler, stubRequestHandler);
    jettyServer.setHandler(handlers);

    if (jettySettings.getStopTimeout().isEmpty()) {
      jettyServer.setStopTimeout(1000);
    }
  }

  protected ServerConnector createHttpConnector(
      String bindAddress, int port, JettySettings jettySettings, NetworkTrafficListener listener) {

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    ConnectionFactory[] connectionFactories =
        Stream.of(
                new HttpConnectionFactory(httpConfig),
                options.getHttp2PlainDisabled()
                    ? null
                    : new HTTP2CServerConnectionFactory(httpConfig))
            .filter(Objects::nonNull)
            .toArray(ConnectionFactory[]::new);

    return createServerConnector(
        jettyServer, bindAddress, jettySettings, port, listener, connectionFactories);
  }

  protected ServerConnector createHttpsConnector(
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener) {

    HttpConfiguration httpConfig = createHttpConfig(jettySettings);

    ConnectionFactory[] connectionFactories;

    if (!options.getHttp2TlsDisabled()) {

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

    return createServerConnector(
        jettyServer,
        bindAddress,
        jettySettings,
        httpsSettings.port(),
        listener,
        connectionFactories);
  }

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
              Netty, but it would be hard and time-consuming.
               */
              HttpVersion.HTTP_1_1.asString());

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
            // is no way to tell it to do so), the RequestDispatcher.FORWARD_SERVLET_PATH is
            // ignored (only RequestDispatcher.INCLUDE_SERVLET_PATH is taken into account but
            // that requires change from to RequestDispatcher#forward() to
            // RequestDispatcher#include()).
            options.filesRoot().child(WireMockApp.FILES_ROOT),
            options.getAsynchronousResponseSettings(),
            options.getChunkedEncodingPolicy(),
            options.getStubCorsEnabled(),
            options.browserProxySettings().enabled(),
            notifier);

    final List<Handler> handlers = new ArrayList<>(Arrays.asList(extensionHandlers()));
    handlers.add(adminContext);
    Handler.Abstract asyncTimeoutSettingHandler =
        new Handler.Abstract() {
          @Override
          public boolean handle(Request request, Response response, Callback callback) {
            if (request instanceof ServletContextRequest r) {
              r.getState().setTimeout(options.timeout());
            }
            return false;
          }
        };
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

  /** Extend only this method if you want to add additional handlers to Jetty. */
  protected Handler[] extensionHandlers() {
    return new Handler[] {};
  }

  @SuppressWarnings("unused")
  protected void decorateAdminServiceContextBeforeConfig(
      ServletContextHandler adminServiceContext) {}

  @SuppressWarnings("unused")
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

    Resource assetsResource =
        ResourceFactory.of(adminContext)
            .newResource(getResource(Jetty12HttpServer.class, "assets"));
    if (Resources.isReadable(assetsResource)) {
      adminContext.setBaseResource(assetsResource);
    }

    adminContext.setWelcomeFiles(new String[] {"index.html", "index.jsp"});

    adminContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    ServletHolder swaggerUiServletHolder =
        adminContext.addServlet(DefaultServlet.class, "/swagger-ui/*");
    swaggerUiServletHolder.setInitParameter("baseResource", "swagger-ui");
    swaggerUiServletHolder.setAsyncSupported(false);

    ServletHolder recorderServletHolder =
        adminContext.addServlet(DefaultServlet.class, "/recorder/*");
    recorderServletHolder.setInitParameter("baseResource", "recorder");

    ServletHolder servletHolder =
        adminContext.addServlet(WireMockHandlerDispatchingServlet.class, "/");
    servletHolder.setInitParameter(
        RequestHandler.HANDLER_CLASS_KEY, AdminRequestHandler.class.getName());
    adminContext.setAttribute(AdminRequestHandler.class.getName(), adminRequestHandler);
    adminContext.setAttribute(Notifier.KEY, notifier);

    servletHolder.getRegistration().setMultipartConfig(buildMultipartRequestConfigurer().build());

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
    Resource fileSourceResource =
        ResourceFactory.of(mockServiceContext).newResource(fileSource.getPath());
    if (Resources.isReadable(fileSourceResource)) {
      mockServiceContext.setBaseResource(fileSourceResource);
    }

    decorateMockServiceContextBeforeConfig(mockServiceContext);

    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.maxCacheSize", "0");
    mockServiceContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

    mockServiceContext.addServlet(DefaultServlet.class, FILES_URL_MATCH);

    mockServiceContext.setAttribute(
        JettyFaultInjectorFactory.class.getName(), new JettyFaultInjectorFactory());
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

    servletHolder.getRegistration().setMultipartConfig(buildMultipartRequestConfigurer().build());

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

  @SuppressWarnings("unused")
  protected void decorateMockServiceContextBeforeConfig(ServletContextHandler mockServiceContext) {}

  @SuppressWarnings("unused")
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

  // Override this for platform-specific impls
  protected MultipartRequestConfigElementBuilder buildMultipartRequestConfigurer() {
    return new DefaultMultipartRequestConfigElementBuilder();
  }

  private static ServerConnector createServerConnector(
      Server jettyServer,
      String bindAddress,
      JettySettings jettySettings,
      int port,
      NetworkTrafficListener listener,
      ConnectionFactory... connectionFactories) {

    int acceptors = jettySettings.getAcceptors().orElse(DEFAULT_ACCEPTORS);

    NetworkTrafficServerConnector connector =
        new NetworkTrafficServerConnector(
            jettyServer, null, null, null, acceptors, 2, connectionFactories);

    connector.setPort(port);
    connector.setNetworkTrafficListener(listener);
    setJettySettings(jettySettings, connector);
    connector.setHost(bindAddress);
    return connector;
  }

  private static void setJettySettings(JettySettings jettySettings, ServerConnector connector) {
    jettySettings.getAcceptQueueSize().ifPresent(connector::setAcceptQueueSize);
    jettySettings.getIdleTimeout().ifPresent(connector::setIdleTimeout);
    connector.setShutdownIdleTimeout(jettySettings.getShutdownIdleTimeout().orElse(200L));
  }

  private static HttpConfiguration createHttpConfig(JettySettings jettySettings) {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setRequestHeaderSize(
        jettySettings.getRequestHeaderSize().orElse(DEFAULT_HEADER_SIZE));
    httpConfig.setResponseHeaderSize(
        jettySettings.getResponseHeaderSize().orElse(DEFAULT_HEADER_SIZE));
    httpConfig.setSendDateHeader(false);
    httpConfig.setSendXPoweredBy(false);
    httpConfig.setSendServerVersion(false);
    httpConfig.addCustomizer(new SecureRequestCustomizer(false));
    httpConfig.setUriCompliance(UNSAFE);
    return httpConfig;
  }

  @Override
  public void start() {
    int retryCount = 0;

    while (true) {
      try {
        jettyServer.start();
        break;
      } catch (IOException bindException) {
        retryCount++;
        if (retryCount >= MAX_RETRIES) {
          throw new FatalStartupException(bindException);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    long timeout = System.currentTimeMillis() + 30000;
    while (!jettyServer.isStarted()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
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

      if (httpConnector != null) {
        httpConnector.getConnectedEndPoints().forEach(EndPoint::close);
      }

      if (httpsConnector != null) {
        httpsConnector.getConnectedEndPoints().forEach(EndPoint::close);
      }

      jettyServer.stop();
      jettyServer.join();
    } catch (TimeoutException ignored) {
    } catch (Exception e) {
      throwUnchecked(e);
    }
  }

  @Override
  public boolean isRunning() {
    return jettyServer.isRunning();
  }

  @Override
  public int port() {
    return httpConnector.getLocalPort();
  }

  @Override
  public int httpsPort() {
    return httpsConnector.getLocalPort();
  }

  public long stopTimeout() {
    return jettyServer.getStopTimeout();
  }

  private record NetworkTrafficListenerAdapter(
      WiremockNetworkTrafficListener wiremockNetworkTrafficListener)
      implements NetworkTrafficListener {

    @Override
    public void opened(Socket socket) {
      wiremockNetworkTrafficListener.opened(socket);
    }

    @Override
    public void incoming(Socket socket, ByteBuffer bytes) {
      wiremockNetworkTrafficListener.incoming(socket, bytes);
    }

    @Override
    public void outgoing(Socket socket, ByteBuffer bytes) {
      wiremockNetworkTrafficListener.outgoing(socket, bytes);
    }

    @Override
    public void closed(Socket socket) {
      wiremockNetworkTrafficListener.closed(socket);
    }
  }
}
