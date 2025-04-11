/*
 * Copyright (C) 2016-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.isNullOrEmpty;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.list;

import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.multipart.PartParser;
import com.github.tomakehurst.wiremock.jetty.JettyHttpUtils;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;

public class WireMockHttpServletRequestAdapter implements Request {

  public static final String ORIGINAL_REQUEST_KEY = "wiremock.ORIGINAL_REQUEST";

  private final HttpServletRequest request;
  private byte[] cachedBody;
  private final Supplier<Map<String, QueryParameter>> cachedQueryParams;

  private final Map<String, FormParameter> cachedFormParameters;
  private final boolean browserProxyingEnabled;
  private final String urlPrefixToRemove;
  private final JettyHttpUtils utils;
  private Collection<Part> cachedMultiparts;

  public WireMockHttpServletRequestAdapter(
      HttpServletRequest request,
      String urlPrefixToRemove,
      boolean browserProxyingEnabled,
      JettyHttpUtils utils) {
    this.request = request;
    this.urlPrefixToRemove = urlPrefixToRemove;
    this.browserProxyingEnabled = browserProxyingEnabled;
    this.utils = utils;

    cachedQueryParams = Suppliers.memoize(() -> splitQuery(request.getQueryString()));

    this.cachedFormParameters = getFormParameters(request);
  }

  @Override
  public String getUrl() {
    String url = request.getRequestURI();

    String contextPath = request.getContextPath();
    if (!isNullOrEmpty(contextPath) && url.startsWith(contextPath)) {
      url = url.substring(contextPath.length());
    }
    if (!isNullOrEmpty(urlPrefixToRemove) && url.startsWith(urlPrefixToRemove)) {
      url = url.substring(urlPrefixToRemove.length());
    }

    return withQueryStringIfPresent(url);
  }

  @Override
  public String getAbsoluteUrl() {
    return withQueryStringIfPresent(request.getRequestURL().toString());
  }

  private String withQueryStringIfPresent(String url) {
    return url + (isNullOrEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
  }

  @Override
  public RequestMethod getMethod() {
    return RequestMethod.fromString(request.getMethod().toUpperCase());
  }

  @Override
  public String getScheme() {
    return request.getScheme();
  }

  @Override
  public String getHost() {
    return request.getServerName();
  }

  @Override
  public int getPort() {
    return request.getServerPort();
  }

  @Override
  public String getClientIp() {
    String forwardedForHeader = this.getHeader("X-Forwarded-For");

    if (forwardedForHeader != null && !forwardedForHeader.isEmpty()) {
      return forwardedForHeader;
    }

    return request.getRemoteAddr();
  }

  @Override
  public byte[] getBody() {
    if (cachedBody == null) {
      try {
        byte[] body = request.getInputStream().readAllBytes();
        boolean isGzipped = hasGzipEncoding() || Gzip.isGzipped(body);
        cachedBody = isGzipped ? Gzip.unGzip(body) : body;
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
    }

    return cachedBody;
  }

  private Charset encodingFromContentTypeHeaderOrUtf8() {
    ContentTypeHeader contentTypeHeader = contentTypeHeader();
    if (contentTypeHeader != null) {
      return contentTypeHeader.charset();
    }
    return UTF_8;
  }

  private boolean hasGzipEncoding() {
    String encodingHeader = request.getHeader("Content-Encoding");
    return encodingHeader != null && encodingHeader.contains("gzip");
  }

  @Override
  public String getBodyAsString() {
    return stringFromBytes(getBody(), encodingFromContentTypeHeaderOrUtf8());
  }

  @Override
  public String getBodyAsBase64() {
    return encodeBase64(getBody());
  }

  @Override
  public String getHeader(String key) {
    return request.getHeader(key); // case-insensitive per javadoc
  }

  @Override
  public HttpHeader header(String key) {
    if (request.getHeader(key) == null) {
      return HttpHeader.absent(key);
    } else {
      List<String> valueList = list(request.getHeaders(key));
      if (valueList.isEmpty()) {
        return HttpHeader.empty(key);
      }

      return new HttpHeader(key, valueList);
    }
  }

  @Override
  public ContentTypeHeader contentTypeHeader() {
    String firstValue = getHeader(ContentTypeHeader.KEY);
    return firstValue == null ? ContentTypeHeader.absent() : new ContentTypeHeader(firstValue);
  }

  @Override
  public boolean containsHeader(String key) {
    return header(key).isPresent();
  }

  @Override
  public HttpHeaders getHeaders() {
    if (request instanceof org.eclipse.jetty.server.Request) {
      return getHeadersLinear((org.eclipse.jetty.server.Request) request);
    } else {
      return getHeadersQuadratic();
    }
  }

  private static HttpHeaders getHeadersLinear(org.eclipse.jetty.server.Request request) {
    List<HttpHeader> headers =
        request.getHeaders().stream()
            .map(field -> HttpHeader.httpHeader(field.getName(), field.getValue()))
            .collect(Collectors.toList());
    return new HttpHeaders(headers);
  }

  private HttpHeaders getHeadersQuadratic() {
    List<HttpHeader> headerList = new ArrayList<>();
    for (String key : getAllHeaderKeys()) {
      headerList.add(header(key));
    }

    return new HttpHeaders(headerList);
  }

  @Override
  public Set<String> getAllHeaderKeys() {
    LinkedHashSet<String> headerKeys = new LinkedHashSet<>();
    for (Enumeration<String> headerNames = request.getHeaderNames();
        headerNames.hasMoreElements(); ) {
      headerKeys.add(headerNames.nextElement());
    }

    return headerKeys;
  }

  @Override
  public Map<String, Cookie> getCookies() {
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

    jakarta.servlet.http.Cookie[] cookies =
        getFirstNonNull(request.getCookies(), new jakarta.servlet.http.Cookie[0]);
    for (jakarta.servlet.http.Cookie cookie : cookies) {
      builder.put(cookie.getName(), cookie.getValue());
    }

    return Maps.transformValues(
        builder.build().asMap(), input -> new Cookie(null, List.copyOf(input)));
  }

  @Override
  public QueryParameter queryParameter(String key) {
    Map<String, QueryParameter> queryParams = cachedQueryParams.get();
    return getFirstNonNull(queryParams.get(key), QueryParameter.absent(key));
  }

  @Override
  public FormParameter formParameter(String key) {
    return getFirstNonNull(cachedFormParameters.get(key), FormParameter.absent(key));
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return cachedFormParameters;
  }

  @Override
  public boolean isBrowserProxyRequest() {
    // Avoid the performance hit if browser proxying is disabled
    if (!browserProxyingEnabled || !JettyHttpUtils.isJetty()) {
      return false;
    }

    return utils.isBrowserProxyRequest(request);
  }

  @Override
  public Collection<Part> getParts() {
    if (!isMultipart()) {
      return null;
    }

    if (cachedMultiparts == null) {
      cachedMultiparts = PartParser.parseFrom(this);
    }

    return (cachedMultiparts.isEmpty()) ? null : cachedMultiparts;
  }

  @Override
  public boolean isMultipart() {
    String header = getHeader("Content-Type");
    return (header != null && header.matches("(?i)^\\s*multipart/.*"));
  }

  @Override
  public Part getPart(final String name) {
    if (isNullOrEmpty(name) || (cachedMultiparts == null && getParts() == null)) {
      return null;
    }

    return cachedMultiparts.stream()
        .filter(part -> name.equals(part.getName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Optional<Request> getOriginalRequest() {
    Request originalRequest = (Request) request.getAttribute(ORIGINAL_REQUEST_KEY);
    return Optional.ofNullable(originalRequest);
  }

  @Override
  public String toString() {
    return request.toString() + getBodyAsString();
  }

  @Override
  public String getProtocol() {
    return request.getProtocol();
  }

  private Map<String, FormParameter> getFormParameters(HttpServletRequest request) {

    final String contentType = request.getContentType();
    if (contentType == null || !contentType.contains("application/x-www-form-urlencoded")) {
      return Collections.emptyMap();
    }

    final MultiMap<String> formParameterMultimap = new MultiMap<>();
    final String characterEncoding = request.getCharacterEncoding();
    final Charset charset =
        characterEncoding != null ? Charset.forName(characterEncoding) : Charset.defaultCharset();
    UrlEncoded.decodeTo(getBodyAsString(), formParameterMultimap, charset);

    return formParameterMultimap.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> new FormParameter(entry.getKey(), entry.getValue())));
  }
}
