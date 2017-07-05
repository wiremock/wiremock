package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Transform stub mappings using any applicable StubMappingTransformers and extract response body when applicable
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
        List<StubMapping> transformedStubMappings;

        // Handle repeated requests by either skipping them or generating scenarios
        transformedStubMappings = repeatedRequestHandler.processStubMappings(stubMappings);

        // Extract response bodies, if applicable
        if (bodyExtractMatcher != null) {
            for (StubMapping stubMapping : stubMappings) {
                if (bodyExtractMatcher.match(stubMapping.getResponse()).isExactMatch()) {
                    bodyExtractor.extractInPlace(stubMapping);
                }
            }
        }

        // Run any stub mapping transformer extensions
        transformedStubMappings = Lists.transform(transformedStubMappings, transformerRunner);

        return transformedStubMappings;
    }
}
