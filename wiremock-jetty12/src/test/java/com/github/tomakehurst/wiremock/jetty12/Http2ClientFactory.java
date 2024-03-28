/*
 * Copyright (C) 2019-2024 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.Exceptions;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

class Http2ClientFactory {
  public static HttpClient create() {
    final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client(true);
    final ClientConnector connector = new ClientConnector();
    connector.setSslContextFactory(sslContextFactory);
    HttpClientTransport transport = new HttpClientTransportOverHTTP2(new HTTP2Client(connector));
    HttpClient httpClient = new HttpClient(transport);

    httpClient.setFollowRedirects(false);
    try {
      httpClient.start();
    } catch (Exception e) {
      return Exceptions.throwUnchecked(e, HttpClient.class);
    }

    return httpClient;
  }
}
