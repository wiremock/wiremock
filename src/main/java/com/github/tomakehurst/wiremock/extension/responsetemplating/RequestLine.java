package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.MultiValue;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;

public class RequestLine {
    private final RequestMethod method;
    private final String scheme;
    private final String host;
    private final int port;
    private final Map<String, ListOrSingle<String>> query;
    private final String path;

    private RequestLine(RequestMethod method, String scheme, String host, int port, String path, Map<String, ListOrSingle<String>> query) {
        this.method = method;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
    }

    public static RequestLine fromRequest(final Request request) {
        URI url = URI.create(request.getUrl());
        Map<String, QueryParameter> rawQuery = Urls.splitQuery(url);
        Map<String, ListOrSingle<String>> adaptedQuery = Maps.transformValues(rawQuery, TO_TEMPLATE_MODEL);
        return new RequestLine(request.getMethod(), request.getScheme(), request.getHost(), request.getPort(), request.getUrl(), adaptedQuery);
    }

    public RequestMethod getMethod() {
        return method;
    }

    public UrlPath getPathSegments() {
        return new UrlPath(path);
    }

    public String getPath() {
        return path;
    }

    public Map<String, ListOrSingle<String>> getQuery() {
        return query;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return  port;
    }

    public String getBaseUrl() {
        String portPart = isStandardPort(scheme, port) ?
            "" :
            ":" + port;

        return scheme + "://" + host + portPart;
    }

    private boolean isStandardPort(String scheme, int port) {
        return (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
    }

    private static final Function<MultiValue, ListOrSingle<String>> TO_TEMPLATE_MODEL = new Function<MultiValue, ListOrSingle<String>>() {
        @Override
        public ListOrSingle<String> apply(MultiValue input) {
            return ListOrSingle.of(input.values());
        }
    };
}
