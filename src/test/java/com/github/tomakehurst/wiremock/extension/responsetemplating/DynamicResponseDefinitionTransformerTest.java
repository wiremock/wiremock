package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.testsupport.NoFileSource.noFileSource;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DynamicResponseDefinitionTransformerTest {

    private GlobalDynamicResponseDefinitionTransformer transformer;

    @Before
    public void setup() {
        transformer = new GlobalDynamicResponseDefinitionTransformer();
    }

    @Test
    public void supportsQueryParameters() {
        ResponseDefinition transformedResponseDef = transform(mockRequest()
            .url("/things?multi_param=one&multi_param=two&single-param=1234"),
            aResponse().withBody(
                "Multi 1: {{request.query.multi_param.[0]}}, Multi 2: {{request.query.multi_param.[1]}}, Single 1: {{request.query.single-param}}"
            )
        );

        assertThat(transformedResponseDef.getBody(), is(
        "Multi 1: one, Multi 2: two, Single 1: 1234"
        ));
    }

    private ResponseDefinition transform(Request request, ResponseDefinitionBuilder responseDefinitionBuilder) {
        return transformer.transform(
            request,
            responseDefinitionBuilder.build(),
            noFileSource(),
            Parameters.empty()
        );
    }
}
