package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.TagType;
import com.github.tomakehurst.wiremock.common.Json;

import java.io.IOException;
import java.util.Map;

public class ParseJsonHelper extends HandlebarsHelper<Object> {

    @Override
    public Object apply(Object context, Options options) throws IOException {
        CharSequence json;
        String variableName;

        boolean hasContext = context != options.context.model();

        if (options.tagType == TagType.SECTION) {
            json =  options.apply(options.fn);
            variableName = hasContext ? context.toString() : null;
        } else {
            if (!hasContext) {
                return handleError("Missing required JSON string parameter");
            }

            json = context.toString();
            variableName = options.params.length > 0 ? options.param(0) : null;
        }

        Map<String, Object> map = json != null ?
                Json.read(json.toString(), new TypeReference<Map<String, Object>>() {}) :
                null;

        if (variableName != null) {
            options.context.data(variableName, map);
            return null;
        }

        return map;
    }
}
