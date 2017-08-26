package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.testsupport.WireMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToXml;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class HandlebarsXPathHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsXPathHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        helper = new HandlebarsXPathHelper();
        this.transformer = new ResponseTemplateTransformer(true);
    }

    @Test
    public void rendersASimpleValue() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/xml")
                    .body("<a><test>success</test></a>"),
                aResponse()
                    .withBody("<test>{{xPath request.body '/a/test/text()'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("<test>success</test>"));
    }

    @Test
    public void rendersNothingWhenTheXPathExpressionResolvesNoContent() {
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
        testHelper(helper, "<test>success</test>", "/test/text()", "success");
    }

    @Test
    public void extractsAnAttribute() throws IOException {
        testHelper(helper, "<test outcome=\"success\"/>", "/test/@outcome", "success");
    }

    @Test
    public void extractsASubElement() throws IOException {
        testHelper(helper, "<outer>\n" +
            "    <inner>stuff</inner>\n" +
            "</outer>",
            "/outer/inner",
            equalToXml("<inner>stuff</inner>"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheInputXmlIsInvalid() {
        testHelperError(helper, "<testsuccess</test>", "/test", is("[ERROR: <testsuccess</test> is not valid XML]"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheXPathExpressionIsInvalid() {
        testHelperError(helper, "<test>success</test>", "/\\test", is("[ERROR: /\\test is not a valid XPath expression]"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenTheXPathExpressionIsAbsent() {
        testHelperError(helper, "<test>success</test>", null, is("[ERROR: The XPath expression cannot be empty]"));
    }

    @Test
    public void rendersABlankWhenTheInputXmlIsAbsent() {
        testHelperError(helper, null, "/test", is(""));
    }



}
