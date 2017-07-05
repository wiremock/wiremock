package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.StubMappingTransformer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;

import java.util.List;

/**
 * Applies all registered StubMappingTransformer extensions against a stub mapping when applicable,
 * passing them any supplied Parameters.
 */
public class SnapshotStubMappingTransformerRunner implements Function<StubMapping, StubMapping> {
    private final FileSource fileSource;
    private final Parameters parameters;
    private final Iterable<StubMappingTransformer> registeredTransformers;
    private final List<String> requestedTransformers;

    public SnapshotStubMappingTransformerRunner(Iterable<StubMappingTransformer> registeredTransformers) {
        this(registeredTransformers, null, null, null);
    }

    public SnapshotStubMappingTransformerRunner(
        Iterable<StubMappingTransformer> registeredTransformers,
        List<String> requestedTransformers,
        Parameters parameters,
        FileSource fileSource
    ) {
        this.requestedTransformers = requestedTransformers;
        this.registeredTransformers = registeredTransformers;
        this.parameters = parameters;
        this.fileSource = fileSource;
    }

    @Override
    public StubMapping apply(StubMapping stubMapping) {
        for (StubMappingTransformer transformer : registeredTransformers) {
            if (
                transformer.applyGlobally()
                || (requestedTransformers != null && requestedTransformers.contains(transformer.getName()))
            ) {
                stubMapping = transformer.transform(stubMapping, fileSource, parameters);
            }
        }

        return stubMapping;
    }
}
