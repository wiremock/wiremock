package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class BodyRequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers = newLinkedHashMap();
    private Map<String, MultiValuePattern> queryParams = newLinkedHashMap();
    private List<StringValuePattern> bodyPatterns = newArrayList();
    private List<FormFieldPattern> formFieldPatterns = newArrayList();
    private Map<String, StringValuePattern> cookies = newLinkedHashMap();
    private BasicCredentials basicCredentials;

    private RequestMatcher customMatcher;

    private CustomMatcherDefinition customMatcherDefinition;

    public BodyRequestPatternBuilder() {
    }

    public BodyRequestPatternBuilder(RequestMatcher customMatcher) {
        this.customMatcher = customMatcher;
    }

    public BodyRequestPatternBuilder(RequestMethod method, UrlPattern url) {
        this.method = method;
        this.url = url;
    }

    public BodyRequestPatternBuilder(String customRequestMatcherName, Parameters parameters) {
        this.customMatcherDefinition = new CustomMatcherDefinition(customRequestMatcherName, parameters);
    }

    public static BodyRequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
        return new BodyRequestPatternBuilder(method, url);
    }

    public static BodyRequestPatternBuilder newRequestPattern() {
        return new BodyRequestPatternBuilder();
    }

    public static BodyRequestPatternBuilder forCustomMatcher(RequestMatcher requestMatcher) {
        return new BodyRequestPatternBuilder(requestMatcher);
    }

    public static BodyRequestPatternBuilder forCustomMatcher(String customRequestMatcherName, Parameters parameters) {
        return new BodyRequestPatternBuilder(customRequestMatcherName, parameters);
    }

    public static BodyRequestPatternBuilder allRequests() {
        return new BodyRequestPatternBuilder(RequestMethod.ANY, WireMock.anyUrl());
    }

    public BodyRequestPatternBuilder withUrl(String url) {
        this.url = WireMock.urlEqualTo(url);
        return this;
    }

    public BodyRequestPatternBuilder withHeader(String key, StringValuePattern valuePattern) {
        headers.put(key, MultiValuePattern.of(valuePattern));
        return this;
    }

    public BodyRequestPatternBuilder withoutHeader(String key) {
        headers.put(key, MultiValuePattern.absent());
        return this;
    }

    public BodyRequestPatternBuilder withQueryParam(String key, StringValuePattern valuePattern) {
        queryParams.put(key, MultiValuePattern.of(valuePattern));
        return this;
    }

    public BodyRequestPatternBuilder withCookie(String key, StringValuePattern valuePattern) {
        cookies.put(key, valuePattern);
        return this;
    }

    public BodyRequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
        this.basicCredentials = basicCredentials;
        return this;
    }

    public BodyRequestPatternBuilder withRequestBody(StringValuePattern valuePattern) {
        this.bodyPatterns.add(valuePattern);
        return this;
    }

    public BodyRequestPatternBuilder withFormParam(FormFieldPattern formFieldPattern) {
        this.formFieldPatterns.add(formFieldPattern);
        return this;
    }

    public BodyRequestPattern build() {
        return customMatcher != null ?
                new BodyRequestPattern(customMatcher) :
                customMatcherDefinition != null ?
                        new BodyRequestPattern(customMatcherDefinition) :
                        new BodyRequestPattern(
                                url,
                                method,
                                headers.isEmpty() ? null : headers,
                                queryParams.isEmpty() ? null : queryParams,
                                cookies.isEmpty() ? null : cookies,
                                basicCredentials,
                                bodyPatterns.isEmpty() ? null : bodyPatterns,
                                formFieldPatterns.isEmpty() ? null : formFieldPatterns,
                                null
                        );
    }
}
