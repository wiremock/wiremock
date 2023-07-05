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
package com.github.tomakehurst.wiremock.verification;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.safelyCreateURL;
import static com.github.tomakehurst.wiremock.common.Urls.splitQueryFromUrl;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.*;
import com.github.tomakehurst.wiremock.common.Dates;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggedRequest implements Request {

  private final String scheme;
  private final String host;
  private final int port;
  private final String url;
  private final String absoluteUrl;
  private final String clientIp;
  private final RequestMethod method;
  private final HttpHeaders headers;
  private final Map<String, Cookie> cookies;
  private final Map<String, QueryParameter> queryParams;
  private final Map<String, FormParameter> formParameters;
  private final byte[] body;
  private final boolean isBrowserProxyRequest;
  private final Date loggedDate;
  private final Collection<Part> multiparts;
  private final String protocol;

  public static LoggedRequest createFrom(Request request) {
    return new LoggedRequest(
        request.getScheme(),
        request.getHost(),
        request.getPort(),
        request.getUrl(),
        request.getAbsoluteUrl(),
        request.getMethod(),
        request.getClientIp(),
        request.getHeaders(),
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
        url,
        absoluteUrl,
        method,
        clientIp,
        headers,
        cookies,
        isBrowserProxyRequest,
        loggedDate,
        decodeBase64(bodyAsBase64),
        multiparts,
        protocol,
        new HashMap<>());
  }

  private LoggedRequest(
      String scheme,
      String host,
      Integer port,
      String url,
      String absoluteUrl,
      RequestMethod method,
      String clientIp,
      HttpHeaders headers,
      Map<String, Cookie> cookies,
      boolean isBrowserProxyRequest,
      Date loggedDate,
      byte[] body,
      Collection<Part> multiparts,
      String protocol,
      Map<String, FormParameter> formParameters) {
    this.url = url;

    this.absoluteUrl = absoluteUrl;
    if (absoluteUrl == null) {
      this.scheme = scheme;
      this.host = host;
      this.port = port != null ? port : -1;
    } else {
      URL fullUrl = safelyCreateURL(absoluteUrl);
      this.scheme = fullUrl.getProtocol();
      this.host = fullUrl.getHost();
      this.port = Urls.getPort(fullUrl);
    }

    this.clientIp = clientIp;
    this.method = method;
    this.body = body;
    this.headers = headers;
    this.cookies = cookies;
    this.queryParams = url != null ? splitQueryFromUrl(url) : Collections.emptyMap();
    this.formParameters = formParameters;
    this.isBrowserProxyRequest = isBrowserProxyRequest;
    this.loggedDate = loggedDate;
    this.multiparts = multiparts;
    this.protocol = protocol;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public String getAbsoluteUrl() {
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
    return stringFromBytes(body, encodingFromContentTypeHeaderOrUtf8());
  }

  @Override
  @JsonProperty("bodyAsBase64")
  public String getBodyAsBase64() {
    return encodeBase64(body);
  }

  @Override
  @JsonIgnore
  public Set<String> getAllHeaderKeys() {
    return headers.keys();
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return getFirstNonNull(queryParams.get(key), QueryParameter.absent(key));
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
