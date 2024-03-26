/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http;

import static com.github.tomakehurst.wiremock.http.Response.response;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import com.github.tomakehurst.wiremock.common.ProhibitedNetworkAddressException;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLException;

public class ProxyResponseRenderer implements ResponseRenderer {

  private final HttpClient reverseProxyClient;
  private final HttpClient forwardProxyClient;
  private final boolean preserveHostHeader;
  private final String hostHeaderValue;
  private final SettingsStore settingsStore;
  private final boolean stubCorsEnabled;

  public ProxyResponseRenderer(
      boolean preserveHostHeader,
      String hostHeaderValue,
      SettingsStore settingsStore,
      boolean stubCorsEnabled,
      HttpClient reverseProxyClient,
      HttpClient forwardProxyClient) {

    this.settingsStore = settingsStore;
    this.preserveHostHeader = preserveHostHeader;
    this.hostHeaderValue = hostHeaderValue;
    this.stubCorsEnabled = stubCorsEnabled;

    this.forwardProxyClient = forwardProxyClient;
    this.reverseProxyClient = reverseProxyClient;
  }

  @Override
  public Response render(ServeEvent serveEvent) {
    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

    final ImmutableRequest.Builder requestBuilder =
        ImmutableRequest.create()
            .withAbsoluteUrl(responseDefinition.getProxyUrl())
            .withMethod(responseDefinition.getOriginalRequest().getMethod());
    addRequestHeaders(requestBuilder, responseDefinition);

    GlobalSettings settings = settingsStore.get();

    Request originalRequest = responseDefinition.getOriginalRequest();

    boolean originalRequestBodyExists =
        originalRequest.getBody() != null && originalRequest.getBody().length > 0;

    if (originalRequestBodyExists || originalRequest.containsHeader(HttpClient.CONTENT_LENGTH)) {
      requestBuilder.withBody(originalRequest.getBody());
    }

    Request request = requestBuilder.build();

    HttpClient client = chooseClient(serveEvent.getRequest().isBrowserProxyRequest());

    try {
      final Response httpResponse = client.execute(request);
      return Response.Builder.like(httpResponse)
          .fromProxy(true)
          .headers(headersFrom(httpResponse, responseDefinition))
          .configureDelay(
              settings.getFixedDelay(),
              settings.getDelayDistribution(),
              responseDefinition.getFixedDelayMilliseconds(),
              responseDefinition.getDelayDistribution())
          .chunkedDribbleDelay(responseDefinition.getChunkedDribbleDelay())
          .build();
    } catch (ProhibitedNetworkAddressException e) {
      return response()
          .status(HTTP_INTERNAL_ERROR)
          .headers(new HttpHeaders(new HttpHeader("Content-Type", "text/plain")))
          .body("The target proxy address is denied in WireMock's configuration.")
          .build();
    } catch (SSLException e) {
      return proxyResponseError("SSL", request, e);
    } catch (IOException e) {
      return proxyResponseError("Network", request, e);
    }
  }

  private Response proxyResponseError(String type, Request request, Exception e) {
    return response()
        .status(HTTP_INTERNAL_ERROR)
        .body(
            type
                + " failure trying to make a proxied request from WireMock to "
                + request.getAbsoluteUrl()
                + "\r\n"
                + e.getMessage())
        .build();
  }

  private HttpClient chooseClient(boolean browserProxyRequest) {
    if (browserProxyRequest) {
      return forwardProxyClient;
    } else {
      return reverseProxyClient;
    }
  }

  private HttpHeaders headersFrom(Response response, ResponseDefinition responseDefinition) {
    List<HttpHeader> httpHeaders = new LinkedList<>();
    for (HttpHeader header : response.getHeaders().all()) {
      if (responseHeaderShouldBeTransferred(header.getKey())) {
        httpHeaders.add(header);
      }
    }

    if (responseDefinition.getHeaders() != null) {
      httpHeaders.addAll(responseDefinition.getHeaders().all());
    }

    return new HttpHeaders(httpHeaders);
  }

  private void addRequestHeaders(
      ImmutableRequest.Builder requestBuilder, ResponseDefinition response) {
    Request originalRequest = response.getOriginalRequest();
    List<String> removeProxyRequestHeaders =
        response.getRemoveProxyRequestHeaders() == null
            ? Collections.emptyList()
            : response.getRemoveProxyRequestHeaders();
    for (String key : originalRequest.getAllHeaderKeys()) {
      String lowerCaseKey = key.toLowerCase();
      if (removeProxyRequestHeaders.contains(lowerCaseKey)) {
        continue;
      }
      if (!HttpClient.HOST_HEADER.equals(lowerCaseKey) || preserveHostHeader) {
        List<String> values = originalRequest.header(key).values();
        requestBuilder.withHeader(key, values);
      } else {
        if (hostHeaderValue != null) {
          requestBuilder.withHeader(key, hostHeaderValue);
        } else if (response.getProxyBaseUrl() != null) {
          requestBuilder.withHeader(key, URI.create(response.getProxyBaseUrl()).getAuthority());
        }
      }
    }

    if (response.getAdditionalProxyRequestHeaders() != null) {
      for (String key : response.getAdditionalProxyRequestHeaders().keys()) {
        requestBuilder.withHeader(
            key, response.getAdditionalProxyRequestHeaders().getHeader(key).firstValue());
      }
    }
  }

  public boolean responseHeaderShouldBeTransferred(String key) {
    final String lowerCaseKey = key.toLowerCase();
    return !HttpClient.FORBIDDEN_RESPONSE_HEADERS.contains(lowerCaseKey)
        && (!stubCorsEnabled || !lowerCaseKey.startsWith("access-control"));
  }
}
