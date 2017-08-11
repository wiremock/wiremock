package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public abstract class HandlebarsHelperTestBase {

    protected static final String FAIL_GRACEFULLY_MSG = "Handlebars helper should fail gracefully and show the issue directly in the response.";

    protected static <T> void testHelperError(Helper<T> helper,
                                              T content,
                                              String pathExpression,
                                              Matcher<String> expectation) {
        try {
            assertThat((String) helper.apply(content, createOptions(pathExpression)), expectation);
        } catch (final IOException e) {
            Assert.fail(FAIL_GRACEFULLY_MSG);
        }
    }

    protected static <T> void testHelper(Helper<T> helper,
                                         T content,
                                         String optionParam,
                                         String expected) throws IOException {
        testHelper(helper, content, optionParam, is(expected));
    }

    protected static <T> void testHelper(Helper<T> helper,
                                         T content,
                                         String optionParam,
                                         Matcher<String> expected) throws IOException {
        assertThat((String) helper.apply(content, createOptions(optionParam)), expected);
    }

    protected static Options createOptions(String optionParam) {
        return new Options(null, null, null, null, null, null,
                           new Object[]{optionParam}, null, new ArrayList<String>(0));
    }
}
