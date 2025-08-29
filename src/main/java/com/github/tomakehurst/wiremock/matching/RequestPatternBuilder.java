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
package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** The type Request pattern builder. */
public class RequestPatternBuilder {

  private String scheme;
  private StringValuePattern hostPattern;
  private Integer port;
  private StringValuePattern clientIpPattern;
  private UrlPattern url = UrlPattern.ANY;
  private RequestMethod method = RequestMethod.ANY;
  private Map<String, MultiValuePattern> headers = new LinkedHashMap<>();
  private Map<String, MultiValuePattern> queryParams = new LinkedHashMap<>();

  private Map<String, MultiValuePattern> formParams = new LinkedHashMap<>();
  private Map<String, StringValuePattern> pathParams = new LinkedHashMap<>();
  private List<ContentPattern<?>> bodyPatterns = new ArrayList<>();
  private Map<String, StringValuePattern> cookies = new LinkedHashMap<>();
  private BasicCredentials basicCredentials;
  private List<MultipartValuePattern> multiparts = new LinkedList<>();

  private ValueMatcher<Request> customMatcher;

  private CustomMatcherDefinition customMatcherDefinition;

  /** Instantiates a new Request pattern builder. */
  public RequestPatternBuilder() {}

  /**
   * Instantiates a new Request pattern builder.
   *
   * @param customMatcher the custom matcher
   */
  public RequestPatternBuilder(ValueMatcher<Request> customMatcher) {
    this.customMatcher = customMatcher;
  }

  /**
   * Instantiates a new Request pattern builder.
   *
   * @param method the method
   * @param url the url
   */
  public RequestPatternBuilder(RequestMethod method, UrlPattern url) {
    this.method = method;
    this.url = url;
  }

  /**
   * Instantiates a new Request pattern builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @param parameters the parameters
   */
  public RequestPatternBuilder(String customRequestMatcherName, Parameters parameters) {
    this.customMatcherDefinition =
        new CustomMatcherDefinition(customRequestMatcherName, parameters);
  }

  /**
   * New request pattern request pattern builder.
   *
   * @param method the method
   * @param url the url
   * @return the request pattern builder
   */
  public static RequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
    return new RequestPatternBuilder(method, url);
  }

  /**
   * New request pattern request pattern builder.
   *
   * @return the request pattern builder
   */
  public static RequestPatternBuilder newRequestPattern() {
    return new RequestPatternBuilder();
  }

  /**
   * For custom matcher request pattern builder.
   *
   * @param requestMatcher the request matcher
   * @return the request pattern builder
   */
  public static RequestPatternBuilder forCustomMatcher(ValueMatcher<Request> requestMatcher) {
    return new RequestPatternBuilder(requestMatcher);
  }

  /**
   * For custom matcher request pattern builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @param parameters the parameters
   * @return the request pattern builder
   */
  public static RequestPatternBuilder forCustomMatcher(
      String customRequestMatcherName, Parameters parameters) {
    return new RequestPatternBuilder(customRequestMatcherName, parameters);
  }

  /**
   * All requests request pattern builder.
   *
   * @return the request pattern builder
   */
  public static RequestPatternBuilder allRequests() {
    return new RequestPatternBuilder(RequestMethod.ANY, WireMock.anyUrl());
  }

  /**
   * Construct a builder that uses an existing RequestPattern as a template
   *
   * @param requestPattern A RequestPattern to copy
   * @return A builder based on the RequestPattern
   */
  public static RequestPatternBuilder like(RequestPattern requestPattern) {
    RequestPatternBuilder builder = new RequestPatternBuilder();
    builder.scheme = requestPattern.getScheme();
    builder.hostPattern = requestPattern.getHost();
    builder.port = requestPattern.getPort();
    builder.clientIpPattern = requestPattern.getClientIp();
    builder.url = requestPattern.getUrlMatcher();
    builder.method = requestPattern.getMethod();
    if (requestPattern.getHeaders() != null) {
      builder.headers = requestPattern.getHeaders();
    }
    if (requestPattern.getPathParameters() != null) {
      builder.pathParams = requestPattern.getPathParameters();
    }
    if (requestPattern.getQueryParameters() != null) {
      builder.queryParams = requestPattern.getQueryParameters();
    }
    if (requestPattern.getFormParameters() != null) {
      builder.formParams = requestPattern.getFormParameters();
    }
    if (requestPattern.getCookies() != null) {
      builder.cookies = requestPattern.getCookies();
    }
    if (requestPattern.getBodyPatterns() != null) {
      builder.bodyPatterns = requestPattern.getBodyPatterns();
    }
    if (requestPattern.hasInlineCustomMatcher()) {
      builder.customMatcher = requestPattern.getMatcher();
    }
    if (requestPattern.getMultipartPatterns() != null) {
      builder.multiparts = requestPattern.getMultipartPatterns();
    }
    builder.basicCredentials = requestPattern.getBasicAuthCredentials();
    builder.customMatcherDefinition = requestPattern.getCustomMatcher();
    return builder;
  }

  /**
   * But request pattern builder.
   *
   * @return the request pattern builder
   */
  public RequestPatternBuilder but() {
    return this;
  }

  /**
   * With scheme request pattern builder.
   *
   * @param scheme the scheme
   * @return the request pattern builder
   */
  public RequestPatternBuilder withScheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  /**
   * With host request pattern builder.
   *
   * @param hostPattern the host pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withHost(StringValuePattern hostPattern) {
    this.hostPattern = hostPattern;
    return this;
  }

  /**
   * With port request pattern builder.
   *
   * @param port the port
   * @return the request pattern builder
   */
  public RequestPatternBuilder withPort(int port) {
    this.port = port;
    return this;
  }

  /**
   * With client ip request pattern builder.
   *
   * @param clientIpPattern the client ip pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withClientIp(StringValuePattern clientIpPattern) {
    this.clientIpPattern = clientIpPattern;
    return this;
  }

  /**
   * With url request pattern builder.
   *
   * @param url the url
   * @return the request pattern builder
   */
  public RequestPatternBuilder withUrl(String url) {
    this.url = WireMock.urlEqualTo(url);
    return this;
  }

  /**
   * With url request pattern builder.
   *
   * @param urlPattern the url pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withUrl(UrlPattern urlPattern) {
    this.url = urlPattern;
    return this;
  }

  /**
   * With header request pattern builder.
   *
   * @param key the key
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withHeader(String key, StringValuePattern valuePattern) {
    headers.put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  /**
   * With header request pattern builder.
   *
   * @param key the key
   * @param multiValuePattern the multi value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withHeader(String key, MultiValuePattern multiValuePattern) {
    headers.put(key, multiValuePattern);
    return this;
  }

  /**
   * Without header request pattern builder.
   *
   * @param key the key
   * @return the request pattern builder
   */
  public RequestPatternBuilder withoutHeader(String key) {
    headers.put(key, MultiValuePattern.absent());
    return this;
  }

  /**
   * With path param request pattern builder.
   *
   * @param key the key
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withPathParam(String key, StringValuePattern valuePattern) {
    pathParams.put(key, valuePattern);
    return this;
  }

  /**
   * With query param request pattern builder.
   *
   * @param key the key
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withQueryParam(String key, StringValuePattern valuePattern) {
    queryParams.put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  /**
   * With query param request pattern builder.
   *
   * @param key the key
   * @param multiValuePattern the multi value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withQueryParam(String key, MultiValuePattern multiValuePattern) {
    queryParams.put(key, multiValuePattern);
    return this;
  }

  /**
   * With form param request pattern builder.
   *
   * @param key the key
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withFormParam(String key, StringValuePattern valuePattern) {
    formParams.put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  /**
   * With form param request pattern builder.
   *
   * @param key the key
   * @param multiValuePattern the multi value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withFormParam(String key, MultiValuePattern multiValuePattern) {
    formParams.put(key, multiValuePattern);
    return this;
  }

  /**
   * Without form param request pattern builder.
   *
   * @param key the key
   * @return the request pattern builder
   */
  public RequestPatternBuilder withoutFormParam(String key) {
    formParams.put(key, MultiValuePattern.absent());
    return this;
  }

  /**
   * Without query param request pattern builder.
   *
   * @param key the key
   * @return the request pattern builder
   */
  public RequestPatternBuilder withoutQueryParam(String key) {
    queryParams.put(key, MultiValuePattern.absent());
    return this;
  }

  /**
   * With cookie request pattern builder.
   *
   * @param key the key
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withCookie(String key, StringValuePattern valuePattern) {
    cookies.put(key, valuePattern);
    return this;
  }

  /**
   * With basic auth request pattern builder.
   *
   * @param basicCredentials the basic credentials
   * @return the request pattern builder
   */
  public RequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
    this.basicCredentials = basicCredentials;
    return this;
  }

  /**
   * With request body request pattern builder.
   *
   * @param valuePattern the value pattern
   * @return the request pattern builder
   */

  public RequestPatternBuilder withRequestBody(ContentPattern valuePattern) {
    this.bodyPatterns.add(valuePattern);
    return null;
  }

  /**
   * With request body part request pattern builder.
   *
   * @param multiPattern the multi pattern
   * @return the request pattern builder
   */
  public RequestPatternBuilder withRequestBodyPart(MultipartValuePattern multiPattern) {
    if (multiPattern != null) {
      multiparts.add(multiPattern);
    }
    return this;
  }

  /**
   * With any request body part request pattern builder.
   *
   * @param multiPatternBuilder the multi pattern builder
   * @return the request pattern builder
   */
  public RequestPatternBuilder withAnyRequestBodyPart(
      MultipartValuePatternBuilder multiPatternBuilder) {
    return withRequestBodyPart(
        multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ANY).build());
  }

  /**
   * With all request body parts request pattern builder.
   *
   * @param multiPatternBuilder the multi pattern builder
   * @return the request pattern builder
   */
  public RequestPatternBuilder withAllRequestBodyParts(
      MultipartValuePatternBuilder multiPatternBuilder) {
    return withRequestBodyPart(
        multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ALL).build());
  }

  /**
   * And matching request pattern builder.
   *
   * @param customMatcher the custom matcher
   * @return the request pattern builder
   */
  public RequestPatternBuilder andMatching(ValueMatcher<Request> customMatcher) {
    this.customMatcher = customMatcher;
    return this;
  }

  /**
   * And matching request pattern builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @return the request pattern builder
   */
  public RequestPatternBuilder andMatching(String customRequestMatcherName) {
    return andMatching(customRequestMatcherName, Parameters.empty());
  }

  /**
   * And matching request pattern builder.
   *
   * @param customRequestMatcherName the custom request matcher name
   * @param parameters the parameters
   * @return the request pattern builder
   */
  public RequestPatternBuilder andMatching(String customRequestMatcherName, Parameters parameters) {
    return andMatching(new CustomMatcherDefinition(customRequestMatcherName, parameters));
  }

  /**
   * And matching request pattern builder.
   *
   * @param matcherDefinition the matcher definition
   * @return the request pattern builder
   */
  public RequestPatternBuilder andMatching(CustomMatcherDefinition matcherDefinition) {
    this.customMatcherDefinition = matcherDefinition;
    return this;
  }

  /**
   * Build request pattern.
   *
   * @return the request pattern
   */
  public RequestPattern build() {
    if (!(url instanceof UrlPathTemplatePattern) && !pathParams.isEmpty()) {
      throw new InvalidInputException(
          Errors.single(
              19, "URL path parameters specified without a path template as the URL matcher"));
    }

    return new RequestPattern(
        scheme,
        hostPattern,
        port,
        clientIpPattern,
        url,
        method,
        headers.isEmpty() ? null : headers,
        pathParams.isEmpty() ? null : pathParams,
        queryParams.isEmpty() ? null : queryParams,
        formParams.isEmpty() ? null : formParams,
        cookies.isEmpty() ? null : cookies,
        basicCredentials,
        bodyPatterns.isEmpty() ? null : bodyPatterns,
        customMatcherDefinition,
        customMatcher,
        multiparts.isEmpty() ? null : multiparts);
  }
}
