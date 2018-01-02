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
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;

public class RequestPatternBuilder {

    private UrlPattern url;
    private RequestMethod method;
    private Map<String, MultiValuePattern> headers = newLinkedHashMap();
    private Map<String, MultiValuePattern> queryParams = newLinkedHashMap();
    private List<ContentPattern<?>> bodyPatterns = newArrayList();
    private Map<String, StringValuePattern> cookies = newLinkedHashMap();
    private BasicCredentials basicCredentials;
    private List<MultipartValuePattern> multiparts = newLinkedList();

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

    /**
     * Construct a builder that uses an existing RequestPattern as a template
     *
     * @param requestPattern A RequestPattern to copy
     * @return A builder based on the RequestPattern
     */
    public static RequestPatternBuilder like(RequestPattern requestPattern) {
        RequestPatternBuilder builder = new RequestPatternBuilder();
        builder.url = requestPattern.getUrlMatcher();
        builder.method = requestPattern.getMethod();
        if (requestPattern.getHeaders() != null) {
            builder.headers = requestPattern.getHeaders();
        }
        if (requestPattern.getQueryParameters() != null) {
            builder.queryParams = requestPattern.getQueryParameters();
        }
        if (requestPattern.getCookies() != null) {
            builder.cookies = requestPattern.getCookies();
        }
        if (requestPattern.getBodyPatterns() != null) {
            builder.bodyPatterns = requestPattern.getBodyPatterns();
        }
        if (requestPattern.hasCustomMatcher()) {
            builder.customMatcher = requestPattern.getMatcher();
        }
        if (requestPattern.getMultipartPatterns() != null) {
            builder.multiparts = requestPattern.getMultipartPatterns();
        }
        builder.basicCredentials = requestPattern.getBasicAuthCredentials();
        builder.customMatcherDefinition = requestPattern.getCustomMatcher();
        return builder;
    }

    public RequestPatternBuilder but() {
        return this;
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

    public RequestPatternBuilder withRequestBodyPart(MultipartValuePattern multiPattern) {
        if (multiPattern != null) {
            multiparts.add(multiPattern);
        }
        return this;
    }

    public RequestPatternBuilder withAnyRequestBodyPart(MultipartValuePatternBuilder multiPatternBuilder) {
        return withRequestBodyPart(multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ANY).build());
    }

    public RequestPatternBuilder withAllRequestBodyParts(MultipartValuePatternBuilder multiPatternBuilder) {
        return withRequestBodyPart(multiPatternBuilder.matchingType(MultipartValuePattern.MatchingType.ALL).build());
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
                    null,
                    multiparts.isEmpty() ? null : multiparts
                );
    }
}
