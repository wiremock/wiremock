/*
 * Copyright (C) 2014-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockApp;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import com.github.tomakehurst.wiremock.servlet.*;
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

public abstract class JettyHttpServer implements HttpServer {
  private static final AtomicBoolean STRICT_HTTP_HEADERS_APPLIED = new AtomicBoolean(false);
  private static final int MAX_RETRIES = 3;

  protected static final String FILES_URL_MATCH = String.format("/%s/*", WireMockApp.FILES_ROOT);
  protected static final String[] GZIPPABLE_METHODS =
      new String[] {"POST", "PUT", "PATCH", "DELETE"};

  protected final Server jettyServer;
  protected final ServerConnector httpConnector;
  protected final ServerConnector httpsConnector;

  protected ScheduledExecutorService scheduledExecutorService;

  public JettyHttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
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

  protected void applyAdditionalServerConfiguration(Server jettyServer, Options options) {}

  protected abstract Handler createHandler(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler);

  protected void finalizeSetup(Options options) {
    if (options.jettySettings().getStopTimeout().isEmpty()) {
      jettyServer.setStopTimeout(1000);
    }
  }

  protected Server createServer(Options options) {
    final Server server = new Server(options.threadPoolFactory().buildThreadPool(options));
    final JettySettings jettySettings = options.jettySettings();
    final Optional<Long> stopTimeout = jettySettings.getStopTimeout();
    stopTimeout.ifPresent(server::setStopTimeout);

    return server;
  }

  /** Extend only this method if you want to add additional handlers to Jetty. */
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

  public long stopTimeout() {
    return jettyServer.getStopTimeout();
  }

  protected abstract ServerConnector createHttpConnector(
      String bindAddress, int port, JettySettings jettySettings, NetworkTrafficListener listener);

  protected abstract ServerConnector createHttpsConnector(
      String bindAddress,
      HttpsSettings httpsSettings,
      JettySettings jettySettings,
      NetworkTrafficListener listener);

  // Override this for platform-specific impls
  protected MultipartRequestConfigurer buildMultipartRequestConfigurer() {
    return new DefaultMultipartRequestConfigurer();
  }

  private static class NetworkTrafficListenerAdapter implements NetworkTrafficListener {
    private final WiremockNetworkTrafficListener wiremockNetworkTrafficListener;

    NetworkTrafficListenerAdapter(WiremockNetworkTrafficListener wiremockNetworkTrafficListener) {
      this.wiremockNetworkTrafficListener = wiremockNetworkTrafficListener;
    }

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
