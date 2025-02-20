/*
 * Copyright (C) 2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.core;

import com.github.tomakehurst.wiremock.common.*;
import com.github.tomakehurst.wiremock.http.ThreadPoolFactory;
import com.github.tomakehurst.wiremock.http.trafficlistener.WiremockNetworkTrafficListener;
import jakarta.servlet.http.HttpServlet;

public interface HttpServerFactoryOptions {

  enum ChunkedEncodingPolicy {
    ALWAYS,
    NEVER,
    BODY_FILE
  }

  boolean getDisableStrictHttpHeaders();

  ThreadPoolFactory threadPoolFactory();

  JettySettings jettySettings();

  WiremockNetworkTrafficListener networkTrafficListener();

  boolean getHttpDisabled();

  String bindAddress();

  int portNumber();

  HttpsSettings httpsSettings();

  boolean getHttp2PlainDisabled();

  boolean getHttp2TlsDisabled();

  BrowserProxySettings browserProxySettings();

  Notifier notifier();

  FileSource filesRoot();

  AsynchronousResponseSettings getAsynchronousResponseSettings();

  ChunkedEncodingPolicy getChunkedEncodingPolicy();

  boolean getStubCorsEnabled();

  long timeout();

  boolean getGzipDisabled();

  int containerThreads();

  Class<? extends HttpServlet> wireMockHandlerDispatchingServletClass();

  Class<?> stubRequestHandlerClass();

  Class<? extends HttpServlet> notMatchedServletClass();
}
