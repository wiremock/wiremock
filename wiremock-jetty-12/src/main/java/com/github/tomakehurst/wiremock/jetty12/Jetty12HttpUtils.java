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

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import org.eclipse.jetty.ee10.servlet.ServletApiResponse;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.io.SelectableChannelEndPoint;
import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.Response;

public class Jetty12HttpUtils {

  private Jetty12HttpUtils() {}

  public static Response unwrapResponse(ServletResponse httpServletResponse) {
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

  public static Socket socket(Response response) {
    SelectableChannelEndPoint ep = (SelectableChannelEndPoint) getEndpoint(response);
    return ((SocketChannel) ep.getChannel()).socket();
  }

  public static Socket tlsSocket(Response response) {
    final SslConnection.SslEndPoint sslEndpoint = (SslConnection.SslEndPoint) getEndpoint(response);
    final SelectableChannelEndPoint endpoint =
        (SelectableChannelEndPoint) sslEndpoint.getSslConnection().getEndPoint();
    return ((SocketChannel) endpoint.getChannel()).socket();
  }

  public static EndPoint unwrapEndPoint(Response jettyResponse) {
    return getEndpoint(jettyResponse);
  }

  private static EndPoint getEndpoint(Response response) {
    return response.getRequest().getConnectionMetaData().getConnection().getEndPoint();
  }
}
