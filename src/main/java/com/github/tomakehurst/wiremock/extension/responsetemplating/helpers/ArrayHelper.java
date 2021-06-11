package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ArrayHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (context == null || context == options.context.model()) {
            return ImmutableList.of();
        }

        return ImmutableList.builder()
                .add(context)
                .addAll(asList(options.params))
                .build();
    }
}
