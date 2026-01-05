/*
 * Copyright (C) 2023-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.http.client.netty;

import static com.github.tomakehurst.wiremock.http.Response.response;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.client.HttpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;

public class NettyBackedHttpClient implements HttpClient {

  private final reactor.netty.http.client.HttpClient nettyHttpClient;
  private final boolean preserveUserAgentProxyHeader;

  public NettyBackedHttpClient(
      reactor.netty.http.client.HttpClient nettyHttpClient, boolean preserveUserAgentProxyHeader) {
    this.nettyHttpClient = nettyHttpClient;
    this.preserveUserAgentProxyHeader = preserveUserAgentProxyHeader;
  }

  @Override
  public Response execute(Request request) throws IOException {
    try {
      return nettyHttpClient
          .request(HttpMethod.valueOf(request.getMethod().getName()))
          .uri(request.getAbsoluteUrl())
          .send(
              (req, nettyOutbound) -> {
                // Add headers
                request.getHeaders().all().stream()
                    .filter(
                        header -> {
                          String headerKey = header.key().toLowerCase();
                          // Allow Transfer-Encoding and Connection to pass through for proxy
                          // scenarios
                          if (headerKey.equals(TRANSFER_ENCODING.toLowerCase())
                              || headerKey.equals(CONNECTION.toLowerCase())) {
                            return true;
                          }
                          // Filter other forbidden headers unless preserveUserAgentProxyHeader is
                          // set
                          return !FORBIDDEN_REQUEST_HEADERS.contains(headerKey)
                              || (preserveUserAgentProxyHeader
                                  && header.key().equalsIgnoreCase(USER_AGENT));
                        })
                    .forEach(
                        header -> {
                          // Set Host header explicitly to override Reactor Netty's default
                          if (header.key().equalsIgnoreCase(HOST_HEADER)) {
                            req.header(
                                io.netty.handler.codec.http.HttpHeaderNames.HOST,
                                header.firstValue());
                          }
                          // Set Transfer-Encoding explicitly to prevent Reactor Netty from removing
                          // it
                          else if (header.key().equalsIgnoreCase(TRANSFER_ENCODING)) {
                            req.header(
                                io.netty.handler.codec.http.HttpHeaderNames.TRANSFER_ENCODING,
                                header.firstValue());
                          } else {
                            for (String value : header.values()) {
                              req.addHeader(header.key(), value);
                            }
                          }
                        });

                // Send body if present
                if (request.getBody() != null) {
                  byte[] bodyBytes = request.getBody();

                  // Apply gzip compression if needed
                  if (request.containsHeader(CONTENT_ENCODING)
                      && request.header(CONTENT_ENCODING).firstValue().contains("gzip")) {
                    try {
                      bodyBytes = gzipCompress(bodyBytes);
                    } catch (IOException e) {
                      return Mono.error(e);
                    }
                  }

                  // Check if original request was chunked
                  boolean shouldUseChunked =
                      request.containsHeader(TRANSFER_ENCODING)
                          && request
                              .header(TRANSFER_ENCODING)
                              .firstValue()
                              .toLowerCase()
                              .contains("chunked");

                  if (shouldUseChunked) {
                    // Force chunked encoding by sending data byte-by-byte
                    byte[] finalBodyBytes = bodyBytes;
                    reactor.core.publisher.Flux<ByteBuf> byteFlux =
                        reactor.core.publisher.Flux.create(
                            sink -> {
                              for (byte b : finalBodyBytes) {
                                sink.next(Unpooled.wrappedBuffer(new byte[] {b}));
                              }
                              sink.complete();
                            });
                    return nettyOutbound.send(byteFlux);
                  } else {
                    ByteBuf buffer = Unpooled.wrappedBuffer(bodyBytes);
                    return nettyOutbound.send(ByteBufMono.just(buffer));
                  }
                }

                return nettyOutbound;
              })
          .responseSingle(
              (nettyResponse, byteBufMono) -> {
                // Convert headers - use Set to avoid duplicates since forEach iterates over all
                // entries
                List<HttpHeader> headers = new ArrayList<>();
                Set<String> processedHeaders = new HashSet<>();
                nettyResponse
                    .responseHeaders()
                    .forEach(
                        entry -> {
                          String headerName = entry.getKey();
                          if (!processedHeaders.contains(headerName)) {
                            List<String> values =
                                nettyResponse.responseHeaders().getAll(headerName);
                            headers.add(new HttpHeader(headerName, values));
                            processedHeaders.add(headerName);
                          }
                        });

                // Build response
                Response.Builder responseBuilder =
                    response()
                        .status(nettyResponse.status().code())
                        .headers(new HttpHeaders(headers))
                        .protocol(nettyResponse.version().text());

                // Set status message if present
                String statusText = nettyResponse.status().reasonPhrase();
                if (statusText != null && !statusText.isEmpty()) {
                  responseBuilder.statusMessage(statusText);
                }

                // Get response body
                return byteBufMono
                    .asByteArray()
                    .map(
                        bytes -> {
                          responseBuilder.body(bytes);
                          return responseBuilder.build();
                        })
                    .defaultIfEmpty(responseBuilder.build());
              })
          .block();
    } catch (RuntimeException e) {
      e.printStackTrace();
      // Let runtime exceptions (like ProhibitedNetworkAddressException) pass through
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof IOException) {
        throw (IOException) e;
      }
      throw new IOException("Error executing request", e);
    }
  }

  private static byte[] gzipCompress(byte[] data) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(data);
    }
    return byteArrayOutputStream.toByteArray();
  }
}
