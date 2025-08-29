/*
 * Copyright (C) 2023-2025 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static java.util.Objects.requireNonNull;

import com.github.tomakehurst.wiremock.common.Strings;
import com.github.tomakehurst.wiremock.common.Urls;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** The type Immutable request. */
public class ImmutableRequest implements Request {

  private final String absoluteUrl;
  private final Map<String, QueryParameter> queryParams;
  private final RequestMethod method;
  private final String protocol;
  private final String scheme;
  private final String host;
  private final int port;
  private final String clientIp;
  private final HttpHeaders headers;
  private final byte[] body;
  private final boolean multipart;

  private final Map<String, Part> parts;
  private final boolean browserProxyRequest;

  /**
   * Create builder.
   *
   * @return the builder
   */
  public static Builder create() {
    return new Builder();
  }

  /**
   * Instantiates a new Immutable request.
   *
   * @param absoluteUrl the absolute url
   * @param method the method
   * @param protocol the protocol
   * @param clientIp the client ip
   * @param headers the headers
   * @param body the body
   * @param multipart the multipart
   * @param browserProxyRequest the browser proxy request
   */
  protected ImmutableRequest(
      String absoluteUrl,
      RequestMethod method,
      String protocol,
      String clientIp,
      HttpHeaders headers,
      byte[] body,
      boolean multipart,
      boolean browserProxyRequest) {
    this.absoluteUrl = requireNonNull(absoluteUrl);
    this.queryParams = Urls.splitQueryFromUrl(absoluteUrl);
    this.method = requireNonNull(method);
    this.protocol = protocol;

    final URI uri = URI.create(absoluteUrl);
    this.scheme = uri.getScheme();
    this.host = uri.getHost();
    this.port = uri.getPort();

    this.clientIp = clientIp;
    this.headers = headers;
    this.body = body;
    this.multipart = multipart;
    this.parts = Collections.emptyMap();
    this.browserProxyRequest = browserProxyRequest;
  }

  @Override
  public String getUrl() {
    return Urls.getPathAndQuery(absoluteUrl);
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
  public String getHeader(String key) {
    final HttpHeader header = header(key);
    return header.isPresent() ? header.firstValue() : null;
  }

  @Override
  public HttpHeader header(String key) {
    return headers.getHeader(key);
  }

  @Override
  public ContentTypeHeader contentTypeHeader() {
    return headers.getContentTypeHeader();
  }

  @Override
  public HttpHeaders getHeaders() {
    return headers;
  }

  @Override
  public boolean containsHeader(String key) {
    return headers.getHeader(key).isPresent();
  }

  @Override
  public Set<String> getAllHeaderKeys() {
    return headers.keys();
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return queryParams.get(key);
  }

  @Override
  public FormParameter formParameter(String key) {
    return null;
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return Collections.emptyMap();
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return null;
  }

  @Override
  public byte[] getBody() {
    return body;
  }

  @Override
  public String getBodyAsString() {
    return Strings.stringFromBytes(body);
  }

  @Override
  public String getBodyAsBase64() {
    return encodeBase64(getBody());
  }

  @Override
  public boolean isMultipart() {
    return multipart;
  }

  @Override
  public Collection<Part> getParts() {
    return parts.values();
  }

  @Override
  public Part getPart(String name) {
    return parts.get(name);
  }

  @Override
  public boolean isBrowserProxyRequest() {
    return browserProxyRequest;
  }

  @Override
  public Optional<Request> getOriginalRequest() {
    return Optional.empty();
  }

  @Override
  public String getProtocol() {
    return protocol;
  }

  /** The type Builder. */
  public static class Builder {
    private String absouteUrl;
    private RequestMethod requestMethod;
    private String protocol;
    private String clientIp;
    private List<HttpHeader> headers = new ArrayList<>();
    private byte[] body;
    private boolean multipart;
    private boolean browserProxyRequest;

    /**
     * With absolute url builder.
     *
     * @param absouteUrl the absoute url
     * @return the builder
     */
    public Builder withAbsoluteUrl(String absouteUrl) {
      this.absouteUrl = absouteUrl;
      return this;
    }

    /**
     * With method builder.
     *
     * @param requestMethod the request method
     * @return the builder
     */
    public Builder withMethod(RequestMethod requestMethod) {
      this.requestMethod = requestMethod;
      return this;
    }

    /**
     * With headers builder.
     *
     * @param headers the headers
     * @return the builder
     */
    public Builder withHeaders(HttpHeaders headers) {
      this.headers = new ArrayList<>(headers.all());
      return this;
    }

    /**
     * With header builder.
     *
     * @param key the key
     * @param value the value
     * @return the builder
     */
    public Builder withHeader(String key, String value) {
      this.headers.add(new HttpHeader(key, Collections.singletonList(value)));
      return this;
    }

    /**
     * With header builder.
     *
     * @param key the key
     * @param values the values
     * @return the builder
     */
    public Builder withHeader(String key, Collection<String> values) {
      this.headers.add(new HttpHeader(key, values));
      return this;
    }

    /**
     * With body builder.
     *
     * @param body the body
     * @return the builder
     */
    public Builder withBody(byte[] body) {
      this.body = body;
      return this;
    }

    /**
     * Build immutable request.
     *
     * @return the immutable request
     */
    public ImmutableRequest build() {
      return new ImmutableRequest(
          absouteUrl,
          requestMethod,
          protocol,
          clientIp,
          new HttpHeaders(headers),
          body,
          multipart,
          browserProxyRequest);
    }
  }
}
