package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.common.Encoding.decodeBase64;
import static com.github.tomakehurst.wiremock.common.Encoding.encodeBase64;

public class Base64Helper implements Helper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        String value = options.tagType == TagType.SECTION ?
                options.fn(context).toString() :
                context.toString();

        if (Boolean.TRUE.equals(options.hash.get("decode"))) {
            return new String(decodeBase64(value));
        }

        return encodeBase64(value.getBytes());
    }
}
