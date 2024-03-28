/*
 * Copyright (C) 2023-2024 Thomas Akehurst
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

public class HttpsProxyDetectingHandler extends Handler.Abstract {

  public static final String IS_HTTPS_PROXY_REQUEST_ATTRIBUTE = "wiremock.isHttpsProxyRequest";

  private final ServerConnector mitmProxyConnector;

  public HttpsProxyDetectingHandler(ServerConnector mitmProxyConnector) {
    this.mitmProxyConnector = mitmProxyConnector;
  }

  @Override
  public boolean handle(Request request, Response response, Callback callback) throws Exception {
    final int httpsProxyPort = mitmProxyConnector.getLocalPort();

    int localPort = -1;
    SocketAddress local = request.getConnectionMetaData().getLocalSocketAddress();
    if (local instanceof InetSocketAddress) {
      localPort = ((InetSocketAddress) local).getPort();
    }

    if (localPort == httpsProxyPort) {
      request.setAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE, true);
    }
    return false;
  }
}
