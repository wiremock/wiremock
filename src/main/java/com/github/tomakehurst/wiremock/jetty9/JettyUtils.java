/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty9;

import org.eclipse.jetty.io.ssl.SslConnection;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.net.Socket;
import java.net.URI;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

public class JettyUtils {

    public static Response unwrapResponse(HttpServletResponse httpServletResponse) {
        if (httpServletResponse instanceof HttpServletResponseWrapper) {
            ServletResponse unwrapped = ((HttpServletResponseWrapper) httpServletResponse).getResponse();
            return (Response) unwrapped;
        }

        return (Response) httpServletResponse;
    }

    public static Socket getTlsSocket(Response response) {
        HttpChannel httpChannel = response.getHttpOutput().getHttpChannel();
        SslConnection.DecryptedEndPoint sslEndpoint = (SslConnection.DecryptedEndPoint) httpChannel.getEndPoint();
        Object endpoint = sslEndpoint.getSslConnection().getEndPoint();
        try {
            return (Socket) endpoint.getClass().getMethod("getSocket").invoke(endpoint);
        } catch (Exception e) {
            return throwUnchecked(e, Socket.class);
        }
    }

    public static URI getUri(Request request) {
        try {
            return toUri(request.getClass().getDeclaredMethod("getUri").invoke(request));
        } catch (Exception ignored) {
            try {
                return toUri(request.getClass().getDeclaredMethod("getHttpURI").invoke(request));
            } catch (Exception ignored2) {
                throw new IllegalArgumentException(request + " does not have a getUri or getHttpURI method");
            }
        }
    }

    private static URI toUri(Object httpURI) {
        return URI.create(httpURI.toString());
    }
}
