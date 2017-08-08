package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Options;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.io.IOException;


/**
 * This class uses JsonPath from jayway for reading a json via jsonPath so that the result can be used for response
 * templating.
 *
 * @author Christopher Holomek
 */
public class HandlebarsJsonHelper extends HandlebarsHelper<String> {

    @Override
    public Object apply(final String context, final Options options) throws IOException {
        if (context == null || options == null || options.param(0, null) == null) {
            return this.handleError("HandlebarsJsonHelper: No parameters defined. Helper not applied");
        }

        final String jsonPath = options.param(0);
        try {
            return String.valueOf(JsonPath.read(context, jsonPath));
        } catch (final InvalidJsonException e) {
            return this.handleError(
                    "HandlebarsJsonHelper: An error occurred. Helper not applied. See cause for more details.",
                    e.getJson(), e);
        }catch(final JsonPathException e){
            return this.handleError(
                    "HandlebarsJsonHelper: An error occurred. Helper not applied. See cause for more details.", e);
        }
    }
}
