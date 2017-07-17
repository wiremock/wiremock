package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.extension.Parameters;

import java.util.List;
import java.util.Map;

/**
 * Encapsulates options for generating and outputting StubMappings
 */
public class SnapshotSpec {

    private final String targetBaseUrl;

    // Whitelist requests to generate StubMappings for
    private final ProxiedServeEventFilters filters;
    // Headers from the request to include in the stub mapping, if they match the corresponding matcher
    private final Map<String, CaptureHeadersSpec> captureHeaders;
    // Criteria for extracting body from responses
    private final ResponseDefinitionBodyMatcher extractBodyCriteria;
    // How to format StubMappings in the response body
    private final SnapshotOutputFormatter outputFormat;
    // Whether to persist stub mappings
    private final Boolean persist;
    // Whether duplicate requests should be recorded as scenarios or just discarded
    private final Boolean repeatsAsScenarios;
    // Stub mapping transformers
    private final List<String> transformers;
    // Parameters for stub mapping transformers
    private final Parameters transformerParameters;
    private final JsonMatchingFlags jsonMatchingFlags;

    @JsonCreator
    public SnapshotSpec(
        @JsonProperty("targetBaseUrl") String targetBaseUrl,
        @JsonProperty("filters") ProxiedServeEventFilters filters,
        @JsonProperty("captureHeaders") Map<String, CaptureHeadersSpec> captureHeaders,
        @JsonProperty("extractBodyCriteria") ResponseDefinitionBodyMatcher extractBodyCriteria,
        @JsonProperty("outputFormat") SnapshotOutputFormatter outputFormat,
        @JsonProperty("persist") Boolean persist,
        @JsonProperty("repeatsAsScenarios") Boolean repeatsAsScenarios,
        @JsonProperty("transformers") List<String> transformers,
        @JsonProperty("transformerParameters") Parameters transformerParameters,
        @JsonProperty("jsonMatchingFlags") JsonMatchingFlags jsonMatchingFlags) {
        this.targetBaseUrl = targetBaseUrl;
        this.filters = filters == null ? new ProxiedServeEventFilters() : filters;
        this.captureHeaders = captureHeaders;
        this.extractBodyCriteria = extractBodyCriteria;
        this.outputFormat = outputFormat == null ? SnapshotOutputFormatter.FULL : outputFormat;
        this.persist = persist == null ? true : persist;
        this.repeatsAsScenarios = repeatsAsScenarios == null ? false : repeatsAsScenarios;
        this.transformers = transformers;
        this.transformerParameters = transformerParameters;
        this.jsonMatchingFlags = jsonMatchingFlags;
    }

    private SnapshotSpec() {
        this(null, null, null, null, null, null, true, null, null, null);
    }

    public static final SnapshotSpec DEFAULTS = new SnapshotSpec();

    public static SnapshotSpec forBaseUrl(String targetBaseUrl) {
        return new SnapshotSpec(targetBaseUrl, null, null, null, null, null, true, null, null, null);
    }

    public String getTargetBaseUrl() {
        return targetBaseUrl;
    }

    public ProxiedServeEventFilters getFilters() { return filters; }

    public Map<String, CaptureHeadersSpec> getCaptureHeaders() { return captureHeaders; }

    public SnapshotOutputFormatter getOutputFormat() { return outputFormat; }

    @JsonProperty("persist")
    public boolean shouldPersist() { return persist; }

    @JsonProperty("repeatsAsScenarios")
    public boolean shouldRecordRepeatsAsScenarios() { return repeatsAsScenarios; }

    public List<String> getTransformers() { return transformers; }

    public Parameters getTransformerParameters() { return transformerParameters; }

    public ResponseDefinitionBodyMatcher getExtractBodyCriteria() { return extractBodyCriteria; }

    public JsonMatchingFlags getJsonMatchingFlags() {
        return jsonMatchingFlags;
    }
}
