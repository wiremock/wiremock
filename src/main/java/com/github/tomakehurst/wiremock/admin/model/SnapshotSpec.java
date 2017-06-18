package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.MultiValuePattern;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
public class SnapshotSpec {
    // Whitelist requests to generate StubMappings for
    private ServeEventRequestFilters filters;
    // Headers from the request to include in the stub mapping, if they match the corresponding matcher
    private RequestPatternTransformer captureHeaders;
    // How to format StubMappings in the response body
    // Either "full" (meaning return an array of rendered StubMappings) or "ids", which returns an array of UUIDs
    private SnapshotOutputFormat outputFormat;
    // Whether to persist stub mappings
    private boolean persist = true;
    // Whether duplicate requests should be recorded as scenarios or just discarded
    private boolean repeatsAsScenarios = false;
    // Stub mapping transformers
    private List<String> transformers;
    // Parameters for stub mapping transformers
    private Parameters transformerParameters;

    @JsonCreator
    public SnapshotSpec(
        @JsonProperty("filters") ServeEventRequestFilters filters ,
        @JsonProperty("sortFields") String[] sortFields,
        @JsonProperty("captureHeaders") Map<String, MultiValuePattern> captureHeaders,
        @JsonProperty("outputFormat") SnapshotOutputFormat outputFormat,
        @JsonProperty("persist") JsonNode persistNode,
        @JsonProperty("repeatsAsScenarios") JsonNode repeatsNode,
        @JsonProperty("transformers") List<String> transformers,
        @JsonProperty("transformerParameters") Parameters transformerParameters
    ) {
        this.filters = filters;
        this.outputFormat = outputFormat == null ? SnapshotOutputFormat.FULL : outputFormat;
        this.captureHeaders = new RequestPatternTransformer(captureHeaders);
        this.persist = persistNode.asBoolean(true);
        this.repeatsAsScenarios = repeatsNode.asBoolean(false);
        this.transformers = transformers;
        this.transformerParameters = transformerParameters;
    }

    public SnapshotSpec() {}

    public ServeEventRequestFilters getFilters() { return filters; }

    public RequestPatternTransformer getCaptureHeaders() { return captureHeaders; }

    public SnapshotOutputFormat getOutputFormat() { return outputFormat; }

    public boolean shouldPersist() { return persist; }

    public boolean shouldRecordRepeatsAsScenarios() { return repeatsAsScenarios; }

    public List<String> getTransformers() { return transformers; }

    public Parameters getTransformerParameters() { return transformerParameters; }
}
