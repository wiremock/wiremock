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
 * This class tests the HandlebarsJsonHelper
 *
 * @author Christopher Holomek
 */
public class HandlebarsJsonHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsJsonHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        this.helper = new HandlebarsJsonHelper();
        this.transformer = new ResponseTemplateTransformer(true);
    }

    @Test
    public void positiveTestSimpleJson() throws IOException {
        testHelper(this.helper, "{\"test\":\"success\"}", "$.test", "success");
    }

    @Test
    public void positiveTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/json").body(
                        "{\"a\": {\"test\": \"success\"}}"),
                aResponse().withBody("{\"test\": \"{{wmJson request.body '$.a.test'}}\"}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("{\"test\": \"success\"}"));
    }

    @Test
    public void negativeTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest().url("/json").body(
                        "{\"a\": {\"test\": \"success\"}}"),
                aResponse().withBody("{\"test\": \"{{wmJson request.body '$.b.test'}}\"}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), startsWith("{\"test\": \"" + HandlebarsHelper.ERROR_PREFIX));
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
