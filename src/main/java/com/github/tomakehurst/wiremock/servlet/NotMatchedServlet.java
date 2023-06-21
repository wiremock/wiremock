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
package com.github.tomakehurst.wiremock.servlet;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.diff.DiffEventData;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class NotMatchedServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Optional.ofNullable(req.getAttribute(ServeEvent.ORIGINAL_SERVE_EVENT_KEY))
        .map(ServeEvent.class::cast)
        .flatMap(ServeEvent::getDiffSubEvent)
        .ifPresentOrElse(
            diffSubEvent -> {
              final DiffEventData diffData = diffSubEvent.getDataAs(DiffEventData.class);
              resp.setStatus(diffData.getStatus());
              resp.setContentType(diffData.getContentType());
              resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

              try (final PrintWriter writer = resp.getWriter()) {
                writer.write(diffData.getReport());
                writer.flush();
              } catch (IOException e) {
                Exceptions.throwUnchecked(e);
              }
            },
            () -> Exceptions.uncheck(() -> resp.sendError(404)));
  }
}
