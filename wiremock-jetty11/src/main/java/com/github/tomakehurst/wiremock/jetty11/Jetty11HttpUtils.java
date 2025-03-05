/*
 * Copyright (C) 2015-2025 Thomas Akehurst
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
import static com.github.tomakehurst.wiremock.jetty11.HttpsProxyDetectingHandler.IS_HTTPS_PROXY_REQUEST_ATTRIBUTE;

import com.github.tomakehurst.wiremock.jetty.JettyHttpUtils;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.SelectableChannelEndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

public class Jetty11HttpUtils implements JettyHttpUtils {
  @Override
  public Response unwrapResponse(HttpServletResponse httpServletResponse) {
    if (httpServletResponse instanceof HttpServletResponseWrapper) {
      ServletResponse unwrapped = ((HttpServletResponseWrapper) httpServletResponse).getResponse();
      return (Response) unwrapped;
    }

    return (Response) httpServletResponse;
  }

  @Override
  public Socket socket(Response response) {
    HttpChannel httpChannel = response.getHttpOutput().getHttpChannel();
    SelectableChannelEndPoint ep = (SelectableChannelEndPoint) httpChannel.getEndPoint();
    return ((SocketChannel) ep.getChannel()).socket();
  }

  @Override
  public Socket tlsSocket(Response response) {
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

  @Override
  public void setStatusWithReason(
      int status, String reason, HttpServletResponse httpServletResponse) {
    // The Jetty 11 does not implement HttpServletResponse::setStatus and always sets the
    // reason as `null`, the workaround using
    // org.eclipse.jetty.server.Response::setStatusWithReason
    // still works.
    if (httpServletResponse instanceof org.eclipse.jetty.server.Response) {
      final org.eclipse.jetty.server.Response jettyResponse =
          (org.eclipse.jetty.server.Response) httpServletResponse;
      jettyResponse.setStatusWithReason(status, reason);
    } else {
      httpServletResponse.setStatus(status, reason);
    }
  }

  @Override
  public EndPoint unwrapEndPoint(Response jettyResponse) {
    return jettyResponse.getHttpOutput().getHttpChannel().getEndPoint();
  }

  @Override
  public boolean isBrowserProxyRequest(HttpServletRequest request) {
    if (request instanceof Request) {
      final Request jettyRequest = (Request) request;
      return Boolean.TRUE.equals(request.getAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE))
          || "http".equals(jettyRequest.getMetaData().getURI().getScheme());
    } else {
      return false;
    }
  }
}
