package com.github.tomakehurst.wiremock.extension.responsetemplating.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.extension.responsetemplating.helpers.DataType.*;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DataTypeHandlingTest extends HandlebarsHelperTestBase {

    @Test
    public void booleanTrueInLowerCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("true", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(true)));
    }

    @Test
    public void booleanTrueInUpperCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("TRUE", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(true)));
    }

    @Test
    public void booleanTrueCapitalizedTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("True", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(true)));
    }

    @Test
    public void booleanFalseInLowerCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("false", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(false)));
    }

    @Test
    public void booleanFalseInUpperCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("FALSE", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(false)));
    }

    @Test
    public void booleanFalseCapitalizedTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("False", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(false)));
    }

    @Test
    public void booleanInvalidValueTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(getTestNodeFromResponse(responseDefinition).asText(), is("ERROR: Cannot deserialize value of type `boolean` from String \"test\": only \"true\"/\"True\"/\"TRUE\" or \"false\"/\"False\"/\"FALSE\" recognized"));
    }

    @Test
    public void emptyBooleanTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __boolean),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(false)));
    }

    @Test
    public void invalidDataTypeTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", "__booolean"),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody("\"__boooleantest\"")));
    }

    @Test
    public void doubleTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("12345.54321", __double),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(12345.54321)));
    }

    @Test
    public void doubleZeroTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("0", __double),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0.0)));
    }

    @Test
    public void negativeDoubleTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("-123.21", __double),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(-123.21)));
    }

    @Test
    public void emptyDoubleTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __double),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0.0)));
    }

    @Test
    public void invalidDoubleTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __double),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(getTestNodeFromResponse(responseDefinition).asText(), is("ERROR: Cannot deserialize value of type `double` from String \"test\": not a valid `double` value (as String to convert)"));
    }

    @Test
    public void floatTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("12.34", __float),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(12.34)));
    }

    @Test
    public void floatZeroTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("0", __float),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0.0)));
    }

    @Test
    public void negativeFloatTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("-123.21", __float),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(-123.21)));
    }

    @Test
    public void emptyFloatTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __float),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0.0)));
    }

    @Test
    public void invalidFloatTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __float),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(getTestNodeFromResponse(responseDefinition).asText(), is("ERROR: Cannot deserialize value of type `float` from String \"test\": not a valid `float` value"));
    }

    @Test
    public void integerTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("12345", __integer),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(12345)));
    }

    @Test
    public void negativeIntegerTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("-123456", __integer),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(-123456)));
    }

    @Test
    public void emptyIntegerTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __integer),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0)));
    }

    @Test
    public void invalidIntegerTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __integer),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(getTestNodeFromResponse(responseDefinition).asText(), is("ERROR: Cannot deserialize value of type `int` from String \"test\": not a valid `int` value"));
    }

    @Test
    public void longTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("12345", __long),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(12345)));
    }

    @Test
    public void negativeLongTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("-123456", __long),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(-123456)));
    }

    @Test
    public void emptyLongTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __long),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(0)));
    }

    @Test
    public void invalidLongTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __long),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(getTestNodeFromResponse(responseDefinition).asText(), is("ERROR: Cannot deserialize value of type `long` from String \"test\": not a valid `long` value"));
    }

    @Test
    public void nullInLowerCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("null", __null),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(null)));
    }

    @Test
    public void nullInUpperCaseTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("NULL", __null),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(null)));
    }

    @Test
    public void invalidNullTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("test", __null),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(null)));
    }

    @Test
    public void emptyNullTest() {
        final ResponseDefinition responseDefinition =
                this.transformer.transform(
                        getMockRequest(),
                        getResponseDefinition("", __null),
                        noFileSource(),
                        Parameters.empty()
                );

        assertThat(responseDefinition.getBody(), is(getExpectedBody(null)));
    }

    private JsonNode getTestNodeFromResponse(ResponseDefinition responseDefinition) {
        return Json.node(responseDefinition.getBody()).get("test");
    }

    private ResponseDefinition getResponseDefinition(String testValue, Object dataType) {
        return aResponse()
                .withBody(getResponseBody(dataType + testValue))
                .build();
    }

    private MockRequest getMockRequest() {
        return mockRequest().url("/api/test");
    }

    private String getResponseBody(String testValue) {
        return String.format("{\"test\":\"%s\"}", testValue);
    }

    private String getExpectedBody(Object testValue) {
        return String.format("{\"test\":%s}", testValue);
    }
}
