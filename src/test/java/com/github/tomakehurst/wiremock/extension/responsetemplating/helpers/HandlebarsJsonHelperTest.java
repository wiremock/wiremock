package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import com.github.tomakehurst.wiremock.common.Notifier;
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

public class HandlebarsJsonHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsJsonHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        this.helper = new HandlebarsJsonHelper();
        this.transformer = new ResponseTemplateTransformer(true);

        LocalNotifier.set(new ConsoleNotifier(true));
    }

    @Test
    public void extractsASimpleValueFromTheInputJson() throws IOException {
        testHelper(this.helper, "{\"test\":\"success\"}", "$.test", "success");
    }

    @Test
    public void mergesASimpleValueFromRequestIntoResponseBody() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                    .url("/json").
                    body("{\"a\": {\"test\": \"success\"}}"),
                aResponse()
                    .withBody("{\"test\": \"{{jsonPath request.body '$.a.test'}}\"}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("{\"test\": \"success\"}"));
    }

    @Test
    public void incluesAnErrorInTheResponseBodyWhenTheJsonPathExpressionReturnsNothing() {
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                    .url("/json")
                    .body("{\"a\": {\"test\": \"success\"}}"),
                aResponse()
                    .withBody("{\"test\": \"{{jsonPath request.body '$.b.test'}}\"}").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), startsWith("{\"test\": \"" + HandlebarsHelper.ERROR_PREFIX));
    }

    @Test
    public void rendersAMeaningfulErrorWhenInputJsonIsInvalid() {
        testHelperError(this.helper, "{\"test\":\"success}", "$.test", is("[ERROR: {\"test\":\"success} is not valid JSON]"));
    }

    @Test
    public void rendersAMeaningfulErrorWhenJsonPathIsInvalid() {
        testHelperError(this.helper, "{\"test\":\"success\"}", "$.\\test", is("[ERROR: $.\\test is not a valid JSONPath expression]"));
    }

    @Test
    public void rendersAnEmptyStringWhenJsonIsNull() {
        testHelperError(this.helper, null, "$.test", is(""));
    }

    @Test
    public void rendersAMeaningfulErrorWhenJsonPathIsNull() {
        testHelperError(this.helper, "{\"test\":\"success}", null, is("[ERROR: The JSONPath cannot be empty]"));
    }
}
