package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * This class tests the HandlebarsSoapHelper
 *
 * @author Christopher Holomek
 */
public class HandlebarsSoapHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsSoapHelper helper;

    @Before
    public void init() {
        this.helper = new HandlebarsSoapHelper();
    }

    @Test
    public void positiveTestSimpleSoap() throws IOException {
        testHelper(this.helper, "<Envelope><Body><test>success</test></Body></Envelope>", "/test", "success");
    }
}
