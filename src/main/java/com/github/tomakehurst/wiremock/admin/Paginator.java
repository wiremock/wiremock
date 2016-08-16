package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

public class Paginator<T> {

    private final List<T> source;
    private final Integer limit;
    private final Integer offset;

    public Paginator(List<T> source, Integer limit, Integer offset) {
        this.source = source;
        checkArgument(limit == null || limit >= 0, "limit must be 0 or greater");
        checkArgument(offset == null || offset >= 0, "offset must be 0 or greater");
        this.limit = limit;
        this.offset = offset;
    }

    public static <T> Paginator<T> fromRequest(List<T> source, Request request) {
        return new Paginator<T>(
            source,
            toInt(request.queryParameter("limit")),
            toInt(request.queryParameter("offset"))
        );
    }

    private static Integer toInt(QueryParameter parameter) {
        return parameter.isPresent() ?
            Integer.valueOf(parameter.firstValue()) :
            null;
    }

    public List<T> select() {
        int start = firstNonNull(offset, 0);
        int end = Math.min(
            source.size(),
            start + firstNonNull(limit, source.size())
        );

        return source.subList(start, end);
    }

    public int getTotal() {
        return source.size();
    }

    public static <T> Paginator<T> none(List<T> source) {
        return new Paginator<>(source, null, null);
    }
}
