package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.http.RequestFieldsComparator;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;

import java.util.Map;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
public class SnapshotSpec {
    // Whitelist requests to generate StubMappings for
    private ServeEventRequestFilters filters;
    // How to sort the StubMappings (mainly for output purposes)
    private RequestFieldsComparator sortFields;
    // Headers from the request to include in the stub mapping, if they match the corresponding matcher
    private RequestPatternTransformer captureHeaders;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private String outputFormat;
    // Whether to persist stub mappings
    private boolean persist = true;

    @JsonCreator
    public SnapshotSpec(@JsonProperty("filters") ServeEventRequestFilters filters ,
                        @JsonProperty("sortFields") String[] sortFields,
                        @JsonProperty("captureHeaders") Map<String, MultiValuePattern> captureHeaders,
                        @JsonProperty("outputFormat") String outputFormat,
                        @JsonProperty("persist") JsonNode persistNode) {
        this.filters = filters;
        this.outputFormat = outputFormat;
        this.captureHeaders = new RequestPatternTransformer(captureHeaders);
        this.persist = persistNode.asBoolean(true);
        if (sortFields != null) this.sortFields = new RequestFieldsComparator(sortFields);
    }

    public SnapshotSpec() {}

    public ServeEventRequestFilters getFilters() { return filters; }

    public RequestFieldsComparator getSortFields() { return sortFields; }

    public RequestPatternTransformer getCaptureHeaders() { return captureHeaders; }

    public String getOutputFormat() { return outputFormat; }

    public boolean shouldPersist() { return persist; }
}
