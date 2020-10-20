package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PickRandomHelper extends HandlebarsHelper<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (context == null) {
            return this.handleError("Must specify either a single list argument or a set of single value arguments.");
        }

        List<Object> valueList = (Iterable.class.isAssignableFrom(context.getClass())) ?
                ImmutableList.copyOf((Iterable<Object>) context) :
                ImmutableList.builder().add(context).add(options.params).build();

        int index = ThreadLocalRandom.current().nextInt(valueList.size());
        return valueList.get(index).toString();
    }
}
