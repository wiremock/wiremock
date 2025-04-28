/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.servlet.ServletUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Callback;

/**
 * The Jetty 11 implementation was relying on relative request URI presence to detect the proxying
 * request. Jetty 12 does not do that anymore and URI is always converted to absolute form. To keep
 * proxy detection working, the Jetty 12 specific implementation does compare connector and URI
 * ports (which are different in case of proxying request).
 */
public class HttpProxyDetectingHandler extends Handler.Abstract {

  private final ServerConnector httpConnector;

  public HttpProxyDetectingHandler(ServerConnector httpConnector) {
    this.httpConnector = httpConnector;
  }

  @Override
  public boolean handle(Request request, Response response, Callback callback) throws Exception {
    final int httpPort = httpConnector.getLocalPort();

    if (httpPort != request.getHttpURI().getPort()
        && "http".equals(request.getHttpURI().getScheme())) {
      request.setAttribute(ServletUtils.IS_PROXY_REQUEST_ATTRIBUTE, true);
    }

    return false;
  }
}
