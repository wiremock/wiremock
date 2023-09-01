/*
 * Copyright (C) 2023 Thomas Akehurst
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class HttpsProxyDetectingHandler extends AbstractHandler {

  public static final String IS_HTTPS_PROXY_REQUEST_ATTRIBUTE = "wiremock.isHttpsProxyRequest";

  private final ServerConnector mitmProxyConnector;

  public HttpsProxyDetectingHandler(ServerConnector mitmProxyConnector) {
    this.mitmProxyConnector = mitmProxyConnector;
  }

  @Override
  public void handle(
      String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final int httpsProxyPort = mitmProxyConnector.getLocalPort();
    if (request.getLocalPort() == httpsProxyPort) {
      request.setAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE, true);
    }
  }
}
