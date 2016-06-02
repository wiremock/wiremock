package com.github.tomakehurst.wiremock.extension;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.google.common.collect.Maps;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.common.Urls.splitQuery;
import static com.google.common.collect.Maps.newHashMap;

public class TemplateRequestAdapter {

    private final Request request;
    private final Map<String, String> headers;
    private final Map<String, String> query;

    public TemplateRequestAdapter(final Request request) {
        this.request = request;
        headers = newHashMap();
        for (HttpHeader header : request.getHeaders().all()) {
            headers.put(header.key().replace('-', '_'), header.firstValue());
        }

        query = getQueryParameters(request);
    }

    private Map<String, String> getQueryParameters(Request request) {
        try {
            URI requestUri = new URI(request.getAbsoluteUrl());
            Map<String, QueryParameter> queryParameters = splitQuery(requestUri.getQuery());
            return Maps.transformEntries(queryParameters, new Maps.EntryTransformer<String, QueryParameter, String>() {
                @Override
                public String transformEntry(String key, QueryParameter value) {
                    return value.firstValue();
                }
            });
        } catch (URISyntaxException e) {
            return newHashMap();
        }
    }

    public String getBody() {
        return request.getBodyAsString();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RequestMethod getMethod() {
        return request.getMethod();
    }

    public String getUrl() {
        return request.getUrl();
    }

    public Map<String, String> getQuery() {
        return query;
    }
}
