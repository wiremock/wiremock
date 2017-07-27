/*
 * Copyright (C) 2011 Thomas Akehurst
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

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class RequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers = newLinkedHashMap();
    private Map<String, MultiValuePattern> queryParams = newLinkedHashMap();
    private List<ContentPattern<?>> bodyPatterns = newArrayList();
    private Map<String, StringValuePattern> cookies = newLinkedHashMap();
    private BasicCredentials basicCredentials;

    private ValueMatcher<Request> customMatcher;

    private CustomMatcherDefinition customMatcherDefinition;

    public RequestPatternBuilder() {
    }

    public RequestPatternBuilder(ValueMatcher<Request> customMatcher) {
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

    public static RequestPatternBuilder forCustomMatcher(ValueMatcher<Request> requestMatcher) {
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

    public RequestPatternBuilder withRequestBody(ContentPattern valuePattern) {
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
