package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ListStubMappingsResult {

    private final List<StubMapping> mappings;

    @JsonCreator
    public ListStubMappingsResult(@JsonProperty("stubMappings") List<StubMapping> mappings) {
        this.mappings = mappings;
    }

    public List<StubMapping> getMappings() {
        return mappings;
    }
}
