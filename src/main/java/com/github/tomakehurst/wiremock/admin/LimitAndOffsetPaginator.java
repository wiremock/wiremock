package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.Request;

import java.util.List;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

public class LimitAndOffsetPaginator<T> implements Paginator<T> {

    private final List<T> source;
    private final Integer limit;
    private final Integer offset;

    public LimitAndOffsetPaginator(List<T> source, Integer limit, Integer offset) {
        this.source = source;
        checkArgument(limit == null || limit >= 0, "limit must be 0 or greater");
        checkArgument(offset == null || offset >= 0, "offset must be 0 or greater");
        this.limit = limit;
        this.offset = offset;
    }

    public static <T> LimitAndOffsetPaginator<T> fromRequest(List<T> source, Request request) {
        return new LimitAndOffsetPaginator<T>(
            source,
            Conversions.toInt(request.queryParameter("limit")),
            Conversions.toInt(request.queryParameter("offset"))
        );
    }

    @Override
    public List<T> select() {
        int start = firstNonNull(offset, 0);
        int end = Math.min(
            source.size(),
            start + firstNonNull(limit, source.size())
        );

        return source.subList(start, end);
    }

    @Override
    public int getTotal() {
        return source.size();
    }

    public static <T> LimitAndOffsetPaginator<T> none(List<T> source) {
        return new LimitAndOffsetPaginator<>(source, null, null);
    }
}
