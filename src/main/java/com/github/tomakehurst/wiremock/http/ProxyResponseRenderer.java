/*
 * Copyright (C) 2011-2023 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.HttpClientUtils.getEntityAsByteArrayAndCloseStream;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

import com.github.tomakehurst.wiremock.common.NetworkAddressRules;
import com.github.tomakehurst.wiremock.common.ProxySettings;
import com.github.tomakehurst.wiremock.common.ssl.KeyStoreSettings;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.store.SettingsStore;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.SSLException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.GzipCompressingEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;

public class ProxyResponseRenderer implements ResponseRenderer {

  private static final String TRANSFER_ENCODING = "transfer-encoding";
  private static final String CONTENT_ENCODING = "content-encoding";
  private static final String CONTENT_LENGTH = "content-length";
  private static final String HOST_HEADER = "host";
  public static final ImmutableList<String> FORBIDDEN_RESPONSE_HEADERS =
      ImmutableList.of(TRANSFER_ENCODING, "connection");
  public static final ImmutableList<String> FORBIDDEN_REQUEST_HEADERS =
      ImmutableList.of(CONTENT_LENGTH, TRANSFER_ENCODING, "connection");

  private final CloseableHttpClient reverseProxyClient;
  private final CloseableHttpClient forwardProxyClient;
  private final boolean preserveHostHeader;
  private final String hostHeaderValue;
  private final SettingsStore settingsStore;
  private final boolean stubCorsEnabled;

  private final NetworkAddressRules targetAddressRules;

  public ProxyResponseRenderer(
      ProxySettings proxySettings,
      KeyStoreSettings trustStoreSettings,
      boolean preserveHostHeader,
      String hostHeaderValue,
      SettingsStore settingsStore,
      boolean trustAllProxyTargets,
      List<String> trustedProxyTargets,
      boolean stubCorsEnabled,
      NetworkAddressRules targetAddressRules,
      int proxyTimeout) {
    this.settingsStore = settingsStore;
    reverseProxyClient =
        HttpClientFactory.createClient(
            1000,
            proxyTimeout,
            proxySettings,
            trustStoreSettings,
            true,
            Collections.<String>emptyList(),
            true);
    forwardProxyClient =
        HttpClientFactory.createClient(
            1000,
            proxyTimeout,
            proxySettings,
            trustStoreSettings,
            trustAllProxyTargets,
            trustAllProxyTargets ? Collections.emptyList() : trustedProxyTargets,
            false);

    this.preserveHostHeader = preserveHostHeader;
    this.hostHeaderValue = hostHeaderValue;
    this.stubCorsEnabled = stubCorsEnabled;
    this.targetAddressRules = targetAddressRules;
  }

  @Override
  public Response render(ServeEvent serveEvent) {
    ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();
    if (targetAddressProhibited(responseDefinition.getProxyUrl())) {
      return response()
          .status(500)
          .headers(new HttpHeaders(new HttpHeader("Content-Type", "text/plain")))
          .body("The target proxy address is denied in WireMock's configuration.")
          .build();
    }

    HttpUriRequest httpRequest = getHttpRequestFor(responseDefinition);
    addRequestHeaders(httpRequest, responseDefinition);

    GlobalSettings settings = settingsStore.get();

    Request originalRequest = responseDefinition.getOriginalRequest();
    if ((originalRequest.getBody() != null && originalRequest.getBody().length > 0)
        || originalRequest.containsHeader(CONTENT_LENGTH)) {
      httpRequest.setEntity(buildEntityFrom(originalRequest));
    }
    CloseableHttpClient client = buildClient(serveEvent.getRequest().isBrowserProxyRequest());
    try (CloseableHttpResponse httpResponse = client.execute(httpRequest)) {
      return response()
          .status(httpResponse.getCode())
          .headers(headersFrom(httpResponse, responseDefinition))
          .body(getEntityAsByteArrayAndCloseStream(httpResponse))
          .fromProxy(true)
          .configureDelay(
              settings.getFixedDelay(),
              settings.getDelayDistribution(),
              responseDefinition.getFixedDelayMilliseconds(),
              responseDefinition.getDelayDistribution())
          .chunkedDribbleDelay(responseDefinition.getChunkedDribbleDelay())
          .build();
    } catch (SSLException e) {
      return proxyResponseError("SSL", httpRequest, e);
    } catch (IOException e) {
      return proxyResponseError("Network", httpRequest, e);
    }
  }

  private boolean targetAddressProhibited(String proxyUrl) {
    String host = URI.create(proxyUrl).getHost();
    try {
      final InetAddress[] resolvedAddresses = InetAddress.getAllByName(host);
      return !Arrays.stream(resolvedAddresses)
          .allMatch(address -> targetAddressRules.isAllowed(address.getHostAddress()));
    } catch (UnknownHostException e) {
      return true;
    }
  }

  private Response proxyResponseError(String type, HttpUriRequest request, Exception e) {
    return response()
        .status(HTTP_INTERNAL_ERROR)
        .body(
            (type
                    + " failure trying to make a proxied request from WireMock to "
                    + extractUri(request))
                + "\r\n"
                + e.getMessage())
        .build();
  }

  private static String extractUri(HttpUriRequest request) {
    try {
      return request.getUri().toString();
    } catch (URISyntaxException ignored) {
    }
    return request.getRequestUri();
  }

  private CloseableHttpClient buildClient(boolean browserProxyRequest) {
    if (browserProxyRequest) {
      return forwardProxyClient;
    } else {
      return reverseProxyClient;
    }
  }

  private HttpHeaders headersFrom(
      HttpResponse httpResponse, ResponseDefinition responseDefinition) {
    List<HttpHeader> httpHeaders = new LinkedList<>();
    for (Header header : httpResponse.getHeaders()) {
      if (responseHeaderShouldBeTransferred(header.getName())) {
        httpHeaders.add(new HttpHeader(header.getName(), header.getValue()));
      }
    }

    if (responseDefinition.getHeaders() != null) {
      httpHeaders.addAll(responseDefinition.getHeaders().all());
    }

    return new HttpHeaders(httpHeaders);
  }

  public static HttpUriRequest getHttpRequestFor(ResponseDefinition response) {
    final RequestMethod method = response.getOriginalRequest().getMethod();
    final String url = response.getProxyUrl();
    return HttpClientFactory.getHttpRequestFor(method, url);
  }

  private void addRequestHeaders(HttpRequest httpRequest, ResponseDefinition response) {
    Request originalRequest = response.getOriginalRequest();
    for (String key : originalRequest.getAllHeaderKeys()) {
      if (requestHeaderShouldBeTransferred(key)) {
        if (!HOST_HEADER.equalsIgnoreCase(key) || preserveHostHeader) {
          List<String> values = originalRequest.header(key).values();
          for (String value : values) {
            httpRequest.addHeader(key, value);
          }
        } else {
          if (hostHeaderValue != null) {
            httpRequest.addHeader(key, hostHeaderValue);
          } else if (response.getProxyBaseUrl() != null) {
            httpRequest.addHeader(key, URI.create(response.getProxyBaseUrl()).getAuthority());
          }
        }
      }
    }

    if (response.getAdditionalProxyRequestHeaders() != null) {
      for (String key : response.getAdditionalProxyRequestHeaders().keys()) {
        httpRequest.setHeader(
            key, response.getAdditionalProxyRequestHeaders().getHeader(key).firstValue());
      }
    }
  }

  private static boolean requestHeaderShouldBeTransferred(String key) {
    return !FORBIDDEN_REQUEST_HEADERS.contains(key.toLowerCase());
  }

  private boolean responseHeaderShouldBeTransferred(String key) {
    final String lowerCaseKey = key.toLowerCase();
    return !FORBIDDEN_RESPONSE_HEADERS.contains(lowerCaseKey)
        && (!stubCorsEnabled || !lowerCaseKey.startsWith("access-control"));
  }

  private static HttpEntity buildEntityFrom(Request originalRequest) {
    ContentTypeHeader contentTypeHeader = originalRequest.contentTypeHeader().or("text/plain");
    ContentType contentType =
        ContentType.create(
            contentTypeHeader.mimeTypePart(), contentTypeHeader.encodingPart().or("utf-8"));

    if (originalRequest.containsHeader(TRANSFER_ENCODING)
        && originalRequest.header(TRANSFER_ENCODING).firstValue().equals("chunked")) {
      return applyGzipWrapperIfRequired(
          originalRequest,
          new InputStreamEntity(
              new ByteArrayInputStream(originalRequest.getBody()), -1, contentType));
    }

    return applyGzipWrapperIfRequired(
        originalRequest,
        new ByteArrayEntity(
            originalRequest.getBody(),
            originalRequest.contentTypeHeader().isPresent() ? contentType : null));
  }

  private static HttpEntity applyGzipWrapperIfRequired(
      Request originalRequest, HttpEntity content) {
    if (originalRequest.containsHeader(CONTENT_ENCODING)
        && originalRequest.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
      return new GzipCompressingEntity(content);
    }

    return content;
  }
}
