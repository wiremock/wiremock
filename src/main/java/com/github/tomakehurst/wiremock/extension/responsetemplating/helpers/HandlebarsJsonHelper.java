package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.io.IOException;

public class HandlebarsJsonHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(final String inputJson, final Options options) throws IOException {
        if (inputJson == null) {
            return "";
        }

        if (options == null || options.param(0, null) == null) {
            return this.handleError("The JSONPath cannot be empty");
        }

        final String jsonPath = options.param(0);
        try {
            return String.valueOf(JsonPath.read(inputJson, jsonPath));
        } catch (InvalidJsonException e) {
            return this.handleError(
                    inputJson + " is not valid JSON",
                    e.getJson(), e);
        } catch (JsonPathException e) {
            return this.handleError(jsonPath + " is not a valid JSONPath expression", e);
        }
    }
}
