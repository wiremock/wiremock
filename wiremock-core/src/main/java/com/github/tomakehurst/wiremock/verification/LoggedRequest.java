/*
 * Copyright (C) 2011-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.Lazy.lazy;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.getQueryParameter;
import static com.github.tomakehurst.wiremock.common.Urls.toQueryParameterMap;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Dates;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.PathAndQuery;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggedRequest implements Request {

  private final UUID id;
  private final String scheme;
  private final String host;
  private final int port;
  private final PathAndQuery pathAndQuery;
  private final AbsoluteUrl absoluteUrl;
  private final String clientIp;
  private final RequestMethod method;
  private final HttpHeaders headers;
  private final PathParams pathParams;
  private final Map<String, Cookie> cookies;
  private final Map<String, QueryParameter> queryParams;
  private final Map<String, FormParameter> formParameters;
  private final byte[] body;
  private final boolean isBrowserProxyRequest;
  private final Date loggedDate;
  private final Collection<Part> multiparts;
  private final String protocol;

  private final Lazy<String> lazyBodyAsString;
  private final Lazy<String> lazyBodyAsBase64;

  public static LoggedRequest createFrom(Request request) {
    return new LoggedRequest(
        request.getId(),
        request.getScheme(),
        request.getHost(),
        request.getPort(),
        request.getPathAndQueryWithoutPrefix(),
        request.getTypedAbsoluteUrl(),
        request.getMethod(),
        request.getClientIp(),
        request.getHeaders(),
        request.getPathParameters(),
        request.getCookies(),
        request.isBrowserProxyRequest(),
        new Date(),
        request.getBody(),
        request.getParts(),
        request.getProtocol(),
        request.formParameters());
  }

  @JsonCreator
  LoggedRequest(
      @JsonProperty("url") String url,
      @JsonProperty("absoluteUrl") String absoluteUrl,
      @JsonProperty("method") RequestMethod method,
      @JsonProperty("clientIp") String clientIp,
      @JsonProperty("headers") HttpHeaders headers,
      @JsonProperty("cookies") Map<String, Cookie> cookies,
      @JsonProperty("browserProxyRequest") boolean isBrowserProxyRequest,
      @JsonProperty("loggedDate") Date loggedDate,
      @JsonProperty("bodyAsBase64") String bodyAsBase64,
      @JsonProperty("body") String ignoredBodyOnlyUsedForBinding,
      @JsonProperty("multiparts") Collection<Part> multiparts,
      @JsonProperty("protocol") String protocol) {
    this(
        null,
        null,
        null,
        null,
        url != null ? PathAndQuery.parse(url) : null,
        absoluteUrl != null ? AbsoluteUrl.parse(absoluteUrl) : null,
        method,
        clientIp,
        headers,
        PathParams.empty(),
        cookies,
        isBrowserProxyRequest,
        loggedDate,
        decodeBase64(bodyAsBase64),
        multiparts,
        protocol,
        new HashMap<>());
  }

  private LoggedRequest(
      UUID id,
      String scheme,
      String host,
      Integer port,
      PathAndQuery pathAndQuery,
      AbsoluteUrl absoluteUrl,
      RequestMethod method,
      String clientIp,
      HttpHeaders headers,
      PathParams pathParams,
      Map<String, Cookie> cookies,
      boolean isBrowserProxyRequest,
      Date loggedDate,
      byte[] body,
      Collection<Part> multiparts,
      String protocol,
      Map<String, FormParameter> formParameters) {
    this.id = id;
    this.pathAndQuery = pathAndQuery;

    this.absoluteUrl = absoluteUrl;
    if (this.absoluteUrl == null) {
      this.scheme = scheme;
      this.host = host;
      this.port = port != null ? port : -1;
    } else {
      this.scheme = this.absoluteUrl.getScheme().toString();
      this.host = this.absoluteUrl.getHost().toString();
      this.port = this.absoluteUrl.getResolvedPort().getIntValue();
    }

    this.clientIp = clientIp;
    this.method = method;
    this.body = body;
    this.headers = headers;
    this.pathParams = pathParams;
    this.cookies = cookies;
    this.queryParams =
        this.pathAndQuery != null
            ? toQueryParameterMap(this.pathAndQuery.getQueryOrEmpty())
            : Collections.emptyMap();
    this.formParameters = formParameters;
    this.isBrowserProxyRequest = isBrowserProxyRequest;
    this.loggedDate = loggedDate;
    this.multiparts = multiparts;
    this.protocol = protocol;

    lazyBodyAsString = lazy(() -> stringFromBytes(body, encodingFromContentTypeHeaderOrUtf8()));
    lazyBodyAsBase64 = lazy(() -> encodeBase64(body));
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public String getUrl() {
    return pathAndQuery.toString();
  }

  @Override
  public PathAndQuery getPathAndQueryWithoutPrefix() {
    return pathAndQuery;
  }

  @Override
  public String getAbsoluteUrl() {
    return absoluteUrl != null ? absoluteUrl.toString() : null;
  }

  @Override
  public AbsoluteUrl getTypedAbsoluteUrl() {
    return absoluteUrl;
  }

  @Override
  public RequestMethod getMethod() {
    return method;
  }

  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String getClientIp() {
    return clientIp;
  }

  @Override
  @JsonIgnore
  public String getHeader(String key) {
    HttpHeader header = header(key);
    if (header.isPresent()) {
      return header.firstValue();
    }

    return null;
  }

  @Override
  public HttpHeader header(String key) {
    return headers.getHeader(key);
  }

  @Override
  public ContentTypeHeader contentTypeHeader() {
    if (headers != null) {
      return headers.getContentTypeHeader();
    }
    return null;
  }

  private Charset encodingFromContentTypeHeaderOrUtf8() {
    ContentTypeHeader contentTypeHeader = contentTypeHeader();
    if (contentTypeHeader != null) {
      return contentTypeHeader.charset();
    }
    return UTF_8;
  }

  @Override
  public boolean containsHeader(String key) {
    return getHeader(key) != null;
  }

  @JsonIgnore
  @Override
  public PathParams getPathParameters() {
    return pathParams;
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return cookies;
  }

  @Override
  public byte[] getBody() {
    return body;
  }

  @Override
  @JsonProperty("body")
  public String getBodyAsString() {
    return lazyBodyAsString.get();
  }

  @Override
  @JsonProperty("bodyAsBase64")
  public String getBodyAsBase64() {
    return lazyBodyAsBase64.get();
  }

  @Override
  @JsonIgnore
  public Set<String> getAllHeaderKeys() {
    return headers.keys();
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return getQueryParameter(getPathAndQueryWithoutPrefix().getQueryOrEmpty(), key);
  }

  @Override
  public FormParameter formParameter(String key) {
    return getFirstNonNull(formParameters.get(key), FormParameter.absent(key));
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return formParameters;
  }

  @JsonProperty("formParams")
  public Map<String, FormParameter> getFormParameters() {
    return formParameters;
  }

  @JsonProperty("queryParams")
  public Map<String, QueryParameter> getQueryParams() {
    return queryParams;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  @Override
  public boolean isBrowserProxyRequest() {
    return isBrowserProxyRequest;
  }

  @JsonIgnore
  @Override
  public Optional<Request> getOriginalRequest() {
    return Optional.empty();
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  public Date getLoggedDate() {
    return loggedDate;
  }

  public String getLoggedDateString() {
    return loggedDate != null ? Dates.format(loggedDate) : null;
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  @JsonIgnore
  @Override
  public boolean isMultipart() {
    return (multiparts != null && !multiparts.isEmpty());
  }

  @JsonIgnore
  @Override
  public Collection<Part> getParts() {
    return multiparts;
  }

  @JsonIgnore
  @Override
  public Part getPart(final String name) {
    return (multiparts != null && name != null)
        ? multiparts.stream()
            .filter(input -> (name.equals(input.getName())))
            .findFirst()
            .orElse(null)
        : null;
  }
}
