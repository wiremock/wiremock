package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.LoggedResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.admin.model.RequestPatternTransformer;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingGenerator;
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

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class SnapshotStubMappingGeneratorTest {
    @Test
    public void apply() {
        final RequestPatternBuilder requestPatternBuilder = newRequestPattern().withUrl("/foo");
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();

        SnapshotStubMappingGenerator stubMappingTransformer = new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition)
        );

        StubMapping actual = stubMappingTransformer.apply(serveEvent());
        StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
        expected.setId(actual.getId());

        assertEquals(expected, actual);
    }

    private static RequestPatternTransformer requestPatternTransformer(final RequestPatternBuilder requestPatternBuilder) {
        return new RequestPatternTransformer(null, null) {
            @Override
            public RequestPatternBuilder apply(Request request) {
                return requestPatternBuilder;
            }
        };
    }

    private static LoggedResponseDefinitionTransformer responseDefinitionTransformer(final ResponseDefinition responseDefinition) {
        return new LoggedResponseDefinitionTransformer() {
            @Override
            public ResponseDefinition apply(LoggedResponse response) {
                return responseDefinition;
            }
        };
    }

    private static ServeEvent serveEvent() {
        return new ServeEvent(
            null,
            LoggedRequest.createFrom(aRequest(new Mockery()).build()),
            null,
            null,
            LoggedResponse.from(Response.notConfigured()),
            false
        );
    }
}
