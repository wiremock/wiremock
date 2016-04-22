package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;

import java.util.Map;

import static com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_NULL;
import static com.google.common.collect.FluentIterable.from;

@JsonSerialize(include = NON_NULL)
public class NewRequestPattern implements ValueMatcher<Request> {

    private final UrlPattern url;
    private final RequestMethod method;
    private final Map<String, MultiValuePattern> headers;
    private final Map<String, MultiValuePattern> queryParams;

    public NewRequestPattern(UrlPattern url,
                             RequestMethod method,
                             Map<String, MultiValuePattern> headers,
                             Map<String, MultiValuePattern> queryParams) {
        this.url = url;
        this.method = method;
        this.headers = headers;
        this.queryParams = queryParams;
    }

    @JsonCreator
    public NewRequestPattern(@JsonProperty("url") String url,
                             @JsonProperty("urlPattern") String urlPattern,
                             @JsonProperty("urlPath") String urlPath,
                             @JsonProperty("urlPathPattern") String urlPathPattern,
                             @JsonProperty("method") RequestMethod method,
                             @JsonProperty("headers") Map<String, MultiValuePattern> headers,
                             @JsonProperty("queryParameters") Map<String, MultiValuePattern> queryParams) {

        this.url = UrlPattern.fromOneOf(url, urlPattern, urlPath, urlPathPattern);
        this.method = method;
        this.headers = headers;
        this.queryParams = queryParams;
    }

    @Override
    public MatchResult match(Request request) {
        return MatchResult.aggregate(
            url.match(request.getUrl()),
            method.match(request.getMethod()),
            allHeadersMatchResult(request),
            allQueryParamsMatch(request)
        );
    }

    private MatchResult allHeadersMatchResult(final Request request) {
        if (headers != null && !headers.isEmpty()) {
            return MatchResult.aggregate(
                from(headers.entrySet())
                .transform(new Function<Map.Entry<String, MultiValuePattern>, MatchResult>() {
                    public MatchResult apply(Map.Entry<String, MultiValuePattern> headerPattern) {
                        return headerPattern.getValue().match(request.header(headerPattern.getKey()));
                    }
                }).toList()
            );
        }

        return MatchResult.exactMatch();
    }

    private MatchResult allQueryParamsMatch(final Request request) {
        if (queryParams != null && !queryParams.isEmpty()) {
            return MatchResult.aggregate(
                from(queryParams.entrySet())
                .transform(new Function<Map.Entry<String, MultiValuePattern>, MatchResult>() {
                    public MatchResult apply(Map.Entry<String, MultiValuePattern> queryParamPattern) {
                        return queryParamPattern.getValue().match(request.queryParameter(queryParamPattern.getKey()));
                    }
                }).toList()
            );
        }

        return MatchResult.exactMatch();
    }

    public boolean isMatchedBy(Request request, Map<String, RequestMatcherExtension> customMatchers) {
        return match(request).isExactMatch();
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

    private String urlPatternOrNull(Class<? extends UrlPattern> clazz, boolean regex) {
        return (url != null && url.getClass().equals(clazz) && url.isRegex() == regex) ? url.getPattern().getValue() : null;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public Map<String, MultiValuePattern> getHeaders() {
        return headers;
    }

    public Map<String, MultiValuePattern> getQueryParameters() {
        return queryParams;
    }

    @Override
    public String getName() {
        return "requestMatching";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewRequestPattern that = (NewRequestPattern) o;
        return Objects.equal(url, that.url) &&
            Objects.equal(method, that.method) &&
            Objects.equal(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url, method, headers);
    }
}
