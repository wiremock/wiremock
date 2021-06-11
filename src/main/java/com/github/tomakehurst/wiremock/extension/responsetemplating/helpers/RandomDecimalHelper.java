package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class RandomDecimalHelper extends HandlebarsHelper<Void> {

    @Override
    public Object apply(Void context, Options options) throws IOException {
        double lowerBound = options.hash("lower", Double.MIN_VALUE);
        double upperBound = options.hash("upper", Double.MAX_VALUE);

        return ThreadLocalRandom.current().nextDouble(lowerBound, upperBound);
    }
}
