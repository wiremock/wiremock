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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.common.Errors;
import com.github.tomakehurst.wiremock.common.InvalidInputException;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.url.PathParams;
import com.github.tomakehurst.wiremock.common.url.PathTemplate;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.RequestPathParamsDecorator;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import org.wiremock.url.Query;

@JsonInclude(Include.NON_NULL)
public class RequestPattern implements NamedValueMatcher<Request> {

  private final String scheme;
  private final StringValuePattern host;
  private final Integer port;
  private final StringValuePattern clientIp;
  private final UrlPattern url;
  private final RequestMethod method;
  @NonNull private final Map<String, MultiValuePattern> headers;

  @NonNull private final Map<String, StringValuePattern> pathParams;
  @NonNull private final Map<String, MultiValuePattern> queryParams;
  @NonNull private final Map<String, MultiValuePattern> formParams;
  @NonNull private final Map<String, StringValuePattern> cookies;
  private final BasicCredentials basicAuthCredentials;
  @NonNull private final List<ContentPattern<?>> bodyPatterns;
  @NonNull private final List<MultipartValuePattern> multipartPatterns;

  private final CustomMatcherDefinition customMatcherDefinition;
  private final ValueMatcher<Request> matcher;
  private final ValueMatcher<Request> inlineCustomMatcher;

  @JsonCreator
  public RequestPattern(
      @JsonProperty("scheme") String scheme,
      @JsonProperty("host") StringValuePattern host,
      @JsonProperty("port") Integer port,
      @JsonProperty("url") String url,
      @JsonProperty("clientIp") StringValuePattern clientIp,
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
        clientIp,
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

  RequestPattern(
      final String scheme,
      final StringValuePattern host,
      final Integer port,
      final StringValuePattern clientIp,
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
    this.clientIp = clientIp;
    this.url = getFirstNonNull(url, UrlPattern.ANY);
    this.method = getFirstNonNull(method, RequestMethod.ANY);
    this.headers = headers != null ? ImmutableMap.copyOf(headers) : Map.of();
    this.pathParams = pathParams != null ? ImmutableMap.copyOf(pathParams) : Map.of();
    this.formParams = formParams != null ? ImmutableMap.copyOf(formParams) : Map.of();
    this.queryParams = queryParams != null ? ImmutableMap.copyOf(queryParams) : Map.of();
    this.cookies = cookies != null ? ImmutableMap.copyOf(cookies) : Map.of();
    this.basicAuthCredentials = basicAuthCredentials;
    this.bodyPatterns = bodyPatterns != null ? List.copyOf(bodyPatterns) : List.of();
    this.customMatcherDefinition = customMatcherDefinition;
    this.multipartPatterns = multiPattern != null ? List.copyOf(multiPattern) : List.of();
    this.inlineCustomMatcher = customMatcher;

    this.matcher =
        new RequestMatcher() {

          @Override
          public MatchResult match(Request request) {
            return match(request, null);
          }

          @Override
          public MatchResult match(Request request, ServeContext matcherContext) {

            final List<WeightedMatchResult> requestPartMatchResults = new ArrayList<>(15);

            requestPartMatchResults.add(weight(schemeMatches(request), 3.0));
            requestPartMatchResults.add(weight(hostMatches(request), 10.0));
            requestPartMatchResults.add(weight(portMatches(request), 10.0));
            requestPartMatchResults.add(weight(clientIpMatches(request), 3.0));
            requestPartMatchResults.add(
                weight(
                    RequestPattern.this.url.match(
                        request.getPathAndQueryWithoutPrefix().toString()),
                    10.0));
            requestPartMatchResults.add(
                weight(RequestPattern.this.method.match(request.getMethod()), 3.0));

            MatchResult matchResult =
                new MemoizingMatchResult(MatchResult.aggregateWeighted(requestPartMatchResults));

            if (!matchResult.isExactMatch()) {
              return matchResult;
            }

            requestPartMatchResults.add(weight(allPathParamsMatch(request)));
            requestPartMatchResults.add(weight(allHeadersMatchResult(request)));
            requestPartMatchResults.add(weight(allQueryParamsMatch(request, matcherContext)));
            requestPartMatchResults.add(weight(allFormParamsMatch(request)));
            requestPartMatchResults.add(weight(allCookiesMatch(request)));
            requestPartMatchResults.add(weight(allBodyPatternsMatch(request)));
            requestPartMatchResults.add(weight(allMultipartPatternsMatch(request)));

            matchResult =
                new MemoizingMatchResult(MatchResult.aggregateWeighted(requestPartMatchResults));
            if (!matchResult.isExactMatch() || customMatcher == null) {
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

  public static final RequestPattern ANYTHING =
      newRequestPattern(RequestMethod.ANY, anyUrl()).build();

  public RequestPattern transform(Consumer<Builder> transformer) {
    final RequestPattern.Builder builder = toBuilder();
    transformer.accept(builder);
    return builder.build();
  }

  public Builder toBuilder() {
    return new RequestPattern.Builder(this);
  }

  @Override
  public MatchResult match(Request request) {
    return match(request, Collections.emptyMap());
  }

  public MatchResult match(Request request, Map<String, RequestMatcherExtension> customMatchers) {
    return match(request, customMatchers, null);
  }

  public MatchResult match(
      Request request, Map<String, RequestMatcherExtension> customMatchers, ServeContext context) {
    request = RequestPathParamsDecorator.decorate(request, this);
    final MatchResult standardMatchResult = matcher.match(request, context);
    if (standardMatchResult.isExactMatch() && customMatcherDefinition != null) {
      RequestMatcherExtension requestMatcher =
          getFirstNonNull(customMatchers.get(customMatcherDefinition.getName()), NEVER);

      MatchResult customMatchResult =
          requestMatcher.match(request, customMatcherDefinition.getParameters());

      return MatchResult.aggregate(standardMatchResult, customMatchResult);
    }

    return standardMatchResult;
  }

  private MatchResult allCookiesMatch(final Request request) {
    if (!cookies.isEmpty()) {
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

  private MatchResult clientIpMatches(final Request request) {
    return clientIp != null ? clientIp.match(request.getClientIp()) : MatchResult.exactMatch();
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

  private MatchResult allQueryParamsMatch(final Request request, ServeContext context) {
    if (!queryParams.isEmpty()) {
      return MatchResult.aggregate(
          queryParams.entrySet().stream()
              .map(
                  queryParamPattern -> {
                    Query query = request.getPathAndQueryWithoutPrefix().getQueryOrEmpty();
                    String key = queryParamPattern.getKey();
                    QueryParameter queryParameter = new QueryParameter(key, query.getDecoded(key));
                    return queryParamPattern.getValue().match(queryParameter, context);
                  })
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  private MatchResult allFormParamsMatch(final Request request) {
    if (!formParams.isEmpty()) {
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
    if (url.getClass().equals(UrlPathTemplatePattern.class) && !pathParams.isEmpty()) {
      final UrlPathTemplatePattern urlPathTemplatePattern = (UrlPathTemplatePattern) url;
      final PathTemplate pathTemplate = urlPathTemplatePattern.getPathTemplate();
      if (!pathTemplate.matches(request.getPathAndQueryWithoutPrefix().getPath())) {
        return MatchResult.noMatch();
      }

      final PathParams requestPathParams =
          pathTemplate.parse(request.getPathAndQueryWithoutPrefix().getPath());
      return MatchResult.aggregate(
          pathParams.entrySet().stream()
              .map(entry -> entry.getValue().match(requestPathParams.get(entry.getKey())))
              .collect(toList()));
    }

    return MatchResult.exactMatch();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private MatchResult allBodyPatternsMatch(final Request request) {
    if (!bodyPatterns.isEmpty() && request.getBody() != null) {
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
    if (!multipartPatterns.isEmpty()) {
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

  public StringValuePattern getClientIp() {
    return clientIp;
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

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public Map<String, MultiValuePattern> getHeaders() {
    return headers;
  }

  public BasicCredentials getBasicAuthCredentials() {
    return basicAuthCredentials;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public Map<String, StringValuePattern> getPathParameters() {
    return pathParams;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public Map<String, MultiValuePattern> getQueryParameters() {
    return queryParams;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public Map<String, MultiValuePattern> getFormParameters() {
    return formParams;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public Map<String, StringValuePattern> getCookies() {
    return cookies;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public List<ContentPattern<?>> getBodyPatterns() {
    return bodyPatterns;
  }

  public CustomMatcherDefinition getCustomMatcher() {
    return customMatcherDefinition;
  }

  @JsonInclude(Include.NON_EMPTY)
  @NonNull
  public List<MultipartValuePattern> getMultipartPatterns() {
    return multipartPatterns;
  }

  @JsonIgnore
  public ValueMatcher<Request> getInlineCustomMatcher() {
    return inlineCustomMatcher;
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
    return inlineCustomMatcher != null;
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
    return Objects.equals(scheme, that.scheme)
        && Objects.equals(host, that.host)
        && Objects.equals(port, that.port)
        && Objects.equals(clientIp, that.clientIp)
        && Objects.equals(url, that.url)
        && Objects.equals(method, that.method)
        && Objects.equals(headers, that.headers)
        && Objects.equals(pathParams, that.pathParams)
        && Objects.equals(queryParams, that.queryParams)
        && Objects.equals(formParams, that.formParams)
        && Objects.equals(cookies, that.cookies)
        && Objects.equals(basicAuthCredentials, that.basicAuthCredentials)
        && Objects.equals(bodyPatterns, that.bodyPatterns)
        && Objects.equals(multipartPatterns, that.multipartPatterns)
        && Objects.equals(customMatcherDefinition, that.customMatcherDefinition)
        && Objects.equals(inlineCustomMatcher, that.inlineCustomMatcher);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scheme,
        host,
        port,
        clientIp,
        url,
        method,
        headers,
        pathParams,
        queryParams,
        formParams,
        cookies,
        basicAuthCredentials,
        bodyPatterns,
        multipartPatterns,
        customMatcherDefinition,
        inlineCustomMatcher);
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

  public static Predicate<ServeEvent> withRequestMatching(
      final RequestPattern pattern, final Map<String, RequestMatcherExtension> customMatchers) {
    return serveEvent -> pattern.match(serveEvent.getRequest(), customMatchers).isExactMatch();
  }

  @SuppressWarnings("UnusedReturnValue")
  public static class Builder {
    private String scheme;
    private StringValuePattern host;
    private Integer port;
    private StringValuePattern clientIp;
    private UrlPattern url;
    private RequestMethod method;
    @NonNull private Map<String, MultiValuePattern> headers = new LinkedHashMap<>();

    @NonNull private Map<String, StringValuePattern> pathParams = new LinkedHashMap<>();
    @NonNull private Map<String, MultiValuePattern> queryParams = new LinkedHashMap<>();
    @NonNull private Map<String, MultiValuePattern> formParams = new LinkedHashMap<>();
    @NonNull private Map<String, StringValuePattern> cookies = new LinkedHashMap<>();
    private BasicCredentials basicAuthCredentials;
    @NonNull private List<ContentPattern<?>> bodyPatterns = new ArrayList<>();
    @NonNull private List<MultipartValuePattern> multipartPatterns = new ArrayList<>();

    private CustomMatcherDefinition customMatcherDefinition;
    private ValueMatcher<Request> inlineCustomMatcher;

    public Builder() {}

    public Builder(RequestPattern existing) {
      this.scheme = existing.getScheme();
      this.host = existing.getHost();
      this.port = existing.getPort();
      this.clientIp = existing.getClientIp();
      this.url = existing.getUrlMatcher();
      this.method = existing.getMethod();
      this.headers.putAll(existing.getHeaders());
      this.pathParams.putAll(existing.getPathParameters());
      this.queryParams.putAll(existing.getQueryParameters());
      this.formParams.putAll(existing.getFormParameters());
      this.cookies.putAll(existing.getCookies());
      this.basicAuthCredentials = existing.getBasicAuthCredentials();
      this.bodyPatterns.addAll(existing.getBodyPatterns());
      this.multipartPatterns.addAll(existing.getMultipartPatterns());
      this.customMatcherDefinition = existing.getCustomMatcher();
      this.inlineCustomMatcher = existing.getInlineCustomMatcher();
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

    public StringValuePattern getClientIp() {
      return clientIp;
    }

    public UrlPattern getUrl() {
      return url;
    }

    public RequestMethod getMethod() {
      return method;
    }

    @NonNull
    public Map<String, MultiValuePattern> getHeaders() {
      return headers;
    }

    @NonNull
    public Map<String, StringValuePattern> getPathParams() {
      return pathParams;
    }

    @NonNull
    public Map<String, MultiValuePattern> getQueryParams() {
      return queryParams;
    }

    @NonNull
    public Map<String, MultiValuePattern> getFormParams() {
      return formParams;
    }

    @NonNull
    public Map<String, StringValuePattern> getCookies() {
      return cookies;
    }

    public BasicCredentials getBasicAuthCredentials() {
      return basicAuthCredentials;
    }

    @NonNull
    public List<ContentPattern<?>> getBodyPatterns() {
      return bodyPatterns;
    }

    @NonNull
    public List<MultipartValuePattern> getMultipartPatterns() {
      return multipartPatterns;
    }

    public CustomMatcherDefinition getCustomMatcherDefinition() {
      return customMatcherDefinition;
    }

    public ValueMatcher<Request> getInlineCustomMatcher() {
      return inlineCustomMatcher;
    }

    public Builder setScheme(String scheme) {
      this.scheme = scheme;
      return this;
    }

    public Builder setHost(StringValuePattern host) {
      this.host = host;
      return this;
    }

    public Builder setPort(Integer port) {
      this.port = port;
      return this;
    }

    public Builder setClientIp(StringValuePattern clientIp) {
      this.clientIp = clientIp;
      return this;
    }

    public Builder setUrl(UrlPattern url) {
      this.url = url;
      return this;
    }

    public Builder setMethod(RequestMethod method) {
      this.method = method;
      return this;
    }

    public Builder setHeaders(@NonNull Map<String, MultiValuePattern> headers) {
      Objects.requireNonNull(headers);
      this.headers = headers;
      return this;
    }

    public Builder setPathParams(@NonNull Map<String, StringValuePattern> pathParams) {
      Objects.requireNonNull(pathParams);
      this.pathParams = pathParams;
      return this;
    }

    public Builder setQueryParams(@NonNull Map<String, MultiValuePattern> queryParams) {
      Objects.requireNonNull(queryParams);
      this.queryParams = queryParams;
      return this;
    }

    public Builder setFormParams(@NonNull Map<String, MultiValuePattern> formParams) {
      Objects.requireNonNull(formParams);
      this.formParams = formParams;
      return this;
    }

    public Builder setCookies(@NonNull Map<String, StringValuePattern> cookies) {
      Objects.requireNonNull(cookies);
      this.cookies = cookies;
      return this;
    }

    public Builder setBasicAuthCredentials(BasicCredentials basicAuthCredentials) {
      this.basicAuthCredentials = basicAuthCredentials;
      return this;
    }

    public Builder setBodyPatterns(@NonNull List<ContentPattern<?>> bodyPatterns) {
      Objects.requireNonNull(bodyPatterns);
      this.bodyPatterns = bodyPatterns;
      return this;
    }

    public Builder setMultipartPatterns(@NonNull List<MultipartValuePattern> multipartPatterns) {
      Objects.requireNonNull(multipartPatterns);
      this.multipartPatterns = multipartPatterns;
      return this;
    }

    public Builder setCustomMatcherDefinition(CustomMatcherDefinition customMatcherDefinition) {
      this.customMatcherDefinition = customMatcherDefinition;
      return this;
    }

    public Builder setInlineCustomMatcher(ValueMatcher<Request> matcher) {
      this.inlineCustomMatcher = matcher;
      return this;
    }

    public RequestPattern build() {
      if (!(url instanceof UrlPathTemplatePattern) && !pathParams.isEmpty()) {
        throw new InvalidInputException(
            Errors.single(
                19, "URL path parameters specified without a path template as the URL matcher"));
      }

      return new RequestPattern(
          scheme,
          host,
          port,
          clientIp,
          url,
          method,
          headers,
          pathParams,
          queryParams,
          formParams,
          cookies,
          basicAuthCredentials,
          bodyPatterns,
          customMatcherDefinition,
          inlineCustomMatcher,
          multipartPatterns);
    }
  }
}
