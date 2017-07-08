package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.SnapshotStubMappingTransformerRunner;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.github.tomakehurst.wiremock.testsupport.GlobalStubMappingTransformer;
import com.github.tomakehurst.wiremock.testsupport.NonGlobalStubMappingTransformer;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.junit.Assert.assertEquals;

public class SnapshotStubMappingTransformerRunnerTest {
    @Test
    public void applyWithNoTransformers() {
        StubMapping stubMapping = stubMapping();
        StubMapping result = new SnapshotStubMappingTransformerRunner(
            new ArrayList<StubMappingTransformer>()
        ).apply(stubMapping);

        assertEquals(stubMapping, result);
    }

    @Test
    public void applyWithUnregisteredNonGlobalTransformer() {
        StubMapping stubMapping = stubMapping();

        // Should not apply the transformer as it isn't registered
        StubMapping result = new SnapshotStubMappingTransformerRunner(
            Lists.<StubMappingTransformer>newArrayList(new NonGlobalStubMappingTransformer())
        ).apply(stubMapping);

        assertEquals(stubMapping, result);
    }

    @Test
    public void applyWithRegisteredNonGlobalTransformer() {
        StubMapping result = new SnapshotStubMappingTransformerRunner(
            Lists.<StubMappingTransformer>newArrayList(new NonGlobalStubMappingTransformer()),
            Lists.newArrayList("nonglobal-transformer"),
            null,
            null
        ).apply(stubMapping());

        assertEquals("/?transformed=nonglobal", result.getRequest().getUrl());
    }

    @Test
    public void applyWithGlobalTransformer() {
        StubMapping result = new SnapshotStubMappingTransformerRunner(
            Lists.<StubMappingTransformer>newArrayList(new GlobalStubMappingTransformer())
        ).apply(stubMapping());

        assertEquals("/?transformed=global", result.getRequest().getUrl());
    }

    private StubMapping stubMapping() {
        return new StubMapping(
            newRequestPattern().withUrl("/").build(),
            ResponseDefinition.ok()
        );
    }
}
