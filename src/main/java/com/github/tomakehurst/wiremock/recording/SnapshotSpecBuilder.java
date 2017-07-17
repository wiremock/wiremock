package com.github.tomakehurst.wiremock.recording;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;

public class SnapshotSpecBuilder {

    private RequestPatternBuilder filterRequestPatternBuilder;
    private List<UUID> filterIds;
    private Map<String, CaptureHeadersSpec> headers = newLinkedHashMap();
    private long maxTextBodySize = ResponseDefinitionBodyMatcher.DEFAULT_MAX_TEXT_SIZE;
    private long maxBinaryBodySize = ResponseDefinitionBodyMatcher.DEFAULT_MAX_BINARY_SIZE;
    private boolean persistentStubs = true;
    private boolean repeatsAsScenarios = true;
    private List<String> transformerNames;
    private Parameters transformerParameters;
    private JsonMatchingFlags jsonMatchingFlags;

    public SnapshotSpecBuilder onlyRequestsMatching(RequestPatternBuilder filterRequestPattern) {
        this.filterRequestPatternBuilder = filterRequestPattern;
        return this;
    }

    public SnapshotSpecBuilder onlyRequestIds(List<UUID> filterIds) {
        this.filterIds = filterIds;
        return this;
    }

    public SnapshotSpecBuilder extractTextBodiesOver(long size) {
        this.maxTextBodySize = size;
        return this;
    }

    public SnapshotSpecBuilder extractBinaryBodiesOver(long size) {
        this.maxBinaryBodySize = size;
        return this;
    }

    public SnapshotSpecBuilder makeStubsPersistent(boolean persistent) {
        this.persistentStubs = persistent;
        return this;
    }

    public SnapshotSpecBuilder ignoreRepeatRequests() {
        this.repeatsAsScenarios = false;
        return this;
    }

    public SnapshotSpecBuilder transformers(String... transformerName) {
        return transformers(asList(transformerName));
    }

    public SnapshotSpecBuilder transformers(List<String> transformerName) {
        this.transformerNames = transformerName;
        return this;
    }

    public SnapshotSpecBuilder transformerParameters(Parameters parameters) {
        this.transformerParameters = parameters;
        return this;
    }

    public SnapshotSpecBuilder captureHeader(String key) {
        return captureHeader(key, null);
    }

    public SnapshotSpecBuilder captureHeader(String key, Boolean caseInsensitive) {
        headers.put(key, new CaptureHeadersSpec(caseInsensitive));
        return this;
    }

    public SnapshotSpec build() {
        RequestPattern filterRequestPattern = filterRequestPatternBuilder != null ?
            filterRequestPatternBuilder.build() :
            null;
        ProxiedServeEventFilters filters = filterRequestPatternBuilder != null || filterIds != null ?
            new ProxiedServeEventFilters(filterRequestPattern, filterIds) :
            null;

        ResponseDefinitionBodyMatcher responseDefinitionBodyMatcher = new ResponseDefinitionBodyMatcher(maxTextBodySize, maxBinaryBodySize);

        return new SnapshotSpec(
            filters,
            headers.isEmpty() ? null : headers,
            responseDefinitionBodyMatcher,
            SnapshotOutputFormatter.FULL,
            persistentStubs,
            repeatsAsScenarios,
            transformerNames,
            transformerParameters,
            jsonMatchingFlags);
    }

    public SnapshotSpecBuilder jsonBodyMatchFlags(boolean ignoreArrayOrder, boolean ignoreExtraElements) {
        this.jsonMatchingFlags = new JsonMatchingFlags(ignoreArrayOrder, ignoreExtraElements);
        return this;
    }
}
