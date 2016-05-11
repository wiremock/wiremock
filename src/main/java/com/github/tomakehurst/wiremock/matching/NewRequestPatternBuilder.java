package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.RequestMethod;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class NewRequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers = newLinkedHashMap();
    private Map<String, MultiValuePattern> queryParams = newLinkedHashMap();
    private List<StringValuePattern> bodyPatterns = newArrayList();
    private Map<String, StringValuePattern> cookies = newLinkedHashMap();
    private BasicCredentials basicCredentials;

    private RequestMatcher customMatcher;

    public NewRequestPatternBuilder() {
    }

    public NewRequestPatternBuilder(RequestMatcher customMatcher) {
        this.customMatcher = customMatcher;
    }

    public NewRequestPatternBuilder(RequestMethod method, UrlPattern url) {
        this.method = method;
        this.url = url;
    }

    public NewRequestPatternBuilder(String customRequestMatcherName, Parameters parameters) {
    }

    public static NewRequestPatternBuilder newRequestPattern(RequestMethod method, UrlPattern url) {
        return new NewRequestPatternBuilder(method, url);
    }

    public static NewRequestPatternBuilder newRequestPattern() {
        return new NewRequestPatternBuilder();
    }

    public NewRequestPatternBuilder withUrl(String url) {
        this.url = UrlPattern.equalTo(url);
        return this;
    }

    public NewRequestPatternBuilder withHeader(String key, MultiValuePattern valuePattern) {
        headers.put(key, valuePattern);
        return this;
    }

    public NewRequestPatternBuilder withQueryParam(String key, MultiValuePattern valuePattern) {
        queryParams.put(key, valuePattern);
        return this;
    }

    public NewRequestPatternBuilder withCookie(String key, StringValuePattern valuePattern) {
        cookies.put(key, valuePattern);
        return this;
    }

    public NewRequestPatternBuilder withBasicAuth(BasicCredentials basicCredentials) {
        this.basicCredentials = basicCredentials;
        return this;
    }

    public NewRequestPatternBuilder withRequestBody(StringValuePattern valuePattern) {
        this.bodyPatterns.add(valuePattern);
        return this;
    }

    public NewRequestPattern build() {
        return customMatcher != null ?
            new NewRequestPattern(customMatcher) :
            new NewRequestPattern(
                url,
                method,
                headers.isEmpty() ? null : headers,
                queryParams.isEmpty() ? null : queryParams,
                cookies.isEmpty() ? null : cookies,
                basicCredentials,
                bodyPatterns.isEmpty() ? null : bodyPatterns
            );
    }
}
