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
package com.github.tomakehurst.wiremock.jetty12;

import static com.github.tomakehurst.wiremock.jetty12.HttpProxyDetectingHandler.IS_HTTP_PROXY_REQUEST_ATTRIBUTE;
import static com.github.tomakehurst.wiremock.jetty12.HttpsProxyDetectingHandler.IS_HTTPS_PROXY_REQUEST_ATTRIBUTE;

import com.github.tomakehurst.wiremock.jetty.JettyHttpUtils;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.ee10.servlet.ServletApiResponse;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.SelectableChannelEndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.AbstractMetaDataConnection;
import org.eclipse.jetty.server.Response;

public class Jetty12HttpUtils implements JettyHttpUtils {
  @Override
  public Response unwrapResponse(ServletResponse httpServletResponse) {
    if (httpServletResponse instanceof HttpServletResponseWrapper) {
      ServletResponse unwrapped = ((HttpServletResponseWrapper) httpServletResponse).getResponse();
      return unwrapResponse(unwrapped);
    } else {
      return unwrap(httpServletResponse);
    }
  }

  private static Response unwrap(ServletResponse wrapped) {
    if (wrapped instanceof Response) {
      return (Response) wrapped;
    } else if (wrapped instanceof ServletApiResponse) {
      return ((ServletApiResponse) wrapped).getResponse();
    } else {
      throw new IllegalStateException(
          "Cannot unwrap a" + Response.class.getName() + " from a " + wrapped.getClass().getName());
    }
  }

  @Override
  public Socket socket(Response response) {
    final AbstractMetaDataConnection connectionMetaData =
        (AbstractMetaDataConnection) response.getRequest().getConnectionMetaData();
    SelectableChannelEndPoint ep = (SelectableChannelEndPoint) connectionMetaData.getEndPoint();
    return ((SocketChannel) ep.getChannel()).socket();
  }

  @Override
  public Socket tlsSocket(Response response) {
    final AbstractMetaDataConnection connectionMetaData =
        (AbstractMetaDataConnection) response.getRequest().getConnectionMetaData();
    final SslConnection.SslEndPoint sslEndpoint =
        (SslConnection.SslEndPoint) connectionMetaData.getEndPoint();
    final SelectableChannelEndPoint endpoint =
        (SelectableChannelEndPoint) sslEndpoint.getSslConnection().getEndPoint();
    return ((SocketChannel) endpoint.getChannel()).socket();
  }

  @Override
  public void setStatusWithReason(
      int status, String reason, HttpServletResponse httpServletResponse) {
    // Servlet 6 is not accepting the reason / message anymore, consequently Jetty 12
    // completely eliminated the possibility to pass reason / message along with a status
    // in case of HTTP 1.x communication.
    httpServletResponse.setStatus(status);
  }

  @Override
  public EndPoint unwrapEndPoint(Response jettyResponse) {
    final AbstractMetaDataConnection connectionMetaData =
        (AbstractMetaDataConnection) jettyResponse.getRequest().getConnectionMetaData();
    return connectionMetaData.getEndPoint();
  }

  @Override
  public boolean isBrowserProxyRequest(HttpServletRequest request) {
    return Boolean.TRUE.equals(request.getAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE))
        || Boolean.TRUE.equals(request.getAttribute(IS_HTTP_PROXY_REQUEST_ATTRIBUTE));
  }
}
