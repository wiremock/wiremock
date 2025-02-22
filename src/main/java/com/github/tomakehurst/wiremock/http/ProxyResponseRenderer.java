/*
 * Copyright (C) 2011-2025 Thomas Akehurst
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.net.ssl.SSLException;

public class ProxyResponseRenderer implements ResponseRenderer {

  private final HttpClient reverseProxyClient;
  private final HttpClient forwardProxyClient;
  private final boolean preserveHostHeader;
  private final String hostHeaderValue;
  private final SettingsStore settingsStore;
  private final boolean stubCorsEnabled;
  private final Set<String> supportedEncodings;

  @SuppressWarnings("unused")
  public ProxyResponseRenderer(
      boolean preserveHostHeader,
      String hostHeaderValue,
      SettingsStore settingsStore,
      boolean stubCorsEnabled,
      HttpClient reverseProxyClient,
      HttpClient forwardProxyClient) {

    this(
        preserveHostHeader,
        hostHeaderValue,
        settingsStore,
        stubCorsEnabled,
        null,
        reverseProxyClient,
        forwardProxyClient);
  }

  public ProxyResponseRenderer(
      boolean preserveHostHeader,
      String hostHeaderValue,
      SettingsStore settingsStore,
      boolean stubCorsEnabled,
      Set<String> supportedEncodings,
      HttpClient reverseProxyClient,
      HttpClient forwardProxyClient) {

    this.settingsStore = settingsStore;
    this.preserveHostHeader = preserveHostHeader;
    this.hostHeaderValue = hostHeaderValue;
    this.stubCorsEnabled = stubCorsEnabled;
    this.supportedEncodings = supportedEncodings;

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
          .headers(HeaderUtil.headersFrom(httpResponse, responseDefinition, stubCorsEnabled))
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
      switch (lowerCaseKey) {
        case HttpClient.HOST_HEADER:
          addHostHeader(requestBuilder, response, key, originalRequest);
          break;
        case HttpClient.ACCEPT_ENCODING_HEADER:
          addAcceptEncodingHeader(requestBuilder, key, originalRequest);
          break;
        default:
          copyHeader(requestBuilder, key, originalRequest);
          break;
      }
    }

    if (response.getAdditionalProxyRequestHeaders() != null) {
      for (String key : response.getAdditionalProxyRequestHeaders().keys()) {
        requestBuilder.withHeader(
            key, response.getAdditionalProxyRequestHeaders().getHeader(key).firstValue());
      }
    }
  }

  private void addHostHeader(
      ImmutableRequest.Builder requestBuilder,
      ResponseDefinition response,
      String key,
      Request originalRequest) {
    if (preserveHostHeader) {
      copyHeader(requestBuilder, key, originalRequest);
    } else if (hostHeaderValue != null) {
      requestBuilder.withHeader(key, hostHeaderValue);
    } else if (response.getProxyBaseUrl() != null) {
      requestBuilder.withHeader(key, URI.create(response.getProxyBaseUrl()).getAuthority());
    }
  }

  private void addAcceptEncodingHeader(
      ImmutableRequest.Builder requestBuilder, String key, Request originalRequest) {
    if (supportedEncodings == null) {
      copyHeader(requestBuilder, key, originalRequest);
    } else {
      List<String> prunedAcceptEncodings =
          originalRequest.header(key).values().stream()
              .flatMap(s -> Arrays.stream(s.split(",")))
              .map(String::trim)
              .filter(supportedEncodings::contains)
              .collect(Collectors.toList());
      if (!prunedAcceptEncodings.isEmpty()) {
        requestBuilder.withHeader(key, String.join(",", prunedAcceptEncodings));
      }
    }
  }

  private static void copyHeader(
      ImmutableRequest.Builder requestBuilder, String key, Request originalRequest) {
    List<String> values = originalRequest.header(key).values();
    requestBuilder.withHeader(key, values);
  }
}
