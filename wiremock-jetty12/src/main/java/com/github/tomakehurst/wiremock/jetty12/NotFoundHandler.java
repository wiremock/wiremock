/*
 * Copyright (C) 2017-2025 Thomas Akehurst
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

import jakarta.servlet.ServletException;
import org.eclipse.jetty.ee10.servlet.Dispatcher;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.ee10.servlet.ServletContextResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;

public class NotFoundHandler extends ErrorHandler {

  private final ErrorHandler DEFAULT_HANDLER = new ErrorHandler();

  private final ServletContextHandler adminServiceHandler;

  public NotFoundHandler(ServletContextHandler adminServiceHandler) {
    this.adminServiceHandler = adminServiceHandler;
  }

  @Override
  public boolean errorPageForMethod(String method) {
    return true;
  }

  @Override
  public boolean handle(Request request, Response response, Callback callback) throws Exception {
    if (response.getStatus() == 404) {

      // Jetty 12 does not currently support cross context dispatch
      Dispatcher requestDispatcher =
          (Dispatcher) adminServiceHandler.getServletContext().getRequestDispatcher("/not-matched");

      try {
        requestDispatcher.error(
            ((ServletContextRequest) request).getServletApiRequest(),
            ((ServletContextResponse) response).getServletApiResponse());
        callback.succeeded();
        return true;
      } catch (ServletException e) {
        callback.failed(e);
      }
    } else {
      try {
        return DEFAULT_HANDLER.handle(request, response, callback);
      } catch (Exception e) {
        callback.failed(e);
      }
    }

    return false;
  }
}
