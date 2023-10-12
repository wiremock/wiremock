package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import net.datafaker.Faker;

import java.io.IOException;

public class RandomHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        Faker faker = new Faker();
        try {
            return faker.expression("#{" + context + "}");
        } catch (RuntimeException e) {
            return handleError("Unable to evaluate the expression " + context, e);
        }
    }
}
