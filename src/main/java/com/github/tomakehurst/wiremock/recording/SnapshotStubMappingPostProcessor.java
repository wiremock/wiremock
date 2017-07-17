package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs stateful post-processing tasks on stub mappings generated from ServeEvents:
 * 1. Detect duplicate requests and either discard them or turn them into scenarios
 * 2. Extract response bodies to a separate file, if applicable
 * 3. Run any applicable StubMappingTransformers against the stub mappings
 */
public class SnapshotStubMappingPostProcessor {
    private final boolean shouldRecordRepeatsAsScenarios;
    private final SnapshotStubMappingTransformerRunner transformerRunner;
    private final ResponseDefinitionBodyMatcher bodyExtractMatcher;
    private final SnapshotStubMappingBodyExtractor bodyExtractor;

    public SnapshotStubMappingPostProcessor(
        boolean shouldRecordRepeatsAsScenarios,
        SnapshotStubMappingTransformerRunner transformerRunner,
        ResponseDefinitionBodyMatcher bodyExtractMatcher,
        SnapshotStubMappingBodyExtractor bodyExtractor
    ) {
        this.shouldRecordRepeatsAsScenarios = shouldRecordRepeatsAsScenarios;
        this.transformerRunner = transformerRunner;
        this.bodyExtractMatcher = bodyExtractMatcher;
        this.bodyExtractor = bodyExtractor;
    }

    public List<StubMapping> process(Iterable<StubMapping> stubMappings) {
        final Multiset<RequestPattern> requestCounts = HashMultiset.create();
        final List<StubMapping> processedStubMappings = new ArrayList<>();

        for (StubMapping stubMapping: stubMappings) {
            requestCounts.add(stubMapping.getRequest());

            // Skip duplicate requests if shouldRecordRepeatsAsScenarios is not enabled
            if (
                requestCounts.count(stubMapping.getRequest()) > 1
                && !shouldRecordRepeatsAsScenarios
            ) {
                continue;
            }

            if (bodyExtractMatcher != null && bodyExtractMatcher.match(stubMapping.getResponse()).isExactMatch()) {
                bodyExtractor.extractInPlace(stubMapping);
            }

            processedStubMappings.add(stubMapping);
        }

        if (shouldRecordRepeatsAsScenarios) {
            new ScenarioProcessor().putRepeatedRequestsInScenarios(processedStubMappings);
        }

        // Run any stub mapping transformer extensions
        return Lists.transform(processedStubMappings, transformerRunner);
    }
}
