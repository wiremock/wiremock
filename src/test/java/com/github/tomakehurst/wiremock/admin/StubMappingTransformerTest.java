package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.LoggedResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.admin.model.RequestPatternTransformer;
import com.github.tomakehurst.wiremock.admin.model.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class StubMappingTransformerTest {
    @Test
    public void apply() {
        final RequestPatternBuilder requestPatternBuilder = newRequestPattern();
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();
        RequestPatternTransformer requestTransformer = new RequestPatternTransformer() {
            @Override
            public RequestPatternBuilder apply(Request request) {
                return requestPatternBuilder;
            }
        };

        LoggedResponseDefinitionTransformer responseTransformer = new LoggedResponseDefinitionTransformer() {
            @Override
            public ResponseDefinition apply(LoggedResponse response) {
                return responseDefinition;
            }
        };

        StubMappingTransformer stubMappingTransformer = new StubMappingTransformer(
            requestTransformer,
            responseTransformer
        );

        StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
        expected.setId(UUID.fromString("808bdbde-19f5-3006-84e1-770c12e737b9"));

        assertEquals(expected, stubMappingTransformer.apply(new ServeEvent(
            null,
            LoggedRequest.createFrom(aRequest(new Mockery()).build()),
            null,
            null,
            LoggedResponse.from(Response.notConfigured()),
            false
        )));
    }
}
