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
package com.github.tomakehurst.wiremock.http.client.jetty;

import static com.github.tomakehurst.wiremock.http.Response.response;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;
import org.eclipse.jetty.client.BytesRequestContent;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;

public class JettyBackedHttpClient implements HttpClient {

  private final org.eclipse.jetty.client.HttpClient jettyClient;
  private final boolean preserveUserAgentProxyHeader;

  public JettyBackedHttpClient(
      org.eclipse.jetty.client.HttpClient jettyClient, boolean preserveUserAgentProxyHeader) {
    this.jettyClient = jettyClient;
    this.preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
  }

  @Override
  public Response execute(Request request) throws IOException {
    org.eclipse.jetty.client.Request jettyRequest =
        createJettyRequest(request, preserveUserAgentProxyHeader);

    try {
      // Jetty is async by default, so we need to block and wait for the response
      ContentResponse jettyResponse = jettyRequest.send();
      return toWireMockHttpResponse(jettyResponse);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    } catch (TimeoutException e) {
      throw new IOException("Request timed out", e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
      }
      throw new IOException("Request failed", cause);
    }
  }

  private org.eclipse.jetty.client.Request createJettyRequest(
      Request request, boolean preserveUserAgentProxyHeader) throws IOException {

    org.eclipse.jetty.client.Request jettyRequest =
        jettyClient.newRequest(request.getAbsoluteUrl()).method(request.getMethod().getName());

    // Add headers
    request.getHeaders().all().stream()
        .filter(
            header ->
                !FORBIDDEN_REQUEST_HEADERS.contains(header.key().toLowerCase())
                    || (preserveUserAgentProxyHeader && header.key().equalsIgnoreCase(USER_AGENT)))
        .forEach(
            header -> {
              for (String value : header.values()) {
                jettyRequest.headers(headers -> headers.add(header.key(), value));
              }
            });

    // Add request body if present
    if (request.getBody() != null && request.getBody().length > 0) {
      byte[] body = request.getBody();

      // Apply gzip compression if needed
      if (request.containsHeader(CONTENT_ENCODING)
          && request.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
        body = gzipCompress(body);
      }

      // Set the content type
      String contentType =
          request.contentTypeHeader().isPresent()
              ? request.contentTypeHeader().firstValue()
              : "application/octet-stream; charset=utf-8";

      jettyRequest.body(new BytesRequestContent(contentType, body));
    }

    return jettyRequest;
  }

  private static Response toWireMockHttpResponse(ContentResponse jettyResponse) {
    // Convert headers
    HttpFields httpFields = jettyResponse.getHeaders();
    List<HttpHeader> headers = new ArrayList<>();

    for (HttpField field : httpFields) {
      String name = field.getName();
      String value = field.getValue();

      // Group headers by name
      boolean found = false;
      for (HttpHeader existing : headers) {
        if (existing.key().equalsIgnoreCase(name)) {
          // Add to existing header
          List<String> values = new ArrayList<>(existing.values());
          values.add(value);
          headers.remove(existing);
          headers.add(new HttpHeader(name, values));
          found = true;
          break;
        }
      }

      if (!found) {
        headers.add(new HttpHeader(name, value));
      }
    }

    Response.Builder responseBuilder =
        response()
            .status(jettyResponse.getStatus())
            .headers(new HttpHeaders(headers))
            .protocol(jettyResponse.getVersion().asString());

    // Get response body
    byte[] content = jettyResponse.getContent();
    if (content != null && content.length > 0) {
      responseBuilder.body(content);
    }

    // Set status message/reason if present
    String reason = jettyResponse.getReason();
    if (reason != null && !reason.isEmpty()) {
      responseBuilder.statusMessage(reason);
    }

    return responseBuilder.build();
  }

  private static byte[] gzipCompress(byte[] data) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(data.length);
    try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
      gzipStream.write(data);
    }
    return byteStream.toByteArray();
  }
}
