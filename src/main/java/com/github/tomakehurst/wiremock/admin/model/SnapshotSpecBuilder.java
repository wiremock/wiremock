package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.admin.model.ResponseDefinitionBodyMatcher.DEFAULT_MAX_BINARY_SIZE;
import static com.github.tomakehurst.wiremock.admin.model.ResponseDefinitionBodyMatcher.DEFAULT_MAX_TEXT_SIZE;

public class SnapshotSpecBuilder {

    private RequestPatternBuilder filterRequestPatternBuilder;
    private List<UUID> filterIds;
    private long maxTextBodySize = DEFAULT_MAX_TEXT_SIZE;
    private long maxBinaryBodySize = DEFAULT_MAX_BINARY_SIZE;
    private boolean persistentStubs = true;
    private boolean repeatsAsScenarios = false;

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

    public SnapshotSpecBuilder buildScenariosForRepeatRequests() {
        this.repeatsAsScenarios = true;
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
            null,
            responseDefinitionBodyMatcher,
            SnapshotOutputFormatter.FULL,
            persistentStubs,
            repeatsAsScenarios,
            null,
            null
        );
    }
}
