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
 * A predicate to filter proxied ServeEvents against RequestPattern filters and IDs
 */
public class ProxiedServeEventFilters implements Predicate<ServeEvent> {

    @JsonUnwrapped
    private final RequestPattern filters;

    @JsonUnwrapped
    private final List<UUID> ids;

    public ProxiedServeEventFilters() {
        this.filters = null;
        this.ids = null;
    }

    @JsonCreator
    public ProxiedServeEventFilters(
        @JsonProperty("filters") RequestPattern filters,
        @JsonProperty("ids") List<UUID> ids
    ) {
        this.filters = filters;
        this.ids = ids;
    }

    @Override
    public boolean apply(ServeEvent serveEvent) {
        if (!serveEvent.getResponseDefinition().isProxyResponse()) {
            return false;
        }

        if (filters != null && !filters.match(serveEvent.getRequest()).isExactMatch()) {
            return false;
        }

        if (ids != null && !ids.contains(serveEvent.getId())) {
            return false;
        }

        return true;
    }
}
