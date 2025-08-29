/*
 * Copyright (C) 2020-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.proxy.ConnectHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.Promise;

/** The type Man in the middle ssl connect handler. */
public class ManInTheMiddleSslConnectHandler extends ConnectHandler {

  private final ServerConnector mitmProxyConnector;

  /**
   * Instantiates a new Man in the middle ssl connect handler.
   *
   * @param mitmProxyConnector the mitm proxy connector
   */
  public ManInTheMiddleSslConnectHandler(ServerConnector mitmProxyConnector) {
    this.mitmProxyConnector = mitmProxyConnector;
  }

  /**
   * Connect to server.
   *
   * @param request the request
   * @param ignoredHost the ignored host
   * @param ignoredPort the ignored port
   * @param promise the promise
   */
  @Override
  protected void connectToServer(
      HttpServletRequest request,
      String ignoredHost,
      int ignoredPort,
      Promise<SocketChannel> promise) {
    SocketChannel channel = null;
    try {
      channel = SocketChannel.open();
      channel.socket().setTcpNoDelay(true);
      channel.configureBlocking(false);

      String host = getFirstNonNull(mitmProxyConnector.getHost(), "localhost");
      int port = mitmProxyConnector.getLocalPort();
      InetSocketAddress address = newConnectAddress(host, port);

      channel.connect(address);
      promise.succeeded(channel);
    } catch (Throwable x) {
      close(channel);
      promise.failed(x);
    }
  }

  private void close(Closeable closeable) {
    try {
      if (closeable != null) closeable.close();
    } catch (Throwable x) {
      /* Ignore */
    }
  }
}
