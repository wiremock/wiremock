/*
 * Copyright (C) 2018-2025 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.requestfilter;

import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.countMatches;
import static com.github.tomakehurst.wiremock.common.Strings.ordinalIndexOf;

import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.CaseInsensitiveKey;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** The type Request wrapper. */
public class RequestWrapper implements Request {

  private final Request delegate;

  private final RequestMethod method;
  private final FieldTransformer<String> absoluteUrlTransformer;
  private final List<HttpHeader> addedHeaders;
  private final List<String> removedHeaders;
  private final Map<CaseInsensitiveKey, FieldTransformer<List<String>>> headerTransformers;
  private final Map<String, Cookie> additionalCookies;
  private final List<String> cookiesToRemove;
  private final Map<String, FieldTransformer<Cookie>> cookieTransformers;
  private final FieldTransformer<Body> bodyTransformer;
  private final FieldTransformer<Part> multipartTransformer;

  /**
   * Instantiates a new Request wrapper.
   *
   * @param delegate the delegate
   */
  public RequestWrapper(Request delegate) {
    this(
        delegate,
        null,
        null,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyList(),
        Collections.emptyMap(),
        null,
        null);
  }

  /**
   * Instantiates a new Request wrapper.
   *
   * @param delegate the delegate
   * @param method the method
   * @param absoluteUrlTransformer the absolute url transformer
   * @param addedHeaders the added headers
   * @param removedHeaders the removed headers
   * @param headerTransformers the header transformers
   * @param additionalCookies the additional cookies
   * @param cookiesToRemove the cookies to remove
   * @param cookieTransformers the cookie transformers
   * @param bodyTransformer the body transformer
   * @param multipartTransformer the multipart transformer
   */
  public RequestWrapper(
      Request delegate,
      RequestMethod method,
      FieldTransformer<String> absoluteUrlTransformer,
      List<HttpHeader> addedHeaders,
      List<String> removedHeaders,
      Map<CaseInsensitiveKey, FieldTransformer<List<String>>> headerTransformers,
      Map<String, Cookie> additionalCookies,
      List<String> cookiesToRemove,
      Map<String, FieldTransformer<Cookie>> cookieTransformers,
      FieldTransformer<Body> bodyTransformer,
      FieldTransformer<Part> multipartTransformer) {
    this.delegate = delegate;

    this.method = method;
    this.absoluteUrlTransformer = absoluteUrlTransformer;
    this.addedHeaders = addedHeaders;
    this.removedHeaders = removedHeaders;
    this.headerTransformers = headerTransformers;
    this.additionalCookies = additionalCookies;
    this.cookiesToRemove = cookiesToRemove;
    this.cookieTransformers = cookieTransformers;
    this.bodyTransformer = bodyTransformer;
    this.multipartTransformer = multipartTransformer;
  }

  /**
   * Create builder.
   *
   * @return the builder
   */
  public static Builder create() {
    return new Builder();
  }

  @Override
  public String getUrl() {
    String absoluteUrl = getAbsoluteUrl();
    int relativeStartIndex =
        countMatches(absoluteUrl, '/') >= 3
            ? ordinalIndexOf(absoluteUrl, "/", 3)
            : absoluteUrl.length();
    return absoluteUrl.substring(relativeStartIndex);
  }

  @Override
  public String getAbsoluteUrl() {
    if (absoluteUrlTransformer != null) {
      return absoluteUrlTransformer.transform(delegate.getAbsoluteUrl());
    }

    return delegate.getAbsoluteUrl();
  }

  @Override
  public RequestMethod getMethod() {
    return getFirstNonNull(method, delegate.getMethod());
  }

  @Override
  public String getScheme() {
    return delegate.getScheme();
  }

  @Override
  public String getHost() {
    return delegate.getHost();
  }

  @Override
  public int getPort() {
    return delegate.getPort();
  }

  @Override
  public String getClientIp() {
    return delegate.getClientIp();
  }

  @Override
  public String getHeader(String key) {
    return getHeaders().getHeader(key).firstValue();
  }

  @Override
  public HttpHeader header(String key) {
    return getHeaders().getHeader(key);
  }

  @Override
  public ContentTypeHeader contentTypeHeader() {
    return delegate.contentTypeHeader();
  }

  @Override
  public HttpHeaders getHeaders() {
    List<HttpHeader> existingHeaders = new ArrayList<>(delegate.getHeaders().all());
    existingHeaders.addAll(addedHeaders);

    List<HttpHeader> combinedHeaders =
        existingHeaders.stream()
            .filter(httpHeader -> !removedHeaders.contains(httpHeader.key()))
            .map(
                httpHeader -> {
                  if (headerTransformers.containsKey(httpHeader.caseInsensitiveKey())) {
                    FieldTransformer<List<String>> transformer =
                        headerTransformers.get(httpHeader.caseInsensitiveKey());
                    List<String> newValues = transformer.transform(httpHeader.values());
                    return new HttpHeader(httpHeader.key(), newValues);
                  }

                  return httpHeader;
                })
            .collect(Collectors.toList());

    return new HttpHeaders(combinedHeaders);
  }

  @Override
  public boolean containsHeader(String key) {
    return getHeaders().getHeader(key).isPresent();
  }

  @Override
  public Set<String> getAllHeaderKeys() {
    return getHeaders().keys();
  }

  @Override
  public Map<String, Cookie> getCookies() {
    Map<String, Cookie> cookieMap = new HashMap<>();
    for (Map.Entry<String, Cookie> entry : delegate.getCookies().entrySet()) {
      Cookie newCookie =
          cookieTransformers.containsKey(entry.getKey())
              ? cookieTransformers.get(entry.getKey()).transform(entry.getValue())
              : entry.getValue();

      if (!cookiesToRemove.contains(entry.getKey())) {
        cookieMap.put(entry.getKey(), newCookie);
      }
    }

    cookieMap.putAll(additionalCookies);

    return Collections.unmodifiableMap(cookieMap);
  }

  @Override
  public QueryParameter queryParameter(String key) {
    return delegate.queryParameter(key);
  }

  @Override
  public FormParameter formParameter(String key) {
    return delegate.formParameter(key);
  }

  @Override
  public Map<String, FormParameter> formParameters() {
    return delegate.formParameters();
  }

  @Override
  public byte[] getBody() {
    if (bodyTransformer != null) {
      return bodyTransformer.transform(new Body(delegate.getBody())).asBytes();
    }

    return delegate.getBody();
  }

  @Override
  public String getBodyAsString() {
    if (bodyTransformer != null) {
      return bodyTransformer.transform(new Body(delegate.getBodyAsString())).asString();
    }

    return delegate.getBodyAsString();
  }

  @Override
  public String getBodyAsBase64() {
    return encodeBase64(getBody());
  }

  @Override
  public boolean isMultipart() {
    return delegate.isMultipart();
  }

  @Override
  public Collection<Part> getParts() {
    if (delegate.getParts() == null || multipartTransformer == null) {
      return delegate.getParts();
    }

    return delegate.getParts().stream()
        .map(multipartTransformer::transform)
        .collect(Collectors.toList());
  }

  @Override
  public Part getPart(String name) {
    if (multipartTransformer != null) {
      return multipartTransformer.transform(delegate.getPart(name));
    }

    return delegate.getPart(name);
  }

  @Override
  public boolean isBrowserProxyRequest() {
    return delegate.isBrowserProxyRequest();
  }

  @Override
  public Optional<Request> getOriginalRequest() {
    return delegate.getOriginalRequest();
  }

  @Override
  public String getProtocol() {
    return delegate.getProtocol();
  }

  /** The type Builder. */
  public static class Builder {

    private RequestMethod requestMethod;
    private FieldTransformer<String> absoluteUrlTransformer;

    private final List<HttpHeader> additionalHeaders = new ArrayList<>();
    private final List<String> headersToRemove = new ArrayList<>();
    private final Map<CaseInsensitiveKey, FieldTransformer<List<String>>> headerTransformers =
        new HashMap<>();

    private final Map<String, Cookie> additionalCookies = new HashMap<>();
    private final List<String> cookiesToRemove = new ArrayList<>();
    private final Map<String, FieldTransformer<Cookie>> cookieTransformers = new HashMap<>();

    private FieldTransformer<Body> bodyTransformer;
    private FieldTransformer<Part> mutlipartTransformer;

    /**
     * Add header builder.
     *
     * @param key the key
     * @param values the values
     * @return the builder
     */
    public Builder addHeader(String key, String... values) {
      additionalHeaders.add(new HttpHeader(key, values));
      return this;
    }

    /**
     * Remove header builder.
     *
     * @param key the key
     * @return the builder
     */
    public Builder removeHeader(String key) {
      headersToRemove.add(key);
      return this;
    }

    /**
     * Transform header builder.
     *
     * @param key the key
     * @param transformer the transformer
     * @return the builder
     */
    public Builder transformHeader(String key, FieldTransformer<List<String>> transformer) {
      headerTransformers.put(CaseInsensitiveKey.from(key), transformer);
      return this;
    }

    /**
     * Sets method.
     *
     * @param method the method
     * @return the method
     */
    public Builder setMethod(RequestMethod method) {
      requestMethod = method;
      return this;
    }

    /**
     * Transform absolute url builder.
     *
     * @param transformer the transformer
     * @return the builder
     */
    public Builder transformAbsoluteUrl(FieldTransformer<String> transformer) {
      absoluteUrlTransformer = transformer;
      return this;
    }

    /**
     * Wrap request.
     *
     * @param request the request
     * @return the request
     */
    public Request wrap(Request request) {
      return new RequestWrapper(
          request,
          requestMethod,
          absoluteUrlTransformer,
          additionalHeaders,
          headersToRemove,
          headerTransformers,
          additionalCookies,
          cookiesToRemove,
          cookieTransformers,
          bodyTransformer,
          mutlipartTransformer);
    }

    /**
     * Transform body builder.
     *
     * @param transformer the transformer
     * @return the builder
     */
    public Builder transformBody(FieldTransformer<Body> transformer) {
      bodyTransformer = transformer;
      return this;
    }

    /**
     * Transform cookie builder.
     *
     * @param name the name
     * @param transformer the transformer
     * @return the builder
     */
    public Builder transformCookie(String name, FieldTransformer<Cookie> transformer) {
      cookieTransformers.put(name, transformer);
      return this;
    }

    /**
     * Transform parts builder.
     *
     * @param transformer the transformer
     * @return the builder
     */
    public Builder transformParts(FieldTransformer<Part> transformer) {
      mutlipartTransformer = transformer;
      return this;
    }

    /**
     * Add cookie builder.
     *
     * @param name the name
     * @param value the value
     * @return the builder
     */
    public Builder addCookie(String name, Cookie value) {
      additionalCookies.put(name, value);
      return this;
    }

    /**
     * Remove cookie builder.
     *
     * @param name the name
     * @return the builder
     */
    public Builder removeCookie(String name) {
      cookiesToRemove.add(name);
      return this;
    }
  }
}
