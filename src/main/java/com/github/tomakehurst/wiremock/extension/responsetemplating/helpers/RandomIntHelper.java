package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class RandomIntHelper extends HandlebarsHelper<Void> {

    @Override
    public Object apply(Void context, Options options) throws IOException {
        int lowerBound = options.hash("lower", Integer.MIN_VALUE);
        int upperBound = options.hash("upper", Integer.MAX_VALUE);
        return ThreadLocalRandom.current().nextInt(lowerBound, upperBound);
    }
}
