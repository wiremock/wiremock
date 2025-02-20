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
package com.github.tomakehurst.wiremock.jetty;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.Socket;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.Response;

/** Helper utility interface to inject Jetty 11/12/... specific response / request handling */
public interface JettyHttpUtils {
  static final boolean IS_JETTY = isClassExist("org.eclipse.jetty.server.Request");

  static boolean isJetty() {
    return IS_JETTY;
  }

  private static boolean isClassExist(String type) {
    try {
      ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
      ClassLoader loader = contextCL == null ? JettyHttpUtils.class.getClassLoader() : contextCL;
      Class.forName(type, false, loader);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Unwraps Jetty's {@link Response} out of the {@link HttpServletResponse}
   *
   * @param httpServletResponse {@link HttpServletResponse} instance
   * @return unwrapped {@link Response} instance
   */
  Response unwrapResponse(HttpServletResponse httpServletResponse);

  /**
   * Extracts the raw network socket of out Jetty's {@link Response}
   *
   * @param response {@link Response} instance
   * @return raw network socket
   */
  Socket socket(Response response);

  /**
   * Sets the {@link HttpServletResponse} status and reason (if supported), depending on Jetty
   * version.
   *
   * @param status status
   * @param reason reason
   * @param httpServletResponse {@link HttpServletResponse} instance to set status and reason (if
   *     supported)
   */
  void setStatusWithReason(int status, String reason, HttpServletResponse httpServletResponse);

  /**
   * Extracts the raw network TLS socket of out Jetty's {@link Response}
   *
   * @param response {@link Response} instance
   * @return raw network TLS socket
   */
  Socket tlsSocket(Response response);

  /**
   * Unwraps Jetty's {@link EndPoint} out of the {@link Response}
   *
   * @param response {@link Response} instance
   * @return unwrapped {@link EndPoint} instance
   */
  EndPoint unwrapEndPoint(Response response);

  /**
   * Checks if the {@link HttpServletRequest} is a browser proxy request
   *
   * @param request {@link HttpServletRequest} instance
   * @return {@code true} if is a request isbrowser proxy request, {@code false} otherwise
   */
  boolean isBrowserProxyRequest(HttpServletRequest request);
}
