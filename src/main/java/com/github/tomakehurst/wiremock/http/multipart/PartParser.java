/*
 * Copyright (C) 2019-2022 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.multipart;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Request.Part;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class PartParser {

  public static Collection<Request.Part> parseFrom(HttpServletRequest request) {
    try {
      final Collection<jakarta.servlet.http.Part> parts = request.getParts();
      if (parts == null) {
        return Collections.emptyList();
      }

      return parts.stream()
          .map(
              part ->
                  new Part() {
                    @Override
                    public String getName() {
                      return part.getName();
                    }

                    @Override
                    public Body getBody() {
                      try {
                        return new Body(part.getInputStream().readAllBytes());
                      } catch (final IOException ex) {
                        throw new UncheckedIOException(ex);
                      }
                    }

                    @Override
                    public HttpHeader getHeader(String name) {
                      return HttpHeader.httpHeader(name, part.getHeader(name));
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                      return new HttpHeaders(
                          part.getHeaderNames().stream()
                              .map(this::getHeader)
                              .toArray(HttpHeader[]::new));
                    }
                  })
          .collect(Collectors.toList());
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    } catch (final ServletException ex) {
      throw new RuntimeException(ex);
    }
  }
}
