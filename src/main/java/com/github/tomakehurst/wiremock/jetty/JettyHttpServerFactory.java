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
package com.github.tomakehurst.wiremock.jetty;

import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.AdminRequestHandler;
import com.github.tomakehurst.wiremock.http.HttpServer;
import com.github.tomakehurst.wiremock.http.HttpServerFactory;
import com.github.tomakehurst.wiremock.http.StubRequestHandler;
import com.github.tomakehurst.wiremock.jetty11.Jetty11HttpServer;

public class JettyHttpServerFactory implements HttpServerFactory {
  @Override
  public HttpServer buildHttpServer(
      Options options,
      AdminRequestHandler adminRequestHandler,
      StubRequestHandler stubRequestHandler) {
    return new Jetty11HttpServer(options, adminRequestHandler, stubRequestHandler);
  }
}
