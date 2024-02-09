/*
 * Copyright (C) 2015-2024 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.jetty11.HttpsProxyDetectingHandler.IS_HTTPS_PROXY_REQUEST_ATTRIBUTE;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

public class JettyUtils {

  private static final boolean IS_JETTY;
  private static final boolean IS_SERVLET_6;

  static {
    // do the check only once per classloader / execution
    IS_JETTY = isClassExist("org.eclipse.jetty.server.Request");
    IS_SERVLET_6 = isClassExist("jakarta.servlet.ServletConnection");
  }

  private JettyUtils() {
    // Hide public constructor
  }

  public static boolean isJetty() {
    return IS_JETTY;
  }

  public static boolean isServlet6() {
    return IS_SERVLET_6;
  }

  private static boolean isClassExist(String type) {
    try {
      ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
      ClassLoader loader = contextCL == null ? JettyUtils.class.getClassLoader() : contextCL;
      Class.forName(type, false, loader);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static Response unwrapResponse(HttpServletResponse httpServletResponse) {
    if (httpServletResponse instanceof HttpServletResponseWrapper) {
      ServletResponse unwrapped = ((HttpServletResponseWrapper) httpServletResponse).getResponse();
      return (Response) unwrapped;
    }

    return (Response) httpServletResponse;
  }

  public static Socket getTlsSocket(Response response) {
    HttpChannel httpChannel = response.getHttpOutput().getHttpChannel();
    SslConnection.DecryptedEndPoint sslEndpoint =
        (SslConnection.DecryptedEndPoint) httpChannel.getEndPoint();
    Object endpoint = sslEndpoint.getSslConnection().getEndPoint();
    try {
      final SocketChannel channel =
          (SocketChannel) endpoint.getClass().getMethod("getChannel").invoke(endpoint);
      return channel.socket();
    } catch (Exception e) {
      return throwUnchecked(e, Socket.class);
    }
  }

  public static boolean isBrowserProxyRequest(HttpServletRequest request) {
    if (request instanceof Request) {
      /* Jetty 11 */
      Request jettyRequest = (Request) request;
      return Boolean.TRUE.equals(request.getAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE))
          || "http".equals(jettyRequest.getMetaData().getURI().getScheme());
    } else {
      /* Jetty 12 */
      return Boolean.TRUE.equals(request.getAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE))
          || "http".equals(request.getScheme());
    }
  }
}
