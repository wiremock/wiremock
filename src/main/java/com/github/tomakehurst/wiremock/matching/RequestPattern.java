/*
 * Copyright (C) 2011-2024 Thomas Akehurst
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

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.common.ContentTypes.AUTHORIZATION;
import static com.github.tomakehurst.wiremock.common.ParameterUtils.getFirstNonNull;
import static com.github.tomakehurst.wiremock.common.Strings.isEmpty;
import static com.github.tomakehurst.wiremock.matching.RequestMatcherExtension.NEVER;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.matching.WeightedMatchResult.weight;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class RequestPattern implements NamedValueMatcher<Request> {

  private final String scheme;
  private final StringValuePattern host;
  private final Integer port;
  private final UrlPattern url;
  private final RequestMethod method;
  private final Map<String, MultiValuePattern> headers;

  private final Map<String, StringValuePattern> pathParams;
  private final Map<String, MultiValuePattern> queryParams;
  private final Map<String, MultiValuePattern> formParams;
  private final Map<String, StringValuePattern> cookies;
  private final BasicCredentials basicAuthCredentials;
  private final List<ContentPattern<?>> bodyPatterns;
  private final List<MultipartValuePattern> multipartPatterns;

  private final CustomMatcherDefinition customMatcherDefinition;
  private final ValueMatcher<Request> matcher;
  private final boolean hasInlineCustomMatcher;

  public RequestPattern(
      final String scheme,
      final StringValuePattern host,
      final Integer port,
      final UrlPattern url,
      final RequestMethod method,
      final Map<String, MultiValuePattern> headers,
      final Map<String, StringValuePattern> pathParams,
      final Map<String, MultiValuePattern> queryParams,
      final Map<String, MultiValuePattern> formParams,
      final Map<String, StringValuePattern> cookies,
      final BasicCredentials basicAuthCredentials,
      final List<ContentPattern<?>> bodyPatterns,
      final CustomMatcherDefinition customMatcherDefinition,
      final ValueMatcher<Request> customMatcher,
      final List<MultipartValuePattern> multiPattern) {
    this.scheme = scheme;
    this.host = host;
    this.port = port;
    this.url = getFirstNonNull(url, UrlPattern.ANY);
    this.method = getFirstNonNull(method, RequestMethod.ANY);
    this.headers = headers;
    this.pathParams = pathParams;
    this.formParams = formParams;
    this.queryParams = queryParams;
    this.cookies = cookies;
    this.basicAuthCredentials = basicAuthCredentials;
    this.bodyPatterns = bodyPatterns;
    this.customMatcherDefinition = customMatcherDefinition;
    this.multipartPatterns = multiPattern;
    this.hasInlineCustomMatcher = customMatcher != null;

    this.matcher =
        new RequestMatcher() {
          @Override
          public MatchResult match(Request request) {
            final List<WeightedMatchResult> requestPartMatchResults = new ArrayList<>(15);

            requestPartMatchResults.add(weight(schemeMatches(request), 3.0));
            requestPartMatchResults.add(weight(hostMatches(request), 10.0));
            requestPartMatchResults.add(weight(portMatches(request), 10.0));
            requestPartMatchResults.add(
                weight(RequestPattern.this.url.match(request.getUrl()), 10.0));
            requestPartMatchResults.add(
                weight(RequestPattern.this.method.match(request.getMethod()), 3.0));

            MatchResult matchResult =
                new MemoizingMatchResult(MatchResult.aggregateWeighted(requestPartMatchResults));

            if (!matchResult.isExactMatch()) {
              return matchResult;
            }

            requestPartMatchResults.add(weight(allPathParamsMatch(request)));
            requestPartMatchResults.add(weight(allHeadersMatchResult(request)));
            requestPartMatchResults.add(weight(allQueryParamsMatch(request)));
            requestPartMatchResults.add(weight(allFormParamsMatch(request)));
            requestPartMatchResults.add(weight(allCookiesMatch(request)));
            requestPartMatchResults.add(weight(allBodyPatternsMatch(request)));
            requestPartMatchResults.add(weight(allMultipartPatternsMatch(request)));

            matchResult =
                new MemoizingMatchResult(MatchResult.aggregateWeighted(requestPartMatchResults));
            if (!matchResult.isExactMatch() || !hasInlineCustomMatcher) {
              return matchResult;
            }

            requestPartMatchResults.add(weight(customMatcher.match(request)));
            return new MemoizingMatchResult(MatchResult.aggregateWeighted(requestPartMatchResults));
          }

          @Override
          public String getName() {
            return "request-matcher";
          }
        };
  }

  @JsonCreator
  public RequestPattern(
      @JsonProperty("scheme") String scheme,
      @JsonProperty("host") StringValuePattern host,
      @JsonProperty("port") Integer port,
      @JsonProperty("url") String url,
      @JsonProperty("urlPattern") String urlPattern,
      @JsonProperty("urlPath") String urlPath,
      @JsonProperty("urlPathPattern") String urlPathPattern,
      @JsonProperty("urlPathTemplate") String urlPathTemplate,
      @JsonProperty("method") RequestMethod method,
      @JsonProperty("headers") Map<String, MultiValuePattern> headers,
      @JsonProperty("pathParameters") Map<String, StringValuePattern> pathParams,
      @JsonProperty("queryParameters") Map<String, MultiValuePattern> queryParams,
      @JsonProperty("formParameters") Map<String, MultiValuePattern> formParams,
      @JsonProperty("cookies") Map<String, StringValuePattern> cookies,
      @JsonProperty("basicAuth") BasicCredentials basicAuthCredentials,
      @JsonProperty("bodyPatterns") List<ContentPattern<?>> bodyPatterns,
      @JsonProperty("customMatcher") CustomMatcherDefinition customMatcherDefinition,
      @JsonProperty("multipartPatterns") List<MultipartValuePattern> multiPattern) {

    this(
        scheme,
        host,
        port,
        UrlPattern.fromOneOf(url, urlPattern, urlPath, urlPathPattern, urlPathTemplate),
        method,
        headers,
        pathParams,
        queryParams,
        formParams,
        cookies,
        basicAuthCredentials,
        bodyPatterns,
        customMatcherDefinition,
        null,
        multiPattern);
  }

  public static final RequestPattern ANYTHING =
      new RequestPattern(
          null,
          null,
          null,
          anyUrl(),
          RequestMethod.ANY,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null,
          null);

  public RequestPattern(ValueMatcher<Request> customMatcher) {
    this(
        null,
        null,
        null,
        UrlPattern.ANY,
        RequestMethod.ANY,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        customMatcher,
        null);
  }

  public RequestPattern(CustomMatcherDefinition customMatcherDefinition) {
    this(
        null,
        null,
        null,
        UrlPattern.ANY,
        RequestMethod.ANY,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        customMatcherDefinition,
        null,
        null);
  }

  @Override
  public MatchResult match(Request request) {
    return match(request, Collections.emptyMap());
  }

  public static RequestPattern everything() {
    return newRequestPattern(RequestMethod.ANY, anyUrl()).build();
  }

  public MatchResult match(Request request, Map<String, RequestMatcherExtension> customMatchers) {
    final MatchResult standardMatchResult = matcher.match(request);
    if (standardMatchResult.isExactMatch() && customMatcherDefinition != null) {
      RequestMatcherExtension requestMatcher =
          getFirstNonNull(customMatchers.get(customMatcherDefinition.getName()), NEVER);

      MatchResult customMatchResult =
          requestMatcher.match(request, customMatcherDefinition.getParameters());

      return new WeightedAggregateMatchResult(
          List.of(
              new WeightedMatchResult(standardMatchResult, 100.0),
              new WeightedMatchResult(customMatchResult, 1.0)));
    }

    return standardMatchResult;
  }

  private MatchResult allCookiesMatch(final Request request) {
    if (cookies != null && !cookies.isEmpty()) {
      return MatchResult.aggregate(
          cookies.entrySet().stream()
              .map(
                  entry -> {
                    final StringValuePattern cookiePattern = entry.getValue();
                    Cookie cookie = request.getCookies().get(entry.getKey());
                    if (cookie == null) {
                      return cookiePattern.nullSafeIsAbsent()
                          ? MatchResult.exactMatch()
                          : MatchResult.noMatch();
                    }

                    return cookie.getValues().stream()
                        .map(cookiePattern::match)
                        .max(Comparator.naturalOrder())
                        .orElseGet(MatchResult::noMatch);
                  })
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult schemeMatches(final Request request) {
    return scheme != null
        ? MatchResult.of(scheme.equals(request.getScheme()))
        : MatchResult.exactMatch();
  }

  private MatchResult hostMatches(final Request request) {
    return host != null ? host.match(request.getHost()) : MatchResult.exactMatch();
  }

  private MatchResult portMatches(final Request request) {
    return port != null ? MatchResult.of(request.getPort() == port) : MatchResult.exactMatch();
  }

  private MatchResult allHeadersMatchResult(final Request request) {
    Map<String, MultiValuePattern> combinedHeaders = combineBasicAuthAndOtherHeaders();

    if (combinedHeaders != null && !combinedHeaders.isEmpty()) {
      return MatchResult.aggregate(
          combinedHeaders.entrySet().stream()
              .map(
                  headerPattern ->
                      headerPattern.getValue().match(request.header(headerPattern.getKey())))
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  public Map<String, MultiValuePattern> combineBasicAuthAndOtherHeaders() {
    if (basicAuthCredentials == null) {
      return headers;
    }

    Map<String, MultiValuePattern> combinedHeaders = headers;
    Map<String, MultiValuePattern> allHeadersBuilder =
        new HashMap<>(getFirstNonNull(combinedHeaders, Collections.emptyMap()));
    allHeadersBuilder.put(AUTHORIZATION, basicAuthCredentials.asAuthorizationMultiValuePattern());
    combinedHeaders = allHeadersBuilder;
    return combinedHeaders;
  }

  private MatchResult allQueryParamsMatch(final Request request) {
    if (queryParams != null && !queryParams.isEmpty()) {
      return MatchResult.aggregate(
          queryParams.entrySet().stream()
              .map(
                  queryParamPattern ->
                      queryParamPattern
                          .getValue()
                          .match(request.queryParameter(queryParamPattern.getKey())))
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult allFormParamsMatch(final Request request) {
    if (formParams != null && !formParams.isEmpty()) {
      return MatchResult.aggregate(
          formParams.entrySet().stream()
              .map(
                  formParamPattern ->
                      formParamPattern
                          .getValue()
                          .match(request.formParameter(formParamPattern.getKey())))
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult allPathParamsMatch(final Request request) {
    if (url.getClass().equals(UrlPathTemplatePattern.class)
        && pathParams != null
        && !pathParams.isEmpty()) {
      final UrlPathTemplatePattern urlPathTemplatePattern = (UrlPathTemplatePattern) url;
      final PathTemplate pathTemplate = urlPathTemplatePattern.getPathTemplate();
      if (!pathTemplate.matches(request.getUrl())) {
        return MatchResult.noMatch();
      }

      final PathParams requestPathParams = pathTemplate.parse(Urls.getPath(request.getUrl()));
      return MatchResult.aggregate(
          pathParams.entrySet().stream()
              .map(entry -> entry.getValue().match(requestPathParams.get(entry.getKey())))
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private MatchResult allBodyPatternsMatch(final Request request) {
    if (bodyPatterns != null && !bodyPatterns.isEmpty() && request.getBody() != null) {
      return MatchResult.aggregate(
          bodyPatterns.stream()
              .map(
                  (Function<ContentPattern, MatchResult>)
                      pattern -> {
                        if (StringValuePattern.class.isAssignableFrom(pattern.getClass())) {
                          String body =
                              isEmpty(request.getBodyAsString()) ? null : request.getBodyAsString();
                          return pattern.match(body);
                        }

                        return pattern.match(request.getBody());
                      })
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult allMultipartPatternsMatch(final Request request) {
    if (multipartPatterns != null && !multipartPatterns.isEmpty()) {
      if (!request.isMultipart()) {
        return MatchResult.noMatch();
      }
      return MatchResult.aggregate(
          multipartPatterns.stream().map(pattern -> pattern.match(request)).collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  public boolean isMatchedBy(Request request, Map<String, RequestMatcherExtension> customMatchers) {
    return match(request, customMatchers).isExactMatch();
  }

  public String getScheme() {
    return scheme;
  }

  public StringValuePattern getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getUrl() {
    return urlPatternOrNull(UrlPattern.class, false);
  }

  public String getUrlPattern() {
    return urlPatternOrNull(UrlPattern.class, true);
  }

  public String getUrlPath() {
    return urlPatternOrNull(UrlPathPattern.class, false);
  }

  public String getUrlPathPattern() {
    return urlPatternOrNull(UrlPathPattern.class, true);
  }

  public String getUrlPathTemplate() {
    return urlPatternOrNull(UrlPathTemplatePattern.class, false);
  }

  @JsonIgnore
  public UrlPattern getUrlMatcher() {
    return url;
  }

  private String urlPatternOrNull(Class<? extends UrlPattern> clazz, boolean regex) {
    return (url != null
            && url.getClass().equals(clazz)
            && url.isRegex() == regex
            && url.isSpecified())
        ? url.getPattern().getValue()
        : null;
  }

  public RequestMethod getMethod() {
    return method;
  }

  public Map<String, MultiValuePattern> getHeaders() {
    return headers;
  }

  public BasicCredentials getBasicAuthCredentials() {
    return basicAuthCredentials;
  }

  public Map<String, StringValuePattern> getPathParameters() {
    return pathParams;
  }

  public Map<String, MultiValuePattern> getQueryParameters() {
    return queryParams;
  }

  public Map<String, MultiValuePattern> getFormParameters() {
    return formParams;
  }

  public Map<String, StringValuePattern> getCookies() {
    return cookies;
  }

  public List<ContentPattern<?>> getBodyPatterns() {
    return bodyPatterns;
  }

  public CustomMatcherDefinition getCustomMatcher() {
    return customMatcherDefinition;
  }

  public List<MultipartValuePattern> getMultipartPatterns() {
    return multipartPatterns;
  }

  @JsonIgnore
  public ValueMatcher<Request> getMatcher() {
    return matcher;
  }

  @Override
  public String getName() {
    return "requestMatching";
  }

  @Override
  public String getExpected() {
    return toString();
  }

  public boolean hasInlineCustomMatcher() {
    return hasInlineCustomMatcher;
  }

  public boolean hasNamedCustomMatcher() {
    return customMatcherDefinition != null;
  }

  public boolean hasCustomMatcher() {
    return hasInlineCustomMatcher() || hasNamedCustomMatcher();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestPattern that = (RequestPattern) o;
    return hasInlineCustomMatcher == that.hasInlineCustomMatcher
        && Objects.equals(scheme, that.scheme)
        && Objects.equals(host, that.host)
        && Objects.equals(port, that.port)
        && Objects.equals(url, that.url)
        && Objects.equals(method, that.method)
        && Objects.equals(headers, that.headers)
        && Objects.equals(queryParams, that.queryParams)
        && Objects.equals(cookies, that.cookies)
        && Objects.equals(basicAuthCredentials, that.basicAuthCredentials)
        && Objects.equals(bodyPatterns, that.bodyPatterns)
        && Objects.equals(multipartPatterns, that.multipartPatterns)
        && Objects.equals(customMatcherDefinition, that.customMatcherDefinition)
        && Objects.equals(matcher, that.matcher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scheme,
        host,
        port,
        url,
        method,
        headers,
        queryParams,
        cookies,
        basicAuthCredentials,
        bodyPatterns,
        multipartPatterns,
        customMatcherDefinition,
        matcher,
        hasInlineCustomMatcher);
  }

  @Override
  public String toString() {
    return Json.write(this);
  }

  public static Predicate<Request> thatMatch(final RequestPattern pattern) {
    return thatMatch(pattern, Collections.emptyMap());
  }

  public static Predicate<Request> thatMatch(
      final RequestPattern pattern, final Map<String, RequestMatcherExtension> customMatchers) {
    return request -> pattern.match(request, customMatchers).isExactMatch();
  }

  public static Predicate<ServeEvent> withRequestMatching(final RequestPattern pattern) {
    return serveEvent -> pattern.match(serveEvent.getRequest()).isExactMatch();
  }
}
