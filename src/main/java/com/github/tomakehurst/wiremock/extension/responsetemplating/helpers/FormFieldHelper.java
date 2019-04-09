package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;

import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

public class FormFieldHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) {
        Map<String, String> formData = FormParser.parse(
                context.toString(),
                Boolean.TRUE.equals(options.hash.get("urlDecode")),
                firstNonNull(options.hash.get("encoding"), "utf-8").toString()
        );

        String key = options.param(0);
        return formData.get(key);
    }
}
