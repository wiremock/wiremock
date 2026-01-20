/*
 * Copyright (C) 2016-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.jetty;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.isNullOrEmpty;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.getQueryParameter;
import static com.github.tomakehurst.wiremock.jetty.proxy.HttpProxyDetectingHandler.IS_HTTP_PROXY_REQUEST_ATTRIBUTE;
import static com.github.tomakehurst.wiremock.jetty.proxy.HttpsProxyDetectingHandler.IS_HTTPS_PROXY_REQUEST_ATTRIBUTE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.list;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.github.tomakehurst.wiremock.common.Gzip;
import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.http.multipart.PartParser;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.PathAndQuery;

public class WireMockHttpServletRequestAdapter implements Request {

  public static final String ORIGINAL_REQUEST_KEY = "wiremock.ORIGINAL_REQUEST";

  private final HttpServletRequest request;
  private final Lazy<String> url;
  private final Lazy<PathAndQuery> pathAndQuery;
  private final Lazy<byte[]> body;
  private final Lazy<Map<String, Cookie>> cookies;
  private final Lazy<Map<String, FormParameter>> formParameters;
  private final Lazy<Collection<Part>> multiParts;
  private final Lazy<HttpHeaders> headers;

  private final boolean browserProxyingEnabled;
  private final String urlPrefixToRemove;

  public WireMockHttpServletRequestAdapter(
      HttpServletRequest request, String urlPrefixToRemove, boolean browserProxyingEnabled) {
    this.request = request;
    this.urlPrefixToRemove = urlPrefixToRemove;
    this.browserProxyingEnabled = browserProxyingEnabled;

    this.url = Lazy.lazy(this::adaptUrl);
    this.pathAndQuery = Lazy.lazy(this::adaptPathAndQuery);
    this.headers = Lazy.lazy(this::adaptHeaders);
    this.cookies = Lazy.lazy(this::adaptCookies);
    this.body = Lazy.lazy(this::adaptBody);
    this.formParameters = Lazy.lazy(() -> adaptFormParameters(request));
    this.multiParts = Lazy.lazy(this::adaptParts);
  }

  @Override
  public String getUrl() {
    return url.get();
  }

  private String adaptUrl() {
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
  public PathAndQuery getPathAndQueryWithoutPrefix() {
    return pathAndQuery.get();
  }

  private PathAndQuery adaptPathAndQuery() {
    String urlString = getUrl();
    return urlString != null ? PathAndQuery.parse(urlString) : null;
  }

  @Override
  public String getAbsoluteUrl() {
    return withQueryStringIfPresent(request.getRequestURL().toString());
  }

  private volatile AbsoluteUrl typedAbsoluteUrl = null;

  @Override
  public AbsoluteUrl getTypedAbsoluteUrl() {
    if (typedAbsoluteUrl == null) {
      typedAbsoluteUrl = AbsoluteUrl.parse(getAbsoluteUrl());
    }
    return typedAbsoluteUrl;
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
    return body.get();
  }

  private byte[] adaptBody() {
    byte[] body = Exceptions.uncheck(() -> request.getInputStream().readAllBytes(), byte[].class);
    boolean isGzipped = hasGzipEncoding() || Gzip.isGzipped(body);
    return isGzipped ? Gzip.unGzip(body) : body;
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
    return headers.get();
  }

  private HttpHeaders adaptHeaders() {
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
    return cookies.get();
  }

  private Map<String, Cookie> adaptCookies() {
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

    jakarta.servlet.http.Cookie[] cookies =
        getFirstNonNull(request.getCookies(), new jakarta.servlet.http.Cookie[0]);
    for (jakarta.servlet.http.Cookie cookie : cookies) {
      builder.put(cookie.getName(), Urls.decode(cookie.getValue()));
    }

    return Maps.transformValues(
        builder.build().asMap(), input -> new Cookie(null, List.copyOf(input)));
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return getQueryParameter(getPathAndQueryWithoutPrefix().getQueryOrEmpty(), key);
  }

  @Override
  public FormParameter formParameter(String key) {
    return getFirstNonNull(formParameters().get(key), FormParameter.absent(key));
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return formParameters.get();
  }

  @Override
  public boolean isBrowserProxyRequest() {
    // Avoid the performance hit if browser proxying is disabled
    if (!browserProxyingEnabled) {
      return false;
    }

    return Boolean.TRUE.equals(request.getAttribute(IS_HTTPS_PROXY_REQUEST_ATTRIBUTE))
        || Boolean.TRUE.equals(request.getAttribute(IS_HTTP_PROXY_REQUEST_ATTRIBUTE));
  }

  @Override
  public Collection<Part> getParts() {
    return multiParts.get();
  }

  private Collection<Part> adaptParts() {
    if (!isMultipart()) {
      return null;
    }

    Collection<Part> multiParts = PartParser.parseFrom(this);

    return (multiParts.isEmpty()) ? null : multiParts;
  }

  @Override
  public boolean isMultipart() {
    String header = getHeader("Content-Type");
    return (header != null && header.matches("(?i)^\\s*multipart/.*"));
  }

  @Override
  public Part getPart(final String name) {
    if (isNullOrEmpty(name) || (getParts() == null)) {
      return null;
    }

    return getParts().stream().filter(part -> name.equals(part.getName())).findFirst().orElse(null);
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

  private Map<String, FormParameter> adaptFormParameters(HttpServletRequest request) {

    final String contentType = request.getContentType();
    if (contentType == null || !contentType.contains("application/x-www-form-urlencoded")) {
      return Collections.emptyMap();
    }

    final MultiMap<String> formParameterMultimap = new MultiMap<>();
    final String characterEncoding = request.getCharacterEncoding();
    final Charset charset =
        characterEncoding != null ? Charset.forName(characterEncoding) : Charset.defaultCharset();
    try {
      UrlEncoded.decodeTo(getBodyAsString(), formParameterMultimap, charset);
    } catch (IllegalArgumentException ignored) {
      return Collections.emptyMap();
    }
    return formParameterMultimap.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> new FormParameter(entry.getKey(), entry.getValue())));
  }
}
