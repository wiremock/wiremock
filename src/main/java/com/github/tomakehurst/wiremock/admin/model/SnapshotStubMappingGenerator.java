package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforms ServeEvents to StubMappings using SnapshotRequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class SnapshotStubMappingGenerator {
    private final RequestPatternTransformer requestTransformer;
    private final LoggedResponseDefinitionTransformer responseTransformer;

    public SnapshotStubMappingGenerator(
        RequestPatternTransformer requestTransformer,
        LoggedResponseDefinitionTransformer responseTransformer
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public SnapshotStubMappingGenerator(RequestPatternTransformer requestTransformer) {
        this(
            requestTransformer == null ? new RequestPatternTransformer() : requestTransformer,
            new LoggedResponseDefinitionTransformer()
        );
    }

    public List<StubMapping> generateFrom(Iterable<ServeEvent> events) {
        final ArrayList<StubMapping> stubMappings = new ArrayList<>();
        for (ServeEvent event : events) {
            stubMappings.add(generateFrom(event));
        }
        return stubMappings;
    }

    private StubMapping generateFrom(ServeEvent event) {
        final RequestPattern requestPattern = requestTransformer.apply(event.getRequest()).build();
        final ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());

        return new StubMapping(requestPattern, responseDefinition);
    }
}
