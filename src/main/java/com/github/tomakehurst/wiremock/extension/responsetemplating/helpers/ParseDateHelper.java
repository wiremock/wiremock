package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.Exceptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParseDateHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(String context, Options options) throws IOException {
        String format = options.hash("format", null);

        try {
            return format == null ?
                new ISO8601DateFormat().parse(context) :
                new SimpleDateFormat(format).parse(context);
        } catch (ParseException e) {
            return Exceptions.throwUnchecked(e, Object.class);
        }
    }
}
