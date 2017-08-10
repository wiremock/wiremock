package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * This class tests the HandlebarsXmlHelper
 *
 * @author Christopher Holomek
 */
public class HandlebarsXmlHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsXmlHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        this.helper = new HandlebarsXmlHelper();
        this.transformer = new ResponseTemplateTransformer(true);
    }

    @Test
    public void positiveTestSimpleXml() throws IOException {
        testHelper(this.helper, "<test>success</test>", "/test", "success");
    }

    @Test
    public void positiveTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/xml").body("<a><test>success</test></a>"),
                aResponse().withBody("<test>{{wmXml request.body '/a/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("<test>success</test>"));
    }

    @Test
    public void negativeTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/xml").body("<a><test>success</test></a>"),
                aResponse().withBody("<test>{{wmXml request.body '/b/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), startsWith("<test></test>"));
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
