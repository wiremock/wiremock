package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

import java.io.IOException;

/**
 * This enum is implemented similar to the StringHelpers of handlebars.
 * It is basically a library of all available wiremock helpers
 */
public enum WiremockHelpers implements Helper<Object> {
    xPath {
        private HandlebarsXPathHelper helper = new HandlebarsXPathHelper();

        @Override
        public Object apply(final Object context, final Options options) throws IOException {
            return this.helper.apply(String.valueOf(context), options);
        }
    },
    soapXPath {
        private HandlebarsSoapHelper helper = new HandlebarsSoapHelper();

        @Override
        public Object apply(final Object context, final Options options) throws IOException {
            return this.helper.apply(String.valueOf(context), options);
        }
    },
    jsonPath {
        private HandlebarsJsonPathHelper helper = new HandlebarsJsonPathHelper();

        @Override
        public Object apply(final Object context, final Options options) throws IOException {
            return this.helper.apply(String.valueOf(context), options);
        }
    }
}
