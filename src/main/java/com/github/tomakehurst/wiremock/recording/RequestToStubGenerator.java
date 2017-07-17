package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.admin.model.GetServeEventsResult;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

import static com.google.common.collect.FluentIterable.from;

public class RequestToStubGenerator {

    public List<StubMapping> serveEventsToStubMappings(
        GetServeEventsResult serveEventsResult,
        ProxiedServeEventFilters serveEventFilters,
        SnapshotStubMappingGenerator stubMappingGenerator,
        SnapshotStubMappingPostProcessor stubMappingPostProcessor
    ) {
        final Iterable<StubMapping> stubMappings = from(serveEventsResult.getServeEvents())
            .filter(serveEventFilters)
            .transform(stubMappingGenerator);

        return stubMappingPostProcessor.process(stubMappings);
    }

    private SnapshotStubMappingPostProcessor getStubMappingPostProcessor(Options options, SnapshotSpec snapshotSpec) {
        final SnapshotStubMappingTransformerRunner transformerRunner = new SnapshotStubMappingTransformerRunner(
            options.extensionsOfType(StubMappingTransformer.class).values(),
            snapshotSpec.getTransformers(),
            snapshotSpec.getTransformerParameters(),
            options.filesRoot()
        );

        return new SnapshotStubMappingPostProcessor(
            snapshotSpec.shouldRecordRepeatsAsScenarios(),
            transformerRunner,
            snapshotSpec.getExtractBodyCriteria(),
            new SnapshotStubMappingBodyExtractor(options.filesRoot())
        );
    }
}
