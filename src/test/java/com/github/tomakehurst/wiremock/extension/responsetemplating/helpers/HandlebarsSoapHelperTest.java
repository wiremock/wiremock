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
import static org.junit.Assert.assertThat;

public class HandlebarsSoapHelperTest extends HandlebarsHelperTestBase {

    private HandlebarsSoapHelper helper;
    private ResponseTemplateTransformer transformer;

    @Before
    public void init() {
        this.helper = new HandlebarsSoapHelper();
        this.transformer = new ResponseTemplateTransformer(true);
    }

    @Test
    public void positiveTestSimpleSoap() throws IOException {
        testHelper(this.helper, "<Envelope><Body><test>success</test></Body></Envelope>", "/test", "success");
    }

    @Test
    public void positiveTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                    .url("/soap")
                    .body("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><soap:Body><m:a><m:test>success</m:test></m:a></soap:Body></soap:Envelope>"),
                aResponse()
                    .withBody("<test>{{soapXPath request.body '/a/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("<test>success</test>"));
    }

    @Test
    public void negativeTestResponseTemplate(){
        final ResponseDefinition responseDefinition = this.transformer.transform(
                mockRequest()
                    .url("/soap")
                    .body("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope/\"><soap:Body><m:a><m:test>success</m:test></m:a></soap:Body></soap:Envelope>"),
                aResponse()
                    .withBody("<test>{{soapXPath request.body '/b/test'}}</test>").build(),
                noFileSource(),
                Parameters.empty());

        assertThat(responseDefinition.getBody(), is("<test></test>"));
    }
}
