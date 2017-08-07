package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.junit.Assert;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * This class is the test base for all handlebars helper
 *
 * @author Christopher Holomek
 */
public abstract class HandlebarsHelperTestBase {

    protected static final String FAIL_GRACEFULLY_MSG = "Handlebars helper should fail gracefully and show the issue directly in the response.";

    protected static <T> void testHelperError(final Helper<T> helper,
                                              final T content, final String xPath) {
        try {
            assertThat((String) helper.apply(content, createOptions(xPath)), startsWith(HandlebarsHelper.ERROR_PREFIX));
        } catch (final IOException e) {
            Assert.fail(FAIL_GRACEFULLY_MSG);
        }
    }

    protected static <T> void testHelper(final Helper<T> helper,
                                         final T content, final String optionParam, final String expected) throws
                                                                                                           IOException {
        assertThat((String) helper.apply(content, createOptions(optionParam)), is(expected));
    }

    protected static Options createOptions(final String optionParam) {
        return new Options(null, null, null, null, null, null,
                           new Object[]{optionParam}, null, new ArrayList<String>(0));
    }
}
