package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import com.google.common.base.Function;
import com.google.common.primitives.Bytes;

import java.util.UUID;

/**
 * Transforms ServeEvents to StubMappings using RequestPatternTransformer and SnapshotResponseDefinitionTransformer
 */
public class StubMappingTransformer implements Function<ServeEvent, StubMapping> {
    private final RequestPatternTransformer requestTransformer;
    private final LoggedResponseDefinitionTransformer responseTransformer;

    public StubMappingTransformer(
        RequestPatternTransformer requestTransformer,
        LoggedResponseDefinitionTransformer responseTransformer
    ) {
        this.requestTransformer = requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public StubMappingTransformer(RequestPatternTransformer requestTransformer) {
        this(
            requestTransformer == null ? new RequestPatternTransformer() : requestTransformer,
            new LoggedResponseDefinitionTransformer()
        );
    }

    @Override
    public StubMapping apply(ServeEvent event) {
        RequestPattern requestPattern = requestTransformer.apply(event.getRequest()).build();
        ResponseDefinition responseDefinition = responseTransformer.apply(event.getResponse());
        StubMapping stubMapping = new StubMapping(requestPattern, responseDefinition);

        // create (hopefully) unique ID for the stub mapping using JSON representation of the RequestPattern,
        // which will be used in Task to de-dupe StubMappings
        byte[] hashCode = Json.toByteArray(requestPattern);
        stubMapping.setId(UUID.nameUUIDFromBytes(hashCode));

        return stubMapping;
    }
}
