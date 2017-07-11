package com.github.tomakehurst.wiremock.admin.model;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;

import java.util.List;
import java.util.UUID;

public class SnapshotSpecBuilder {

    private RequestPatternBuilder filterRequestPattern;
    private List<UUID> filterIds;

    public SnapshotSpecBuilder onlyRequestsMatching(RequestPatternBuilder filterRequestPattern) {
        this.filterRequestPattern = filterRequestPattern;
        return this;
    }

    public SnapshotSpecBuilder onlyRequestIds(List<UUID> filterIds) {
        this.filterIds = filterIds;
        return this;
    }

    public SnapshotSpec build() {
        return new SnapshotSpec(
            new ProxiedServeEventFilters(filterRequestPattern.build(), filterIds),
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
    }
}
