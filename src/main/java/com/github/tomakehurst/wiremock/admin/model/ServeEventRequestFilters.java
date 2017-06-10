package com.github.tomakehurst.wiremock.admin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.google.common.base.Predicate;

import java.util.List;
import java.util.UUID;

/**
 * A collection a RequestPattern filters and IDs to apply to ServeEvents
 */
public class ServeEventRequestFilters implements Predicate<ServeEvent> {
    @JsonUnwrapped
    private final RequestPattern filters;
    private final List<UUID> ids;

    @JsonCreator
    public ServeEventRequestFilters(
        @JsonProperty("filters") RequestPattern filters,
        @JsonProperty("ids") List<UUID> ids
    ) {
        this.filters = filters;
        this.ids = ids;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        if (
            filters != null
                && !filters.match(serveEvent.getRequest()).isExactMatch()
            ) {
            return false;
        }
        if (ids != null && !ids.contains(serveEvent.getId())) {
            return false;
        }
        return true;
    }
}
