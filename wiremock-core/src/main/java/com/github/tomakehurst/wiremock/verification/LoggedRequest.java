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
import static com.github.tomakehurst.wiremock.common.ParameterUtils.ensureImmutable;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.stringFromBytes;
import static com.github.tomakehurst.wiremock.common.Urls.toQueryParameterMap;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.annotation.*;
import com.github.tomakehurst.wiremock.common.Dates;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Lazy;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.http.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wiremock.url.AbsoluteUrl;
import org.wiremock.url.PathAndQuery;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoggedRequest implements Request {

  private final UUID id;
  private final String scheme;
  private final String host;
  private final int port;
  private final @NonNull PathAndQuery pathAndQuery;
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
      @JsonProperty("url") @NonNull String url,
      @JsonProperty("absoluteUrl") @Nullable String absoluteUrl,
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
        PathAndQuery.parse(url),
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
      @NonNull PathAndQuery pathAndQuery,
      @Nullable AbsoluteUrl absoluteUrl,
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
      //noinspection DataFlowIssue - getResolvedPort should never return null for a url we support
      this.port = this.absoluteUrl.getResolvedPort().getIntValue();
    }

    this.clientIp = clientIp;
    this.method = method;
    this.body = body;
    this.headers = headers;
    this.pathParams = pathParams;
    this.cookies = ensureImmutable(cookies);
    this.queryParams = toQueryParameterMap(this.pathAndQuery.getQueryOrEmpty());
    this.formParameters = ensureImmutable(formParameters);
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
  public @NonNull String getUrl() {
    return pathAndQuery.toString();
  }

  @Override
  public @NonNull PathAndQuery getPathAndQueryWithoutPrefix() {
    return pathAndQuery;
  }

  @Override
  public @Nullable String getAbsoluteUrl() {
    return absoluteUrl != null ? absoluteUrl.toString() : null;
  }

  @Override
  public @Nullable AbsoluteUrl getTypedAbsoluteUrl() {
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

  public LoggedRequest transform(Consumer<Builder> transformer) {
    Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    private UUID id;
    private String scheme;
    private String host;
    private Integer port;
    private PathAndQuery pathAndQuery;
    private AbsoluteUrl absoluteUrl;
    private RequestMethod method;
    private String clientIp;
    private HttpHeaders headers;
    private PathParams pathParams;
    private Map<String, Cookie> cookies;
    private boolean isBrowserProxyRequest;
    private Date loggedDate;
    private byte[] body;
    private Collection<Part> multiparts;
    private String protocol;
    private Map<String, FormParameter> formParameters;

    public Builder(LoggedRequest original) {
      this.id = original.id;
      this.scheme = original.scheme;
      this.host = original.host;
      this.port = original.port;
      this.pathAndQuery = original.pathAndQuery;
      this.absoluteUrl = original.absoluteUrl;
      this.method = original.method;
      this.clientIp = original.clientIp;
      this.headers = original.headers;
      this.pathParams = original.pathParams;
      this.cookies = original.cookies;
      this.isBrowserProxyRequest = original.isBrowserProxyRequest;
      this.loggedDate = original.loggedDate;
      this.body = original.body != null ? Arrays.copyOf(original.body, original.body.length) : null;
      this.multiparts = original.multiparts;
      this.protocol = original.protocol;
      this.formParameters = original.formParameters;
    }

    public UUID getId() {
      return id;
    }

    public Builder withId(UUID id) {
      this.id = id;
      return this;
    }

    public String getScheme() {
      return scheme;
    }

    public Builder withScheme(String scheme) {
      this.scheme = scheme;
      return this;
    }

    public String getHost() {
      return host;
    }

    public Builder withHost(String host) {
      this.host = host;
      return this;
    }

    public Integer getPort() {
      return port;
    }

    public Builder withPort(Integer port) {
      this.port = port;
      return this;
    }

    public PathAndQuery getPathAndQuery() {
      return pathAndQuery;
    }

    public Builder withPathAndQuery(PathAndQuery pathAndQuery) {
      this.pathAndQuery = pathAndQuery;
      return this;
    }

    public AbsoluteUrl getAbsoluteUrl() {
      return absoluteUrl;
    }

    public Builder withAbsoluteUrl(AbsoluteUrl absoluteUrl) {
      this.absoluteUrl = absoluteUrl;
      return this;
    }

    public RequestMethod getMethod() {
      return method;
    }

    public Builder withMethod(RequestMethod method) {
      this.method = method;
      return this;
    }

    public String getClientIp() {
      return clientIp;
    }

    public Builder withClientIp(String clientIp) {
      this.clientIp = clientIp;
      return this;
    }

    public HttpHeaders getHeaders() {
      return headers;
    }

    public Builder withHeaders(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    public PathParams getPathParams() {
      return pathParams;
    }

    public Builder withPathParams(PathParams pathParams) {
      this.pathParams = pathParams;
      return this;
    }

    public Map<String, Cookie> getCookies() {
      return cookies;
    }

    public Builder withCookies(Map<String, Cookie> cookies) {
      this.cookies = cookies;
      return this;
    }

    public boolean isBrowserProxyRequest() {
      return isBrowserProxyRequest;
    }

    public Builder withBrowserProxyRequest(boolean isBrowserProxyRequest) {
      this.isBrowserProxyRequest = isBrowserProxyRequest;
      return this;
    }

    public Date getLoggedDate() {
      return loggedDate;
    }

    public Builder withLoggedDate(Date loggedDate) {
      this.loggedDate = loggedDate;
      return this;
    }

    public byte[] getBody() {
      return body;
    }

    public Builder withBody(byte[] body) {
      this.body = body;
      return this;
    }

    public Builder withBody(String body) {
      this.body = body != null ? body.getBytes(UTF_8) : null;
      return this;
    }

    public Collection<Part> getMultiparts() {
      return multiparts;
    }

    public Builder withMultiparts(Collection<Part> multiparts) {
      this.multiparts = multiparts;
      return this;
    }

    public String getProtocol() {
      return protocol;
    }

    public Builder withProtocol(String protocol) {
      this.protocol = protocol;
      return this;
    }

    public Map<String, FormParameter> getFormParameters() {
      return formParameters;
    }

    public Builder withFormParameters(Map<String, FormParameter> formParameters) {
      this.formParameters = formParameters;
      return this;
    }

    public LoggedRequest build() {
      return new LoggedRequest(
          id,
          scheme,
          host,
          port,
          pathAndQuery,
          absoluteUrl,
          method,
          clientIp,
          headers,
          pathParams,
          cookies,
          isBrowserProxyRequest,
          loggedDate,
          body,
          multiparts,
          protocol,
          formParameters);
    }
  }
}
