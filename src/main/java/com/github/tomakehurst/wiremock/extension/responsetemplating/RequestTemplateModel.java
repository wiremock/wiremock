package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.Urls;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.Map;

public class RequestTemplateModel {

    private final Map<String, ListOrSingle<String>> query;

    public RequestTemplateModel(Map<String, ListOrSingle<String>> query) {
        this.query = query;
    }

    public static RequestTemplateModel from(Request request) {
        Map<String, QueryParameter> rawQuery = Urls.splitQuery(URI.create(request.getUrl()));
        Map<String, ListOrSingle<String>> adaptedQuery = Maps.transformValues(rawQuery, new Function<QueryParameter, ListOrSingle<String>>() {
            @Override
            public ListOrSingle<String> apply(QueryParameter input) {
                return ListOrSingle.of(input.values());
            }
        });

        return new RequestTemplateModel(
            adaptedQuery
        );
    }

    public Map<String, ListOrSingle<String>> getQuery() {
        return query;
    }
}
