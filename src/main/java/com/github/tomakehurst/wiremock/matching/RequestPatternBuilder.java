package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class RequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers = newLinkedHashMap();
    private Map<String, MultiValuePattern> queryParams = newLinkedHashMap();
    private List<StringValuePattern> bodyPatterns = newArrayList();
    private Map<String, StringValuePattern> cookies = newLinkedHashMap();
    private BasicCredentials basicCredentials;

    private RequestMatcher customMatcher;

    private CustomMatcherDefinition customMatcherDefinition;

    public RequestPatternBuilder() {
    }

    public RequestPatternBuilder(RequestMatcher customMatcher) {
        this.customMatcher = customMatcher;
    }

    public RequestPatternBuilder(RequestMethod method, UrlPattern url) {
        this.method = method;
        this.url = url;
    }

    public RequestPatternBuilder(String customRequestMatcherName, Parameters parameters) {
        this.customMatcherDefinition = new CustomMatcherDefinition(customRequestMatcherName, parameters);
    }

    public static RequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
        return new RequestPatternBuilder(method, url);
    }

    public static RequestPatternBuilder newRequestPattern() {
        return new RequestPatternBuilder();
    }

    public static RequestPatternBuilder forCustomMatcher(RequestMatcher requestMatcher) {
        return new RequestPatternBuilder(requestMatcher);
    }

    public static RequestPatternBuilder forCustomMatcher(String customRequestMatcherName, Parameters parameters) {
        return new RequestPatternBuilder(customRequestMatcherName, parameters);
    }

    public static RequestPatternBuilder allRequests() {
        return new RequestPatternBuilder(RequestMethod.ANY, WireMock.anyUrl());
    }

    public RequestPatternBuilder withUrl(String url) {
        this.url = WireMock.urlEqualTo(url);
        return this;
    }

    public RequestPatternBuilder withHeader(String key, StringValuePattern valuePattern) {
        headers.put(key, MultiValuePattern.of(valuePattern));
        return this;
    }

    public RequestPatternBuilder withoutHeader(String key) {
        headers.put(key, MultiValuePattern.absent());
        return this;
    }

    public RequestPatternBuilder withQueryParam(String key, StringValuePattern valuePattern) {
        queryParams.put(key, MultiValuePattern.of(valuePattern));
        return this;
    }

    public RequestPatternBuilder withCookie(String key, StringValuePattern valuePattern) {
        cookies.put(key, valuePattern);
        return this;
    }

    public RequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
        this.basicCredentials = basicCredentials;
        return this;
    }

    public RequestPatternBuilder withRequestBody(StringValuePattern valuePattern) {
        this.bodyPatterns.add(valuePattern);
        return this;
    }



    public RequestPattern build() {
        return customMatcher != null ?
            new RequestPattern(customMatcher) :
            customMatcherDefinition != null ?
                new RequestPattern(customMatcherDefinition) :
                new RequestPattern(
                    url,
                    method,
                    headers.isEmpty() ? null : headers,
                    queryParams.isEmpty() ? null : queryParams,
                    cookies.isEmpty() ? null : cookies,
                    basicCredentials,
                    bodyPatterns.isEmpty() ? null : bodyPatterns,
                    null
                );
    }
}
