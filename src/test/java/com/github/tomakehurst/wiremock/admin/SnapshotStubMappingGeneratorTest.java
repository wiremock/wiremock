package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.LoggedResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.admin.model.RequestPatternTransformer;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingGenerator;
import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingScenarioHandler;
import com.github.tomakehurst.wiremock.http.LoggedResponse;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Lists;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;
import static org.junit.Assert.assertEquals;

public class SnapshotStubMappingGeneratorTest {
    @Test
    public void generateFromWithEmptyList() {
        List<StubMapping> actual = new SnapshotStubMappingGenerator(
            requestPatternTransformer(null),
            false
        ).generateFrom(Lists.<ServeEvent>newArrayList());

        assertEquals(new ArrayList<>(), actual);
    }

    @Test
    public void generateFromWithSingleServeEvent() {
        final RequestPatternBuilder requestPatternBuilder = newRequestPattern();
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();

        SnapshotStubMappingGenerator stubMappingTransformer = new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition),
            false,
            stubbedScenarioHandler()
        );

        List<StubMapping> actual = stubMappingTransformer.generateFrom(
            Lists.newArrayList(serveEvent())
        );

        StubMapping expected = new StubMapping(requestPatternBuilder.build(), responseDefinition);
        expected.setId(actual.get(0).getId());

        assertEquals(expected, actual.get(0));
    }

    @Test
    public void generateWithMultipleServeEvents() {
        final RequestPatternBuilder requestPatternBuilder = newRequestPattern().withUrl("/foo");
        final ResponseDefinition responseDefinition = ResponseDefinition.ok();

        SnapshotStubMappingGenerator stubMappingTransformer = new SnapshotStubMappingGenerator(
            requestPatternTransformer(requestPatternBuilder),
            responseDefinitionTransformer(responseDefinition),
            true,
            stubbedScenarioHandler()
        );

        List<StubMapping> actual = stubMappingTransformer.generateFrom(
            Lists.newArrayList(serveEvent(), serveEvent())
        );

        ArrayList<StubMapping> expected = new ArrayList<>();
        for (int i = 0; i < 2; i++ ) {
            StubMapping stubMapping = new StubMapping(requestPatternBuilder.build(), responseDefinition);
            stubMapping.setId(actual.get(i).getId());
            expected.add(stubMapping);
        }

        assertEquals(expected, actual);
    }

    private static SnapshotStubMappingScenarioHandler stubbedScenarioHandler() {
        return new SnapshotStubMappingScenarioHandler() {
            @Override
            public void trackStubMapping(StubMapping stubMapping) {}
        };
    }

    private static RequestPatternTransformer requestPatternTransformer(final RequestPatternBuilder requestPatternBuilder) {
        return new RequestPatternTransformer() {
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
