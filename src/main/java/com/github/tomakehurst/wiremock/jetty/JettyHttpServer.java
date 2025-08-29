/*
 * Copyright (C) 2014-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.github.tomakehurst.wiremock.common.FatalStartupException;
import com.github.tomakehurst.wiremock.common.HttpsSettings;
import com.github.tomakehurst.wiremock.common.JettySettings;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.servlet.MultipartRequestConfigurer;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/** The type Jetty http server. */
public abstract class JettyHttpServer implements HttpServer {
  private static final AtomicBoolean STRICT_HTTP_HEADERS_APPLIED = new AtomicBoolean(false);
  private static final int MAX_RETRIES = 3;

  /** The constant FILES_URL_MATCH. */
  protected static final String FILES_URL_MATCH = String.format("/%s/*", WireMockApp.FILES_ROOT);

  /** The constant GZIPPABLE_METHODS. */
  protected static final String[] GZIPPABLE_METHODS =
      new String[] {"POST", "PUT", "PATCH", "DELETE"};

  /** The Options. */
  protected final Options options;

  /** The Jetty server. */
  protected final Server jettyServer;

  /** The Http connector. */
  protected final ServerConnector httpConnector;

  /** The Https connector. */
  protected final ServerConnector httpsConnector;

  /** The Scheduled executor service. */
  protected ScheduledExecutorService scheduledExecutorService;

  /**
   * Instantiates a new Jetty http server.
   *
   * @param options the options
   * @param adminRequestHandler the admin request handler
   * @param stubRequestHandler the stub request handler
   */
  public JettyHttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    this.options = options;

    if (!options.getDisableStrictHttpHeaders()
        && Boolean.FALSE.equals(STRICT_HTTP_HEADERS_APPLIED.get())) {
      System.setProperty("org.eclipse.jetty.http.HttpGenerator.STRICT", String.valueOf(true));
      STRICT_HTTP_HEADERS_APPLIED.set(true);
    }

    jettyServer = createServer(options);

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
      httpsConnector =
          createHttpsConnector(
              options.bindAddress(),
              options.httpsSettings(),
              options.jettySettings(),
              networkTrafficListenerAdapter);
      jettyServer.addConnector(httpsConnector);
    } else {
      httpsConnector = null;
    }

    applyAdditionalServerConfiguration(jettyServer, options);

    final Handler handlers = createHandler(options, adminRequestHandler, stubRequestHandler);
    jettyServer.setHandler(handlers);

    finalizeSetup(options);
  }

  /**
   * Apply additional server configuration.
   *
   * @param jettyServer the jetty server
   * @param options the options
   */
  protected void applyAdditionalServerConfiguration(Server jettyServer, Options options) {}

  /**
   * Create handler handler.
   *
   * @param options the options
   * @param adminRequestHandler the admin request handler
   * @param stubRequestHandler the stub request handler
   * @return the handler
   */
  protected abstract Handler createHandler(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler);

  /**
   * Finalize setup.
   *
   * @param options the options
   */
  protected void finalizeSetup(Options options) {
    if (options.jettySettings().getStopTimeout().isEmpty()) {
      jettyServer.setStopTimeout(1000);
    }
  }

  /**
   * Create server server.
   *
   * @param options the options
   * @return the server
   */
  protected Server createServer(Options options) {
    final ThreadPoolFactory threadPoolFactory =
        options.threadPoolFactory() != null
            ? options.threadPoolFactory()
            : new QueuedThreadPoolFactory();
    final Server server = new Server(threadPoolFactory.buildThreadPool(options));
    final JettySettings jettySettings = options.jettySettings();
    final Optional<Long> stopTimeout = jettySettings.getStopTimeout();
    stopTimeout.ifPresent(server::setStopTimeout);

    return server;
  }

  /**
   * Extend only this method if you want to add additional handlers to Jetty. @return the handler [
   * ]
   */
  public Handler[] extensionHandlers() {
    return new Handler[] {};
  }

  @Override
  public void start() {
    int retryCount = 0;

    while (retryCount < MAX_RETRIES) {
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

  /**
   * Stop timeout long.
   *
   * @return the long
   */
  public long stopTimeout() {
    return jettyServer.getStopTimeout();
  }

  /**
   * Create http connector server connector.
   *
   * @param bindAddress the bind address
   * @param port the port
   * @param jettySettings the jetty settings
   * @param listener the listener
   * @return the server connector
   */
  protected abstract ServerConnector createHttpConnector(
      String bindAddress, int port, JettySettings jettySettings, NetworkTrafficListener listener);

  /**
   * Create https connector server connector.
   *
   * @param bindAddress the bind address
   * @param httpsSettings the https settings
   * @param jettySettings the jetty settings
   * @param listener the listener
   * @return the server connector
   */
  protected abstract ServerConnector createHttpsConnector(
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener);

  /**
   * Build multipart request configurer multipart request configurer.
   *
   * @return the multipart request configurer
   */
  // Override this for platform-specific impls
  protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
    return new DefaultMultipartRequestConfigurer();
  }

  private static class NetworkTrafficListenerAdapter implements NetworkTrafficListener {
    private final WiremockNetworkTrafficListener wiremockNetworkTrafficListener;

    /**
     * Instantiates a new Network traffic listener adapter.
     *
     * @param wiremockNetworkTrafficListener the wiremock network traffic listener
     */
    NetworkTrafficListenerAdapter(WiremockNetworkTrafficListener wiremockNetworkTrafficListener) {
      this.wiremockNetworkTrafficListener = wiremockNetworkTrafficListener;
    }

    /**
     * Opened.
     *
     * @param socket the socket
     */
    @Override
    public void opened(Socket socket) {
      wiremockNetworkTrafficListener.opened(socket);
    }

    /**
     * Incoming.
     *
     * @param socket the socket
     * @param bytes the bytes
     */
    @Override
    public void incoming(Socket socket, ByteBuffer bytes) {
      wiremockNetworkTrafficListener.incoming(socket, bytes);
    }

    /**
     * Outgoing.
     *
     * @param socket the socket
     * @param bytes the bytes
     */
    @Override
    public void outgoing(Socket socket, ByteBuffer bytes) {
      wiremockNetworkTrafficListener.outgoing(socket, bytes);
    }

    /**
     * Closed.
     *
     * @param socket the socket
     */
    @Override
    public void closed(Socket socket) {
      wiremockNetworkTrafficListener.closed(socket);
    }
  }
}
