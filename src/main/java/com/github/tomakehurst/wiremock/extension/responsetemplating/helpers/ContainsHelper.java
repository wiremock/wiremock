package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;

import java.io.IOException;
import java.util.Collection;

public class ContainsHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (options.params.length < 1) {
            return handleError("You must specify the string or array to be matched and the regular expression");
        }

        String expected = options.param(0);

        if (context == null || expected == null) {
            return false;
        }

        boolean isMatch = Collection.class.isAssignableFrom(context.getClass()) ?
                ((Collection<?>) context).contains(expected) :
                context.toString().contains(expected);

        if (options.tagType == TagType.SECTION) {
            return isMatch ? options.apply(options.fn) : "";
        }

        return isMatch;
    }
}
