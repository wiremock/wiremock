/*
 * Copyright (C) 2013-2024 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.servlet;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;

import com.github.tomakehurst.wiremock.http.*;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WireMockHttpServletMultipartAdapter implements Request.Part {

  private final Part mPart;
  private final HttpHeaders headers;

  public WireMockHttpServletMultipartAdapter(final Part servletPart) {
    mPart = servletPart;
    List<HttpHeader> httpHeaders =
        mPart.getHeaderNames().stream()
            .map(
                name -> {
                  Collection<String> headerValues = servletPart.getHeaders(name);
                  return HttpHeader.httpHeader(
                      name, headerValues.toArray(new String[headerValues.size()]));
                })
            .collect(Collectors.toList());
    headers = new HttpHeaders(httpHeaders);
  }

  public static WireMockHttpServletMultipartAdapter from(Part servletPart) {
    return new WireMockHttpServletMultipartAdapter(servletPart);
  }

  @Override
  public String getName() {
    return mPart.getName();
  }

  @Override
  public String getFilename() {
    return mPart.getSubmittedFileName();
  }

  @Override
  public HttpHeader getHeader(String name) {
    return headers.getHeader(name);
  }

  @Override
  public HttpHeaders getHeaders() {
    return headers;
  }

  @Override
  public Body getBody() {
    try {
      byte[] bytes = mPart.getInputStream().readAllBytes();
      HttpHeader header = getHeader(ContentTypeHeader.KEY);
      ContentTypeHeader contentTypeHeader =
          header.isPresent()
              ? new ContentTypeHeader(header.firstValue())
              : ContentTypeHeader.absent();
      return Body.ofBinaryOrText(bytes, contentTypeHeader);
    } catch (IOException e) {
      return throwUnchecked(e, Body.class);
    }
  }
}
