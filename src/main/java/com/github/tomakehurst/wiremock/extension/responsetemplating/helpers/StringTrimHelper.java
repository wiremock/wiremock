package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;

import java.io.IOException;

public class StringTrimHelper implements Helper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        String value = options.tagType == TagType.SECTION ?
                options.fn(context).toString() :
                context.toString();

        return value.trim();
    }
}
