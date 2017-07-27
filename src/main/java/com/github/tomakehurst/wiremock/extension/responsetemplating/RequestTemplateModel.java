package com.github.tomakehurst.wiremock.extension.responsetemplating;

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
            public ListOrSingle<String> apply(Cookie input) {
                return ListOrSingle.of(input.getValue());
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
