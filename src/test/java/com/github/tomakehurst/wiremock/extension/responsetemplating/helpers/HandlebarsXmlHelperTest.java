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

public class HandlebarsXmlHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsXmlHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        this.helper = new HandlebarsXmlHelper();
        this.transformer = new ResponseTemplateTransformer(true);
    }

    @Test
    public void positiveTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/xml")
                    .body("<a><test>success</test></a>"),
                aResponse()
                    .withBody("<test>{{xPath request.body '/a/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("<test>success</test>"));
    }

    @Test
    public void negativeTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/xml")
                    .body("<a><test>success</test></a>"),
                aResponse()
                    .withBody("<test>{{xPath request.body '/b/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), startsWith("<test></test>"));
    }

    @Test
    public void extractsASimpleValue() throws IOException {
        testHelper(this.helper, "<test>success</test>", "/test", "success");
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheInputXmlIsInvalid() {
        testHelperError(this.helper, "<testsuccess</test>", "/test", is("[ERROR: <testsuccess</test> is not valid XML]"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheXPathExpressionIsInvalid() {
        testHelperError(this.helper, "<test>success</test>", "/\\test", is("[ERROR: /\\test is not a valid XPath expression]"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheXPathExpressionIsAbsent() {
        testHelperError(this.helper, "<test>success</test>", null, is("[ERROR: The XPath expression cannot be empty]"));
    }

    @Test
    public void rendersABlankWhenTheInputXmlIsAbsent() {
        testHelperError(this.helper, null, "/test", is(""));
    }



}
