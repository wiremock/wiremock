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

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.Cookie;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.util.Map;

public class RequestTemplateModel {

    private final RequestLine requestLine;
    private final Map<String, ListOrSingle<String>> headers;
    private final Map<String, ListOrSingle<String>> cookies;
    private final String body;


    protected RequestTemplateModel(RequestLine requestLine, Map<String, ListOrSingle<String>> headers, Map<String, ListOrSingle<String>> cookies, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.cookies = cookies;
        this.body = body;
    }

    public static RequestTemplateModel from(final Request request) {
        RequestLine requestLine = RequestLine.fromRequest(request);
        Map<String, ListOrSingle<String>> adaptedHeaders = Maps.toMap(request.getAllHeaderKeys(), new Function<String, ListOrSingle<String>>() {
            @Override
            public ListOrSingle<String> apply(String input) {
                return ListOrSingle.of(request.header(input).values());
            }
        });
        Map<String, ListOrSingle<String>> adaptedCookies = Maps.transformValues(request.getCookies(), new Function<Cookie, ListOrSingle<String>>() {
            @Override
            public ListOrSingle<String> apply(Cookie cookie) {
                return ListOrSingle.of(cookie.getValues());
            }
        });

        return new RequestTemplateModel(
            requestLine,
            adaptedHeaders,
            adaptedCookies,
            request.getBodyAsString()
        );
    }

    public RequestLine getRequestLine() {
        return requestLine;
    }

    /**
     * @deprecated use requestLine to access information about the request
     */
    @Deprecated
    public String getUrl() {
        return requestLine.getPath();
    }

    /**
     * @deprecated use requestLine to access information about the request
     */
    @Deprecated
    public UrlPath getPath() {
        return requestLine.getPathSegments();
    }

    /**
     * @deprecated use requestLine to access information about the request
     */
    @Deprecated
    public Map<String, ListOrSingle<String>> getQuery() {
        return requestLine.getQuery();
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

}
