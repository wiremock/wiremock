/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import static org.eclipse.jetty.http.UriCompliance.UNSAFE;

import org.eclipse.jetty.io.NetworkTrafficListener;
import org.eclipse.jetty.server.*;

public class Jetty11Utils {

  private Jetty11Utils() {}

  private static final int DEFAULT_ACCEPTORS = 3;
  private static final int DEFAULT_HEADER_SIZE = 32768;

  public static ServerConnector createServerConnector(
      Server jettyServer,
      String bindAddress,
      JettySettings jettySettings,
      int port,
      NetworkTrafficListener listener,
      ConnectionFactory... connectionFactories) {

    int acceptors = jettySettings.getAcceptors().orElse(DEFAULT_ACCEPTORS);

    NetworkTrafficServerConnector connector =
        new NetworkTrafficServerConnector(
            jettyServer, null, null, null, acceptors, 2, connectionFactories);

    connector.setPort(port);
    connector.setNetworkTrafficListener(listener);
    setJettySettings(jettySettings, connector);
    connector.setHost(bindAddress);
    return connector;
  }

  public static void setJettySettings(JettySettings jettySettings, ServerConnector connector) {
    jettySettings.getAcceptQueueSize().ifPresent(connector::setAcceptQueueSize);
    jettySettings.getIdleTimeout().ifPresent(connector::setIdleTimeout);
    connector.setShutdownIdleTimeout(jettySettings.getShutdownIdleTimeout().orElse(200L));
  }

  public static HttpConfiguration createHttpConfig(JettySettings jettySettings) {
    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setRequestHeaderSize(
        jettySettings.getRequestHeaderSize().orElse(DEFAULT_HEADER_SIZE));
    httpConfig.setResponseHeaderSize(
        jettySettings.getResponseHeaderSize().orElse(DEFAULT_HEADER_SIZE));
    httpConfig.setSendDateHeader(false);
    httpConfig.setSendXPoweredBy(false);
    httpConfig.setSendServerVersion(false);
    httpConfig.addCustomizer(new SecureRequestCustomizer(false));
    httpConfig.setUriCompliance(UNSAFE);
    return httpConfig;
  }
}
