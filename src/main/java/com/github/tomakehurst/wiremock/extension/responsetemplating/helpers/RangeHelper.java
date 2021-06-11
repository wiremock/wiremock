package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RangeHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        Integer lowerBound = context instanceof Integer ? (Integer) context : null;
        Integer upperBound = options.params.length > 0 ? options.param(0) : null;

        if (lowerBound == null || upperBound == null) {
            return handleError("The range helper requires both lower and upper bounds as integer parameters");
        }

        int limit = (upperBound - lowerBound) + 1;
        return Stream.iterate(lowerBound, n -> n + 1)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
