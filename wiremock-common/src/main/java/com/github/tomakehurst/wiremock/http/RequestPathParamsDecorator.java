/*
 * Copyright (C) 2024-2025 Thomas Akehurst
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

import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class RequestPathParamsDecorator implements Request {

  private final Request request;
  private final PathTemplate pathTemplate;

  public static Request decorate(Request request, RequestPattern requestPattern) {
    final PathTemplate pathTemplate = requestPattern.getUrlMatcher().getPathTemplate();
    return pathTemplate != null ? new RequestPathParamsDecorator(request, pathTemplate) : request;
  }

  public RequestPathParamsDecorator(Request request, PathTemplate pathTemplate) {
    this.request = request;
    this.pathTemplate = pathTemplate;
  }

  @Override
  public UUID getId() {
    return request.getId();
  }

  @Override
  public String getUrl() {
    return request.getUrl();
  }

  @Override
  public String getAbsoluteUrl() {
    return request.getAbsoluteUrl();
  }

  @Override
  public RequestMethod getMethod() {
    return request.getMethod();
  }

  @Override
  public String getScheme() {
    return request.getScheme();
  }

  @Override
  public String getHost() {
    return request.getHost();
  }

  @Override
  public int getPort() {
    return request.getPort();
  }

  @Override
  public String getClientIp() {
    return request.getClientIp();
  }

  @Override
  public String getHeader(String key) {
    return request.getHeader(key);
  }

  @Override
  public HttpHeader header(String key) {
    return request.header(key);
  }

  @Override
  public ContentTypeHeader contentTypeHeader() {
    return request.contentTypeHeader();
  }

  @Override
  public HttpHeaders getHeaders() {
    return request.getHeaders();
  }

  @Override
  public boolean containsHeader(String key) {
    return request.containsHeader(key);
  }

  @Override
  public Set<String> getAllHeaderKeys() {
    return request.getAllHeaderKeys();
  }

  @Override
  public PathParams getPathParameters() {
    return pathTemplate.parse(getUrl());
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return request.queryParameter(key);
  }

  @Override
  public FormParameter formParameter(String key) {
    return request.formParameter(key);
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return request.formParameters();
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return request.getCookies();
  }

  @Override
  public byte[] getBody() {
    return request.getBody();
  }

  @Override
  public String getBodyAsString() {
    return request.getBodyAsString();
  }

  @Override
  public String getBodyAsBase64() {
    return request.getBodyAsBase64();
  }

  @Override
  public boolean isMultipart() {
    return request.isMultipart();
  }

  @Override
  public Collection<Part> getParts() {
    return request.getParts();
  }

  @Override
  public Part getPart(String name) {
    return request.getPart(name);
  }

  @Override
  public boolean isBrowserProxyRequest() {
    return request.isBrowserProxyRequest();
  }

  @Override
  public Optional<Request> getOriginalRequest() {
    return request.getOriginalRequest();
  }

  @Override
  public String getProtocol() {
    return request.getProtocol();
  }
}
