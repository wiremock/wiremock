package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;

import java.io.IOException;

public class MatchesRegexHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        if (options.params.length < 1) {
            return handleError("You must specify the string to be matched and the regular expression");
        }

        String value = context.toString();
        String regex = options.param(0);

        boolean isMatch = value.matches(regex);

        if (options.tagType == TagType.SECTION) {
            return isMatch ? options.apply(options.fn) : "";
        }

        return isMatch;
    }
}
