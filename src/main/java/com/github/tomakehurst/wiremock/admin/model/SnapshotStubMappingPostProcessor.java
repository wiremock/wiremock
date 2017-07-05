package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Performs post-processing tasks on stub mappings generated from ServeEvents:
 * 1. Detect and process duplicate requests
 * 2. Extract response bodies to a separate file, if applicable
 * 3. Run any applicable StubMappingTransformers against the stub mappings
 */
public class SnapshotStubMappingPostProcessor {
    private final SnapshotRepeatedRequestHandler repeatedRequestHandler;
    private final SnapshotStubMappingTransformerRunner transformerRunner;
    private final ResponseDefinitionBodyMatcher bodyExtractMatcher;
    private final SnapshotStubMappingBodyExtractor bodyExtractor;

    public SnapshotStubMappingPostProcessor(
        SnapshotRepeatedRequestHandler repeatedRequestHandler,
        SnapshotStubMappingTransformerRunner transformerRunner,
        ResponseDefinitionBodyMatcher bodyExtractMatcher,
        SnapshotStubMappingBodyExtractor bodyExtractor
    ) {
        this.repeatedRequestHandler = repeatedRequestHandler;
        this.transformerRunner = transformerRunner;
        this.bodyExtractMatcher = bodyExtractMatcher;
        this.bodyExtractor = bodyExtractor;
    }

    public List<StubMapping> process(Iterable<StubMapping> stubMappings) {
        final List<StubMapping> processedStubMappings = Lists.newLinkedList(stubMappings);

        // Handle repeated requests by either removing them or generating scenarios
        repeatedRequestHandler.processStubMappingsInPlace(processedStubMappings);

        // Extract response bodies, if applicable
        if (bodyExtractMatcher != null) {
            for (StubMapping stubMapping : stubMappings) {
                if (bodyExtractMatcher.match(stubMapping.getResponse()).isExactMatch()) {
                    bodyExtractor.extractInPlace(stubMapping);
                }
            }
        }

        // Run any stub mapping transformer extensions
        return Lists.transform(processedStubMappings, transformerRunner);
    }
}
