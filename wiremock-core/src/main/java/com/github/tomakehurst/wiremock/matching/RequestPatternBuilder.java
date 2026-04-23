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
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern.Builder;

@SuppressWarnings("UnusedReturnValue")
public class RequestPatternBuilder {

  private final RequestPattern.Builder builder;

  public RequestPatternBuilder() {
    this(new RequestPattern.Builder());
  }

  public RequestPatternBuilder(ValueMatcher<Request> customMatcher) {
    this();
    builder.setInlineCustomMatcher(customMatcher);
  }

  public RequestPatternBuilder(RequestMethod method, UrlPattern url) {
    this();
    builder.setMethod(method);
    builder.setUrl(url);
  }

  public RequestPatternBuilder(String customRequestMatcherName, Parameters parameters) {
    this();
    builder.setCustomMatcherDefinition(
        new CustomMatcherDefinition(customRequestMatcherName, parameters));
  }

  private RequestPatternBuilder(Builder builder) {
    this.builder = builder;
  }

  public static RequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
    return new RequestPatternBuilder(method, url);
  }

  public static RequestPatternBuilder newRequestPattern() {
    return new RequestPatternBuilder();
  }

  public static RequestPatternBuilder forCustomMatcher(ValueMatcher<Request> requestMatcher) {
    return new RequestPatternBuilder(requestMatcher);
  }

  public static RequestPatternBuilder forCustomMatcher(
      String customRequestMatcherName, Parameters parameters) {
    return new RequestPatternBuilder(customRequestMatcherName, parameters);
  }

  public static RequestPatternBuilder allRequests() {
    return new RequestPatternBuilder(RequestMethod.ANY, WireMock.anyUrl());
  }

  /**
   * Construct a builder that uses an existing RequestPattern as a template
   *
   * @param requestPattern A RequestPattern to copy
   * @return A builder based on the RequestPattern
   * @deprecated use RequestPattern.transform() instead
   */
  @Deprecated
  public static RequestPatternBuilder like(RequestPattern requestPattern) {
    return new RequestPatternBuilder(requestPattern.toBuilder());
  }

  /**
   * @deprecated intended for use with now-deprecated like() method. Use RequestPattern.transform()
   *     instead.
   */
  @Deprecated
  public RequestPatternBuilder but() {
    return this;
  }

  public RequestPatternBuilder withScheme(String scheme) {
    builder.setScheme(scheme);
    return this;
  }

  public RequestPatternBuilder withHost(StringValuePattern hostPattern) {
    builder.setHost(hostPattern);
    return this;
  }

  public RequestPatternBuilder withPort(int port) {
    builder.setPort(port);
    return this;
  }

  public RequestPatternBuilder withClientIp(StringValuePattern clientIpPattern) {
    builder.setClientIp(clientIpPattern);
    return this;
  }

  public RequestPatternBuilder withUrl(String url) {
    builder.setUrl(WireMock.urlEqualTo(url));
    return this;
  }

  public RequestPatternBuilder withUrl(UrlPattern urlPattern) {
    builder.setUrl(urlPattern);
    return this;
  }

  public RequestPatternBuilder withHeader(String key, StringValuePattern valuePattern) {
    builder.getHeaders().put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  public RequestPatternBuilder withHeader(String key, MultiValuePattern multiValuePattern) {
    builder.getHeaders().put(key, multiValuePattern);
    return this;
  }

  public RequestPatternBuilder withoutHeader(String key) {
    builder.getHeaders().put(key, MultiValuePattern.absent());
    return this;
  }

  public RequestPatternBuilder withPathParam(String key, StringValuePattern valuePattern) {
    builder.getPathParams().put(key, valuePattern);
    return this;
  }

  public RequestPatternBuilder withQueryParam(String key, StringValuePattern valuePattern) {
    builder.getQueryParams().put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  public RequestPatternBuilder withQueryParam(String key, MultiValuePattern multiValuePattern) {
    builder.getQueryParams().put(key, multiValuePattern);
    return this;
  }

  public RequestPatternBuilder withFormParam(String key, StringValuePattern valuePattern) {
    builder.getFormParams().put(key, MultiValuePattern.of(valuePattern));
    return this;
  }

  public RequestPatternBuilder withFormParam(String key, MultiValuePattern multiValuePattern) {
    builder.getFormParams().put(key, multiValuePattern);
    return this;
  }

  public RequestPatternBuilder withoutFormParam(String key) {
    builder.getFormParams().put(key, MultiValuePattern.absent());
    return this;
  }

  public RequestPatternBuilder withoutQueryParam(String key) {
    builder.getQueryParams().put(key, MultiValuePattern.absent());
    return this;
  }

  public RequestPatternBuilder withCookie(String key, StringValuePattern valuePattern) {
    builder.getCookies().put(key, valuePattern);
    return this;
  }

  public RequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
    builder.setBasicAuthCredentials(basicCredentials);
    return this;
  }

  public RequestPatternBuilder withRequestBody(ContentPattern valuePattern) {
    builder.getBodyPatterns().add(valuePattern);
    return this;
  }

  public RequestPatternBuilder withRequestBodyPart(MultipartValuePattern multiPattern) {
    if (multiPattern != null) {
      builder.getMultipartPatterns().add(multiPattern);
    }
    return this;
  }

  public RequestPatternBuilder withAnyRequestBodyPart(
      MultipartValuePatternBuilder multiPatternBuilder) {
    return withRequestBodyPart(
        multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ANY).build());
  }

  public RequestPatternBuilder withAllRequestBodyParts(
      MultipartValuePatternBuilder multiPatternBuilder) {
    return withRequestBodyPart(
        multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ALL).build());
  }

  public RequestPatternBuilder andMatching(ValueMatcher<Request> customMatcher) {
    builder.setInlineCustomMatcher(customMatcher);
    return this;
  }

  public RequestPatternBuilder andMatching(String customRequestMatcherName) {
    return andMatching(customRequestMatcherName, Parameters.empty());
  }

  public RequestPatternBuilder andMatching(String customRequestMatcherName, Parameters parameters) {
    return andMatching(new CustomMatcherDefinition(customRequestMatcherName, parameters));
  }

  public RequestPatternBuilder andMatching(CustomMatcherDefinition matcherDefinition) {
    builder.setCustomMatcherDefinition(matcherDefinition);
    return this;
  }

  public RequestPatternBuilder clearQueryParams() {
    builder.getQueryParams().clear();
    return this;
  }

  public RequestPatternBuilder clearFormParams() {
    builder.getFormParams().clear();
    return this;
  }

  public RequestPatternBuilder clearBodyPatterns() {
    builder.getBodyPatterns().clear();
    return this;
  }

  public RequestPattern build() {
    return builder.build();
  }
}
