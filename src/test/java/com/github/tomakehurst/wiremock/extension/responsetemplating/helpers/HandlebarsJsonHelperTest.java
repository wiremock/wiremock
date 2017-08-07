package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * This class tests the HandlebarsJsonHelper
 *
 * @author Christopher Holomek
 */
public class HandlebarsJsonHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsJsonHelper helper;

    @Before
    public void init() {
        this.helper = new HandlebarsJsonHelper();
    }

    @Test
    public void positiveTestSimpleJson() throws IOException {
        testHelper(this.helper, "{\"test\":\"success\"}", "$.test", "success");
    }

    @Test
    public void negativeTestInvalidJson() {
        testHelperError(this.helper, "{\"test\":\"success}", "$.test");
    }

    @Test
    public void negativeTestInvalidJsonPath() {
        testHelperError(this.helper, "{\"test\":\"success}", "$.\\test");
    }

    @Test
    public void negativeTestJsonNull() {
        testHelperError(this.helper, null, "$.test");
    }

    @Test
    public void negativeTestJsonPathNull() {
        testHelperError(this.helper, "{\"test\":\"success}", null);
    }

    @Test
    public void negativeTestAllNull() {
        testHelperError(this.helper, null, null);
    }
}
