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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Multimaps;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public class RequestTemplateModel {

    private final String url;
    private final UrlPath path;
    private final Map<String, ListOrSingle<String>> query;
    private final Map<String, ListOrSingle<String>> headers;
    private final Map<String, ListOrSingle<String>> cookies;
    private final String body;


    public RequestTemplateModel(String url, UrlPath path, Map<String, ListOrSingle<String>> query, Map<String, ListOrSingle<String>> headers, Map<String, ListOrSingle<String>> cookies, String body) {
        this.url = url;
        this.path = path;
        this.query = query;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
    }

    public static RequestTemplateModel from(final Request request) {
        URI url = URI.create(request.getUrl());
        Map<String, QueryParameter> rawQuery = Urls.splitQuery(url);
        Map<String, ListOrSingle<String>> adaptedQuery = Maps.transformValues(rawQuery, TO_TEMPLATE_MODEL);
        Map<String, ListOrSingle<String>> adaptedHeaders = Maps.toMap(request.getAllHeaderKeys(), new Function<String, ListOrSingle<String>>() {
            @Override
            public ListOrSingle<String> apply(String input) {
                return ListOrSingle.of(request.header(input).values());
            }
        });

        ImmutableMap<String, Collection<Cookie>> indexedCookies = Multimaps.index(request.getCookies(), new Function<Cookie, String>() {
            @Override
            public String apply(Cookie input) {
                return input.getName();
            }
        }).asMap();
        Map<String, ListOrSingle<String>> adaptedCookies = Maps.transformEntries(indexedCookies , new EntryTransformer<String, Collection<Cookie>, ListOrSingle<String>>() {
            @Override
            public ListOrSingle<String> transformEntry(String key, Collection<Cookie> value) {
                List<String> cookieValues = newArrayList(Collections2.transform(value, new Function<Cookie, String>() {
                    @Override
                    public String apply(Cookie input) {
                        return input.getValue();
                    }
                }));
                return ListOrSingle.of(cookieValues);
            }
        });

        UrlPath path = new UrlPath(request.getUrl());

        return new RequestTemplateModel(
            request.getUrl(),
            path,
            adaptedQuery,
            adaptedHeaders,
            adaptedCookies,
            request.getBodyAsString()
        );
    }

    public String getUrl() {
        return url;
    }

    public UrlPath getPath() {
        return path;
    }

    public Map<String, ListOrSingle<String>> getQuery() {
        return query;
    }

    public Map<String, ListOrSingle<String>> getHeaders() {
        return headers;
    }

    public Map<String, ListOrSingle<String>> getCookies() {
        return cookies;
    }

    public String getBody() {
        return body;
    }

    private static final Function<MultiValue, ListOrSingle<String>> TO_TEMPLATE_MODEL = new Function<MultiValue, ListOrSingle<String>>() {
        @Override
        public ListOrSingle<String> apply(MultiValue input) {
            return ListOrSingle.of(input.values());
        }
    };
}
