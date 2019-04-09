package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.github.tomakehurst.wiremock.common.ListOrSingle;

import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

public class FormDataHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) {
        Map<String, ListOrSingle<String>> formData = FormParser.parse(
                context.toString(),
                Boolean.TRUE.equals(options.hash.get("urlDecode")),
                firstNonNull(options.hash.get("encoding"), "utf-8").toString()
        );

        if (options.params.length > 0) {
            String variableName = options.param(0);
            options.context.data(variableName, formData);
            return null;
        }

        return formData;
    }
}
