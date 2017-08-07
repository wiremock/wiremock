package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * This class tests the HandlebarsXmlHelper
 *
 * @author Christopher Holomek
 */
public class HandlebarsXmlHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsXmlHelper helper;

    @Before
    public void init() {
        this.helper = new HandlebarsXmlHelper();
    }

    @Test
    public void positiveTestSimpleXml() throws IOException {
        testHelper(this.helper, "<test>success</test>", "/test", "success");
    }

    @Test
    public void negativeTestInvalidXml() {
        testHelperError(this.helper, "<testsuccess</test>", "/test");
    }

    @Test
    public void negativeTestInvalidXPath() {
        testHelperError(this.helper, "<test>success</test>", "/\\test");
    }

    @Test
    public void negativeTestXmlNull() {
        testHelperError(this.helper, null, "/test");
    }

    @Test
    public void negativeTestXPathNull() {
        testHelperError(this.helper, "<test>success</test>", null);
    }

    @Test
    public void negativeTestAllNull() {
        testHelperError(this.helper, null, null);
    }
}
