/*
 * Copyright (C) 2014-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.jetty.JettyHttpServerFactoryLoader;
import java.util.Optional;

public class Jetty11HttpServerFactoryLoader implements JettyHttpServerFactoryLoader {
  private static final String JETTY_11 = "11"; /* Jetty 11 */

  @Override
  public Optional<HttpServerFactory> getHttpServerFactory(String jettyMajorVersion) {
    if (JETTY_11.equalsIgnoreCase(jettyMajorVersion)) {
      return Optional.of(
          (options, adminRequestHandler, stubRequestHandler) ->
              new Jetty11HttpServer(options, adminRequestHandler, stubRequestHandler));
    } else {
      return Optional.empty();
    }
  }
}
